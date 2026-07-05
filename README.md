# Mini ATS 招聘候选人跟进系统

这是一个用于实战考核的 Mini ATS 系统，支持固定账号登录、候选人新增编辑、状态流转、面试记录、搜索筛选和统计看板。

## 技术栈

- 前端：Vue 3、Vite、TypeScript、Vue Router、Pinia、Axios、Element Plus
- 后端：JDK 17、Spring Boot 3.3、Spring Web、Spring Security、JWT、MyBatis Plus、Flyway
- 数据库：MySQL 8
- 当前阶段：本地开发与测试

## 测试账号

- 账号：admin
- 密码：admin123456

系统不开放注册，账号由数据库初始化脚本写入。

## 本地开发

后端：

```bash
set SPRING_DATASOURCE_PASSWORD=你的本地MySQL密码
set APP_JWT_SECRET=至少32位的本地JWT密钥
mvn -s backend/maven-settings.xml -pl backend test
mvn -s backend/maven-settings.xml -pl backend spring-boot:run
```

也可以进入 `backend` 目录后执行：

```bash
cd backend
set SPRING_DATASOURCE_PASSWORD=你的本地MySQL密码
set APP_JWT_SECRET=至少32位的本地JWT密钥
mvn -s maven-settings.xml test
mvn -s maven-settings.xml spring-boot:run
```

前端：

```bash
cd frontend
npm.cmd install
npm.cmd run dev
```

本地前端默认访问：

```text
http://localhost:5173
```

## 已完成功能

- 固定账号密码登录
- JWT 鉴权
- 候选人列表、新增、编辑、删除
- 候选人详情
- 招聘状态流转校验
- 状态变更记录
- 面试记录新增、编辑、删除
- 按关键词、岗位、状态搜索筛选
- 统计看板：总候选人数、各状态人数、平均评分、录用率

## 状态流转规则

主流程：

```text
已投递 -> 初筛通过 -> 面试中 -> 待 offer -> 已录用
```

淘汰路径：

```text
已投递 / 初筛通过 / 面试中 / 待 offer -> 已淘汰
```

系统不允许从“已投递”直接跳到“已录用”，也不允许从终态继续流转。

## 测试方式

- 后端核心测试覆盖候选人状态流转策略。
- 后端可用 `mvn -s maven-settings.xml test` 运行测试。
- 前端可用 `npm.cmd run build` 做类型检查和生产构建。
- 本地联调时通过浏览器完成登录、新增候选人、编辑候选人、推进状态、添加面试记录、搜索筛选、查看统计看板的完整自测。

## 未完成项

- 没有做注册功能，符合本次需求。
- 没有引入 Redis，目前业务不需要缓存、验证码或分布式 session。
- 没有做复杂角色权限，系统只有固定管理员账号。

## AI Agent 使用说明

AI Agent 参与了需求拆解、方案设计、代码生成、接口设计、状态流转规则设计和开发测试说明整理。关键业务规则包括状态流转、JWT 鉴权和数据库表结构，需要由开发者最终 review 并通过本地环境验证。
