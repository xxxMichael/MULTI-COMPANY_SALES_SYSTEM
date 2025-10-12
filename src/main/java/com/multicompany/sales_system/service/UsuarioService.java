package com.multicompany.sales_system.service;

import org.springframework.transaction.annotation.Transactional;

import com.multicompany.sales_system.dto.user.AdminCreateModeratorRequest;
import com.multicompany.sales_system.dto.user.LoginRequest;
import com.multicompany.sales_system.dto.user.LoginResponse;
import com.multicompany.sales_system.dto.user.RegisterRequest;
import com.multicompany.sales_system.dto.user.RegisterResponse;
import com.multicompany.sales_system.dto.user.VerifyEmailRequest;

public interface UsuarioService {
    RegisterResponse registrar(RegisterRequest dto);
    String verificarCorreo(VerifyEmailRequest req);
    void reenviarCodigo(String email);
    boolean correoDisponible(String email); 
    RegisterResponse crearModerador(AdminCreateModeratorRequest dto);
    @Transactional
    RegisterResponse crearModerador(AdminCreateModeratorRequest dto, String adminKeyHeader);
    void iniciarRecuperacionContrasena(String correo);
    void resetPassword(com.multicompany.sales_system.dto.user.PasswordResetRequest request);

    LoginResponse login(LoginRequest request);
}
