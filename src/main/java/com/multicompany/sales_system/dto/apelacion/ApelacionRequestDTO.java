package com.multicompany.sales_system.dto.apelacion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;

/**
 * DTO para crear una apelación
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApelacionRequestDTO {

    @NotNull(message = "El ID del producto es obligatorio")
    private Long idProducto;

    @NotNull(message = "El ID del vendedor es obligatorio")
    private Long idVendedor;

    @NotBlank(message = "La justificación es obligatoria")
    @Size(max = 1000, message = "La justificación no puede exceder los 1000 caracteres")
    private String justificacion;

    @Size(max = 500, message = "Los comentarios adicionales no pueden exceder los 500 caracteres")
    private String comentariosAdicionales;
}