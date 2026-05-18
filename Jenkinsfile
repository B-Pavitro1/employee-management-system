pipeline {
    agent any

    tools {
        maven 'Maven-3'    // Use the name you set in 'Global Tool Configuration'
    }

    environment {
        // Define variables for your Docker image and registry
        DOCKER_IMAGE = 'deepovi164/employee-management-system'
        DOCKER_TAG = "1.0.0-${env.BUILD_NUMBER}" // Use the Jenkins build number as a tag
        // The path to your app. We'll use 'deploy' stage for details
    }

    stages {
        stage('Checkout') {
            steps {
                // Pull the latest code from GitHub
                git branch: 'main', credentialsId: 'github-pat', url: 'https://github.com/B-Pavitro1/employee-management-system'
                echo 'Code successfully checked out.'
            }
        }

        stage('Build with Maven') {
            steps {
                // Clean and package the application, skipping tests for speed.
                sh 'mvn clean package -DskipTests'
                echo 'Maven build successful. JAR file created in target/'
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    // Build the Docker image using the Dockerfile in the current directory
                    sh "docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} ."
                    echo 'Docker image built successfully.'
                }
            }
        }

        stage('Push Docker Image to Registry') {
            steps {
                script {
                    // Log in to Docker Hub (requires credentials in Jenkins)
                    // You can add your Docker Hub credentials in Jenkins with ID 'docker-hub-cred'
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
                    // Capture environment variables
                    def imageName = "${DOCKER_IMAGE}:${DOCKER_TAG}"
                    
                    sshagent(['ec2-deploy-key']) {
                        // Write deploy script to a temporary file
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
                            
                            # Copy the script to EC2
                            scp -o StrictHostKeyChecking=no /tmp/deploy.sh ec2-user@13.222.175.126:/tmp/deploy.sh
                            
                            # Execute the script on EC2
                            ssh -o StrictHostKeyChecking=no ec2-user@13.222.175.126 "chmod +x /tmp/deploy.sh && /tmp/deploy.sh"
                            
                            # Clean up local temp file
                            rm -f /tmp/deploy.sh
                        """
                    }
                }
            }
        }
    }
    
    post {
        always {
            // Clean up workspace to save disk space
            cleanWs()
            echo 'Pipeline finished. Workspace cleaned.'
        }
    }
}
