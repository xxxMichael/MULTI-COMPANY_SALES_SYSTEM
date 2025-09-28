package com.multicompany.sales_system.controller;

import com.multicompany.sales_system.dto.user.*;
import com.multicompany.sales_system.dto.user.RegisterRequest;
import com.multicompany.sales_system.dto.user.RegisterResponse;
import com.multicompany.sales_system.dto.user.VerifyEmailRequest;
import com.multicompany.sales_system.dto.user.ResendCodeRequest;
import com.multicompany.sales_system.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    private final UsuarioService usuarioService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest dto) {
        RegisterResponse created = usuarioService.registrar(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, Object>> verify(@Valid @RequestBody VerifyEmailRequest dto) {
        String message = usuarioService.verificarCorreo(dto);
        return ResponseEntity.ok(Map.of("message", message));
    }

    @PostMapping("/resend-code")
    public ResponseEntity<Map<String, Object>> resend(@Valid @RequestBody ResendCodeRequest dto) {
        usuarioService.reenviarCodigo(dto.getCorreo());
        return ResponseEntity.ok(Map.of("message", "Nuevo código enviado"));
    }

    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Object>> checkEmail(@RequestParam String email) {
        boolean available = usuarioService.correoDisponible(email);
        return ResponseEntity.ok(Map.of("email", email, "available", available));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntime(RuntimeException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
