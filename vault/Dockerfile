FROM vault:1.7.0 AS vault-dev

RUN apk add jq
COPY . /vault

ENV VAULT_ADDR='http://127.0.0.1:8200'
ENV VAULT_API_ADDR='http://127.0.0.1:8200'
RUN /vault/init.sh

ENTRYPOINT ["/vault/entrypoint.sh"]
