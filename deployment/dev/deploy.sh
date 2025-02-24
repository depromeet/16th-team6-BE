#!/bin/bash

IS_BLUE=$(docker compose ps | grep atcha-blue)
DEFAULT_CONF=" data/nginx/nginx.conf"
MAX_RETRIES=30

check_service() {
  local RETRIES=0
  local SERVICE_NAME=$1

  local container_ids=($(docker compose ps -q $SERVICE_NAME))

  # 최대 재시도 횟수
  while [ $RETRIES -lt $MAX_RETRIES ]; do
    echo "Checking service at $SERVICE_NAME (attempt: $((RETRIES+1)))"
    sleep 3

    # 서비스에 대한 모든 컨테이너 ID 가져오기
    local all_healthy=true

    # 각 컨테이너의 헬스 상태 검사
    for id in "${container_ids[@]}"; do
      local health_status=$(docker container inspect --format='{{.State.Health.Status}}' "$id")
      echo "Health status of container $id: $health_status"

      if [ "$health_status" != "healthy" ]; then
        all_healthy=false
        break
      fi
    done

    # 모든 컨테이너가 healthy 상태일 경우
    if [ "$all_healthy" = true ]; then
      echo "$SERVICE_NAME health check passed."
      return 0
    fi

    RETRIES=$((RETRIES+1))
  done;

  echo "Failed to check service $SERVICE_NAME after $MAX_RETRIES attempts."
  return 1
}

if [ -z "$IS_BLUE" ];then
  echo "### GREEN => BLUE ###"

  echo "1. BLUE 이미지 받기"
  docker compose pull atcha-blue

  echo "2. BLUE 컨테이너 실행"
  docker compose up -d atcha-blue --scale atcha-blue=2

  echo "3. health check"
  if ! check_service "atcha-blue"; then
    echo "BLUE health check failed."
    exit 1
  fi

  echo "4. nginx 재실행"
  sudo cp data/nginx/nginx-blue.conf data/nginx/nginx.conf
  sudo docker compose exec -it nginx nginx -s reload

  echo "5. GREEN  컨테이너 내리기"
  docker compose stop atcha-green
  docker compose rm -f atcha-green

else
  echo "### BLUE => GREEN ###"

  echo "1. GREEN 이미지 받기"
  docker compose pull atcha-green

  echo "2. GREEN 컨테이너 실행"
  docker compose up -d atcha-green --scale atcha-green=2

  echo "3. health check"
  if ! check_service "atcha-green"; then
    echo "GREEN health check failed."
    exit 1
  fi

  echo "4. nginx 재실행"
  sudo cp data/nginx/nginx-green.conf data/nginx/nginx.conf
  sudo docker compose exec -it nginx nginx -s reload

  echo "5. BLUE 컨테이너 내리기"
  docker compose stop atcha-blue
  docker compose rm -f atcha-blue
fi
