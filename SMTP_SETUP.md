# Configuración SMTP para Verificación de Correo

## Resumen de Cambios

Se ha actualizado el sistema de verificación de correo para usar SMTP configurado desde variables de entorno.

### Archivos Modificados

1. **MailService.java** - Mejorado con logging y manejo de errores
2. **UsuarioServiceImpl.java** - Actualizado para usar el nuevo método de envío
3. **application.properties** - Ya configurado para usar variables de entorno

## Configuración Requerida

### 1. Crear archivo .env

Crea un archivo `.env` en la raíz del proyecto con las siguientes variables:

```env
# ========================
# Configuración de la aplicación
# ========================
APP_NAME=Multi-Company Sales System

# ========================
# Base de datos
# ========================
DB_URL=jdbc:postgresql://localhost:5432/tu_base_datos
DB_USERNAME=tu_usuario_db
DB_PASSWORD=tu_password_db

# ========================
# Configuración SMTP para envío de correos
# ========================
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=tu_email@gmail.com
SMTP_PASS=tu_password_app_gmail
SMTP_AUTH=true
SMTP_STARTTLS=true

# ========================
# Parámetros de verificación por código
# ========================
APP_VERIFICATION_CODE_LENGTH=6
APP_VERIFICATION_CODE_TTL_MINUTES=15
APP_VERIFICATION_MAX_ATTEMPTS=3

# ========================
# Clave de administrador (para crear moderadores)
# ========================
APP_ADMIN_KEY=tu_clave_admin_segura

# ========================
# Configuración JWT
# ========================
JWT_SECRET=tu_jwt_secret_muy_seguro_y_largo
JWT_EXP_MINUTES=60
```

### 2. Configuración para Gmail

Si usas Gmail, necesitarás:

1. **Activar verificación en 2 pasos** en tu cuenta de Google
2. **Generar una contraseña de aplicación**:
   - Ve a tu cuenta de Google → Seguridad
   - Selecciona "Contraseñas de aplicaciones"
   - Genera una nueva contraseña para "Correo"
   - Usa esta contraseña en `SMTP_PASS`

### 3. Configuración para Otros Proveedores

#### Outlook/Hotmail
```env
SMTP_HOST=smtp-mail.outlook.com
SMTP_PORT=587
SMTP_STARTTLS=true
```

#### Yahoo
```env
SMTP_HOST=smtp.mail.yahoo.com
SMTP_PORT=587
SMTP_STARTTLS=true
```

#### Proveedor Personalizado
```env
SMTP_HOST=tu.servidor.smtp.com
SMTP_PORT=587
SMTP_STARTTLS=true
```

## Mejoras Implementadas

### MailService
- ✅ Logging detallado para debugging
- ✅ Manejo de errores mejorado
- ✅ Configuración automática del remitente desde variables de entorno
- ✅ Método específico para correos de verificación

### UsuarioServiceImpl
- ✅ Código simplificado usando el nuevo método del MailService
- ✅ Mensajes de correo más profesionales

## Pruebas

Para probar la configuración:

1. Inicia la aplicación
2. Registra un nuevo usuario
3. Verifica que el correo llegue al destinatario
4. Revisa los logs para confirmar el envío exitoso

## Troubleshooting

### Error: "Authentication failed"
- Verifica que las credenciales SMTP sean correctas
- Para Gmail, asegúrate de usar una contraseña de aplicación

### Error: "Connection timeout"
- Verifica que el puerto y host SMTP sean correctos
- Revisa que no haya firewall bloqueando la conexión

### Correos no llegan
- Revisa la carpeta de spam
- Verifica que el remitente esté configurado correctamente
- Revisa los logs de la aplicación para errores específicos

## Seguridad

- ✅ Nunca commits el archivo `.env` al repositorio
- ✅ Usa contraseñas de aplicación para Gmail
- ✅ Configura variables de entorno en producción
- ✅ Revisa los logs regularmente para detectar intentos de acceso no autorizados
