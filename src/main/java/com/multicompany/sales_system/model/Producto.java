package com.multicompany.sales_system.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "producto")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProducto;

    @Column(unique = true, nullable = false)
    private String codigo;

    private String nombre;
    private String descripcion;
    private Double precio;
    private String ubicacion;
    private Boolean disponibilidad = true;

    @Enumerated(EnumType.STRING)
    private Tipo tipo;

    @Enumerated(EnumType.STRING)
    private Estado estado = Estado.ACTIVO;

    private LocalDateTime fechaPublicacion = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "id_vendedor")
    private Usuario vendedor;

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FotoProducto> fotos;

    public enum Tipo { PRODUCTO, SERVICIO }
    public enum Estado { ACTIVO, OCULTO, PROHIBIDO, APELADO, ELIMINADO }
}
