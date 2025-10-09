package com.multicompany.sales_system.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Entidad que representa el interés de un usuario en un producto
 * (Funcionalidad "Me interesa")
 */
@Data
@Entity
@Table(name = "producto_interesado", uniqueConstraints = @UniqueConstraint(columnNames = { "id_usuario",
        "id_producto" }))
public class ProductoInteresado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idInteresado;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_producto")
    private Producto producto;

    @Column(name = "fecha_interes", nullable = false)
    private LocalDateTime fechaInteres = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (fechaInteres == null) {
            fechaInteres = LocalDateTime.now();
        }
    }
}