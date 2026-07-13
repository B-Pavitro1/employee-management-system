pipeline {
    agent any

    options {
        timestamps()
        ansiColor('xterm')
        disableConcurrentBuilds()
        buildDiscarder(logRotator(
            numToKeepStr: '10',
            artifactNumToKeepStr: '5'
        ))
    }

    tools {
        jdk 'JDK-21'
        maven 'Maven-3'
    }

    environment {
        APP_NAME      = 'employee-management-system'

        GIT_BRANCH    = 'main'

        DOCKER_IMAGE  = 'deepovi164/employee-management-system'
        DOCKER_TAG    = "1.0.${BUILD_NUMBER}"

        MAVEN_REPO    = "${WORKSPACE}/.m2/repository"

        EC2_HOST      = 'ubuntu@ec2-50-16-138-215.compute-1.amazonaws.com'

        CONTAINER_NAME = 'employee-management-system'
    }

    stages {

        stage('Checkout Source Code') {
            options {
                retry(2)
            }
            steps {
                git branch: "${GIT_BRANCH}",
                    credentialsId: 'github-pat',
                    url: 'https://github.com/B-Pavitro1/employee-management-system'

                echo "Source code checked out successfully."
            }
        }

        stage('Verify Build Tools') {
            steps {
                sh '''
                    java -version
                    mvn -version
                    docker --version
                '''
            }
        }

        stage('Build Maven Project') {
            steps {
                sh """
                    mkdir -p ${MAVEN_REPO}

                    mvn clean package \
                    -DskipTests \
                    -Dmaven.repo.local=${MAVEN_REPO} \
                    -B
                """
            }
        }

        stage('Build Docker Image') {
            steps {
                sh """
                    docker build \
                    -t ${DOCKER_IMAGE}:${DOCKER_TAG} \
                    .
                """
                echo "Docker image built successfully."
            }
        }

        stage('Push Docker Image') {
            steps {
                script {
                    docker.withRegistry('', 'docker-hub-cred') {
                        sh """
                            docker push ${DOCKER_IMAGE}:${DOCKER_TAG}
                        """
                    }
                }
                echo "Docker image pushed successfully."
            }
        }

        stage('Deploy to EC2') {
            steps {
                sshagent(credentials: ['ec2-deploy-key']) {
                    sh """
                        ssh -o StrictHostKeyChecking=no ${EC2_HOST} '
                        docker stop ${CONTAINER_NAME} || true
                        docker rm ${CONTAINER_NAME} || true

                        docker pull ${DOCKER_IMAGE}:${DOCKER_TAG}

                        docker run -d \
                        --name ${CONTAINER_NAME} \
                        -p 8080:8080 \
                        --restart unless-stopped \
                        ${DOCKER_IMAGE}:${DOCKER_TAG}
                        '
                    """
                }
                echo "Application deployed successfully."
            }
        }

        stage('Verify Deployment') {
            steps {
                sshagent(credentials: ['ec2-deploy-key']) {
                    sh """
                        ssh -o StrictHostKeyChecking=no ${EC2_HOST} '
                        docker ps
                        '
                    """
                }
            }
        }
    }

    post {

        success {
            echo "=================================="
            echo "Build Successful"
            echo "Image : ${DOCKER_IMAGE}:${DOCKER_TAG}"
            echo "=================================="
        }

        failure {
            echo "=================================="
            echo "Build Failed"
            echo "Check Jenkins Console Output"
            echo "=================================="
        }

        always {
            cleanWs(deleteDirs: true)
        }
    }
}