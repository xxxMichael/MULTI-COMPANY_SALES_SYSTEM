package com.multicompany.sales_system.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.multicompany.sales_system.model.Incidencia;
import com.multicompany.sales_system.model.Producto;
import com.multicompany.sales_system.repository.IncidenciaRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class IncidenciaService {

    private final IncidenciaRepository incidenciaRepository;

    @Transactional
    public Incidencia crearIncidenciaPorDeteccion(Producto producto, String motivo, String descripcion) {
        Incidencia inc = new Incidencia();
        inc.setProducto(producto);
        inc.setMotivo(motivo);
        inc.setDescripcion(descripcion);
        inc.setEstado(Incidencia.Estado.PENDIENTE);
        inc.setFechaRegistro(LocalDateTime.now());
        return incidenciaRepository.save(inc);
    }
}