local:
	make -j 2 db-local users-local

db-local:
	cd .. && \
	docker-compose run --rm \
		--name dispute_db \
		-p 5432:5432 \
		dispute_management_db

users-local:
	cd .. && \
	docker-compose run --rm \
    		--name users \
    		-p 8085:9000 \
    		user_management

