#!/usr/bin/dumb-init /bin/sh

set -m

# If there is no data, copy over the initial data
ls /vault/data &> dev/null
if [ $? -ne 0 ]; then
  mv /opt/data-ini /vault/data
fi

# Start Vault server
/usr/local/bin/docker-entrypoint.sh server &

while sleep 0.1; do
  # wait until the vault server is running
  vault status &> /dev/null
  if [ $? -ne 1 ]; then
    vault operator unseal "$(jq -r .unseal_keys_b64[0] < /vault/data/init.json)" > /dev/null
    vault operator unseal "$(jq -r .unseal_keys_b64[1] < /vault/data/init.json)" > /dev/null
    vault operator unseal "$(jq -r .unseal_keys_b64[2] < /vault/data/init.json)" > /dev/null
    # login the vault CLI
    jq -r .root_token < /vault/data/init.json | vault login -

    # Generate secret_id for the services
    ROLE_ID=$(vault read --format=json auth/approle/role/finance/role-id | jq -r .data.role_id)
    vault write -format json auth/approle/role/finance/secret-id \
      metadata='{"service": "dispute_management"}' \
      | jq --arg role_id $ROLE_ID '.data + {role_id: $role_id}' \
      > /services/dispute_management/credentials.json
    vault write -format json auth/approle/role/finance/secret-id \
      metadata='{"service": "product_inventory"}' \
      | jq --arg role_id $ROLE_ID '.data + {role_id: $role_id}' \
      > /services/product_inventory/credentials.json
    vault write -format json auth/approle/role/finance/secret-id \
      metadata='{"service": "user_management"}' \
      | jq --arg role_id $ROLE_ID '.data + {role_id: $role_id}' \
      > /services/user_management/credentials.json

    break
  fi
done

fg %1
