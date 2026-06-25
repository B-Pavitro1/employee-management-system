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

        stage('Deploy to EC2 via SSH') {
            steps {
                script {
                    def imageName = "${DOCKER_IMAGE}:${DOCKER_TAG}"
                    
                    sshagent(['ec2-deploy-key']) {
                        sh """
                            # Test SSH connection first
                            echo "Testing SSH connection..."
                            ssh -o StrictHostKeyChecking=no -v ubuntu@ec2-54-89-162-75 "echo 'SSH connected successfully'"
                            
                            # Create deployment script
                            cat > /tmp/deploy.sh << 'EOF'
                            #!/bin/bash
                            set -e
                            
                            echo "=== Deploying Employee Management System ==="
                            echo "Image: ${imageName}"
                            echo "Current user: \$(whoami)"
                            echo "Docker version: \$(docker --version 2>/dev/null || echo 'Docker not found')"
                            
                            # Stop old container
                            echo "Stopping and removing old container..."
                            docker stop employee-management-app 2>/dev/null || true
                            docker rm employee-management-app 2>/dev/null || true
                            
                            # Clean up
                            echo "Cleaning up unused Docker resources..."
                            docker system prune -f
                            
                            # Pull latest image
                            echo "Pulling latest image..."
                            docker pull ${imageName}
                            
                            # Start new container
                            echo "Starting new container..."
                            docker run -d --name employee-management-app -p 8080:8080 ${imageName}
                            
                            # Verify deployment
                            if docker ps | grep -q employee-management-app; then
                                echo "✅ Container deployed successfully!"
                                docker ps | grep employee-management-app
                            else
                                echo "❌ Container deployment failed!"
                                docker logs employee-management-app --tail 50
                                exit 1
                            fi
                            EOF
                            
                            # Copy and execute
                            scp -o StrictHostKeyChecking=no /tmp/deploy.sh ubuntu@ec2-54-89-162-75:/tmp/deploy.sh
                            ssh -o StrictHostKeyChecking=no ubuntu@ec2-54-89-162-75 "bash -x /tmp/deploy.sh"
                            
                            # Cleanup
                            rm -f /tmp/deploy.sh
                        """
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