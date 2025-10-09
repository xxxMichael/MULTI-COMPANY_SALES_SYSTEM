package com.multicompany.sales_system.dto.interes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * DTO para respuestas de ProductoInteresado
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoInteresadoResponseDTO {
    private Long idInteresado;
    private Long idUsuario;
    private String nombreUsuario;
    private Long idProducto;
    private String nombreProducto;
    private String descripcionProducto;
    private Double precioProducto;
    private String tipoProducto;
    private String estadoProducto;
    private LocalDateTime fechaInteres;
    private LocalDateTime fechaPublicacionProducto;
    private Long idVendedor;
    private String nombreVendedor;
}