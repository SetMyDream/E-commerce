build: build-userman

start:
	docker-compose up

build-userman:
	cd user_management_service && \
	sbt docker:publishLocal
