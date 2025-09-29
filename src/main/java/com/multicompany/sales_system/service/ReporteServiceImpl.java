package com.multicompany.sales_system.service;

import org.springframework.stereotype.Service;

import com.multicompany.sales_system.model.Reporte;
import com.multicompany.sales_system.repository.ReporteRepository;

import java.util.List;

@Service
public class ReporteServiceImpl implements ReporteService {

    private final ReporteRepository repository;

    public ReporteServiceImpl(ReporteRepository repository) {
        this.repository = repository;
    }

    @Override
    public Reporte crearReporte(Reporte reporte) {
        return repository.save(reporte);
    }

    @Override
    public List<Reporte> obtenerPorIncidencia(Long incidenciaId) {
        return repository.findByIncidenciaIdIncidencia(incidenciaId);
    }

}
