package com.multicompany.sales_system.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class DotenvConfig {

    @PostConstruct
    public void loadEnv() {
        try {
            Dotenv dotenv = Dotenv.configure()
                    .directory("./")
                    .filename(".env")
                    .ignoreIfMissing()
                    .load();

            // Cargar variables de entorno en el sistema
            dotenv.entries().forEach(entry -> {
                System.setProperty(entry.getKey(), entry.getValue());
            });

        } catch (Exception e) {
            System.err.println("Error cargando archivo .env: " + e.getMessage());
            System.err.println("Las variables de entorno deben estar configuradas en el sistema");
        }
    }
}