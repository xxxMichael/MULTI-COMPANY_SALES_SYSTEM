package com.multicompany.sales_system.repository;

import com.multicompany.sales_system.model.Valoracion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ValoracionRepository extends JpaRepository<Valoracion, Long> {

    /**
     * Verifica si un comprador ya valoró a un vendedor específico
     */
    boolean existsByCompradorIdUsuarioAndVendedorIdUsuario(Long compradorId, Long vendedorId);

    /**
     * Obtiene la valoración de un comprador hacia un vendedor específico
     */
    Optional<Valoracion> findByCompradorIdUsuarioAndVendedorIdUsuario(Long compradorId, Long vendedorId);

    /**
     * Obtiene todas las valoraciones recibidas por un vendedor
     */
    List<Valoracion> findByVendedorIdUsuarioOrderByFechaValoracionDesc(Long vendedorId);

    /**
     * Obtiene todas las valoraciones realizadas por un comprador
     */
    List<Valoracion> findByCompradorIdUsuarioOrderByFechaValoracionDesc(Long compradorId);

    /**
     * Calcula el promedio de puntuación de un vendedor
     */
    @Query("SELECT AVG(v.puntuacion) FROM Valoracion v WHERE v.vendedor.idUsuario = :vendedorId")
    Double calcularPromedioVendedor(@Param("vendedorId") Long vendedorId);

    /**
     * Cuenta el número total de valoraciones de un vendedor
     */
    long countByVendedorIdUsuario(Long vendedorId);

    /**
     * Obtiene valoraciones por puntuación específica de un vendedor
     */
    List<Valoracion> findByVendedorIdUsuarioAndPuntuacion(Long vendedorId, Integer puntuacion);

    /**
     * Obtiene las últimas N valoraciones de un vendedor
     */
    @Query("SELECT v FROM Valoracion v WHERE v.vendedor.idUsuario = :vendedorId ORDER BY v.fechaValoracion DESC")
    List<Valoracion> findTopNByVendedorId(@Param("vendedorId") Long vendedorId, 
                                          org.springframework.data.domain.Pageable pageable);
}
