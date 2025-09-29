package com.multicompany.sales_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import com.multicompany.sales_system.model.Configuracion;

public interface ConfiguracionRepository extends JpaRepository<Configuracion, Long> {
    Optional<Configuracion> findByOpcion(String opcion);
}