package com.multicompany.sales_system.service;

import com.multicompany.sales_system.dto.valoracion.CrearValoracionRequest;
import com.multicompany.sales_system.dto.valoracion.EstadisticasVendedorResponse;
import com.multicompany.sales_system.dto.valoracion.ValoracionResponse;

import java.util.List;

public interface ValoracionService {

    /**
     * Crea una nueva valoración de un comprador hacia un vendedor
     * @param compradorId ID del usuario que realiza la valoración
     * @param request Datos de la valoración
     * @return La valoración creada
     * @throws IllegalArgumentException si el comprador intenta valorarse a sí mismo
     * @throws IllegalStateException si el comprador ya valoró a este vendedor
     */
    ValoracionResponse crearValoracion(Long compradorId, CrearValoracionRequest request);

    /**
     * Obtiene todas las valoraciones recibidas por un vendedor
     * @param vendedorId ID del vendedor
     * @return Lista de valoraciones
     */
    List<ValoracionResponse> obtenerValoracionesDeVendedor(Long vendedorId);

    /**
     * Obtiene todas las valoraciones realizadas por un comprador
     * @param compradorId ID del comprador
     * @return Lista de valoraciones
     */
    List<ValoracionResponse> obtenerValoracionesRealizadas(Long compradorId);

    /**
     * Obtiene las estadísticas de valoración de un vendedor
     * @param vendedorId ID del vendedor
     * @return Estadísticas completas
     */
    EstadisticasVendedorResponse obtenerEstadisticasVendedor(Long vendedorId);

    /**
     * Verifica si un comprador ya valoró a un vendedor
     * @param compradorId ID del comprador
     * @param vendedorId ID del vendedor
     * @return true si ya existe una valoración
     */
    boolean yaValorado(Long compradorId, Long vendedorId);

    /**
     * Obtiene las últimas N valoraciones de un vendedor
     * @param vendedorId ID del vendedor
     * @param limit Número de valoraciones a obtener
     * @return Lista de valoraciones
     */
    List<ValoracionResponse> obtenerUltimasValoraciones(Long vendedorId, int limit);

    /**
     * Elimina una valoración (solo administradores)
     * @param valoracionId ID de la valoración
     */
    void eliminarValoracion(Long valoracionId);
}
