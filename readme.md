export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
cd /Users/jiangchao/Dev/fullStack/Insurance/insurance-platform/insurance-web
mvn spring-boot:run -Dmaven.repo.local=/Users/jiangchao/Maven/jar

# 本地环境准备
docker compose -f docker-compose.local.yml up -d

# 只启动某一个
docker compose -f docker-compose.local.yml up -d postgres

# TODO
接入Flyway 数据库迁移
