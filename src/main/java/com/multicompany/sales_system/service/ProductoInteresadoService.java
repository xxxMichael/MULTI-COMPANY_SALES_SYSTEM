package com.multicompany.sales_system.service;

import com.multicompany.sales_system.dto.interes.ProductoInteresadoResponseDTO;
import com.multicompany.sales_system.model.Producto;
import com.multicompany.sales_system.model.ProductoInteresado;
import com.multicompany.sales_system.model.Usuario;
import com.multicompany.sales_system.repository.ProductRepository;
import com.multicompany.sales_system.repository.ProductoInteresadoRepository;
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
 * Servicio para gestionar la funcionalidad "Me interesa"
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductoInteresadoService {

    private final ProductoInteresadoRepository productoInteresadoRepository;
    private final ProductRepository productRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * Agregar un producto a la lista de intereses de un usuario
     */
    @Transactional
    public ProductoInteresadoResponseDTO agregarInteres(Long idUsuario, Long idProducto) {
        // Verificar que el usuario existe
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + idUsuario));

        // Verificar que el producto existe
        Producto producto = productRepository.findById(idProducto)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + idProducto));

        // Verificar que el usuario no sea el vendedor del producto
        if (producto.getVendedor().getIdUsuario().equals(idUsuario)) {
            throw new RuntimeException("No puedes marcar como interesante tu propio producto");
        }

        // Verificar que no existe ya el interés
        if (productoInteresadoRepository.existsByUsuarioIdUsuarioAndProductoIdProducto(idUsuario, idProducto)) {
            throw new RuntimeException("Ya tienes este producto marcado como interesante");
        }

        // Crear el interés
        ProductoInteresado interes = new ProductoInteresado();
        interes.setUsuario(usuario);
        interes.setProducto(producto);
        interes.setFechaInteres(LocalDateTime.now());

        ProductoInteresado savedInteres = productoInteresadoRepository.save(interes);

        log.info("Usuario {} agregó interés en producto {}", idUsuario, idProducto);

        return convertToResponseDTO(savedInteres);
    }

    /**
     * Retirar un producto de la lista de intereses de un usuario
     */
    @Transactional
    public void retirarInteres(Long idUsuario, Long idProducto) {
        // Verificar que el interés existe
        if (!productoInteresadoRepository.existsByUsuarioIdUsuarioAndProductoIdProducto(idUsuario, idProducto)) {
            throw new RuntimeException("No tienes este producto marcado como interesante");
        }

        productoInteresadoRepository.deleteByUsuarioIdUsuarioAndProductoIdProducto(idUsuario, idProducto);

        log.info("Usuario {} retiró interés en producto {}", idUsuario, idProducto);
    }

    /**
     * Verificar si un usuario tiene interés en un producto
     */
    @Transactional(readOnly = true)
    public boolean tieneInteres(Long idUsuario, Long idProducto) {
        return productoInteresadoRepository.existsByUsuarioIdUsuarioAndProductoIdProducto(idUsuario, idProducto);
    }

    /**
     * Obtener productos de interés de un usuario con paginación
     */
    @Transactional(readOnly = true)
    public Page<ProductoInteresadoResponseDTO> getProductosDeInteres(Long idUsuario, Pageable pageable) {
        Page<ProductoInteresado> intereses = productoInteresadoRepository.findByUsuarioIdUsuario(idUsuario, pageable);
        return intereses.map(this::convertToResponseDTO);
    }

    /**
     * Obtener productos de interés de un usuario sin paginación
     */
    @Transactional(readOnly = true)
    public List<ProductoInteresadoResponseDTO> getProductosDeInteresList(Long idUsuario) {
        List<ProductoInteresado> intereses = productoInteresadoRepository
                .findByUsuarioIdUsuarioOrderByFechaInteresDesc(idUsuario);
        return intereses.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Contar productos de interés de un usuario
     */
    @Transactional(readOnly = true)
    public long contarProductosDeInteres(Long idUsuario) {
        return productoInteresadoRepository.countByUsuarioIdUsuario(idUsuario);
    }

    /**
     * Obtener usuarios interesados en un producto específico
     */
    @Transactional(readOnly = true)
    public Page<ProductoInteresadoResponseDTO> getUsuariosInteresados(Long idProducto, Pageable pageable) {
        Page<ProductoInteresado> intereses = productoInteresadoRepository.findByProductoIdProducto(idProducto,
                pageable);
        return intereses.map(this::convertToResponseDTO);
    }

    /**
     * Contar usuarios interesados en un producto
     */
    @Transactional(readOnly = true)
    public long contarUsuariosInteresados(Long idProducto) {
        return productoInteresadoRepository.countByProductoIdProducto(idProducto);
    }

    /**
     * Obtener productos más populares (con más intereses)
     */
    @Transactional(readOnly = true)
    public Page<Object[]> getProductosMasPopulares(Pageable pageable) {
        return productoInteresadoRepository.findProductosMasPopulares(pageable);
    }

    // === MÉTODOS PRIVADOS ===

    private ProductoInteresadoResponseDTO convertToResponseDTO(ProductoInteresado interes) {
        ProductoInteresadoResponseDTO dto = new ProductoInteresadoResponseDTO();
        dto.setIdInteresado(interes.getIdInteresado());
        dto.setFechaInteres(interes.getFechaInteres());

        // Información del usuario
        if (interes.getUsuario() != null) {
            dto.setIdUsuario(interes.getUsuario().getIdUsuario());
            dto.setNombreUsuario(interes.getUsuario().getNombre() + " " +
                    interes.getUsuario().getApellido());
        }

        // Información del producto
        if (interes.getProducto() != null) {
            Producto producto = interes.getProducto();
            dto.setIdProducto(producto.getIdProducto());
            dto.setNombreProducto(producto.getNombre());
            dto.setDescripcionProducto(producto.getDescripcion());
            dto.setPrecioProducto(producto.getPrecio());
            dto.setTipoProducto(producto.getTipo().name());
            dto.setEstadoProducto(producto.getEstado().name());
            dto.setFechaPublicacionProducto(producto.getFechaPublicacion());

            // Información del vendedor
            if (producto.getVendedor() != null) {
                dto.setIdVendedor(producto.getVendedor().getIdUsuario());
                dto.setNombreVendedor(producto.getVendedor().getNombre() + " " +
                        producto.getVendedor().getApellido());
            }
        }

        return dto;
    }
}