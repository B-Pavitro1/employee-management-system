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
                // Clean and package the application, skipping tests for speed
                bat 'mvn clean package -DskipTests'
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
                    // Use the SSH key we added to Jenkins
                    sshagent(['ec2-docker-key']) {
                        // SSH into EC2, stop and remove the old container, then run the new one
                        sh """
                            ssh -o StrictHostKeyChecking=no ec2-user@98.83.147.216 << EOF
                                docker stop my-app-container || true
                                docker rm my-app-container || true
                                docker pull ${DOCKER_IMAGE}:${DOCKER_TAG}
                                docker run -d -p 8080:8080 --name my-app-container ${DOCKER_IMAGE}:${DOCKER_TAG}
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
