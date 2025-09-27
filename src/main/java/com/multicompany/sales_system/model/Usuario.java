package com.multicompany.sales_system.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUsuario;

    @Column(nullable = false, unique = true)
    private String cedula;

    private String nombre;
    private String apellido;

    @Column(unique = true, nullable = false)
    private String correo;

    private String telefono;
    private String direccion;
    private String genero;
    private String contrasena;

    @Enumerated(EnumType.STRING)
    private Rol rol;

    @Enumerated(EnumType.STRING)
    private EstadoUsuario estado = EstadoUsuario.ACTIVO;

    private LocalDateTime fechaRegistro = LocalDateTime.now();

    public enum Rol { COMPRADOR, VENDEDOR, MODERADOR, ADMINISTRADOR }
    public enum EstadoUsuario { ACTIVO, SUSPENDIDO, ELIMINADO }
}
