package com.multicompany.sales_system.service;

import com.multicompany.sales_system.model.Incidencia;

import java.util.List;

public interface IncidenciaService {

    // Listar todas las incidencias pendientes
    List<Incidencia> listarPendientes();

    // Marcar incidencia como atendida
    Incidencia marcarAtendida(Long idIncidencia);

    // Descartar incidencia
    Incidencia descartar(Long idIncidencia);

    // Crear incidencia automáticamente (detección de producto prohibido)
    Incidencia crearPorDeteccion(Long idProducto, Long idUsuarioReporta, String motivo, String descripcion);

    List<Incidencia> listarAtendidas();

    List<Incidencia> listarDescartadas();
}