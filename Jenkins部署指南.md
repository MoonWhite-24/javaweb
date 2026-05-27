# Jenkins CI/CD 部署指南

## 环境信息

| 项目 | 详情 |
|------|------|
| **Jenkins 版本** | 2.555.2 |
| **Jenkins URL** | http://192.168.200.100:9090 |
| **运行方式** | WAR 文件 + systemd 服务 |
| **JENKINS_HOME** | /opt/jenkins/home |
| **运行用户** | root |
| **操作系统** | Rocky Linux 10.1 |
| **JDK** | OpenJDK 21.0.11 |
| **GitHub 仓库** | git@github.com:MoonWhite-24/javaweb.git |

## 账号密码

| 用途 | 用户名 | 密码 |
|------|--------|------|
| **Jenkins 管理员** | admin | admin123 |
| **Master VM (192.168.200.100)** | root | admin |
| **Node1 VM (192.168.200.101)** | root | admin |
| **Node2 VM (192.168.200.102)** | root | admin |

---

## 一、Jenkins 安装与配置

### 1.1 安装 Jenkins WAR

Jenkins 通过 YUM 仓库安装失败（GPG key 过期），改为直接下载 WAR 文件：

```bash
# 创建目录
mkdir -p /opt/jenkins/home

# 下载 WAR 文件
wget -O /opt/jenkins/jenkins.war \
  https://get.jenkins.io/war-stable/2.555.2/jenkins.war
```

### 1.2 初始化安全配置

Jenkins 启动时通过 `init.groovy.d` 脚本自动创建管理员并跳过安装向导。

**`/opt/jenkins/home/init.groovy.d/security.groovy`：**

```groovy
import jenkins.model.*
import hudson.security.*

def instance = Jenkins.getInstanceOrNull()
if (instance == null) {
    instance = Jenkins.get()
}
def realm = new HudsonPrivateSecurityRealm(false)
realm.createAccount('admin', 'admin123')
instance.setSecurityRealm(realm)
def strategy = new FullControlOnceLoggedInAuthorizationStrategy()
strategy.setAllowAnonymousRead(false)
instance.setAuthorizationStrategy(strategy)
instance.save()
```

### 1.3 配置 Jenkins URL

Jenkins 需要知道自己的访问地址，否则 CLI 和 API 会返回 403。

**`/opt/jenkins/home/jenkins.model.JenkinsLocationConfiguration.xml`：**

```xml
<?xml version="1.1" encoding="UTF-8"?>
<jenkins.model.JenkinsLocationConfiguration>
  <adminAddress>admin@market.local</adminAddress>
  <jenkinsUrl>http://192.168.200.100:9090/</jenkinsUrl>
</jenkins.model.JenkinsLocationConfiguration>
```

### 1.4 systemd 服务配置

**`/etc/systemd/system/jenkins.service`：**

```ini
[Unit]
Description=Jenkins CI/CD Server
After=network.target

[Service]
User=root
WorkingDirectory=/opt/jenkins
Environment="JENKINS_HOME=/opt/jenkins/home"
ExecStart=/usr/bin/java \
  -Djenkins.install.runSetupWizard=false \
  -jar /opt/jenkins/jenkins.war \
  --httpPort=9090
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

> `-Djenkins.install.runSetupWizard=false` 必须添加，否则 `init.groovy.d` 脚本不会生效，Jenkins 将要求初始密码。

```bash
systemctl daemon-reload
systemctl enable jenkins
systemctl start jenkins
```

### 1.5 验证安装

```bash
# 等待启动完成
sleep 10

