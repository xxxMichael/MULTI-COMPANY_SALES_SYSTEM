package com.multicompany.sales_system.repository;

import com.multicompany.sales_system.model.ProductoInteresado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoInteresadoRepository extends JpaRepository<ProductoInteresado, Long> {

    // Buscar interés específico entre usuario y producto
    Optional<ProductoInteresado> findByUsuarioIdUsuarioAndProductoIdProducto(Long idUsuario, Long idProducto);

    // Verificar si un usuario tiene interés en un producto
    boolean existsByUsuarioIdUsuarioAndProductoIdProducto(Long idUsuario, Long idProducto);

    // Obtener todos los productos de interés de un usuario con paginación
    @Query("SELECT pi FROM ProductoInteresado pi WHERE pi.usuario.idUsuario = :idUsuario " +
            "ORDER BY pi.fechaInteres DESC")
    Page<ProductoInteresado> findByUsuarioIdUsuario(@Param("idUsuario") Long idUsuario, Pageable pageable);

    // Obtener lista de productos de interés de un usuario sin paginación
    List<ProductoInteresado> findByUsuarioIdUsuarioOrderByFechaInteresDesc(Long idUsuario);

    // Contar productos de interés de un usuario
    long countByUsuarioIdUsuario(Long idUsuario);

    // Obtener usuarios interesados en un producto específico
    @Query("SELECT pi FROM ProductoInteresado pi WHERE pi.producto.idProducto = :idProducto " +
            "ORDER BY pi.fechaInteres DESC")
    Page<ProductoInteresado> findByProductoIdProducto(@Param("idProducto") Long idProducto, Pageable pageable);

    // Contar usuarios interesados en un producto
    long countByProductoIdProducto(Long idProducto);

    // Eliminar interés específico
    void deleteByUsuarioIdUsuarioAndProductoIdProducto(Long idUsuario, Long idProducto);

    // Obtener productos más populares (con más intereses)
    @Query("SELECT pi.producto.idProducto, COUNT(pi) as total " +
            "FROM ProductoInteresado pi " +
            "GROUP BY pi.producto.idProducto " +
            "ORDER BY total DESC")
    Page<Object[]> findProductosMasPopulares(Pageable pageable);
}