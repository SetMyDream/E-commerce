#!/bin/sh

# start vault server
VAULT_DISABLE_MLOCK=true /usr/local/bin/docker-entrypoint.sh server > /dev/null &

while sleep 0.2; do
  # wait until vault server is running
  vault status > /dev/null 2>&1
  if [ $? -ne 1 ]; then
    # start vault and unseal it
    mkdir /opt/data-ini
    vault operator init -address=http://127.0.0.1:8200 --format=json > /opt/data-ini/init.json
    vault operator unseal "$(jq -r .unseal_keys_b64[0] < /opt/data-ini/init.json)" > /dev/null
    vault operator unseal "$(jq -r .unseal_keys_b64[1] < /opt/data-ini/init.json)" > /dev/null
    vault operator unseal "$(jq -r .unseal_keys_b64[2] < /opt/data-ini/init.json)" > /dev/null
    # login the vault CLI
    jq -r .root_token < /opt/data-ini/init.json | vault login - > /dev/null

    vault auth enable approle > /dev/null
    vault secrets enable totp > /dev/null

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

# kill the vault server
kill $!

# save our initial data when vault is completely shut down
while sleep 0.1; do
  pgrep vault > /dev/null
  if [ $? -ne 0 ]; then
    mv /vault/data/* /opt/data-ini/
    break
  fi
done
