package com.multicompany.sales_system.service.impl;

import com.multicompany.sales_system.dto.valoracion.CrearValoracionRequest;
import com.multicompany.sales_system.dto.valoracion.EstadisticasVendedorResponse;
import com.multicompany.sales_system.dto.valoracion.ValoracionResponse;
import com.multicompany.sales_system.model.Usuario;
import com.multicompany.sales_system.model.Valoracion;
import com.multicompany.sales_system.repository.UsuarioRepository;
import com.multicompany.sales_system.repository.ValoracionRepository;
import com.multicompany.sales_system.service.ValoracionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ValoracionServiceImpl implements ValoracionService {

    private final ValoracionRepository valoracionRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public ValoracionResponse crearValoracion(Long compradorId, CrearValoracionRequest request) {
        log.info("Creando valoración de comprador {} para vendedor {}", compradorId, request.getVendedorId());

        // Validar que el comprador no se valore a sí mismo
        if (compradorId.equals(request.getVendedorId())) {
            throw new IllegalArgumentException("No puedes valorarte a ti mismo");
        }

        // Verificar que no exista ya una valoración
        if (valoracionRepository.existsByCompradorIdUsuarioAndVendedorIdUsuario(compradorId, request.getVendedorId())) {
            throw new IllegalStateException("Ya has valorado a este vendedor previamente");
        }

        // Obtener comprador y vendedor
        Usuario comprador = usuarioRepository.findById(compradorId)
                .orElseThrow(() -> new IllegalArgumentException("Comprador no encontrado"));

        Usuario vendedor = usuarioRepository.findById(request.getVendedorId())
                .orElseThrow(() -> new IllegalArgumentException("Vendedor no encontrado"));

        // Verificar que el vendedor esté activo
        if (vendedor.getEstado() != Usuario.EstadoUsuario.ACTIVO) {
            throw new IllegalStateException("El vendedor no está activo");
        }

        // Crear la valoración
        Valoracion valoracion = new Valoracion();
        valoracion.setComprador(comprador);
        valoracion.setVendedor(vendedor);
        valoracion.setPuntuacion(request.getPuntuacion());
        valoracion.setComentario(request.getComentario());

        Valoracion saved = valoracionRepository.save(valoracion);
        log.info("Valoración creada exitosamente con ID: {}", saved.getIdValoracion());

        return mapearAResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ValoracionResponse> obtenerValoracionesDeVendedor(Long vendedorId) {
        log.info("Obteniendo valoraciones del vendedor {}", vendedorId);

        // Verificar que el vendedor exista
        if (!usuarioRepository.existsById(vendedorId)) {
            throw new IllegalArgumentException("Vendedor no encontrado");
        }

        List<Valoracion> valoraciones = valoracionRepository
                .findByVendedorIdUsuarioOrderByFechaValoracionDesc(vendedorId);

        return valoraciones.stream()
                .map(this::mapearAResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ValoracionResponse> obtenerValoracionesRealizadas(Long compradorId) {
        log.info("Obteniendo valoraciones realizadas por el comprador {}", compradorId);

        // Verificar que el comprador exista
        if (!usuarioRepository.existsById(compradorId)) {
            throw new IllegalArgumentException("Comprador no encontrado");
        }

        List<Valoracion> valoraciones = valoracionRepository
                .findByCompradorIdUsuarioOrderByFechaValoracionDesc(compradorId);

        return valoraciones.stream()
                .map(this::mapearAResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EstadisticasVendedorResponse obtenerEstadisticasVendedor(Long vendedorId) {
        log.info("Obteniendo estadísticas del vendedor {}", vendedorId);

        // Verificar que el vendedor exista
        Usuario vendedor = usuarioRepository.findById(vendedorId)
                .orElseThrow(() -> new IllegalArgumentException("Vendedor no encontrado"));

        Double promedio = valoracionRepository.calcularPromedioVendedor(vendedorId);
        long total = valoracionRepository.countByVendedorIdUsuario(vendedorId);

        // Contar valoraciones por puntuación
        long val5 = valoracionRepository.findByVendedorIdUsuarioAndPuntuacion(vendedorId, 5).size();
        long val4 = valoracionRepository.findByVendedorIdUsuarioAndPuntuacion(vendedorId, 4).size();
        long val3 = valoracionRepository.findByVendedorIdUsuarioAndPuntuacion(vendedorId, 3).size();
        long val2 = valoracionRepository.findByVendedorIdUsuarioAndPuntuacion(vendedorId, 2).size();
        long val1 = valoracionRepository.findByVendedorIdUsuarioAndPuntuacion(vendedorId, 1).size();

        return EstadisticasVendedorResponse.builder()
                .vendedorId(vendedorId)
                .vendedorNombre(vendedor.getNombre())
                .vendedorApellido(vendedor.getApellido())
                .promedioValoracion(promedio != null ? Math.round(promedio * 100.0) / 100.0 : 0.0)
                .totalValoraciones(total)
                .valoraciones5Estrellas(val5)
                .valoraciones4Estrellas(val4)
                .valoraciones3Estrellas(val3)
                .valoraciones2Estrellas(val2)
                .valoraciones1Estrella(val1)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean yaValorado(Long compradorId, Long vendedorId) {
        return valoracionRepository.existsByCompradorIdUsuarioAndVendedorIdUsuario(compradorId, vendedorId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ValoracionResponse> obtenerUltimasValoraciones(Long vendedorId, int limit) {
        log.info("Obteniendo últimas {} valoraciones del vendedor {}", limit, vendedorId);

        // Verificar que el vendedor exista
        if (!usuarioRepository.existsById(vendedorId)) {
            throw new IllegalArgumentException("Vendedor no encontrado");
        }

        List<Valoracion> valoraciones = valoracionRepository
                .findTopNByVendedorId(vendedorId, PageRequest.of(0, limit));

        return valoraciones.stream()
                .map(this::mapearAResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void eliminarValoracion(Long valoracionId) {
        log.info("Eliminando valoración con ID: {}", valoracionId);

        Valoracion valoracion = valoracionRepository.findById(valoracionId)
                .orElseThrow(() -> new IllegalArgumentException("Valoración no encontrada"));

        valoracionRepository.delete(valoracion);
        log.info("Valoración eliminada exitosamente");
    }

    /**
     * Mapea una entidad Valoracion a ValoracionResponse
     */
    private ValoracionResponse mapearAResponse(Valoracion valoracion) {
        return ValoracionResponse.builder()
                .idValoracion(valoracion.getIdValoracion())
                .vendedorId(valoracion.getVendedor().getIdUsuario())
                .vendedorNombre(valoracion.getVendedor().getNombre())
                .vendedorApellido(valoracion.getVendedor().getApellido())
                .compradorId(valoracion.getComprador().getIdUsuario())
                .compradorNombre(valoracion.getComprador().getNombre())
                .compradorApellido(valoracion.getComprador().getApellido())
                .puntuacion(valoracion.getPuntuacion())
                .comentario(valoracion.getComentario())
                .fechaValoracion(valoracion.getFechaValoracion())
                .build();
    }
}
