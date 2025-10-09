package com.multicompany.sales_system.service;

import com.multicompany.sales_system.dto.apelacion.ApelacionRequestDTO;
import com.multicompany.sales_system.dto.apelacion.ApelacionResponseDTO;
import com.multicompany.sales_system.model.Producto;
import com.multicompany.sales_system.model.Usuario;
import com.multicompany.sales_system.model.enums.EstadoProducto;
import com.multicompany.sales_system.repository.ProductRepository;
import com.multicompany.sales_system.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar apelaciones de productos rechazados
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ApelacionService {

    private final ProductRepository productRepository;
    private final UsuarioRepository usuarioRepository;
    private final IncidenciaService incidenciaService;

    /**
     * Crear una nueva apelación para un producto prohibido
     */
    @Transactional
    public ApelacionResponseDTO crearApelacion(ApelacionRequestDTO apelacionDTO) {
        // Verificar que el producto existe
        Producto producto = productRepository.findById(apelacionDTO.getIdProducto())
                .orElseThrow(
                        () -> new RuntimeException("Producto no encontrado con ID: " + apelacionDTO.getIdProducto()));

        // Verificar que el vendedor existe
        Usuario vendedor = usuarioRepository.findById(apelacionDTO.getIdVendedor())
                .orElseThrow(
                        () -> new RuntimeException("Vendedor no encontrado con ID: " + apelacionDTO.getIdVendedor()));

        // Verificar que el vendedor es dueño del producto
        if (!producto.getVendedor().getIdUsuario().equals(apelacionDTO.getIdVendedor())) {
            throw new RuntimeException("Solo el vendedor del producto puede crear una apelación");
        }

        // Verificar que el producto está en estado PROHIBIDO
        if (producto.getEstado() != EstadoProducto.PROHIBIDO) {
            throw new RuntimeException("Solo se pueden apelar productos en estado PROHIBIDO");
        }

        // Verificar que no hay ya una apelación pendiente
        if (producto.getEstado() == EstadoProducto.APELADO) {
            throw new RuntimeException("Este producto ya tiene una apelación pendiente");
        }

        // Cambiar el estado del producto a APELADO
        EstadoProducto estadoAnterior = producto.getEstado();
        producto.setEstado(EstadoProducto.APELADO);
        productRepository.save(producto);

        // Crear una incidencia para la apelación
        String motivo = "Apelación de producto prohibido";
        String descripcion = String.format(
                "El vendedor %s (%s) ha presentado una apelación para el producto '%s' (ID: %s).\n\n" +
                        "Justificación: %s\n\n" +
                        "Comentarios adicionales: %s\n\n" +
                        "Fecha de apelación: %s",
                vendedor.getNombre() + " " + vendedor.getApellido(),
                vendedor.getCorreo(),
                producto.getNombre(),
                producto.getIdProducto(),
                apelacionDTO.getJustificacion(),
                apelacionDTO.getComentariosAdicionales() != null ? apelacionDTO.getComentariosAdicionales() : "Ninguno",
                LocalDateTime.now());

        incidenciaService.crearPorDeteccion(
                producto.getIdProducto(),
                apelacionDTO.getIdVendedor(),
                motivo,
                descripcion);

        log.info("Apelación creada para producto ID={} por vendedor ID={}",
                producto.getIdProducto(), apelacionDTO.getIdVendedor());

        return convertToApelacionResponseDTO(producto, estadoAnterior, apelacionDTO, null, null, null);
    }

    /**
     * Procesar una apelación (aprobar o rechazar)
     */
    @Transactional
    public ApelacionResponseDTO procesarApelacion(Long idProducto, Long idModerador,
            boolean aprobar, String razonDecision) {
        // Verificar que el producto existe
        Producto producto = productRepository.findById(idProducto)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + idProducto));

        // Verificar que el moderador existe y tiene permisos
        usuarioRepository.findById(idModerador)
                .orElseThrow(() -> new RuntimeException("Moderador no encontrado con ID: " + idModerador));

        // Verificar que el producto está en estado APELADO
        if (producto.getEstado() != EstadoProducto.APELADO) {
            throw new RuntimeException("El producto no tiene apelación pendiente");
        }

        EstadoProducto estadoAnterior = producto.getEstado();
        String resultado;

        if (aprobar) {
            // Aprobar la apelación: reactivar el producto
            producto.setEstado(EstadoProducto.ACTIVO);
            producto.setDisponibilidad(true);
            resultado = "APROBADA";
            log.info("Apelación APROBADA para producto ID={} por moderador ID={}", idProducto, idModerador);
        } else {
            // Rechazar la apelación: mantener como prohibido
            producto.setEstado(EstadoProducto.PROHIBIDO);
            producto.setDisponibilidad(false);
            resultado = "RECHAZADA";
            log.info("Apelación RECHAZADA para producto ID={} por moderador ID={}", idProducto, idModerador);
        }

        productRepository.save(producto);

        log.info("Apelación procesada: {} para producto ID={}", resultado, idProducto);

        return convertToApelacionResponseDTO(producto, estadoAnterior, null, resultado, razonDecision,
                LocalDateTime.now());
    }

    /**
     * Obtener productos en estado APELADO (apelaciones pendientes)
     */
    @Transactional(readOnly = true)
    public Page<ApelacionResponseDTO> getApelacionesPendientes(Pageable pageable) {
        Page<Producto> productos = productRepository.findByEstado(EstadoProducto.APELADO, pageable);
        return productos.map(p -> convertToApelacionResponseDTO(p, null, null, "PENDIENTE", null, null));
    }

    /**
     * Obtener historial de apelaciones de un vendedor
     */
    @Transactional(readOnly = true)
    public List<ApelacionResponseDTO> getHistorialApelaciones(Long idVendedor) {
        // Buscar productos del vendedor que han estado en estado APELADO
        List<Producto> productos = productRepository.findByVendedorIdUsuario(idVendedor);

        return productos.stream()
                .filter(p -> p.getEstado() == EstadoProducto.APELADO ||
                        p.getEstado() == EstadoProducto.ACTIVO ||
                        p.getEstado() == EstadoProducto.PROHIBIDO)
                .map(p -> convertToApelacionResponseDTO(p, null, null,
                        determinarEstadoApelacion(p), null, null))
                .collect(Collectors.toList());
    }

    /**
     * Verificar si un producto puede ser apelado
     */
    @Transactional(readOnly = true)
    public boolean puedeSerApelado(Long idProducto, Long idVendedor) {
        Producto producto = productRepository.findById(idProducto)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        return producto.getEstado() == EstadoProducto.PROHIBIDO &&
                producto.getVendedor().getIdUsuario().equals(idVendedor);
    }

    // === MÉTODOS PRIVADOS ===

    private String determinarEstadoApelacion(Producto producto) {
        switch (producto.getEstado()) {
            case APELADO:
                return "PENDIENTE";
            case ACTIVO:
                return "APROBADA";
            case PROHIBIDO:
                return "RECHAZADA";
            default:
                return "NO_APLICA";
        }
    }

    private ApelacionResponseDTO convertToApelacionResponseDTO(Producto producto, EstadoProducto estadoAnterior,
            ApelacionRequestDTO request, String resultado,
            String razonDecision, LocalDateTime fechaDecision) {
        ApelacionResponseDTO dto = new ApelacionResponseDTO();

        dto.setIdProducto(producto.getIdProducto());
        dto.setNombreProducto(producto.getNombre());
        dto.setDescripcionProducto(producto.getDescripcion());
        dto.setEstadoProducto(producto.getEstado().name());
        dto.setEstadoAnterior(estadoAnterior != null ? estadoAnterior.name() : null);

        if (producto.getVendedor() != null) {
            dto.setIdVendedor(producto.getVendedor().getIdUsuario());
            dto.setNombreVendedor(producto.getVendedor().getNombre() + " " +
                    producto.getVendedor().getApellido());
        }

        if (request != null) {
            dto.setJustificacion(request.getJustificacion());
            dto.setComentariosAdicionales(request.getComentariosAdicionales());
            dto.setFechaApelacion(LocalDateTime.now());
        }

        dto.setResultado(resultado);
        dto.setRazonDecision(razonDecision);
        dto.setFechaDecision(fechaDecision);

        return dto;
    }
}