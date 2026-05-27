import jenkins.model.*
import hudson.model.*
import hudson.tasks.*

def jenkinsInstance = Jenkins.getInstanceOrNull()
if (jenkinsInstance == null) {
    println 'Jenkins instance is null!'
    return
}

// Create the freestyle job
def jobName = 'market2-deploy'
def existingJob = jenkinsInstance.getItem(jobName)
if (existingJob != null) {
    existingJob.delete()
    println 'Deleted existing job'
}

def job = jenkinsInstance.createProject(FreeStyleProject.class, jobName)
job.description = 'JavaWeb Market 2.0 CI/CD - Build and Deploy to 3 VMs'

// Step 0: Git checkout (pull latest from GitHub)
def gitCheckout = new hudson.tasks.Shell('''
echo "=== Stage 0: Git Checkout ==="
cd /opt/javaweb2.0
git fetch origin main
git reset --hard origin/main
echo "Git checkout: $(git log --oneline -1)"
''')

// Step 1: Build backend (Maven)
def backendBuild = new hudson.tasks.Shell('''
echo "=== Stage 1: Backend Build ==="
cd /opt/javaweb2.0
mvn clean package -DskipTests -q
echo "Backend build completed"
''')

// Step 2: Build frontend (npm)
def frontendBuild = new hudson.tasks.Shell('''
echo "=== Stage 2: Frontend Build ==="
cd /opt/javaweb2.0/vue-market
npm ci --silent 2>/dev/null || echo "Using existing node_modules"
npm run build
echo "Frontend build completed"
''')

// Step 3: Deploy to all 3 nodes (with post-deploy sleep to ensure startup)
def deployAll = new hudson.tasks.Shell('''
echo "=== Stage 3: Distribute and Deploy ==="
JAR=/opt/javaweb2.0/market-boot/target/market-boot-2.0.0.jar
DIST=/opt/javaweb2.0/vue-market/dist/

# Deploy to Master (worker 1)
echo "--- Deploying to Master (192.168.200.100) ---"
cp ${JAR} /opt/javaweb2.0/market-boot.jar
sed -i "s|SNOWFLAKE_WORKER_ID=.*|SNOWFLAKE_WORKER_ID=1|" /etc/systemd/system/market2.service
systemctl daemon-reload
systemctl restart market2
sleep 10
echo "Master deployed"

# Deploy to Node1 (worker 2)
echo "--- Deploying to Node1 (192.168.200.101) ---"
scp ${JAR} root@192.168.200.101:/opt/javaweb2.0/market-boot.jar
scp -r ${DIST}* root@192.168.200.101:/opt/javaweb2.0/vue-market/dist/
ssh root@192.168.200.101 "sed -i 's|SNOWFLAKE_WORKER_ID=.*|SNOWFLAKE_WORKER_ID=2|' /etc/systemd/system/market2.service && systemctl daemon-reload && systemctl restart market2"
sleep 10
echo "Node1 deployed"

# Deploy to Node2 (worker 3)
echo "--- Deploying to Node2 (192.168.200.102) ---"
scp ${JAR} root@192.168.200.102:/opt/javaweb2.0/market-boot.jar
scp -r ${DIST}* root@192.168.200.102:/opt/javaweb2.0/vue-market/dist/
ssh root@192.168.200.102 "sed -i 's|SNOWFLAKE_WORKER_ID=.*|SNOWFLAKE_WORKER_ID=3|' /etc/systemd/system/market2.service && systemctl daemon-reload && systemctl restart market2"
sleep 10
echo "Node2 deployed"
''')

// Step 4: Health check (via SSH localhost on each node to avoid network issues)
def healthCheck = new hudson.tasks.Shell('''
echo "=== Stage 4: Health Check ==="
check_node() {
    HOST=$1
    echo "Checking ${HOST}..."
    OK=0
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

// Add all build steps
job.getBuildersList().add(gitCheckout)
job.getBuildersList().add(backendBuild)
job.getBuildersList().add(frontendBuild)
job.getBuildersList().add(deployAll)
job.getBuildersList().add(healthCheck)

job.save()

println "SUCCESS: Freestyle job 'market2-deploy' created"
println "Job URL: ${jenkinsInstance.rootUrl}job/market2-deploy/"
