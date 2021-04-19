path "totp/keys/finance__*" {
  capabilities = ["read", "create", "update"]
}

path "totp/code/finance__*" {
  capabilities = ["read", "create", "update"]
}
