package com.multicompany.sales_system.repository;

import com.multicompany.sales_system.model.Producto;
import com.multicompany.sales_system.model.enums.TipoProducto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ProductRepository extends JpaRepository<Producto, Long> {

    // Métodos existentes
    List<Producto> findByVendedorIdUsuario(Long vendedorId);

    List<Producto> findByNombreContainingIgnoreCaseOrDescripcionContainingIgnoreCase(String nombre, String descripcion);

    @Query("SELECT p FROM Producto p WHERE p.estado = com.multicompany.sales_system.model.enums.EstadoProducto.ACTIVO")
    List<Producto> findActiveProducts();

    @Query("SELECT p FROM Producto p WHERE p.tipo = :tipo AND p.estado = com.multicompany.sales_system.model.enums.EstadoProducto.ACTIVO")
    List<Producto> findByTipoAndActive(@Param("tipo") TipoProducto tipo);

    // NUEVOS MÉTODOS CON FILTROS Y PAGINACIÓN

    // Filtrar por rango de precio
    @Query("SELECT p FROM Producto p WHERE p.estado = com.multicompany.sales_system.model.enums.EstadoProducto.ACTIVO " +
           "AND p.precio BETWEEN :minPrice AND :maxPrice")
    Page<Producto> findByPrecioBetween(@Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice, Pageable pageable);

    // Filtrar por precio mínimo
    @Query("SELECT p FROM Producto p WHERE p.estado = com.multicompany.sales_system.model.enums.EstadoProducto.ACTIVO " +
           "AND p.precio >= :minPrice")
    Page<Producto> findByPrecioGreaterThanEqual(@Param("minPrice") Double minPrice, Pageable pageable);

    // Filtrar por precio máximo  
    @Query("SELECT p FROM Producto p WHERE p.estado = com.multicompany.sales_system.model.enums.EstadoProducto.ACTIVO " +
           "AND p.precio <= :maxPrice")
    Page<Producto> findByPrecioLessThanEqual(@Param("maxPrice") Double maxPrice, Pageable pageable);

    // Filtrar por tipo con paginación
    @Query("SELECT p FROM Producto p WHERE p.estado = com.multicompany.sales_system.model.enums.EstadoProducto.ACTIVO " +
           "AND p.tipo = :tipo")
    Page<Producto> findByTipoAndEstadoActivo(@Param("tipo") TipoProducto tipo, Pageable pageable);

    // Filtrar por ubicación
    @Query("SELECT p FROM Producto p WHERE p.estado = com.multicompany.sales_system.model.enums.EstadoProducto.ACTIVO " +
           "AND p.ubicacion LIKE :ubicacion")
    Page<Producto> findByUbicacionContaining(@Param("ubicacion") String ubicacion, Pageable pageable);

    // Búsqueda por texto con paginación
    @Query("SELECT p FROM Producto p WHERE p.estado = com.multicompany.sales_system.model.enums.EstadoProducto.ACTIVO " +
           "AND (p.nombre LIKE :searchTerm OR " +
           "p.descripcion LIKE :searchTerm)")
    Page<Producto> findByNombreOrDescripcionContaining(@Param("searchTerm") String searchTerm, Pageable pageable);

    // FILTRO COMBINADO AVANZADO
    @Query("SELECT p FROM Producto p WHERE p.estado = com.multicompany.sales_system.model.enums.EstadoProducto.ACTIVO " +
           "AND (:minPrice IS NULL OR p.precio >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.precio <= :maxPrice) " +
           "AND (:tipo IS NULL OR p.tipo = :tipo) " +
           "AND (:searchTerm IS NULL OR " +
           "     p.nombre LIKE :searchTerm OR " +
           "     p.descripcion LIKE :searchTerm) " +
           "AND (:ubicacion IS NULL OR p.ubicacion LIKE :ubicacion) " +
           "AND (:disponibilidad IS NULL OR p.disponibilidad = :disponibilidad)")
    Page<Producto> findWithFilters(@Param("minPrice") Double minPrice,
                                  @Param("maxPrice") Double maxPrice,
                                  @Param("tipo") TipoProducto tipo,
                                  @Param("searchTerm") String searchTerm,
                                  @Param("ubicacion") String ubicacion,
                                  @Param("disponibilidad") Boolean disponibilidad,
                                  Pageable pageable);
}
