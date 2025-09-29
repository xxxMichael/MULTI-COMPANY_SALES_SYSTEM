package com.multicompany.sales_system.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.multicompany.sales_system.model.Incidencia;
import com.multicompany.sales_system.model.Producto;
import com.multicompany.sales_system.model.Usuario;
import com.multicompany.sales_system.repository.IncidenciaRepository;
import com.multicompany.sales_system.repository.ProductRepository;
import com.multicompany.sales_system.repository.UsuarioRepository;

import java.time.LocalDateTime;
// import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class IncidenciaServiceImpl implements IncidenciaService {

    private final IncidenciaRepository incidenciaRepository;
    private final ProductRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    public IncidenciaServiceImpl(IncidenciaRepository incidenciaRepository,
            ProductRepository productoRepository,
            UsuarioRepository usuarioRepository) {
        this.incidenciaRepository = incidenciaRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Incidencia> listarPendientes() {
        return incidenciaRepository.findByEstado(Incidencia.Estado.PENDIENTE);
    }

    @Override
    public List<Incidencia> listarAtendidas() {
        return incidenciaRepository.findByEstado(Incidencia.Estado.ATENDIDA);
    }

    @Override
    public List<Incidencia> listarDescartadas() {
        return incidenciaRepository.findByEstado(Incidencia.Estado.DESCARTADA);
    }

    @Override
    public Incidencia marcarAtendida(Long idIncidencia) {
        Incidencia inc = incidenciaRepository.findById(idIncidencia)
                .orElseThrow(() -> new RuntimeException("Incidencia no encontrada"));
        inc.setEstado(Incidencia.Estado.ATENDIDA);
        return incidenciaRepository.save(inc);
    }

    @Override
    public Incidencia descartar(Long idIncidencia) {
        Incidencia inc = incidenciaRepository.findById(idIncidencia)
                .orElseThrow(() -> new RuntimeException("Incidencia no encontrada"));
        inc.setEstado(Incidencia.Estado.DESCARTADA);
        return incidenciaRepository.save(inc);
    }

    @Override
    public Incidencia crearPorDeteccion(Long idProducto, Long idUsuarioReporta, String motivo, String descripcion) {
        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        Usuario usuario = usuarioRepository.findById(idUsuarioReporta)
                .orElseThrow(() -> new RuntimeException("Usuario reportante no encontrado"));

        Incidencia inc = new Incidencia();
        inc.setProducto(producto);
        inc.setUsuarioReporta(usuario);
        inc.setMotivo(motivo);
        inc.setDescripcion(descripcion);
        inc.setEstado(Incidencia.Estado.PENDIENTE);
        inc.setFechaRegistro(LocalDateTime.now());

        return incidenciaRepository.save(inc);
    }
}