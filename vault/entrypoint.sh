#!/usr/bin/dumb-init /bin/sh

set -m

# If there is no data, copy over initial data
ls /vault/data &> dev/null
if [ $? -ne 0 ]; then
  mv /opt/data-ini /vault/data
fi

# Start Vault server
/usr/local/bin/docker-entrypoint.sh server &

while sleep 0.1; do
  # wait until vault server is running
  vault status &> /dev/null
  if [ $? -ne 1 ]; then
    vault operator unseal "$(jq -r .unseal_keys_b64[0] < /opt/credentials/init.json)" > /dev/null
    vault operator unseal "$(jq -r .unseal_keys_b64[1] < /opt/credentials/init.json)" > /dev/null
    vault operator unseal "$(jq -r .unseal_keys_b64[2] < /opt/credentials/init.json)" > /dev/null
    # login the vault CLI
    jq -r .root_token < /opt/credentials/init.json | vault login -

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

    break
  fi
done

fg %1
