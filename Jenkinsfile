pipeline {
    agent any

    environment {
        AWS_REGION = 'us-east-1'
        ECR_REGISTRY = '123456789.dkr.ecr.us-east-1.amazonaws.com'
        IMAGE_TAG = "${env.BUILD_NUMBER}-${env.GIT_COMMIT?.take(7) ?: 'latest'}"
        SERVICES = 'order-service inventory-service payment-service shipping-service notification-service api-gateway'
    }

    stages {
        stage('Checkout') {
            steps { checkout scm }
        }

        stage('Build & Test') {
            parallel {
                stage('order-service')       { steps { dir('order-service') { sh 'mvn clean verify -q' } } }
                stage('inventory-service')   { steps { dir('inventory-service') { sh 'mvn clean verify -q' } } }
                stage('payment-service')     { steps { dir('payment-service') { sh 'mvn clean verify -q' } } }
                stage('shipping-service')    { steps { dir('shipping-service') { sh 'mvn clean verify -q' } } }
                stage('notification-service'){ steps { dir('notification-service') { sh 'mvn clean verify -q' } } }
            }
        }

        stage('Docker Build') {
            steps {
                script {
                    def services = ['order-service', 'inventory-service', 'payment-service',
                                    'shipping-service', 'notification-service', 'api-gateway']
                    services.each { svc ->
                        sh "docker build -t ${ECR_REGISTRY}/${svc}:${IMAGE_TAG} ./${svc}"
                    }
                }
            }
        }

        stage('Push to ECR') {
            steps {
                sh "aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REGISTRY}"
                script {
                    ['order-service', 'inventory-service', 'payment-service',
                     'shipping-service', 'notification-service', 'api-gateway'].each { svc ->
                        sh "docker push ${ECR_REGISTRY}/${svc}:${IMAGE_TAG}"
                        sh "docker tag ${ECR_REGISTRY}/${svc}:${IMAGE_TAG} ${ECR_REGISTRY}/${svc}:latest"
                        sh "docker push ${ECR_REGISTRY}/${svc}:latest"
                    }
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                sh """
                    kubectl set image deployment/order-service order-service=${ECR_REGISTRY}/order-service:${IMAGE_TAG} -n ecommerce
                    kubectl set image deployment/inventory-service inventory-service=${ECR_REGISTRY}/inventory-service:${IMAGE_TAG} -n ecommerce
                    kubectl set image deployment/payment-service payment-service=${ECR_REGISTRY}/payment-service:${IMAGE_TAG} -n ecommerce
                    kubectl set image deployment/shipping-service shipping-service=${ECR_REGISTRY}/shipping-service:${IMAGE_TAG} -n ecommerce
                    kubectl set image deployment/notification-service notification-service=${ECR_REGISTRY}/notification-service:${IMAGE_TAG} -n ecommerce
                    kubectl set image deployment/api-gateway api-gateway=${ECR_REGISTRY}/api-gateway:${IMAGE_TAG} -n ecommerce
                    kubectl rollout status deployment/order-service -n ecommerce
                """
            }
        }

        stage('Health Check') {
            steps {
                sh "sleep 20 && curl -f http://\${GATEWAY_URL}/api/orders/health || exit 1"
            }
        }
    }

    post {
        success { echo "Deployment ${IMAGE_TAG} completed successfully" }
        failure { echo "Build ${BUILD_NUMBER} failed" }
        always  { sh "docker system prune -f" }
    }
}
