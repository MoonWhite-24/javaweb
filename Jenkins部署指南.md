# Jenkins CI/CD 部署指南

## 环境信息

| 项目 | 详情 |
|------|------|
| **Jenkins 版本** | 2.555.2 |
| **Jenkins URL** | http://192.168.200.100:9090 |
| **运行方式** | WAR 文件 + systemd |
| **JENKINS_HOME** | /opt/jenkins/home |
| **Jenkins 用户** | root |
| **操作系统** | Rocky Linux 10.1 |
| **JDK** | OpenJDK 21.0.11 |

## 账号密码

| 用途 | 用户名 | 密码 |
|------|--------|------|
| **Jenkins 管理员** | admin | admin123 |
| **Master VM (192.168.200.100)** | root | admin |
| **Node1 VM (192.168.200.101)** | root | admin |
| **Node2 VM (192.168.200.102)** | root | admin |
| **GitHub SSH** | git@github.com:MoonWhite-24/javaweb.git | — |

## Jenkins 服务管理

### systemd 服务配置

```ini
# /etc/systemd/system/jenkins.service
[Unit]
Description=Jenkins CI/CD Server
After=network.target

[Service]
User=root
WorkingDirectory=/opt/jenkins
Environment="JENKINS_HOME=/opt/jenkins/home"
ExecStart=/usr/bin/java -Djenkins.install.runSetupWizard=false -jar /opt/jenkins/jenkins.war --httpPort=9090
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

### 常用命令

```bash
# 启动 Jenkins
systemctl start jenkins

# 停止 Jenkins
systemctl stop jenkins

# 重启 Jenkins
systemctl restart jenkins

# 查看状态
systemctl status jenkins

# 查看日志
journalctl -u jenkins -f
```

### Jenkins 关键目录

| 路径 | 说明 |
|------|------|
| `/opt/jenkins/jenkins.war` | Jenkins WAR 文件 |
| `/opt/jenkins/home/` | JENKINS_HOME 根目录 |
| `/opt/jenkins/home/jobs/` | 所有 Job 配置和构建历史 |
| `/opt/jenkins/home/init.groovy.d/security.groovy` | 初始化脚本（创建管理员、跳过向导） |
| `/opt/jenkins/home/jenkins.model.JenkinsLocationConfiguration.xml` | Jenkins URL 配置 |

## CI/CD 流水线

### Job 名称：`market2-deploy`

**类型：** Freestyle Project（无需额外插件）

**5 个构建阶段：**

| 阶段 | 内容 | 关键操作 |
|------|------|----------|
| Stage 0: Git Checkout | 从 GitHub 拉取最新代码 | `git fetch origin main` + `git reset --hard origin/main` |
| Stage 1: Backend Build | Maven 构建后端 | `mvn clean package -DskipTests -q` |
| Stage 2: Frontend Build | npm + Vite 构建前端 | `npm ci` + `npm run build` |
| Stage 3: Deploy | 分发并部署到 3 节点 | scp JAR + dist → 3 VM → `systemctl restart market2` |
| Stage 4: Health Check | 健康检查 | SSH 到每节点执行 `curl localhost:8080/api/health`（最多 15 次重试） |

### 节点与 Snowflake Worker ID

| 节点 | IP | Worker ID |
|------|-----|-----------|
| Master | 192.168.200.100 | 1 |
| Node1 | 192.168.200.101 | 2 |
| Node2 | 192.168.200.102 | 3 |

### 触发构建

**方式一：Jenkins Web UI**
1. 打开 http://192.168.200.100:9090
2. 登录 admin / admin123
3. 点击 `market2-deploy` → `Build Now`

**方式二：REST API（命令行）**

```bash
# 获取 CSRF Token
CRUMB=$(curl -s -c /tmp/cookie -u admin:admin123 \
  "http://192.168.200.100:9090/crumbIssuer/api/json" \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['crumb'])")

# 触发构建
curl -s -b /tmp/cookie -H "Jenkins-Crumb: $CRUMB" \
  -u admin:admin123 -X POST \
  "http://192.168.200.100:9090/job/market2-deploy/build"

# 查看最新构建状态
curl -s -u admin:admin123 \
  "http://192.168.200.100:9090/job/market2-deploy/lastBuild/api/json"

# 查看构建日志
curl -s -u admin:admin123 \
  "http://192.168.200.100:9090/job/market2-deploy/lastBuild/consoleText"
```

### 通过 Script Console 更新 Job

Job 的 Groovy 脚本位于 `/opt/javaweb2.0/create-freestyle-job.groovy`，可通过 Script Console API 执行：

```bash
curl -s -c /tmp/cookie -u admin:admin123 \
  "http://localhost:9090/crumbIssuer/api/json" > /tmp/crumb.json
CRUMB=$(python3 -c "import json; print(json.load(open('/tmp/crumb.json'))['crumb'])")

curl -s -b /tmp/cookie -H "Jenkins-Crumb: $CRUMB" \
  -u admin:admin123 \
  --data-binary @/opt/javaweb2.0/create-freestyle-job.groovy \
  "http://localhost:9090/scriptText"
```

## 故障排查

### Jenkins 无法访问

```bash
# 检查服务状态
systemctl status jenkins

# 检查端口
ss -tlnp | grep 9090

# 检查认证
curl -s -u admin:admin123 http://localhost:9090/crumbIssuer/api/json
```

### Job 构建失败

1. 查看构建日志：Jenkins Web UI → `market2-deploy` → 构建编号 → Console Output
2. 常见问题：
   - **8080 端口被占用**：确认旧 `market.service` 已禁用（`systemctl mask market`）
   - **SSH 连接失败**：确认 Master 到各节点的 SSH 免密登录正常
   - **Maven 构建失败**：检查 `/opt/javaweb2.0` 代码是否完整
   - **Health Check 404**：确认 market2 服务已绑定 8080 端口

### Jenkins 重置管理员密码

```bash
# 停止 Jenkins
systemctl stop jenkins

# 删除 admin 用户目录
rm -rf /opt/jenkins/home/users/admin_*

# 删除配置（触发 init.groovy.d 重新执行）
rm -f /opt/jenkins/home/config.xml

# 启动（init.groovy.d/security.groovy 会重新创建 admin/admin123）
systemctl start jenkins
```
