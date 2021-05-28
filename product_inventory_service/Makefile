db-local:
	cd .. && \
	docker-compose run --rm \
		--name product_db \
		-p 5432:5432 \
		product_management_db

vault-local:
	docker volume create \
		-o type=none \
		-o device="$$(pwd)/.dependencies/vault" \
		-o o=bind \
		product_management_credentials && \
 	cd .. && \
	docker-compose run --rm \
		--name vault \
		-v product_management_credentials:/services/product_inventory_management \
		-p 8200:8200 \
		vault