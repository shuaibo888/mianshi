# Mini ATS 部署流程：只上传 jar 和 dist

这版部署方式和你的原项目一致：服务器不执行 Maven / Node 构建，只上传后端 jar 包和前端 dist。

服务器目录建议：

```text
/root/miniats/
  backend/
    mini-ats.jar
  frontend/
    dist/
  uploads/
  .env
  docker-compose.yml
  Dockerfile.backend
  Dockerfile.frontend
  miniats-nginx.conf
```

## 1. 本地构建后端 jar

在本地项目根目录执行：

```bash
mvn -s backend/maven-settings.xml -pl backend clean package -DskipTests
```

构建完成后，本地 jar 在：

```text
backend/target/mini-ats-0.0.1-SNAPSHOT.jar
```

上传到服务器后建议重命名为：

```text
/root/miniats/backend/mini-ats.jar
```

## 2. 本地构建前端 dist

在本地执行：

```bash
cd frontend
npm run build
```

构建完成后，本地 dist 在：

```text
frontend/dist
```

上传到服务器：

```text
/root/miniats/frontend/dist
```

## 3. 上传部署配置文件

把下面这些文件也放到服务器 `/root/miniats`：

```text
docker-compose.yml
Dockerfile.backend
Dockerfile.frontend
miniats-nginx.conf
.env
```

`.env` 不提交到 Git，只放在服务器本地，内容参考：

```env
MINIATS_DB_USERNAME=root
MINIATS_DB_PASSWORD=你的MySQL密码
MINIATS_JWT_SECRET=至少32位的JWT密钥
```

## 4. 确认公共 Docker 网络

```bash
docker network create cboo-net
```

如果提示已存在，可以忽略。

## 5. 确认公共 MySQL 已启动

```bash
docker ps | grep cboo-mysql
```

如果没启动：

```bash
cd /root/common-mysql
docker compose up -d
```

## 6. 创建 Mini ATS 数据库

```bash
docker exec -it cboo-mysql mysql -uroot -p
```

输入密码：

```text
使用 .env 中的 MINIATS_DB_PASSWORD
```

执行：

```sql
CREATE DATABASE IF NOT EXISTS mini_ats
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;
exit;
```

表不用手动建，后端启动后 Flyway 会自动建表。

## 7. 启动 Mini ATS

```bash
cd /root/miniats
docker compose up -d --build
```

## 8. 检查容器

```bash
docker ps
```

应该看到：

```text
miniats-backend
miniats-frontend
```

## 9. 检查后端日志

```bash
docker logs miniats-backend
```

如果看到 Spring Boot 正常启动，并且没有数据库连接错误，就说明后端正常。

检查数据库表：

```bash
docker exec -it cboo-mysql mysql -uroot -p
```

```sql
USE mini_ats;
SHOW TABLES;
exit;
```

## 10. 确认公共 Nginx 配置

`/root/common-nginx/nginx.conf` 里需要包含：

```nginx
server {
    listen 443 ssl;
    server_name miniats.cboo.cloud;

    ssl_certificate      /etc/nginx/cert/server.crt;
    ssl_certificate_key  /etc/nginx/cert/server.key;

    ssl_session_cache shared:SSL:10m;
    ssl_session_timeout 10m;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;

    location /api/ {
        proxy_set_header Host $http_host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header REMOTE-HOST $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_pass http://miniats-backend:8080;
    }

    location / {
        proxy_set_header Host $http_host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_pass http://miniats-frontend:80;
    }
}
```

80 跳 HTTPS 的配置里也要包含：

```nginx
server_name cboo.cloud miniats.cboo.cloud;
```

## 11. 重启公共 Nginx

建议先检查配置：

```bash
docker exec cboo-nginx nginx -t
```

没问题后重启：

```bash
cd /root/common-nginx
docker compose restart
```

## 12. 访问验证

```bash
curl -I https://miniats.cboo.cloud
```

浏览器访问：

```text
https://miniats.cboo.cloud
```

默认账号：

```text
admin
admin123456
```

## 后续更新

以后更新时，只需要重新上传：

```text
/root/miniats/backend/mini-ats.jar
/root/miniats/frontend/dist
```

然后执行：

```bash
cd /root/miniats
docker compose up -d --build
```

简历文件保存在：

```text
/root/miniats/uploads/resumes
```

因为它是 volume 挂载，重建容器不会丢。