# 测试 API 是否可用（应返回 JSON）
curl -s -u admin:admin123 http://localhost:9090/crumbIssuer/api/json
# 预期输出: {"_class":"...","crumb":"...","crumbRequestField":"Jenkins-Crumb"}
```

### 1.6 Jenkins 关键目录

| 路径 | 说明 |
|------|------|
| `/opt/jenkins/jenkins.war` | Jenkins WAR 文件 |
| `/opt/jenkins/home/` | JENKINS_HOME 根目录 |
| `/opt/jenkins/home/config.xml` | Jenkins 主配置 |
| `/opt/jenkins/home/users/` | 用户账号数据 |
| `/opt/jenkins/home/jobs/` | Job 配置 + 构建历史 |
| `/opt/jenkins/home/workspace/` | Job 工作空间 |
| `/opt/jenkins/home/init.groovy.d/` | 启动时执行的初始化脚本 |
| `/opt/jenkins/home/jenkins.model.JenkinsLocationConfiguration.xml` | Jenkins URL 配置 |

### 1.7 服务管理命令

```bash
systemctl start jenkins     # 启动
systemctl stop jenkins      # 停止
systemctl restart jenkins   # 重启
systemctl status jenkins    # 查看状态
journalctl -u jenkins -f    # 实时查看日志
```

---

## 二、CI/CD 流水线定义

Job `market2-deploy` 通过 **Groovy 脚本** 声明式创建，以上传至 `/opt/javaweb2.0/create-freestyle-job.groovy`。

**Job 类型：** Freestyle Project（`hudson.model.FreeStyleProject`）  
**不使用 Pipeline 插件**（无需安装 `workflow-job` 等插件），仅使用 Jenkins 核心内置的 Shell 构建步骤。

### 2.1 完整 Groovy 脚本（create-freestyle-job.groovy）

```groovy
import jenkins.model.*
import hudson.model.*
import hudson.tasks.*

def jenkinsInstance = Jenkins.getInstanceOrNull()
if (jenkinsInstance == null) {
    println 'Jenkins instance is null!'
    return
}

// 创建 Freestyle Job（如已存在则先删除）
def jobName = 'market2-deploy'
def existingJob = jenkinsInstance.getItem(jobName)
if (existingJob != null) {
    existingJob.delete()
    println 'Deleted existing job'
}

def job = jenkinsInstance.createProject(FreeStyleProject.class, jobName)
job.description = 'JavaWeb Market 2.0 CI/CD - Build and Deploy to 3 VMs'

// ============================================================
// Stage 0: Git Checkout —— 从 GitHub 拉取最新代码
// ============================================================
def gitCheckout = new hudson.tasks.Shell('''
echo "=== Stage 0: Git Checkout ==="
cd /opt/javaweb2.0
git fetch origin main
git reset --hard origin/main
echo "Git checkout: $(git log --oneline -1)"
''')

// ============================================================
// Stage 1: Backend Build —— Maven 构建所有模块
// ============================================================
def backendBuild = new hudson.tasks.Shell('''
echo "=== Stage 1: Backend Build ==="
cd /opt/javaweb2.0
mvn clean package -DskipTests -q
echo "Backend build completed"
''')

// ============================================================
// Stage 2: Frontend Build —— npm + Vite 构建
// ============================================================
def frontendBuild = new hudson.tasks.Shell('''
echo "=== Stage 2: Frontend Build ==="
cd /opt/javaweb2.0/vue-market
npm ci --silent 2>/dev/null || echo "Using existing node_modules"
npm run build
echo "Frontend build completed"
''')

// ============================================================
// Stage 3: Distribute and Deploy —— 分发到 3 节点并重启
// ============================================================
def deployAll = new hudson.tasks.Shell('''
echo "=== Stage 3: Distribute and Deploy ==="
JAR=/opt/javaweb2.0/market-boot/target/market-boot-2.0.0.jar
DIST=/opt/javaweb2.0/vue-market/dist/

# --- Deploy to Master (worker 1) ---
echo "--- Deploying to Master (192.168.200.100) ---"
cp ${JAR} /opt/javaweb2.0/market-boot.jar
sed -i "s|SNOWFLAKE_WORKER_ID=.*|SNOWFLAKE_WORKER_ID=1|" /etc/systemd/system/market2.service
systemctl daemon-reload
systemctl restart market2
sleep 10
echo "Master deployed"

# --- Deploy to Node1 (worker 2) ---
echo "--- Deploying to Node1 (192.168.200.101) ---"
scp ${JAR} root@192.168.200.101:/opt/javaweb2.0/market-boot.jar
scp -r ${DIST}* root@192.168.200.101:/opt/javaweb2.0/vue-market/dist/
ssh root@192.168.200.101 "sed -i 's|SNOWFLAKE_WORKER_ID=.*|SNOWFLAKE_WORKER_ID=2|' /etc/systemd/system/market2.service && systemctl daemon-reload && systemctl restart market2"
sleep 10
echo "Node1 deployed"

# --- Deploy to Node2 (worker 3) ---
echo "--- Deploying to Node2 (192.168.200.102) ---"
scp ${JAR} root@192.168.200.102:/opt/javaweb2.0/market-boot.jar
scp -r ${DIST}* root@192.168.200.102:/opt/javaweb2.0/vue-market/dist/
ssh root@192.168.200.102 "sed -i 's|SNOWFLAKE_WORKER_ID=.*|SNOWFLAKE_WORKER_ID=3|' /etc/systemd/system/market2.service && systemctl daemon-reload && systemctl restart market2"
sleep 10
echo "Node2 deployed"
''')

