package com.multicompany.sales_system.service;

import java.util.List;

import com.multicompany.sales_system.model.Reporte;

public interface ReporteService {
    Reporte crearReporte(Reporte reporte);

    List<Reporte> obtenerPorIncidencia(Long incidenciaId);
}