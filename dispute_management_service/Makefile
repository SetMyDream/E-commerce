COMPOSE_MAINPROJECT_PROJNAME := e-commerce
COMPOSE_INTEGRATION_PROJNAME := dispute_integration

start-local-db = cd $(1) && \
    docker-compose -p $(2) run --rm \
        --name dispute_db \
        -p 5432:5432 \
        dispute_management_db

start-local-user-management = cd $(1) && \
    docker-compose -p $(2) run --rm \
        --name users \
        -p 8085:9000 \
        user_management

local:
	make -j 2 db-local users-local

integration-local:
	make -j 2 integration-db-local integration-users-local

test-integration:
	cd integration && docker-compose up --build

db-local:
	$(call start-local-db,"..",$(COMPOSE_MAINPROJECT_PROJNAME))

users-local:
	$(call start-local-user-management,"..",$(COMPOSE_MAINPROJECT_PROJNAME))

integration-db-local:
	$(call start-local-db,integration,$(COMPOSE_INTEGRATION_PROJNAME))

integration-users-local:
	$(call start-local-user-management,integration,$(COMPOSE_INTEGRATION_PROJNAME))
