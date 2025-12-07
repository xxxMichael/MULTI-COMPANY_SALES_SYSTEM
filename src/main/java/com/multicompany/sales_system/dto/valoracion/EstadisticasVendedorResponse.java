package com.multicompany.sales_system.dto.valoracion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstadisticasVendedorResponse {

    private Long vendedorId;
    private String vendedorNombre;
    private String vendedorApellido;
    private Double promedioValoracion;
    private Long totalValoraciones;
    private Long valoraciones5Estrellas;
    private Long valoraciones4Estrellas;
    private Long valoraciones3Estrellas;
    private Long valoraciones2Estrellas;
    private Long valoraciones1Estrella;
}
