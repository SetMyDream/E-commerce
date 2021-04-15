#!/bin/bash

# Enable jobs commands
set -m

# start vault server
VAULT_DISABLE_MLOCK=true /usr/local/bin/docker-entrypoint.sh server &

while sleep 0.2; do
  # wait until vault server is running
  vault status &> /dev/null
  if [ $? -ne 1 ]; then
    # start vault and unseal it
    mkdir /opt/credentials
    vault operator init -address=http://127.0.0.1:8200 --format=json > /opt/credentials/init.json
    vault operator unseal "$(jq -r .unseal_keys_b64[0] < /opt/credentials/init.json)" &> /dev/null
    vault operator unseal "$(jq -r .unseal_keys_b64[1] < /opt/credentials/init.json)" &> /dev/null
    vault operator unseal "$(jq -r .unseal_keys_b64[2] < /opt/credentials/init.json)" &> /dev/null
    # login vault CLI
    export VAULT_TOKEN=$(jq -r .root_token < /opt/credentials/init.json)

    vault auth enable approle
    vault secrets enable totp # totp secrets at /totp/*

    # Upload policy from file
    vault policy write finance-service /vault/config/finance-service-policy.hcl
    # Create "finance" app role
    vault write auth/approle/role/finance \
      token_policies=finance-service \
      secret_id_ttl=8h \
      token_num_uses=0 \
      token_ttl=0 \
      token_max_ttl=0 \
      secret_id_num_uses=0
    # Change role_id of "finance" app role to a custom value
    vault write auth/approle/role/finance/role-id \
      role_id=finance-role-id

    break
  fi
done

# kill vault server
kill %1
