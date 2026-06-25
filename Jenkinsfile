pipeline {
    agent any

    tools {
        jdk 'JDK-21'
        maven 'Maven-3'
    }

    environment {
        DOCKER_IMAGE = 'deepovi164/employee-management-system'
        DOCKER_TAG = "1.0.0-${env.BUILD_NUMBER}"
        MAVEN_REPO = "${env.WORKSPACE}/.m2/repository"
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', credentialsId: 'github-pat', url: 'https://github.com/B-Pavitro1/employee-management-system'
                echo 'Code successfully checked out.'
            }
        }

        stage('Check Java Version') {
            steps {
                sh 'java -version'
                sh 'mvn -version'
            }
        }

        stage('Build with Maven') {
            steps {
                script {
                    sh """
                        mkdir -p ${MAVEN_REPO}
                        mvn clean package -DskipTests \
                            -Dmaven.repo.local=${MAVEN_REPO} \
                            -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn \
                            -B
                    """
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    sh "docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} ."
                    echo 'Docker image built successfully.'
                }
            }
        }

        stage('Push Docker Image to Registry') {
            steps {
                script {
                    docker.withRegistry('', 'docker-hub-cred') {
                        sh "docker push ${DOCKER_IMAGE}:${DOCKER_TAG}"
                        echo 'Image pushed to Docker Hub successfully.'
                    }
                }
            }
        }

        stage('Debug SSH Connection') {
            steps {
                script {
                    sshagent(['ec2-deploy-key']) {
                        sh """
                            echo "=== Debugging SSH Connection ==="
                            # Check SSH connectivity and permissions
                            ssh -v -o StrictHostKeyChecking=no ubuntu@ec2-54-89-162-75.compute-1.amazonaws.com "echo 'Connected'; ls -la ~/; whoami; hostname"
                            
                            # Check if docker is installed and running
                            ssh -o StrictHostKeyChecking=no ubuntu@ec2-54-89-162-75.compute-1.amazonaws.com "which docker || echo 'Docker not found'"
                            ssh -o StrictHostKeyChecking=no ubuntu@ec2-54-89-162-75.compute-1.amazonaws.com "sudo docker --version || echo 'Docker not installed or permission denied'"
                            
                            # Check if user is in docker group
                            ssh -o StrictHostKeyChecking=no ubuntu@ec2-54-89-162-75.compute-1.amazonaws.com "groups"
                        """
                    }
                }
            }
        }

        stage('Deploy to EC2 via SSH') {
            steps {
                script {
                    def imageName = "${DOCKER_IMAGE}:${DOCKER_TAG}"
                    
                    // Use withEnv to ensure environment variables are passed correctly
                    withEnv(["DOCKER_IMAGE=${imageName}"]) {
                        sshagent(['ec2-deploy-key']) {
                            sh '''
                                set -x  # Enable debug mode
                                
                                # Test SSH connection with timeout
                                echo "Testing SSH connection..."
                                if ! ssh -o StrictHostKeyChecking=no -o ConnectTimeout=30 -o ServerAliveInterval=10 ubuntu@ec2-54-89-162-75.compute-1.amazonaws.com "echo 'SSH connected successfully'"; then
                                    echo "❌ SSH connection failed!"
                                    exit 1
                                fi
                                
                                # Verify Docker is installed on EC2
                                echo "Checking Docker installation on EC2..."
                                ssh -o StrictHostKeyChecking=no ubuntu@ec2-54-89-162-75.compute-1.amazonaws.com "docker --version || echo 'Docker not installed'"
                                
                                # Create deployment script with proper escaping
                                cat > /tmp/deploy.sh << 'EOF'
                                #!/bin/bash
                                set -ex  # Enable debug and exit on error
                                
                                echo "=== Deploying Employee Management System ==="
                                echo "Image: ${DOCKER_IMAGE}"
                                echo "Current user: $(whoami)"
                                
                                # Login to Docker Hub if needed (optional)
                                # echo "Logging into Docker Hub..."
                                # docker login -u your-username -p your-token
                                
                                # Stop old container
                                echo "Stopping old container..."
                                docker stop employee-management-app 2>/dev/null || true
                                docker rm employee-management-app 2>/dev/null || true
                                
                                # Clean up
                                echo "Cleaning up unused Docker resources..."
                                docker system prune -f
                                
                                # Pull latest image with retry
                                echo "Pulling latest image..."
                                for i in {1..3}; do
                                    docker pull ${DOCKER_IMAGE} && break || sleep 10
                                done
                                
                                # Check if image was pulled successfully
                                if ! docker images --format "{{.Repository}}:{{.Tag}}" | grep -q "${DOCKER_IMAGE}"; then
                                    echo "❌ Failed to pull image ${DOCKER_IMAGE}"
                                    exit 1
                                fi
                                
                                # Start new container
                                echo "Starting new container..."
                                docker run -d \
                                    --name employee-management-app \
                                    -p 8080:8080 \
                                    --restart unless-stopped \
                                    ${DOCKER_IMAGE}
                                
                                # Wait for container to start
                                sleep 5
                                
                                # Verify deployment
                                if docker ps | grep -q employee-management-app; then
                                    echo "✅ Container deployed successfully!"
                                    docker ps | grep employee-management-app
                                    
                                    # Test the application (optional)
                                    echo "Testing application health..."
                                    for i in {1..5}; do
                                        if curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health | grep -q "200"; then
                                            echo "✅ Application is healthy"
                                            break
                                        fi
                                        sleep 5
                                    done
                                else
                                    echo "❌ Container deployment failed!"
                                    docker logs employee-management-app --tail 50
                                    exit 1
                                fi
                                EOF
                                
                                # Copy deployment script to EC2
                                echo "Copying deployment script to EC2..."
                                scp -o StrictHostKeyChecking=no /tmp/deploy.sh ubuntu@ec2-54-89-162-75.compute-1.amazonaws.com:/tmp/deploy.sh
                                
                                # Make script executable and run it
                                echo "Running deployment script on EC2..."
                                ssh -o StrictHostKeyChecking=no ubuntu@ec2-54-89-162-75.compute-1.amazonaws.com "chmod +x /tmp/deploy.sh && bash -x /tmp/deploy.sh"
                                
                                # Cleanup local script
                                rm -f /tmp/deploy.sh
                                
                                echo "✅ Deployment completed successfully!"
                            '''
                        }
                    }
                }
            }
        }
    }
    
    post {
        always {
            cleanWs()
            echo 'Pipeline finished. Workspace cleaned.'
        }
    }
}