// Lab 8 CD Pipeline - 6 stages, independent from Lab 7 CI job.
// Only edit DOCKER_USER (Docker Hub username) before running.
pipeline {
    agent any

    environment {
        DOCKER_USER    = 'khanhduy22'
        DOCKER_IMAGE   = "${DOCKER_USER}/devops-lab8-app"
        DOCKER_TAG     = "${env.BUILD_NUMBER}"
        CONTAINER_NAME = 'lab8-cd-production'
        APP_PORT       = '3002'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    if (env.DOCKER_USER == 'CHANGE_TO_YOUR_DOCKERHUB_USERNAME') {
                        error('Set DOCKER_USER to your Docker Hub username in Jenkinsfile.')
                    }
                }
                sh 'docker --version'
            }
        }

        stage('Docker Build') {
            steps {
                sh '''
                    set -eu
                    docker build \
                        -t "$DOCKER_IMAGE:$DOCKER_TAG" \
                        -t "$DOCKER_IMAGE:latest" \
                        .
                '''
            }
        }

        stage('Docker Scan') {
            steps {
                sh '''
                    if docker scout version >/dev/null 2>&1; then
                        docker scout quickview "$DOCKER_IMAGE:$DOCKER_TAG" || \
                            echo 'Docker Scout scan could not finish (optional).'
                    else
                        echo 'Docker Scout is not installed in Jenkins; optional scan skipped.'
                    fi
                '''
            }
        }

        stage('Push to Registry') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'docker-hub-credentials_1',
                        usernameVariable: 'HUB_USER',
                        passwordVariable: 'HUB_TOKEN'
                    )
                ]) {
                    sh '''
                        set -eu
                        if [ "$HUB_USER" != "$DOCKER_USER" ]; then
                            echo 'Docker Hub username in Jenkinsfile differs from Jenkins Credential.'
                            exit 1
                        fi
                        export DOCKER_CONFIG="$(mktemp -d)"
                        trap 'rm -rf "$DOCKER_CONFIG"' EXIT
                        printf '%s' "$HUB_TOKEN" | docker login \
                            --username "$HUB_USER" --password-stdin
                        docker push "$DOCKER_IMAGE:$DOCKER_TAG"
                        docker push "$DOCKER_IMAGE:latest"
                    '''
                }
            }
        }

        stage('Deploy Container') {
            steps {
                sh '''
                    set -eu
                    docker rm -f "$CONTAINER_NAME" 2>/dev/null || true
                    docker run -d \
                        --name "$CONTAINER_NAME" \
                        -p "$APP_PORT:3000" \
                        --restart unless-stopped \
                        "$DOCKER_IMAGE:$DOCKER_TAG"
                '''
            }
        }

        stage('Verify Deploy') {
            steps {
                sh '''
                    set -eu
                    for n in 1 2 3 4 5 6 7 8 9 10; do
                        if curl -fsS "http://host.docker.internal:$APP_PORT/health"; then
                            echo ''
                            curl -fsS "http://host.docker.internal:$APP_PORT/"
                            echo ''
                            exit 0
                        fi
                        echo 'Waiting for the deployed app...'
                        sleep 2
                    done
                    echo 'Health check failed.'
                    docker logs "$CONTAINER_NAME" || true
                    exit 1
                '''
            }
        }
    }

    post {
        success {
            echo "CD PIPELINE SUCCESS - Deployed ${DOCKER_IMAGE}:${DOCKER_TAG}"
        }
        failure {
            echo 'CD PIPELINE FAILED - Check Console Output.'
        }
        always {
            cleanWs()
        }
    }
}
