package com.multicompany.sales_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.multicompany.sales_system.model.Reporte;

import java.util.List;

public interface ReporteRepository extends JpaRepository<Reporte, Long> {
    List<Reporte> findByIncidenciaIdIncidencia(Long idIncidencia);

    List<Reporte> findByModeradorIdUsuario(Long idModerador);

    List<Reporte> findByOrderByFechaAccionDesc();
}