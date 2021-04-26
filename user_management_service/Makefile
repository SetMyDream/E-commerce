db-local:
	cd .. && \
	docker-compose run --rm \
		--name user_db \
		-p 5432:5432 \
		user_management_db

vault-local:
	docker volume create \
		-o type=none \
		-o device="$$(pwd)/.dependencies/vault" \
		-o o=bind \
		user_management_credentials && \
 	cd .. && \
	docker-compose run --rm \
		--name vault \
		-v user_management_credentials:/services/user_management \
		-p 8200:8200 \
		vault
