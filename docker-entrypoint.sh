#!/bin/sh
set -e

# Script de entrada para Railway con manejo de volúmenes
echo "🚀 Iniciando configuración de Railway..."
echo "👤 Usuario actual: $(whoami) (UID: $(id -u))"

UPLOAD_DIR="${UPLOAD_DIR:-/app/uploads}"

# Crear directorio si no existe
if [ ! -d "$UPLOAD_DIR" ]; then
    echo "📁 Creando directorio de uploads: $UPLOAD_DIR"
    mkdir -p "$UPLOAD_DIR"
fi

# Ajustar permisos y ownership (solo funciona si somos root)
echo "🔒 Ajustando permisos de $UPLOAD_DIR..."
if [ "$(id -u)" = "0" ]; then
    # Somos root - podemos ajustar permisos y ownership
    echo "✅ Ejecutando como root - ajustando permisos..."
    chown -R 1001:1001 "$UPLOAD_DIR"
    chmod -R 777 "$UPLOAD_DIR"
    echo "✅ Permisos ajustados: owner=appuser(1001:1001), mode=777"
else
    # No somos root - solo podemos verificar
    echo "⚠️ No ejecutando como root - no se pueden ajustar permisos"
fi

# Verificar permisos finales
if [ -w "$UPLOAD_DIR" ]; then
    echo "✅ Directorio de uploads listo con permisos de escritura"
    ls -ld "$UPLOAD_DIR"
else
    echo "❌ ADVERTENCIA: El directorio no tiene permisos de escritura"
    echo "   Directorio: $UPLOAD_DIR"
    ls -ld "$UPLOAD_DIR" || true
fi

# Si somos root, cambiar a usuario appuser para ejecutar la app
if [ "$(id -u)" = "0" ]; then
    echo "🔄 Cambiando a usuario appuser para ejecutar la aplicación..."
    # Cambiar ownership del JAR también
    chown 1001:1001 /app/app.jar
    # Ejecutar como usuario appuser
    echo "🌟 Iniciando Spring Boot como appuser..."
    exec su-exec 1001:1001 java ${JAVA_OPTS:--Xmx512m -Xms256m} -jar app.jar "$@"
else
    # Ya somos usuario no-root, ejecutar directamente
    echo "🌟 Iniciando Spring Boot como $(whoami)..."
    exec java ${JAVA_OPTS:--Xmx512m -Xms256m} -jar app.jar "$@"
fi
