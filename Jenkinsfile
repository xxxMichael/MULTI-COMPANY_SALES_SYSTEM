pipeline {
    agent any

    tools {
        jdk 'jdk-17'         
        maven 'maven-3.9.6'  
    }

    environment {
        // Aplicación
        APP_NAME = 'Multi-Company Sales System'
        APP_VERIFICATION_CODE_LENGTH = '6'
        APP_VERIFICATION_CODE_TTL_MINUTES = '15'
        APP_VERIFICATION_MAX_ATTEMPTS = '5'
        
        // Base de datos
        DB_URL = 'jdbc:postgresql://localhost:5432/Sales'
        DB_USERNAME = 'postgres'
        DB_PASSWORD = '12345678'
        
        // SMTP
        SMTP_HOST = 'smtp.gmail.com'
        SMTP_PORT = '587'
        SMTP_USER = 'qpan609@gmail.com'
        SMTP_PASS = 'jpqhssapqiusqenz'
        SMTP_AUTH = 'true'
        SMTP_STARTTLS = 'true'
        
        // Admin y JWT
        APP_ADMIN_KEY = 'SuperClaveUltraSegura123'
        JWT_SECRET = 'qP6B82/hRz2KeC0I5VgN0SglSk0wQezpA2S+KnjdQfE='
        JWT_EXP_MINUTES = '60'
    }

    stages {
        stage('Build Backend') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }
    }
}