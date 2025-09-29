package com.multicompany.sales_system.service.impl;

import com.multicompany.sales_system.dto.user.RegisterRequest;
import com.multicompany.sales_system.dto.user.RegisterResponse;
import com.multicompany.sales_system.dto.user.VerifyEmailRequest;
import com.multicompany.sales_system.model.EmailVerification;
import com.multicompany.sales_system.model.Usuario;
import com.multicompany.sales_system.repository.EmailVerificationRepository;
import com.multicompany.sales_system.repository.UsuarioRepository;
import com.multicompany.sales_system.service.MailService;
import com.multicompany.sales_system.service.UsuarioService;
import com.multicompany.sales_system.service.VerificationCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepo;
    private final EmailVerificationRepository verifyRepo;
    private final VerificationCodeGenerator codeGen;
    private final MailService mail;
    private final PasswordEncoder encoder;

    @Value("${app.verification.code.ttl-minutes:15}") private int ttlMinutes;
    @Value("${app.verification.max-attempts:5}") private int maxAttempts;
    @Value("${app.app-name:Multi-Company Sales System}") private String appName;

    @Override
    @Transactional
    public RegisterResponse registrar(RegisterRequest dto) {
        // Validaciones de unicidad
        if (usuarioRepo.existsByCorreoIgnoreCase(dto.getCorreo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado");
        }
        if (usuarioRepo.existsByCedula(dto.getCedula())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La cédula ya está registrada");
        }

        // Mapear DTO -> Entidad
        Usuario u = new Usuario();
        u.setNombre(dto.getNombre());
        u.setApellido(dto.getApellido());
        u.setCedula(dto.getCedula()); // obligatorio por NOT NULL en DB
        u.setCorreo(dto.getCorreo().toLowerCase().trim());
        u.setContrasena(encoder.encode(dto.getContrasena()));
        u.setRol(Usuario.Rol.valueOf(dto.getRol().trim().toUpperCase()));

        // Campos opcionales que existen en la tabla
        u.setTelefono(dto.getTelefono());     // varchar(25), puede ser null
        u.setDireccion(dto.getDireccion());   // varchar(255), puede ser null
        u.setGenero(dto.getGenero());         // varchar(2),    puede ser null

        Usuario saved = usuarioRepo.save(u);
        crearYEnviarCodigo(saved);

        return new RegisterResponse(
                saved.getIdUsuario(),
                saved.getCorreo(),
                saved.getRol().name(),
                "Usuario creado. Se envió un código a tu correo."
        );
    }

    @Override
    @Transactional
    public String verificarCorreo(VerifyEmailRequest req) {
        Usuario user = usuarioRepo.findByCorreoIgnoreCase(req.getCorreo())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        if (user.isEmailVerificado()) return "El correo ya está verificado";

        EmailVerification ev = verifyRepo
                .findTopByUserIdUsuarioAndUsedFalseOrderByExpiresAtDesc(user.getIdUsuario())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solicita un nuevo código"));

        if (OffsetDateTime.now().isAfter(ev.getExpiresAt())) {
            ev.setUsed(true);
            verifyRepo.save(ev);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código expirado");
        }
        if (ev.getAttempts() >= maxAttempts) {
            ev.setUsed(true);
            verifyRepo.save(ev);
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Demasiados intentos");
        }
        if (!ev.getCode().equals(req.getCode())) {
            ev.setAttempts(ev.getAttempts() + 1);
            verifyRepo.save(ev);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código incorrecto");
        }

        ev.setUsed(true);
        verifyRepo.save(ev);

        user.setEmailVerificado(true);
        usuarioRepo.save(user);
        return "Correo verificado correctamente";
    }

    @Override
    @Transactional
    public void reenviarCodigo(String email) {
        Usuario user = usuarioRepo.findByCorreoIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        if (user.isEmailVerificado()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El correo ya está verificado");
        }

        crearYEnviarCodigo(user);
    }

    @Override
    public boolean correoDisponible(String email) {
        return !usuarioRepo.existsByCorreoIgnoreCase(email);
    }

    private void crearYEnviarCodigo(Usuario user) {
        // Invalida código previo no usado (si existe)
        verifyRepo.findTopByUserIdUsuarioAndUsedFalseOrderByExpiresAtDesc(user.getIdUsuario())
                .ifPresent(prev -> {
                    prev.setUsed(true);
                    verifyRepo.save(prev);
                });

        String code = codeGen.generateNumeric();

        EmailVerification ev = new EmailVerification();
        ev.setUser(user);
        ev.setCode(code);
        ev.setExpiresAt(OffsetDateTime.now().plusMinutes(ttlMinutes));
        verifyRepo.save(ev);

        String subject = "[" + appName + "] Verifica tu correo";
        String body = "Hola " + user.getNombre() + ",\n\n"
                + "Tu código de verificación es: " + code + "\n"
                + "Caduca en " + ttlMinutes + " minutos.\n\n"
                + "Si no solicitaste esto, ignora el mensaje.";
        mail.sendPlain(user.getCorreo(), subject, body);
    }
}
