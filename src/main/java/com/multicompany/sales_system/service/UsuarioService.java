package com.multicompany.sales_system.service;

import com.multicompany.sales_system.dto.user.RegisterRequest;
import com.multicompany.sales_system.dto.user.RegisterResponse;
import com.multicompany.sales_system.dto.user.VerifyEmailRequest;

public interface UsuarioService {
    RegisterResponse registrar(RegisterRequest dto);
    String verificarCorreo(VerifyEmailRequest req);
    void reenviarCodigo(String email);
    boolean correoDisponible(String email);
}
