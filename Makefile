.PHONY: up down

up:
	docker-compose -f docker/common.yml -f docker/mysql/mysql.yml -f docker/redis/redis.yml up -d

down:
	docker-compose -f docker/common.yml -f docker/mysql/mysql.yml -f docker/redis/redis.yml down
