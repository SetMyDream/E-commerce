build:
	make -j 2 build-images build-userman

start:
	docker-compose up

build-images:
	docker-compose build

build-userman:
	cd user_management_service && \
	sbt docker:publishLocal
