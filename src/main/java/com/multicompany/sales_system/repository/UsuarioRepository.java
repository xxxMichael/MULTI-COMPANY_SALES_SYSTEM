package com.multicompany.sales_system.repository;

import com.multicompany.sales_system.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    boolean existsByCorreoIgnoreCase(String correo);
    boolean existsByCedula(String cedula);
    Optional<Usuario> findByCorreoIgnoreCase(String correo);
}
