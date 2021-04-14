#!/bin/sh

while sleep 0.1; do
  # wait until vault is unsealed
  if vault status > /dev/null; then
    vault auth enable approle

    # Create "finance" app role
    vault write auth/approle/role/finance \
      secret_id_ttl=8h \
      token_num_uses=0 \
      token_ttl=0 \
      token_max_ttl=0 \
      secret_id_num_uses=0
    # Change role_id of "finance" app role to a custom value
    vault write auth/approle/role/finance/role-id \
      role_id=finance-role-id

    # Generate secret_id for the services
    vault write -format json auth/approle/role/finance/secret-id \
      metadata='{"service": "dispute_management"}' \
      > /services/dispute_management/credentials.json
    vault write -format json auth/approle/role/finance/secret-id \
      metadata='{"service": "product_inventory"}' \
      > /services/product_inventory/credentials.json
    vault write -format json auth/approle/role/finance/secret-id \
      metadata='{"service": "user_management"}' \
      > /services/user_management/credentials.json

    exit 1
  fi
done
