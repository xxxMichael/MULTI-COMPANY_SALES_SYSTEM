package com.multicompany.sales_system.dto.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

import com.multicompany.sales_system.dto.photo.PhotoResponseDTO;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDTO {
    private Long idProducto;
    private String codigo;
    private String nombre;
    private String descripcion;
    private Double precio;
    private String ubicacion;
    private Boolean disponibilidad;
    private String tipo;
    private String estado;
    private LocalDateTime fechaPublicacion;
    private LocalDateTime fechaExpiracion; // ✅ NUEVO: Fecha de expiración
    private Long idVendedor;
    private String nombreVendedor;
    private List<PhotoResponseDTO> fotos;
}
