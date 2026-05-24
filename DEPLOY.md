# Guía de Despliegue en la Nube - Sales System

Esta guía explica cómo desplegar la aplicación en diferentes plataformas cloud con almacenamiento persistente de imágenes.

## Cambios Realizados para Cloud

### ✅ Adaptaciones implementadas:

1. **Path configurable**: `FileStorageServiceImpl` ahora usa `@Value("${file.upload-dir}")` en lugar de path hardcodeado
2. **Dockerfile optimizado**: Multi-stage build con usuario no-root y healthcheck
3. **Docker Compose**: Setup local completo con PostgreSQL y volúmenes
4. **Railway config**: Archivo `railway.toml` para deploy automático
5. **Variables de entorno**: Configuración flexible desde `.env` o UI de plataforma

---

## Opción 1: Railway (Recomendado)

### Características:
- ✅ Deploy automático desde GitHub
- ✅ Volúmenes persistentes nativos
- ✅ PostgreSQL incluido
- ✅ Precio: desde $5/mes

### Pasos:

1. **Crear cuenta en Railway**: https://railway.app
2. **Crear nuevo proyecto** → "Deploy from GitHub"
3. **Conectar tu repositorio**: `MULTI-COMPANY_SALES_SYSTEM`
4. **Agregar PostgreSQL**:
   - En Railway UI: "New" → "Database" → "PostgreSQL"
   - Railway crea automáticamente las variables `DATABASE_URL`
5. **Configurar variables de entorno**:
   ```
   UPLOAD_DIR=/app/uploads
   DB_URL=${DATABASE_URL}  # Railway lo inyecta automáticamente
   SMTP_HOST=smtp.gmail.com
   SMTP_PORT=587
   SMTP_USER=tu-email@gmail.com
   SMTP_PASS=tu-app-password
   JWT_SECRET=tu-secret-key-seguro-largo
   APP_ADMIN_KEY=admin-key-change-me
   ```

6. **Crear volumen persistente**:
   - Railway UI → Tu servicio → "Volumes"
   - Click "New Volume"
   - Mount Path: `/app/uploads`
   - Capacity: 1GB (o según necesites)

7. **Deploy**: Railway despliega automáticamente al hacer push a GitHub

8. **Verificar**: 
   - URL: Railway te da una URL pública (ej: `https://tu-app.railway.app`)
   - Healthcheck: `https://tu-app.railway.app/actuator/health`
   - Test upload: `POST https://tu-app.railway.app/api/photos/upload/{productId}`

---

## Opción 2: DigitalOcean App Platform

### Pasos:

1. **Crear cuenta**: https://cloud.digitalocean.com
2. **Apps** → "Create App" → "GitHub"
3. **Configurar**:
   - Repository: tu repo
   - Branch: `main` o `feature/products`
   - Autodeploy: Enabled
4. **Agregar PostgreSQL**:
   - "Create Resource" → "Database" → "PostgreSQL"
5. **Variables de entorno**: Similar a Railway
6. **Volumen persistente**:
   - En App settings → "Add Volume"
   - Mount path: `/app/uploads`
   - Size: 1GB

---

## Opción 3: Docker Compose (Local/VPS)

### Para desarrollo local o VPS tradicional:

1. **Clonar repositorio**:
   ```bash
   git clone https://github.com/xxxMichael/MULTI-COMPANY_SALES_SYSTEM.git
   cd MULTI-COMPANY_SALES_SYSTEM
   ```

2. **Configurar variables**:
   ```bash
   cp .env.example .env
   # Editar .env con tus valores
   ```

3. **Build y start**:
   ```bash
   docker-compose up -d --build
   ```

4. **Verificar**:
   ```bash
   docker-compose logs -f app
   curl http://localhost:8080/actuator/health
   ```

5. **Las imágenes se guardan en**: `./uploads` (en el host)

---

## Variables de Entorno Requeridas

