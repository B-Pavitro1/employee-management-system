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

        stage('Deploy to EC2 via SSH') {
            steps {
                script {
                    def imageName = "${DOCKER_IMAGE}:${DOCKER_TAG}"
                    def EC2_HOST = "ubuntu@ec2-3-25-60-85.compute-1.amazonaws.com"
                    
                    // Use withEnv to ensure environment variables are passed correctly
                    withEnv(["DOCKER_IMAGE=${imageName}"]) {
                        sshagent(['ec2-deploy-key']) {
                            sh '''
                                set -x  # Enable debug mode

                                # Create deployment script with proper escaping
                                cat > /tmp/deploy.sh << 'EOF'
                                #!/bin/bash
                                set -ex  # Enable debug and exit on error
                                
                                # Source environment variables
                                export DOCKER_IMAGE="${DOCKER_IMAGE}"
                                
                                echo "=== Deploying Employee Management System ==="
                                echo "Image: ${DOCKER_IMAGE}"
                                echo "Current user: $(whoami)"
                                
                                # Stop old container
                                echo "Stopping old container..."
                                docker stop employee-management-system 2>/dev/null || true
                                docker rm employee-management-system 2>/dev/null || true
                                
                                # Clean up
                                echo "Cleaning up unused Docker resources..."
                                docker system prune -f
                                
                                # Pull latest image with retry
                                echo "Pulling latest image..."
                                for i in {1..3}; do
                                    docker pull ${DOCKER_IMAGE} && break || {
                                        echo "Pull attempt $i failed, retrying in 10 seconds..."
                                        sleep 10
                                    }
                                done
                                
                                # Check if image was pulled successfully
                                if ! docker images --format "{{.Repository}}:{{.Tag}}" | grep -q "${DOCKER_IMAGE}"; then
                                    echo "❌ Failed to pull image ${DOCKER_IMAGE}"
                                    exit 1
                                fi
                                
                                # Start new container
                                echo "Starting new container..."
                                docker run -d \
                                    --env-file /opt/employee-management-system/.env \
                                    --name employee-management-system \
                                    -p 8081:8081 \
                                    --restart unless-stopped \
                                    ${DOCKER_IMAGE}
                                
                                # Wait for container to start
                                sleep 10
                                
                                # Check container status
                                if docker ps | grep -q employee-management-system; then
                                    echo "✅ Container started successfully!"
                                    docker ps | grep employee-management-system
                                else
                                    echo "❌ Container deployment failed!"
                                    docker logs employee-management-system --tail 50
                                    exit 1
                                fi
                                EOF
                                
                                # Copy deployment script to EC2
                                echo "Copying deployment script to EC2..."
                                scp -o StrictHostKeyChecking=no /tmp/deploy.sh ${EC2_HOST}:/tmp/deploy.sh
                                
                                # Make script executable and run it
                                echo "Running deployment script on EC2..."
                                ssh -o StrictHostKeyChecking=no ${EC2_HOST} "chmod +x /tmp/deploy.sh && bash -x /tmp/deploy.sh"
                                
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