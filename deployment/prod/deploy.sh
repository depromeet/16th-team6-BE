#!/usr/bin/env bash

IS_BLUE=$(docker compose ps | grep atcha-blue)
DEFAULT_CONF="data/nginx/nginx.conf"
MAX_RETRIES=30

check_service() {
  local RETRIES=0
  local SERVICE_NAME=$1

  local container_ids=($(docker compose ps -q $SERVICE_NAME))

  # 최대 재시도 횟수
  while [ $RETRIES -lt $MAX_RETRIES ]; do
    echo "Checking service $SERVICE_NAME (attempt: $((RETRIES+1)))"
    sleep 3

    local all_healthy=true

    # 각 컨테이너의 헬스 상태 검사
    for id in "${container_ids[@]}"; do
      local health_status
      health_status=$(docker container inspect --format='{{.State.Health.Status}}' "$id")
      echo "Health status of container $id: $health_status"
      if [ "$health_status" != "healthy" ]; then
        all_healthy=false
        break
      fi
    done

    if [ "$all_healthy" = true ]; then
      echo "$SERVICE_NAME health check passed."
      return 0
    fi

    RETRIES=$((RETRIES+1))
  done

  echo "Failed to check service $SERVICE_NAME after $MAX_RETRIES attempts."
  return 1
}

ensure_nginx_running() {
  local nginx_exists
  nginx_exists=$(docker compose ps -q nginx)
  if [ -z "$nginx_exists" ]; then
    echo "nginx 컨테이너가 존재하지 않습니다. nginx 컨테이너를 실행합니다."
    docker compose up -d nginx
    # nginx가 완전히 실행될 때까지 잠시 대기
    sleep 5
  else
    echo "nginx 컨테이너가 이미 실행 중입니다."
  fi
}

restart_nginx() {
  echo "nginx 컨테이너를 재시작합니다."
  docker compose restart nginx
}

if [ -z "$IS_BLUE" ]; then
  echo "### GREEN => BLUE ###"

  echo "1. BLUE 이미지 받기"
  docker compose pull atcha-blue

  echo "2. BLUE 컨테이너 실행"
  docker compose up -d atcha-blue

  echo "3. BLUE 컨테이너 헬스 체크"
  if ! check_service "atcha-blue"; then
    echo "BLUE health check failed."
    exit 1
  fi

  echo "4. nginx 재실행"
  ensure_nginx_running
  sudo cp /home/atcha/data/nginx/nginx-blue.conf /home/atcha/data/nginx/nginx.conf
  restart_nginx

  echo "5. GREEN 컨테이너 중지 및 삭제"
  docker compose stop atcha-green
  docker compose rm -f atcha-green

else
  echo "### BLUE => GREEN ###"

  echo "1. GREEN 이미지 받기"
  docker compose pull atcha-green

  echo "2. GREEN 컨테이너 실행"
  docker compose up -d atcha-green

  echo "3. GREEN 컨테이너 헬스 체크"
  if ! check_service "atcha-green"; then
    echo "GREEN health check failed."
    exit 1
  fi

  echo "4. nginx 재실행"
  ensure_nginx_running
  sudo cp /home/atcha/data/nginx/nginx-green.conf /home/atcha/data/nginx/nginx.conf
  restart_nginx

  echo "5. BLUE 컨테이너 중지 및 삭제"
  docker compose stop atcha-blue
  docker compose rm -f atcha-blue
fi
