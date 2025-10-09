package com.multicompany.sales_system.dto.apelacion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * DTO para respuestas de apelaciones
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApelacionResponseDTO {
    private Long idProducto;
    private String nombreProducto;
    private String descripcionProducto;
    private String estadoProducto;
    private String estadoAnterior;
    private Long idVendedor;
    private String nombreVendedor;
    private String justificacion;
    private String comentariosAdicionales;
    private LocalDateTime fechaApelacion;
    private String resultado;
    private String razonDecision;
    private Long idIncidenciaOriginal;
    private LocalDateTime fechaDecision;
}