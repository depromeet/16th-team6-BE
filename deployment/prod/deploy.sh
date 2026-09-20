#!/usr/bin/env bash

set -euo pipefail

IS_BLUE=$(docker compose ps | grep atcha-blue || true)
NGINX_DIR="/home/atcha/data/nginx"
MAX_RETRIES=100

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

# nginx.conf 를 지정한 색으로 교체한다.
# 호스트 파일이 새 inode 로 바뀌면 실행 중인 컨테이너의 바인드 마운트가 끊기므로,
# 마운트를 통해 컨테이너 안으로 직접 써 넣는다.
switch_nginx_conf() {
  local color=$1
  echo "nginx 설정을 ${color} 로 교체합니다."
  sudo cp -f "${NGINX_DIR}/nginx-${color}.conf" "${NGINX_DIR}/nginx.conf"
  docker exec -i nginx sh -c 'cat > /etc/nginx/conf.d/nginx.conf' < "${NGINX_DIR}/nginx.conf"
  docker exec nginx nginx -t
  docker exec nginx nginx -s reload
}

# nginx 를 통해 실제로 새 컨테이너에 요청이 닿는지 확인한다.
verify_upstream() {
  local color=$1
  local retries=0
  while [ $retries -lt 10 ]; do
    if docker exec nginx wget -q -O /dev/null "http://atcha-${color}:8080/api/health"; then
      echo "${color} 업스트림 확인 완료."
      return 0
    fi
    echo "${color} 업스트림 확인 실패, 재시도합니다. (attempt: $((retries+1)))"
    sleep 3
    retries=$((retries+1))
  done
  return 1
}

if [ -z "$IS_BLUE" ]; then
  NEW_COLOR="blue"
  OLD_COLOR="green"
  echo "### GREEN => BLUE ###"
else
  NEW_COLOR="green"
  OLD_COLOR="blue"
  echo "### BLUE => GREEN ###"
fi

echo "1. ${NEW_COLOR} 이미지 받기"
docker compose pull "atcha-${NEW_COLOR}"

echo "2. ${NEW_COLOR} 컨테이너 실행"
docker compose up -d "atcha-${NEW_COLOR}"

echo "3. ${NEW_COLOR} 컨테이너 헬스 체크"
if ! check_service "atcha-${NEW_COLOR}"; then
  echo "${NEW_COLOR} health check failed. 기존 컨테이너를 유지한 채 배포를 중단합니다."
  exit 1
fi

echo "4. nginx 설정 전환"
ensure_nginx_running
switch_nginx_conf "$NEW_COLOR"

echo "5. 전환 확인"
if ! verify_upstream "$NEW_COLOR"; then
  echo "${NEW_COLOR} 로 요청이 닿지 않습니다. ${OLD_COLOR} 로 되돌립니다."
  switch_nginx_conf "$OLD_COLOR"
  exit 1
fi

echo "6. ${OLD_COLOR} 컨테이너 중지 및 삭제"
docker compose stop "atcha-${OLD_COLOR}"
docker compose rm -f "atcha-${OLD_COLOR}"

echo "배포 완료: ${OLD_COLOR} => ${NEW_COLOR}"
