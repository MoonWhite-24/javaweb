pipeline {
    agent any
    
    environment {
        NODES = "192.168.200.101 192.168.200.102"
        LOCAL_NODE = "192.168.200.100"
    }
    
    stages {
        stage("Checkout") {
            steps {
                echo "使用本地项目 /opt/javaweb2.0"
                // 如果配置了 GitHub SSH key，可以改用 git checkout
                // git url: "git@github.com:MoonWhite-24/javaweb.git", branch: "main"
                dir("/opt/javaweb2.0") {
                    sh "git pull 2>/dev/null || echo No git, using local files"
                }
            }
        }
        
        stage("Backend Build") {
            steps {
                dir("/opt/javaweb2.0") {
                    sh "mvn clean package -DskipTests -q"
                }
            }
        }
        
        stage("Frontend Build") {
            steps {
                dir("/opt/javaweb2.0/vue-market") {
                    sh "npm ci --silent 2>/dev/null || echo Using existing node_modules"
                    sh "npm run build"
                }
            }
        }
        
        stage("Distribute & Deploy") {
            steps {
                script {
                    def jarFile = "/opt/javaweb2.0/market-boot/target/market-boot-2.0.0.jar"
                    def distDir = "/opt/javaweb2.0/vue-market/dist/"
                    
                    // Distribute to all 3 nodes
                    for (host in ["192.168.200.100", "192.168.200.101", "192.168.200.102"]) {
                        echo "Deploying to ${host}..."
                        
                        // Set worker ID
                        def workerId = "1"
                        if (host == "192.168.200.101") workerId = "2"
                        if (host == "192.168.200.102") workerId = "3"
                        
                        // Copy JAR
                        sh "scp ${jarFile} root@${host}:/opt/javaweb2.0/market-boot.jar"
                        
                        // Copy frontend dist
                        sh "ssh root@${host} mkdir -p /opt/javaweb2.0/vue-market/dist"
                        sh "scp -r ${distDir}* root@${host}:/opt/javaweb2.0/vue-market/dist/"
                        
                        // Update Snowflake worker ID and restart
                        sh """
                            ssh root@${host} sed -i "s/SNOWFLAKE_WORKER_ID=.*/SNOWFLAKE_WORKER_ID=/" E:/Git/etc/systemd/system/market2.service