// ============================================================
// Stage 4: Health Check —— 通过 SSH localhost 检查健康状态
// ============================================================
def healthCheck = new hudson.tasks.Shell('''
echo "=== Stage 4: Health Check ==="
check_node() {
    HOST=$1
    echo "Checking ${HOST}..."
    for i in $(seq 1 15); do
        STATUS=$(ssh root@${HOST} "curl -s -o /dev/null -w '%{http_code}' http://localhost:8080/api/health" 2>/dev/null)
        if [ "${STATUS}" = "200" ]; then
            echo "  ${HOST} health check PASSED (attempt ${i})"
            return 0
        fi
        echo "  ${HOST} attempt ${i}: HTTP ${STATUS}"
        sleep 3
    done
    echo "  ${HOST} health check FAILED!"
    return 1
}

check_node 192.168.200.100 || exit 1
check_node 192.168.200.101 || exit 1
check_node 192.168.200.102 || exit 1
echo "All 3 nodes healthy!"
''')

// 将 5 个构建步骤按顺序加入 Job
job.getBuildersList().add(gitCheckout)
job.getBuildersList().add(backendBuild)
job.getBuildersList().add(frontendBuild)
job.getBuildersList().add(deployAll)
job.getBuildersList().add(healthCheck)

job.save()

println "SUCCESS: Freestyle job 'market2-deploy' created"
println "Job URL: ${jenkinsInstance.rootUrl}job/market2-deploy/"
```

### 2.2 流水线阶段说明

| 阶段 | 步骤 | Shell 命令 | 预计耗时 |
|------|------|-----------|----------|
| **Stage 0** | Git Checkout | `git fetch origin main` + `git reset --hard origin/main` | ~5s |
| **Stage 1** | Backend Build | `mvn clean package -DskipTests -q` | ~30s |
| **Stage 2** | Frontend Build | `npm ci` + `npm run build` | ~20s |
| **Stage 3** | Distribute & Deploy | scp JAR + dist 到 3 节点，更新 worker_id，重启 market2，每节点等 10s | ~40s |
| **Stage 4** | Health Check | SSH 到每节点执行 `curl localhost:8080/api/health`，最多 15 次重试 × 3s 间隔 | ~10s |
| **总计** | — | — | **~2 min** |

### 2.3 Snowflake Worker ID 分配

每个节点运行的应用需要唯一的 Worker ID 以生成全局唯一 ID。

| 节点 | IP 地址 | Worker ID | 设置方式 |
|------|---------|-----------|----------|
| Master | 192.168.200.100 | 1 | systemd Environment |
| Node1 | 192.168.200.101 | 2 | systemd Environment |
| Node2 | 192.168.200.102 | 3 | systemd Environment |

部署阶段通过 `sed` 更新 `/etc/systemd/system/market2.service` 中的 `SNOWFLAKE_WORKER_ID` 环境变量。

### 2.4 Health Check 原理

健康检查通过 **SSH 到目标节点后执行 `curl localhost`**，而非从 Master 直接 HTTP 访问：

```bash
ssh root@192.168.200.101 "curl -s -o /dev/null -w '%{http_code}' http://localhost:8080/api/health"
```

**原因：** 之前出现过 Jenkins 构建上下文无法直连 Node1 的网络问题（HTTP 000），改用 SSH 中转后解决了此问题。

**重试机制：** 最多 15 次，间隔 3 秒（共 45 秒），足以覆盖 Spring Boot 应用的重启时间。

---

## 三、通过 Script Console API 管理 Job

### 3.1 执行 Groovy 脚本创建/更新 Job

Jenkins Script Console 支持 REST API，但需要 CSRF Token + Session Cookie 双认证。

```bash
# Step 1: 获取 CSRF Crumb + Session Cookie
curl -s -c /tmp/jenkins_cookie -u admin:admin123 \
  "http://192.168.200.100:9090/crumbIssuer/api/json" > /tmp/crumb.json

CRUMB=$(python3 -c "import json; print(json.load(open('/tmp/crumb.json'))['crumb'])")

# Step 2: 执行 Groovy 脚本
curl -s -b /tmp/jenkins_cookie \
  -H "Jenkins-Crumb: ${CRUMB}" \
  -u admin:admin123 \
  --data-binary @/opt/javaweb2.0/create-freestyle-job.groovy \
  "http://192.168.200.100:9090/scriptText"
