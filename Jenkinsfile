pipeline {
    agent any

    tools {
        jdk 'jdk-17'         
        maven 'maven-3.9.6'  
    }

    stages {

        stage('Build Backend') {
            steps {
                sh 'mvn clean package'
            }
        }

        stage('Build Frontend') {
            steps {
                sh '''
                cd frontend
                npm install
                npm run build
                '''
            }
        }
    }
}