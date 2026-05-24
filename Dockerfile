# ============================================
# Multi-stage Dockerfile for Railway Deploy
# ============================================

# Etapa 1: Build
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copiar archivos de configuración de Maven
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Descargar dependencias (cacheado si no cambia pom.xml)
RUN mvn dependency:go-offline -B

# Copiar código fuente
COPY src ./src

# Compilar aplicación (sin tests para acelerar build)
RUN mvn clean package -DskipTests

# Etapa 2: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Instalar utilidades necesarias
RUN apk add --no-cache wget su-exec

# Crear usuario no-root para seguridad
RUN addgroup -g 1001 -S appuser && \
    adduser -u 1001 -S appuser -G appuser

# Crear directorio uploads con permisos amplios
# Railway montará el volumen aquí y el script ajustará permisos
RUN mkdir -p /app/uploads && \
    chmod -R 777 /app/uploads

# Copiar JAR desde etapa de build
COPY --from=build /app/target/*.jar app.jar

# Copiar script de entrada
COPY docker-entrypoint.sh /app/docker-entrypoint.sh

# Dar permisos de ejecución al script
RUN chmod +x /app/docker-entrypoint.sh

# NO cambiar a usuario no-root aquí - el script lo hará después de ajustar permisos

# Exponer puerto
EXPOSE 8080

# Healthcheck para Railway/Cloud platforms
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Variables de entorno por defecto (pueden sobrescribirse)
ENV UPLOAD_DIR=/app/uploads

# Comando de inicio
# Usa el script de entrada que maneja permisos de volumen
ENTRYPOINT ["/app/docker-entrypoint.sh"]
