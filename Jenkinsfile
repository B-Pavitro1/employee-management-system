pipeline {
    agent any

    tools {
        maven 'Maven-3'
    }

    environment {
        // Set Java 21 as the JDK for this build
        JAVA_HOME = '/usr/lib/jvm/java-21-openjdk-amd64'
        PATH = "${env.JAVA_HOME}/bin:${env.PATH}"
        MAVEN_OPTS = '-Djava.home=/usr/lib/jvm/java-21-openjdk-amd64'
        
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

        stage('Check Environment') {
            steps {
                script {
                    sh """
                        echo "=== Environment Check ==="
                        echo "JAVA_HOME: ${JAVA_HOME}"
                        echo "PATH: ${PATH}"
                        echo "MAVEN_OPTS: ${MAVEN_OPTS}"
                        echo "=== Java Version ==="
                        java -version
                        echo "=== Maven Version ==="
                        mvn -version
                        echo "=== Maven's Java Home ==="
                        mvn help:evaluate -Dexpression=java.home -q -DforceStdout
                        echo "========================"
                    """
                }
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
                            -Djava.home=${JAVA_HOME} \
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
                            cat > /tmp/deploy.sh << 'EOF'
                            #!/bin/bash
                            set -e
                            echo "Pulling Docker image: ${imageName}"
                            sudo docker stop employee-management-app || true
                            sudo docker rm employee-management-app || true
                            sudo docker system prune -f
                            sudo docker pull ${imageName}
                            sudo docker run -d --name employee-management-app -p 8080:8080 ${imageName}

                            if sudo docker ps | grep -q employee-management-app; then
                                echo "Container deployed successfully!"
                                sudo docker ps | grep employee-management-app
                            else
                                echo "Container deployment failed!"
                                exit 1
                            fi
                            EOF
                            
                            scp -o StrictHostKeyChecking=no /tmp/deploy.sh ec2-user@54-89-162-75:/tmp/deploy.sh
                            ssh -o StrictHostKeyChecking=no ec2-user@54-89-162-75 "chmod +x /tmp/deploy.sh && /tmp/deploy.sh"
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