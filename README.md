# JavaWeb Market 2.0

基于 **Spring Boot 3.x + Vue 3** 的多模块电商平台，支持分布式集群部署。

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3.x |
| 构建工具 | Maven 3.9 |
| JDK | OpenJDK 21 |
| 前端 | Vue 3 + Vite + Pinia + Element Plus |
| 数据库 | MySQL 8.0 |
| 缓存 | Redis 7 |
| 消息队列 | Kafka |
| 反向代理 | Nginx |
| CI/CD | Jenkins |

## 项目结构

```
javaweb2.0/
├── market-api/        # API 层（REST Controller）
├── market-service/    # 业务逻辑层
├── market-dal/        # 数据访问层（MyBatis）
├── market-common/     # 公共模块（DTO、工具、异常）
├── market-security/   # 安全模块（JWT 认证 + 鉴权拦截器）
├── market-boot/       # 启动模块（Spring Boot Application）
├── market-task/       # 定时任务（订单取消、缓存刷新）
├── market-monitor/    # 监控指标
├── sql/               # 数据库初始化脚本
└── vue-market/        # 前端 SPA（Vue 3 + Vite）
```

## 核心功能

- 用户注册/登录（JWT 认证）
- 商品浏览、搜索、分类
- 购物车管理
- 订单创建、支付、取消
- 秒杀活动（Redis 缓存 + 分布式锁）
- 后台管理（商品管理、订单管理、用户管理、数据统计）
- 雪花算法分布式 ID 生成
- 令牌桶限流

## 快速开始

### 环境要求

- JDK 21+
- Maven 3.9+
- Node.js 22+
- MySQL 8.0+
- Redis 7+
- Kafka 4.2+

### 本地构建

```bash
# 克隆项目
git clone git@github.com:MoonWhite-24/javaweb.git
cd javaweb

# 后端构建
mvn clean package -DskipTests

# 前端构建
cd vue-market
npm install
npm run dev
```

### 部署

完整的部署文档请参考：

- **[项目部署指南](./项目部署指南.md)** — 集群拓扑、环境搭建、初次部署、日常运维
- **[Jenkins部署指南](./Jenkins部署指南.md)** — CI/CD 流水线配置、账号密码、API 调用

## 集群架构

```
Nginx (80) ──→ Master  (192.168.200.100:8080)  worker_id=1
            ├── Node1   (192.168.200.101:8080)  worker_id=2
            └── Node2   (192.168.200.102:8080)  worker_id=3
```

## 部署文档与 CI/CD

| 文档 | 说明 |
|------|------|
| [项目部署指南.md](./项目部署指南.md) | 完整部署流程、集群配置、故障排查 |
| [Jenkins部署指南.md](./Jenkins部署指南.md) | Jenkins 安装、Job 配置、账号密码 |

## License

MIT
