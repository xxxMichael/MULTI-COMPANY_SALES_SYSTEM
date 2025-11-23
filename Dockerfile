# Etapa de ejecución del backend
FROM eclipse-temurin:17-jdk-jammy

# Directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiar el archivo JAR generado por Maven (ajusta el nombre si difiere)
COPY target/multi-company-sales-system.jar app.jar

# Exponer el puerto de Spring Boot
EXPOSE 9090

# Comando para ejecutar el JAR
CMD ["java", "-jar", "app.jar","--server.port=9090"]
