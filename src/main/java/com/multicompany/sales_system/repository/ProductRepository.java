package com.multicompany.sales_system.repository;

import com.multicompany.sales_system.model.Producto;
import com.multicompany.sales_system.model.enums.TipoProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ProductRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByVendedorIdUsuario(Long vendedorId);

    List<Producto> findByNombreContainingIgnoreCaseOrDescripcionContainingIgnoreCase(String nombre, String descripcion);

    @Query("SELECT p FROM Producto p WHERE p.estado = com.multicompany.sales_system.model.enums.EstadoProducto.ACTIVO")
    List<Producto> findActiveProducts();

    @Query("SELECT p FROM Producto p WHERE p.tipo = :tipo AND p.estado = com.multicompany.sales_system.model.enums.EstadoProducto.ACTIVO")
    List<Producto> findByTipoAndActive(@Param("tipo") TipoProducto tipo);
}
