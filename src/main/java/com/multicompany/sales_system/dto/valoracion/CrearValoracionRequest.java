package com.multicompany.sales_system.dto.valoracion;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearValoracionRequest {

    @NotNull(message = "El ID del vendedor es requerido")
    private Long vendedorId;

    @NotNull(message = "La puntuación es requerida")
    @Min(value = 1, message = "La puntuación mínima es 1")
    @Max(value = 5, message = "La puntuación máxima es 5")
    private Integer puntuacion;

    @Size(max = 255, message = "El comentario no puede exceder los 255 caracteres")
    private String comentario;
}