```

### 3.2 触发构建

```bash
# 获取 crumb（如果 cookie 还有效可复用）
curl -s -c /tmp/jenkins_cookie -u admin:admin123 \
  "http://192.168.200.100:9090/crumbIssuer/api/json" > /tmp/crumb.json
CRUMB=$(python3 -c "import json; print(json.load(open('/tmp/crumb.json'))['crumb'])")

# 触发构建（返回 201 Created 表示成功入队）
curl -s -o /dev/null -w "HTTP %{http_code}\n" \
  -b /tmp/jenkins_cookie \
  -H "Jenkins-Crumb: ${CRUMB}" \
  -u admin:admin123 \
  -X POST "http://192.168.200.100:9090/job/market2-deploy/build"

# 查看构建队列
curl -s -u admin:admin123 \
  "http://192.168.200.100:9090/queue/api/json"
```

### 3.3 查询构建状态

```bash
# 最新构建概览（JSON）
curl -s -u admin:admin123 \
  "http://192.168.200.100:9090/job/market2-deploy/lastBuild/api/json" \
  | python3 -c "import sys,json; d=json.load(sys.stdin); print(f'Build #{d[\"number\"]}: {d[\"result\"]} ({d[\"duration\"]}ms)')"

# 最新构建控制台日志
curl -s -u admin:admin123 \
  "http://192.168.200.100:9090/job/market2-deploy/lastBuild/consoleText"

# 查看指定构建的控制台日志
curl -s -u admin:admin123 \
  "http://192.168.200.100:9090/job/market2-deploy/3/consoleText"

# 构建历史列表
curl -s -u admin:admin123 \
  "http://192.168.200.100:9090/job/market2-deploy/api/json?tree=builds[number,result,duration,timestamp]"
```

### 3.4 其他 Job 操作

```bash
# 查看 Job 配置 (config.xml)
curl -s -u admin:admin123 \
  "http://192.168.200.100:9090/job/market2-deploy/config.xml"

# 禁用 Job
curl -s -u admin:admin123 -X POST \
  "http://192.168.200.100:9090/job/market2-deploy/disable"

# 启用 Job
curl -s -u admin:admin123 -X POST \
  "http://192.168.200.100:9090/job/market2-deploy/enable"

# 删除某次构建记录
curl -s -u admin:admin123 -X POST \
  "http://192.168.200.100:9090/job/market2-deploy/2/doDelete"
