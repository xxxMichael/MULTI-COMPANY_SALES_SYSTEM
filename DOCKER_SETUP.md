# 🐳 Guía de Configuración Docker

## ✅ Tu archivo `.env` ya está correctamente configurado

**No necesitas cambiar ningún valor en `docker-compose.yml`** porque ahora lee automáticamente las variables desde tu archivo `.env` existente.

---

## 📋 Variables Configuradas

### ✅ Aplicación
```bash
APP_NAME=Multi-Company Sales System
APP_VERIFICATION_CODE_LENGTH=6
APP_VERIFICATION_CODE_TTL_MINUTES=15
APP_VERIFICATION_MAX_ATTEMPTS=5
APP_ADMIN_KEY=SuperClaveUltraSegura123
```

### ✅ Base de Datos
```bash
# Para desarrollo local (sin Docker)
DB_URL=jdbc:postgresql://localhost:5432/Sales
DB_USERNAME=postgres
DB_PASSWORD=root

# Para Docker Compose
POSTGRES_DB=Sales
POSTGRES_USER=postgres
POSTGRES_PASSWORD=root
```

**Nota:** Cuando ejecutas con Docker Compose:
- La aplicación usa `jdbc:postgresql://db:5432/Sales` (automático)
- `db` es el nombre del contenedor PostgreSQL en la red interna

### ✅ Email (Gmail)
```bash
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=qpan609@gmail.com
SMTP_PASS=jpqhssapqiusqenz
```

### ✅ JWT
```bash
JWT_SECRET=qP6B82/hRz2KeC0I5VgN0SglSk0wQezpA2S+KnjdQfE=
JWT_EXP_MINUTES=60
```

### ✅ Almacenamiento de Imágenes
```bash
UPLOAD_DIR=uploads  # Para desarrollo local
# En Docker se mapea automáticamente a /app/uploads
```

---

## 🚀 Cómo Usar Docker Compose

### 1️⃣ Primera vez (Build completo)
```bash
docker-compose up --build
```

### 2️⃣ Arrancar servicios (build ya hecho)
```bash
docker-compose up
```

### 3️⃣ Arrancar en segundo plano
```bash
docker-compose up -d
```

### 4️⃣ Ver logs
```bash
# Todos los servicios
docker-compose logs -f

# Solo la aplicación
docker-compose logs -f app

# Solo la base de datos
docker-compose logs -f db
```

### 5️⃣ Detener servicios
```bash
docker-compose down
```

### 6️⃣ Detener y eliminar volúmenes (⚠️ Borra datos)
```bash
docker-compose down -v
```

---

## 🔄 Diferencias entre Local y Docker

| Aspecto | Local (sin Docker) | Docker Compose |
|---------|-------------------|----------------|
| **DB Host** | `localhost:5432` | `db:5432` (interno) |
| **DB Name** | `Sales` | `Sales` |
| **DB User** | `postgres` | `postgres` |
| **DB Pass** | `root` | `root` |
| **Upload Dir** | `./uploads` | `/app/uploads` (montado) |
| **Puerto App** | `8080` | `8080` (expuesto) |
| **Puerto DB** | `5432` | `5432` (expuesto) |

---

## 📁 Estructura de Volúmenes

```
sales-system/
├── uploads/                    # Imágenes (local)
│   └── (archivos subidos)
├── .env                        # Variables de entorno
├── docker-compose.yml          # Configuración Docker
└── Dockerfile                  # Imagen de la aplicación
```

**En Docker:**
- `./uploads` → `/app/uploads` (volumen montado)
- `postgres-data` → Volumen Docker para PostgreSQL

---

## 🔍 Verificar que todo funciona

### 1️⃣ Verificar contenedores corriendo
```bash
docker ps
```

Deberías ver:
- `sales-system-app` (puerto 8080)
- `sales-system-db` (puerto 5432)

### 2️⃣ Probar health check
```bash
curl http://localhost:8080/actuator/health
```

Respuesta esperada:
```json
{
  "status": "UP"
}
```

### 3️⃣ Probar endpoint de productos
```bash
curl http://localhost:8080/api/products
```

