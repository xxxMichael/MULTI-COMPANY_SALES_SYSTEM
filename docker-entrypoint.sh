#!/bin/sh
set -e

# Script de entrada para Railway con manejo de volúmenes
echo "🚀 Iniciando aplicación Spring Boot..."

UPLOAD_DIR="${UPLOAD_DIR:-/app/uploads}"

# Crear directorio si no existe
if [ ! -d "$UPLOAD_DIR" ]; then
    echo "📁 Creando directorio de uploads: $UPLOAD_DIR"
    mkdir -p "$UPLOAD_DIR"
fi

# Verificar y ajustar permisos
echo "🔒 Verificando permisos de $UPLOAD_DIR..."
if [ ! -w "$UPLOAD_DIR" ]; then
    echo "⚠️ El directorio no tiene permisos de escritura. Intentando ajustar..."
    
    # Intentar ajustar permisos (puede fallar si no somos root)
    chmod -R 775 "$UPLOAD_DIR" 2>/dev/null || echo "⚠️ No se pudieron ajustar permisos (normal si no eres root)"
fi

# Verificar permisos finales
if [ -w "$UPLOAD_DIR" ]; then
    echo "✅ Directorio de uploads listo con permisos de escritura"
else
    echo "⚠️ ADVERTENCIA: El directorio no tiene permisos de escritura"
    echo "   Esto puede causar errores al subir archivos"
    echo "   Directorio: $UPLOAD_DIR"
fi

# Iniciar aplicación Spring Boot
echo "🌟 Iniciando Spring Boot..."
exec java ${JAVA_OPTS:--Xmx512m -Xms256m} -jar app.jar "$@"
