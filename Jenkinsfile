pipeline {
    agent any
    stages{
        stage('Checkout'){
            steps{
                 git branch: 'jenkins', url: 'https://github.com/xxxMichael/MULTI-COMPANY_SALES_SYSTEM.git'
            }
        }
        stage('Compilar'){
            steps{
                sh 'mvn clean compile'
            }
        }
        stage('Prueba y cobertura'){
            steps{
                sh 'mvn test'
                sh 'mvn verify'
            }
        }
        stage('Publicar reporte de cobertura'){
            steps{
                jacoco execPattern: '**/target/jacoco.exec', classPattern: '**/target/classes',sourcePattern:'/src/main/java',exclusionPattern:'**/src/test*'
            }
        }
        stage('Construir'){
            steps{
                sh 'mvn package'
            }
        }  
    }

    post {
        always {
            junit '**/target/surefire-reports/*.xml'
        }
    }
}