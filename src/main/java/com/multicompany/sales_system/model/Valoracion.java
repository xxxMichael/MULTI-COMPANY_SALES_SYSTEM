package com.multicompany.sales_system.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "valoracion", uniqueConstraints = {
    @UniqueConstraint(name = "uk_valoracion_comprador_vendedor", 
                      columnNames = {"id_comprador", "id_vendedor"})
})
public class Valoracion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_valoracion")
    private Long idValoracion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vendedor", nullable = false)
    private Usuario vendedor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_comprador", nullable = false)
    private Usuario comprador;

    @Column(name = "puntuacion", nullable = false)
    private Integer puntuacion; // 1-5 estrellas

    @Column(name = "comentario", length = 255)
    private String comentario;

    @Column(name = "fecha_valoracion", nullable = false)
    private LocalDateTime fechaValoracion;

    @PrePersist
    protected void onCreate() {
        if (fechaValoracion == null) {
            fechaValoracion = LocalDateTime.now();
        }
    }
}
