pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build Maven') {
            tools {
    maven 'maven-3.9'
    jdk 'jdk17'
}

stage('Build Maven') {
    steps {
        sh 'mvn clean package -DskipTests'
    }
}
        }

        stage('Build Docker') {
            steps {
                sh 'docker build -t multi-company-sales-system .'
            }
        }

        stage('Deploy (docker-compose)') {
            steps {
                sh 'docker compose down'
                sh 'docker compose up -d --build'
            }
        }
    }

    post {
        success {
            echo 'CI/CD executed successfully'
        }
        failure {
            echo 'Pipeline failed'
        }
    }
}
