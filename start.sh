#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

echo "==> 检测前置环境"
missing=0
check() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "  [缺失] $1 —— $2"
    missing=1
  else
    echo "  [OK] $1"
  fi
}
check java "需要 JDK 17+，请先安装"
check mvn "需要 Maven，请先安装"
check node "需要 Node.js，请先安装"
check npm "需要 npm，请先安装"
check docker "需要 Docker，请先安装并启动 Docker Desktop"
if ! docker info >/dev/null 2>&1; then
  echo "  [缺失] Docker 未运行 —— 请启动 Docker Desktop"
  missing=1
fi
[ "$missing" -eq 1 ] && exit 1

echo "==> 构建前端"
(cd hotel-web && npm install && npm run build)

echo "==> 拷贝前端产物到后端 static"
rm -rf hotel-server/src/main/resources/static
mkdir -p hotel-server/src/main/resources/static
cp -r hotel-web/dist/. hotel-server/src/main/resources/static/

echo "==> 打包后端"
(cd hotel-server && mvn -q package -DskipTests)

echo "==> 启动 MySQL 容器"
docker compose up -d

echo "==> 等待 MySQL 就绪"
for i in $(seq 1 60); do
  if docker exec hotel-mysql mysqladmin ping -uroot -proot123 --silent >/dev/null 2>&1; then
    echo "  MySQL 已就绪"
    break
  fi
  [ "$i" -eq 60 ] && { echo "  MySQL 启动超时"; exit 1; }
  sleep 2
done

echo "==> 启动应用（http://localhost:8080）"
exec java -jar hotel-server/target/hotel-server.jar
