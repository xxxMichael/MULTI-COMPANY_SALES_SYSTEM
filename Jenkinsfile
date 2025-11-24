pipeline {
    agent any

    tools {
        jdk 'jdk17'
        maven 'maven-3.9'   // ⬅ ESTE ES EL CAMBIO CORRECTO
    }

    environment {
        DOCKER_IMAGE = "multi-company-sales-system"
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'jenkins', url: 'https://github.com/xxxMichael/MULTI-COMPANY_SALES_SYSTEM.git'
            }
        }

        stage('Build Maven') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Build Docker Image') {
            steps {
                sh """
                docker build -t ${DOCKER_IMAGE}:latest .
                """
            }
        }

        stage('Deploy with Docker Compose') {
            steps {
                sh """
                docker compose down || true
                docker compose up -d --build
                """
            }
        }
    }

    post {
        success {
            echo 'Pipeline completed successfully!'
        }
        failure {
            echo 'Pipeline failed.'
        }
    }
}
