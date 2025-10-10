package com.multicompany.sales_system.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.multicompany.sales_system.dto.user.AdminCreateModeratorRequest;
import com.multicompany.sales_system.dto.user.LoginRequest;
import com.multicompany.sales_system.dto.user.LoginResponse;
import com.multicompany.sales_system.dto.user.RegisterRequest;
import com.multicompany.sales_system.dto.user.RegisterResponse;
import com.multicompany.sales_system.dto.user.ResendCodeRequest;
import com.multicompany.sales_system.dto.user.VerifyEmailRequest;
import com.multicompany.sales_system.service.UsuarioService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    private final UsuarioService usuarioService;

    // =========================
    // Registro normal
    // =========================
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest dto, BindingResult br) {
        var cedulaError = br.getFieldError("cedula");
        if (cedulaError != null) {
            return ResponseEntity.badRequest().body(cedulaError.getDefaultMessage());
        }
        if (br.hasErrors()) {
            var first = br.getFieldErrors().get(0);
            return ResponseEntity.badRequest().body(first.getDefaultMessage());
        }

        RegisterResponse created = usuarioService.registrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // =========================
    // ADMIN: Crear moderador
    // =========================
@PostMapping("/admin/moderators")
public ResponseEntity<RegisterResponse> createModerator(
        @Valid @RequestBody AdminCreateModeratorRequest dto,
        BindingResult br,
        @RequestHeader(value = "X-ADMIN-KEY", required = false) String adminKeyHeader) {

    if (br.hasErrors()) {
        var first = br.getFieldErrors().get(0);
        return ResponseEntity.badRequest().body(
                new RegisterResponse(null, dto.getCorreo(), null, first.getDefaultMessage())
        );
    }
    if (adminKeyHeader == null || adminKeyHeader.isBlank()) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new RegisterResponse(null, dto.getCorreo(), null, "Falta el header X-ADMIN-KEY"));
    }

    RegisterResponse created = usuarioService.crearModerador(dto, adminKeyHeader);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
}
    // =========================
    // Verificación de correo
    // =========================
    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, Object>> verify(@Valid @RequestBody VerifyEmailRequest dto) {
        String message = usuarioService.verificarCorreo(dto);
        return ResponseEntity.ok(Map.of("message", message));
    }

    // =========================
    // Reenviar código
    // =========================
    @PostMapping("/resend-code")
    public ResponseEntity<Map<String, Object>> resend(@Valid @RequestBody ResendCodeRequest dto) {
        usuarioService.reenviarCodigo(dto.getCorreo());
        return ResponseEntity.ok(Map.of("message", "Nuevo código enviado"));
    }

    // =========================
    // Check email
    // =========================
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Object>> checkEmail(@RequestParam String email) {
        boolean available = usuarioService.correoDisponible(email);
        return ResponseEntity.ok(Map.of("email", email, "available", available));
    }

    // =========================
    // Login
    // =========================
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest dto, BindingResult br) {
        if (br.hasErrors()) {
            var first = br.getFieldErrors().get(0);
            return ResponseEntity.badRequest().body(
                    Map.of("field", first.getField(), "message", first.getDefaultMessage())
            );
        }
        LoginResponse res = usuarioService.login(dto);
        return ResponseEntity.ok(res);
    }

    // =========================
    // Manejo de errores
    // =========================
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleRse(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode())
                .body(Map.of("message", ex.getReason()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntime(RuntimeException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