```bash
# Base de datos
DB_URL=jdbc:postgresql://host:5432/sales_system
DB_USERNAME=postgres
DB_PASSWORD=tu-password

# Email (SMTP)
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=tu-email@gmail.com
SMTP_PASS=tu-app-password

# JWT
JWT_SECRET=secret-key-muy-largo-y-seguro-cambiar-en-produccion
JWT_EXP_MINUTES=60

# App
APP_NAME=Sales System
APP_VERIFICATION_CODE_LENGTH=6
APP_VERIFICATION_CODE_TTL_MINUTES=10
APP_VERIFICATION_MAX_ATTEMPTS=3
APP_ADMIN_KEY=admin-key-change-me

# Upload (importante para cloud)
UPLOAD_DIR=/app/uploads  # Railway/DigitalOcean
# o
UPLOAD_DIR=/var/www/productos  # VPS tradicional
```

---

## Estructura de Volumen Persistente

```
/app/uploads/
├── producto_1_abc123.jpg
├── producto_1_def456.png
├── producto_2_xyz789.jpg
└── ...
```

- Las imágenes se nombran: `producto_{productId}_{uuid}.{ext}`
- El volumen debe montarse en `/app/uploads` (o el path configurado en `UPLOAD_DIR`)
- Permisos: el usuario `appuser` (UID 1001) debe tener permisos de escritura

---

## Comandos Útiles

### Build local:
```bash
docker build -t sales-system:latest .
```

### Run local con volumen:
```bash
docker run -d \
  -p 8080:8080 \
  -v $(pwd)/uploads:/app/uploads \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/sales_system \
  -e UPLOAD_DIR=/app/uploads \
  --name sales-system \
  sales-system:latest
```

### Ver logs:
```bash
docker logs -f sales-system
# o con docker-compose
docker-compose logs -f app
```

### Test de subida de imagen:
```bash
curl -X POST http://localhost:8080/api/photos/upload/1 \
  -F "file=@test-image.jpg" \
  -H "Authorization: Bearer tu-jwt-token"
```

---

## Troubleshooting

### Error: "Sin permisos de escritura"
- Verificar que el volumen esté montado correctamente
- En Railway/DigitalOcean: revisar que el volumen esté configurado
- En Docker local: `chmod 755 ./uploads`

### Error: "Directorio no encontrado"
- Verificar variable `UPLOAD_DIR`
- El Dockerfile crea `/app/uploads` automáticamente

### Imágenes no persisten entre deploys
- ⚠️ **IMPORTANTE**: Asegúrate de tener un volumen montado
- Sin volumen, las imágenes se pierden al recrear el contenedor

### Base de datos no conecta
- Verificar `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- En Railway/DigitalOcean: usan conexión interna automática

---

## Backups

### Backup de imágenes:
```bash
# En el host con volumen montado
tar -czf uploads-backup-$(date +%Y%m%d).tar.gz uploads/

# Restaurar
tar -xzf uploads-backup-YYYYMMDD.tar.gz
```

### Backup de PostgreSQL:
```bash
docker exec sales-system-db pg_dump -U postgres sales_system > backup.sql

# Restaurar
docker exec -i sales-system-db psql -U postgres sales_system < backup.sql
```

---

## Monitoreo

### Railway:
- Dashboard muestra CPU, RAM, requests
- Logs en tiempo real en UI

### DigitalOcean:
- App Insights dashboard
- Alertas configurables

### Docker Compose:
- `docker stats` para recursos
- `docker-compose logs` para logs

---

## Escalado

### Railway:
- Ajustar recursos en UI (CPU/RAM)
- Volumen crece dinámicamente

### DigitalOcean:
- Cambiar plan desde UI
- Aumentar tamaño de volumen

### Múltiples réplicas:
⚠️ **No recomendado con almacenamiento local**
- Para múltiples instancias, considera migrar a S3/Blob
- O usar almacenamiento compartido (NFS/EFS)

---

## Próximos Pasos Recomendados

1. ✅ **Configurar HTTPS**: Railway/DigitalOcean lo hacen automático
2. ✅ **Configurar dominio custom**: En settings de la plataforma
3. ⚠️ **Backups automáticos**: Script cron para backup de volumen
4. 📊 **Monitoreo**: Configurar alertas de espacio en disco
5. 🔒 **Seguridad**: Rotar JWT_SECRET y APP_ADMIN_KEY regularmente

---

## Soporte

- Railway docs: https://docs.railway.app
- DigitalOcean docs: https://docs.digitalocean.com/products/app-platform/
- Docker docs: https://docs.docker.com

Para dudas sobre la implementación, revisar el código en:
- `FileStorageServiceImpl.java`
- `PhotoServiceImpl.java`
- `PhotoController.java`
