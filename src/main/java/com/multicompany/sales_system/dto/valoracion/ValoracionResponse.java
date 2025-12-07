package com.multicompany.sales_system.dto.valoracion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValoracionResponse {

    private Long idValoracion;
    private Long vendedorId;
    private String vendedorNombre;
    private String vendedorApellido;
    private Long compradorId;
    private String compradorNombre;
    private String compradorApellido;
    private Integer puntuacion;
    private String comentario;
    private LocalDateTime fechaValoracion;
}
