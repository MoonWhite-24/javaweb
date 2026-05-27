# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
# Backend
mvn clean package -DskipTests              # Build all modules
mvn clean package -DskipTests -pl market-boot -am  # Build only boot module + dependencies
mvn test -pl market-service -am            # Run tests for service module

# Frontend
cd vue-market
npm install        # First time setup
npm run dev        # Dev server on :5173, proxies /api → 192.168.200.100:8080
npm run build      # Production build → dist/

# Database
# Scripts in sql/sql/ (init.sql = schema, init_data.sql = seed data)
```

## Architecture

### Backend (Spring Boot 3.3.7, JDK 17, Maven multi-module)

Root `pom.xml` is the parent POM declaring 8 modules. Module dependency chain: `market-common` → `market-dal` → `market-service` → `market-api` → `market-boot`. `market-security` and `market-monitor` are standalone; `market-boot` wires everything together. Entry point: `Application.java` (`@EnableScheduling` is on).

Several modules use a two-level structure (e.g., `market-dal/market-dal/pom.xml`) where the outer POM is a grouping parent that only contains the inner child module. The root parent POM lists the direct child dirs (market-common, market-dal, etc.), not the inner module artifacts.

**Layer flow:**
- **market-common** — shared types used everywhere: `R<T>` (unified response), `UserDTO`, `BusinessException`/`ForbiddenException`/`UnauthorizedException`, enums (`OrderStatusEnum`, `ResponseCode`, `RedisKeyPrefix`, `KafkaTopic`), utility classes
- **market-dal** — MyBatis mappers (no JPA/Repository pattern) + entity classes. Uses XML mapper files at `classpath:mapper/*.xml`. `map-underscore-to-camel-case` is on.
- **market-service** — business logic, `@Service` implementations. Houses cross-cutting concerns: `SnowflakeIdGenerator`, `RedisDistributedLock`, `TokenBucketRateLimiter`, `OperateLogAspect`
- **market-api** — REST controllers + `GlobalExceptionHandler` (`@RestControllerAdvice`). Also holds `WebMvcConfig` (CORS + `AdminInterceptor` registration)
- **market-security** — `JwtAuthFilter` (`OncePerRequestFilter`, registered on `/api/*`), `JwtUtil`, `AdminInterceptor`, `SecurityConfig` (creates the filter bean)
- **market-boot** — `Application.java` entry point, `application.yml`, `SnowflakeConfig`
- **market-task** — `@Scheduled` tasks: `OrderTimeoutCancelTask`, `RedisCacheRefreshTask`, `SeckillStatusSyncTask`
- **market-monitor** — `MetricsCollector` for monitoring

**Request lifecycle:** `JwtAuthFilter` (checks Bearer token unless path is public — see `isPublicPath()` whitelist) → extracts `UserDTO` onto `request.setAttribute("currentUser", user)` → `AdminInterceptor` (for `/api/admin/**` routes, checks `user.role == 1`) → controller → service → mapper.

**JWT:** Access token (15min) and refresh token (7d). Claims: `sub` = userId, `username`, `role`. The frontend auto-refreshes on 401 via axios interceptor.

**Seckill flow (critical path):** Controller → `SeckillService.doSeckill()` → Lua script (`lua/seckill_stock.lua`) atomically decrements Redis stock and records user → Snowflake generates orderNo → sends order-create message to Kafka → order processed asynchronously by consumer in `OrderServiceImpl`.

**Key Redis/Valkey keys (see `RedisKeyPrefix`):** `seckill:stock:{id}`, `seckill:users:{id}` (set of userIds), `seckill:order:{userId}:{spId}` (temporary, 5min TTL). Server runs **Valkey 8.0.7** (Redis OSS drop-in replacement) at `192.168.200.100:6379`, password `Redis@2026!`, CLI: `valkey-cli`. Spring Boot still uses `spring.data.redis` config — Valkey speaks the Redis protocol.

### Frontend (Vue 3, Vite 5, Element Plus 2)

**Directory layout:**
- `api/` — Axios API modules (auth, product, cart, order, seckill, admin). `request.js` is the configured axios instance with JWT interceptor and auto-refresh logic.
- `views/` — page components organized by domain (product/, user/, cart/, order/, seckill/, admin/, error/)
- `components/` — shared UI (Navbar, Footer, Sidebar, ProductCard, Pagination, AdminHeader, AdminSidebar)
- `layouts/` — `DefaultLayout.vue` (public/user pages) and `AdminLayout.vue` (admin pages)
- `stores/` — Pinia stores: `user.js`, `cart.js`, `app.js`
- `router/` — `index.js` with route guards: `requiresAuth` checks `localStorage.accessToken`, `requiresAdmin` checks `userInfo.role === 1`

**Role model:** `role === 1` = admin, anything else = regular user.

### Deployment (3-node cluster)

JAR deployed to `/opt/javaweb2.0/market-boot.jar`, run via systemd service `market2`. Each node gets a unique Snowflake worker ID via `SNOWFLAKE_WORKER_ID` env var in the systemd unit file. Nginx load balances across all three nodes. Jenkins pipeline (declarative) builds backend with `mvn clean package -DskipTests -q`, frontend with `npm ci && npm run build`, then SCPs artifacts and restarts services.

### Infrastructure (3 nodes, Rocky Linux 10.1)

| Node | IP | Role | Services |
|------|----|------|----------|
| master | 192.168.200.100 | Master | mariadb, valkey, kafka, nginx, market2 (worker_id=1) |
| node1 | 192.168.200.101 | Worker | market2 (worker_id=2) |
| node2 | 192.168.200.102 | Worker | market2 (worker_id=3) |

- **MariaDB 8.0** — `192.168.200.100:3306`, database `javaweb_market`, user `market` / `Market@2026!`. Driver: `org.mariadb.jdbc.Driver`. Connection pool: Druid (`type: com.alibaba.druid.pool.DruidDataSource` — Hikari settings in `application.yml` are overridden by Druid).
- **Valkey 8.0.7** — `192.168.200.100:6379`, systemd service `valkey`, CLI `valkey-cli`, password `Redis@2026!`. Fully Redis-compatible protocol.
- **Kafka** — `192.168.200.100:9092`
- **Nginx** — load balancer on port 80, distributing to all 3 nodes. Config version-controlled at `nginx/market2.conf`.

## After Making Changes — Sync & Deploy

This PC cannot reach GitHub directly, but the Master VM can. The workflow:

```bash
# 1. After editing files locally, SCP changed files to the master VM
scp <changed-files> root@192.168.200.100:/opt/javaweb2.0/<path>

# Example: sync everything
scp -r nginx/ root@192.168.200.100:/opt/javaweb2.0/
scp CLAUDE.md root@192.168.200.100:/opt/javaweb2.0/

# 2. Commit and push from the master VM
ssh root@192.168.200.100 "cd /opt/javaweb2.0 && git add -A && git commit -m '<message>' && git push origin main"

# 3. Trigger Jenkins build
ssh root@192.168.200.100 '
CRUMB=$(curl -s -c /tmp/jenkins_cookie -u admin:admin123 "http://localhost:9090/crumbIssuer/api/json" | python3 -c "import sys,json; print(json.load(sys.stdin)[\"crumb\"])")
curl -s -o /dev/null -w "HTTP %{http_code}\n" -b /tmp/jenkins_cookie -H "Jenkins-Crumb: $CRUMB" -u admin:admin123 -X POST "http://localhost:9090/job/market2-deploy/build"
'

# 4. Check build status
ssh root@192.168.200.100 'curl -s -u admin:admin123 "http://localhost:9090/job/market2-deploy/lastBuild/api/json" | python3 -c "import sys,json; d=json.load(sys.stdin); print(f\"Build #{d[\"number\"]}: {d[\"result\"]}\")"'
```

**SSH passwords for all 3 VMs:** root / admin  
**Jenkins:** http://192.168.200.100:9090 — admin / admin123  
**GitHub repo:** git@github.com:MoonWhite-24/javaweb.git  

### Nginx config sync to servers

After updating `nginx/` files in the repo, deploy them to all 3 nodes:

```bash
for ip in 100 101 102; do
  scp nginx/nginx.conf root@192.168.200.$ip:/etc/nginx/nginx.conf
  scp nginx/market2.conf root@192.168.200.$ip:/etc/nginx/conf.d/market2.conf
  ssh root@192.168.200.$ip "nginx -t && systemctl reload nginx && echo '192.168.200.$ip: OK'"
done
```

All 3 nodes need port 80 open in firewalld. If adding a new node:
```bash
firewall-cmd --add-port=80/tcp --permanent && firewall-cmd --reload
```
- **market2** — systemd service at `/opt/javaweb2.0/market-boot.jar`, Snowflake worker ID set via `SNOWFLAKE_WORKER_ID` env var in systemd unit file

## Module nesting note

Four modules have an extra parent-child nesting: `market-common/market-common/`, `market-dal/market-dal/`, `market-service/market-service/`, `market-monitor/market-monitor/`, `market-task/market-task/`. The outer directory has a POM that groups a single child module. The root POM's `<modules>` references the outer directories (e.g., `<module>market-common</module>`), which in turn references the inner artifact. When adding a new Java file, place it under the inner module's `src/main/java/com/market/...`.

The flat modules (`market-api`, `market-security`, `market-boot`) have no inner nesting — their `pom.xml` and `src/` are directly under the module directory.
