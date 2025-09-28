package com.multicompany.sales_system.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(
    name = "usuario",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_usuario_correo", columnNames = "correo"),
        @UniqueConstraint(name = "uk_usuario_cedula", columnNames = "cedula")
    }
)
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(nullable = false, length = 25)
    private String cedula;

    @Column(length = 255) private String nombre;
    @Column(length = 255) private String apellido;

    @Column(nullable = false, length = 255)
    private String correo;

    @Column(length = 25)  private String telefono;
    @Column(length = 255) private String direccion;
    @Column(length = 2)   private String genero;

    @Column(nullable = false, length = 255)
    private String contrasena; // hash BCrypt

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol; // COMPRADOR | VENDEDOR | MODERADOR | ADMINISTRADOR

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoUsuario estado = EstadoUsuario.ACTIVO;

    @Column(name = "email_verificado", nullable = false)
    private boolean emailVerificado = false;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (fechaRegistro == null) fechaRegistro = LocalDateTime.now();
    }

    public enum Rol { COMPRADOR, VENDEDOR, MODERADOR, ADMINISTRADOR }
    public enum EstadoUsuario { ACTIVO, SUSPENDIDO, ELIMINADO }
}