```

---

## 四、备选方案：Declarative Pipeline（Jenkinsfile）

如果安装了 Pipeline 插件（`workflow-job`、`workflow-cps`），也可使用 Declarative Pipeline。以下为完整版 Jenkinsfile，支持 Groovy script 块进行节点循环：

```groovy
pipeline {
    agent any

    environment {
        NODES = "192.168.200.101 192.168.200.102"
    }

    stages {
        stage('Checkout') {
            steps {
                echo '使用本地项目 /opt/javaweb2.0'
                dir('/opt/javaweb2.0') {
                    sh 'git pull 2>/dev/null || echo "No git remote, using local files"'
                }
            }
        }

        stage('Backend Build') {
            steps {
                dir('/opt/javaweb2.0') {
                    sh 'mvn clean package -DskipTests -q'
                }
            }
        }

        stage('Frontend Build') {
            steps {
                dir('/opt/javaweb2.0/vue-market') {
                    sh 'npm ci --silent 2>/dev/null || echo "Using existing node_modules"'
                    sh 'npm run build'
                }
            }
        }

        stage('Distribute & Deploy') {
            steps {
                script {
                    def jarFile = '/opt/javaweb2.0/market-boot/target/market-boot-2.0.0.jar'
                    def distDir = '/opt/javaweb2.0/vue-market/dist/'

                    def nodes = [
                        [host: '192.168.200.100', workerId: '1'],
                        [host: '192.168.200.101', workerId: '2'],
                        [host: '192.168.200.102', workerId: '3']
                    ]

                    nodes.each { node ->
                        echo "Deploying to ${node.host}..."
                        sh "scp ${jarFile} root@${node.host}:/opt/javaweb2.0/market-boot.jar"
                        sh "ssh root@${node.host} 'mkdir -p /opt/javaweb2.0/vue-market/dist'"
                        sh "scp -r ${distDir}* root@${node.host}:/opt/javaweb2.0/vue-market/dist/"
                        sh """
                            ssh root@${node.host} '
                                sed -i \"s|SNOWFLAKE_WORKER_ID=.*|SNOWFLAKE_WORKER_ID=${node.workerId}|\" /etc/systemd/system/market2.service
                                systemctl daemon-reload
                                systemctl restart market2
                            '
                        """
                    }
                }
            }
        }

        stage('Health Check') {
            steps {
                script {
                    ['192.168.200.100', '192.168.200.101', '192.168.200.102'].each { host ->
                        sh """
                            for i in 1 2 3 4 5; do
                                STATUS=\$(curl -s -o /dev/null -w '%{http_code}' http://${host}/api/health)
                                if [ "\$STATUS" = "200" ]; then
                                    echo "${host} health check PASSED"
                                    break
                                fi
                                echo "${host} attempt \$i: status \$STATUS, retrying..."
                                sleep 3
                            done
                        """
                    }
                }
            }
        }
    }

    post {
        success {
            echo 'Pipeline completed successfully! All 3 nodes deployed and healthy.'
        }
        failure {
            echo 'Pipeline failed. Check logs for details.'
        }
    }
}
```

**注意：** 当前环境未安装 Pipeline 插件，上述 Jenkinsfile 仅供后续扩展参考。实际使用的是 Freestyle Job（2.1 节）。

### 4.1 流水线版本对比

| 特性 | Freestyle (当前) | Declarative Pipeline |
|------|-----------------|---------------------|
| 所需插件 | 无（核心内置） | workflow-job, workflow-cps |
| 脚本语言 | Shell（每阶段单独） | Groovy + Shell |
| 版本控制 | 需手动上传 .groovy | Jenkinsfile 随代码一起管理 |
| 可视化 | 每个阶段独立 | Pipeline Stage View |
| 并行执行 | 不支持 | `parallel` 指令 |
| 当前状态 | ✅ 正在使用 | ❌ 插件未安装 |

---

## 五、故障排查

### 5.1 Jenkins 无法访问

```bash
# 检查服务状态
systemctl status jenkins

# 检查端口
ss -tlnp | grep 9090

# 查看启动日志
journalctl -u jenkins -n 50
```

### 5.2 认证失败 / 密码不正确

```bash
# 重置管理员密码
systemctl stop jenkins
rm -rf /opt/jenkins/home/users/admin_*
rm -f /opt/jenkins/home/config.xml
systemctl start jenkins
# init.groovy.d/security.groovy 会在启动时重建 admin/admin123
```

### 5.3 API 返回 403 Forbidden

CSRF 保护要求提供 Session Cookie + Crumb Token。确保：
1. 同一个 curl 会话（`-c` / `-b` 使用同一个 cookie jar）
2. Crumb 通过 Header 传递：`-H "Jenkins-Crumb: <token>"`
3. 同时提供 Basic Auth：`-u admin:admin123`

### 5.4 Script Console 执行 Groovy 报错

常见错误：
- **`BuildStepComposer` class not found** → 改用 `job.getBuildersList().add(...)` 逐个添加步骤
- **`WorkflowJob` / `CpsFlowDefinition` class not found** → 改用 `FreeStyleProject`（无需 Pipeline 插件）
- **`sed` 替换失败** → 检查 Groovy 字符串中的 `$` 和 `"` 转义（需在 Shell 字符串中正确处理）

### 5.5 构建失败诊断流程

```
Jenkins Web UI → market2-deploy → 构建编号 → Console Output
                    ↓
             查看具体哪一步失败
                    ↓
    ┌───────────────┼───────────────┐
    ↓               ↓               ↓
Stage 0 失败   Stage 1/2 失败   Stage 3/4 失败
GitHub 连通性   Maven/npm 报错   SSH/服务问题
ssh -T git@    查看具体错误    检查端口占用
github.com     mvn validate    systemctl status
```

---

## 六、配置备份与恢复

### 6.1 备份 Jenkins 配置

```bash
# 备份整个 JENKINS_HOME
tar -czf /opt/jenkins-home-backup-$(date +%Y%m%d).tar.gz \
  -C /opt/jenkins/home .

# 仅备份 Job 配置
tar -czf /opt/jenkins-jobs-backup-$(date +%Y%m%d).tar.gz \
  -C /opt/jenkins/home/jobs .
```

### 6.2 恢复

```bash
systemctl stop jenkins
tar -xzf /opt/jenkins-home-backup-YYYYMMDD.tar.gz -C /opt/jenkins/home/
systemctl start jenkins
```
