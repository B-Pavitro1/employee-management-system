pipeline {
    agent any

    tools {
        maven 'Maven-3'    // Use the name you set in 'Global Tool Configuration'
        jdk 'JDK-17'       // Use the name you set in 'Global Tool Configuration'
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
                git branch: 'main', credentialsId: 'github-deepovi', url: 'https://github.com/B-Pavitro1/employee-management-system'
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
                    // You can add your Docker Hub credentials in Jenkins with ID 'docker-hub'
                    docker.withRegistry('', 'docker-hub') {
                        sh "docker push ${DOCKER_IMAGE}:${DOCKER_TAG}"
                        echo 'Image pushed to Docker Hub successfully.'
                    }
                }
            }
        }

        stage('Deploy to EC2 via SSH') {
            steps {
                script {
                    sshagent(['ec2-docker-key']) {
                        // Test SSH connection first
                        sh """
                            ssh -o StrictHostKeyChecking=no -o ConnectTimeout=10 ec2-user@98.83.147.216 "echo 'SSH connection successful'"
                        """
                        
                        // Deploy with detailed error output
                        sh """
                            ssh -o StrictHostKeyChecking=no ec2-user@98.83.147.216 << 'EOF'
                                set -e  # Exit on any error
                                set -x  # Print commands being executed
                                
                                echo "=== Starting deployment ==="
                                
                                # Check if docker is installed
                                if ! command -v docker &> /dev/null; then
                                    echo "Docker is not installed. Please install Docker first."
                                    exit 1
                                fi
                                
                                # Login to Docker Hub (if needed)
                                # echo "${DOCKER_PASSWORD}" | docker login -u "${DOCKER_USERNAME}" --password-stdin
                                
                                # Stop and remove old container
                                echo "Stopping old container if exists..."
                                docker stop my-app-container || true
                                docker rm my-app-container || true
                                
                                # Pull latest image
                                echo "Pulling image: ${DOCKER_IMAGE}:${DOCKER_TAG}"
                                docker pull ${DOCKER_IMAGE}:${DOCKER_TAG}
                                
                                # Run new container
                                echo "Starting new container..."
                                docker run -d -p 8080:8080 --name my-app-container ${DOCKER_IMAGE}:${DOCKER_TAG}
                                
                                # Verify container is running
                                sleep 2
                                if docker ps | grep -q my-app-container; then
                                    echo "Container started successfully!"
                                    docker ps | grep my-app-container
                                else
                                    echo "Container failed to start. Showing logs:"
                                    docker logs my-app-container || true
                                    exit 1
                                fi
                                
                                echo "=== Deployment completed ==="
EOF
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