### 4️⃣ Conectar a PostgreSQL desde tu máquina
```bash
# Host: localhost
# Port: 5432
# Database: Sales
# User: postgres
# Password: root
```

Puedes usar DBeaver, pgAdmin, o psql:
```bash
psql -h localhost -p 5432 -U postgres -d Sales
```

---

## ⚠️ Solución de Problemas

### Problema: Puerto 8080 ya está en uso
**Solución:** Cambiar el puerto en `docker-compose.yml`:
```yaml
ports:
  - "8081:8080"  # Mapea puerto local 8081 → contenedor 8080
```

### Problema: Puerto 5432 ya está en uso
**Tienes PostgreSQL corriendo localmente**

**Opciones:**
1. Detener PostgreSQL local: `net stop postgresql-x64-15`
2. Cambiar puerto en `docker-compose.yml`:
   ```yaml
   ports:
     - "5433:5432"  # Mapea puerto local 5433 → contenedor 5432
   ```

### Problema: Error de conexión a base de datos
**Solución:** Verificar que el contenedor `db` esté corriendo:
```bash
docker-compose logs db
```

Si ves errores, reconstruye:
```bash
docker-compose down -v
docker-compose up --build
```

### Problema: Imágenes no se guardan
**Verificación:**
1. Verifica que la carpeta `uploads/` exista
2. Revisa permisos de escritura
3. Verifica logs: `docker-compose logs app | grep "uploads"`

---

## 🔒 Seguridad en Producción

### ⚠️ Antes de desplegar en producción:

1. **Cambiar contraseñas:**
   ```bash
   POSTGRES_PASSWORD=una_clave_muy_segura_aqui
   JWT_SECRET=$(openssl rand -base64 32)
   APP_ADMIN_KEY=otra_clave_ultra_secreta
   ```

2. **Cambiar modo Hibernate:**
   En `application.properties`:
   ```properties
   spring.jpa.hibernate.ddl-auto=validate  # NO usar 'update'
   ```

3. **Deshabilitar logs SQL:**
   ```properties
   spring.jpa.show-sql=false
   ```

4. **No exponer puerto de PostgreSQL:**
   En `docker-compose.yml`, comentar:
   ```yaml
   # ports:
   #   - "5432:5432"  # Solo usar en desarrollo
   ```

---

## 📊 Resumen de Configuración

| Variable | Valor Actual | Descripción |
|----------|-------------|-------------|
| `APP_NAME` | Multi-Company Sales System | Nombre de la aplicación |
| `POSTGRES_DB` | Sales | Nombre de la base de datos |
| `POSTGRES_USER` | postgres | Usuario de PostgreSQL |
| `POSTGRES_PASSWORD` | root | ⚠️ Cambiar en producción |
| `JWT_SECRET` | qP6B82/... | ✅ Ya es seguro |
| `JWT_EXP_MINUTES` | 60 | 1 hora de expiración |
| `SMTP_USER` | qpan609@gmail.com | Email configurado |
| `APP_ADMIN_KEY` | SuperClave... | ⚠️ Cambiar en producción |
| `UPLOAD_DIR` | uploads | Carpeta de imágenes |

---

## ✅ Checklist de Inicio

- [x] Archivo `.env` configurado
- [x] Variables de entorno correctas
- [x] `docker-compose.yml` lee desde `.env`
- [x] Volumen `uploads/` configurado
- [x] PostgreSQL configurado
- [x] SMTP configurado
- [ ] Ejecutar `docker-compose up --build`
- [ ] Verificar health check
- [ ] Probar API

---

## 🎯 Siguiente Paso

```bash
# Ejecuta esto para arrancar todo:
docker-compose up --build
```

Espera unos 30-60 segundos hasta ver:
```
sales-system-app | Started MultiCompanySalesSystemApplication in X seconds
```

Luego accede a: **http://localhost:8080**

---

**Fecha:** 11 de noviembre de 2025  
**Versión Docker:** 3.8  
**Stack:** Spring Boot 3.5.6 + PostgreSQL 15 + Alpine Linux
