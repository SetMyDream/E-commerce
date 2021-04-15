FROM vault:1.7.0 AS vault-dev

RUN apk add bash jq
COPY . /vault

ENV VAULT_ADDR='http://127.0.0.1:8200'
ENV VAULT_API_ADDR='http://127.0.0.1:8200'
RUN /vault/init.sh
RUN mv /vault/data /opt/data-ini

ENTRYPOINT ["/vault/entrypoint.sh"]
