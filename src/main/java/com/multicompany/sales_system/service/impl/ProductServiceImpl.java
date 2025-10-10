package com.multicompany.sales_system.service.impl;

import com.multicompany.sales_system.dto.photo.PhotoResponseDTO;
import com.multicompany.sales_system.dto.product.ProductRequestDTO;
import com.multicompany.sales_system.dto.product.ProductResponseDTO;
import com.multicompany.sales_system.model.Producto;
import com.multicompany.sales_system.model.Usuario;
import com.multicompany.sales_system.model.enums.TipoProducto;
import com.multicompany.sales_system.repository.ProductRepository;
import com.multicompany.sales_system.repository.UsuarioRepository;
import com.multicompany.sales_system.service.ProductService;
import com.multicompany.sales_system.service.DetectorService;
import com.multicompany.sales_system.service.IncidenciaService;
import com.multicompany.sales_system.model.enums.EstadoProducto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final UsuarioRepository usuarioRepository; // Agregado
    private final DetectorService detectorService; // Agregado
    private final IncidenciaService incidenciaService; // Agregado

    @Override
    public ProductResponseDTO createProduct(ProductRequestDTO productRequestDTO) {
        // Buscar el vendedor real en la base de datos
        Usuario vendedor = usuarioRepository.findById(productRequestDTO.getIdVendedor())
                .orElseThrow(() -> new RuntimeException(
                        "Vendedor no encontrado con ID: " + productRequestDTO.getIdVendedor()));

        Producto producto = new Producto();
        producto.setCodigo(productRequestDTO.getCodigo());
        producto.setNombre(productRequestDTO.getNombre());
        producto.setDescripcion(productRequestDTO.getDescripcion());
        producto.setPrecio(productRequestDTO.getPrecio());
        producto.setUbicacion(productRequestDTO.getUbicacion());
        producto.setDisponibilidad(productRequestDTO.getDisponibilidad());
        producto.setTipo(TipoProducto.valueOf(productRequestDTO.getTipo().toUpperCase()));
        producto.setFechaPublicacion(LocalDateTime.now());
        producto.setVendedor(vendedor);

        // Guardar el producto primero
        Producto savedProduct = productRepository.save(producto);

        // ✅ VALIDAR CONTENIDO Y DETERMINAR ESTADO
        boolean contieneProhibidas = validarYProcesarContenido(savedProduct, productRequestDTO);

        if (contieneProhibidas) {
            savedProduct.setEstado(EstadoProducto.PROHIBIDO);
            savedProduct.setDisponibilidad(false);
        } else {
            savedProduct.setEstado(EstadoProducto.ACTIVO);
        }

        // Guardar cambios de estado
        savedProduct = productRepository.save(savedProduct);

        // ✅ CREAR INCIDENCIA DESPUÉS DE GUARDAR (cuando tenemos el ID)
        if (contieneProhibidas) {
            crearIncidenciaAutomatica(savedProduct);
        }

        return convertToResponseDTO(savedProduct);
    }

    @Override
    public ProductResponseDTO updateProduct(Long id, ProductRequestDTO productRequestDTO) {
        Producto producto = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));

        producto.setCodigo(productRequestDTO.getCodigo());
        producto.setNombre(productRequestDTO.getNombre());
        producto.setDescripcion(productRequestDTO.getDescripcion());
        producto.setPrecio(productRequestDTO.getPrecio());
        producto.setUbicacion(productRequestDTO.getUbicacion());
        producto.setDisponibilidad(productRequestDTO.getDisponibilidad());
        producto.setTipo(TipoProducto.valueOf(productRequestDTO.getTipo().toUpperCase()));

        Producto updatedProduct = productRepository.save(producto);

        // ✅ VALIDAR CONTENIDO Y ACTUALIZAR ESTADO SI ES NECESARIO
        EstadoProducto estadoAnterior = updatedProduct.getEstado();
        boolean contieneProhibidas = validarYProcesarContenido(updatedProduct, productRequestDTO);

        if (contieneProhibidas) {
            updatedProduct.setEstado(EstadoProducto.PROHIBIDO);
            updatedProduct.setDisponibilidad(false);
        } else if (estadoAnterior == EstadoProducto.PROHIBIDO) {
            // Si antes estaba prohibido y ahora está limpio, reactivar
            updatedProduct.setEstado(EstadoProducto.ACTIVO);
            updatedProduct.setDisponibilidad(true);
        }

        // Guardar cambios de estado
        updatedProduct = productRepository.save(updatedProduct);

        return convertToResponseDTO(updatedProduct);
    }

    // ✅ TODOS LOS DEMÁS MÉTODOS PERMANECEN EXACTAMENTE IGUAL
    @Override
    @Transactional(readOnly = true)
    public ProductResponseDTO getProductById(Long id) {
        Producto producto = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));
        return convertToResponseDTO(producto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(this::convertToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> getProductsByVendedor(Long vendedorId) {
        List<Producto> productos = productRepository.findByVendedorIdUsuario(vendedorId);
        return productos.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Producto no encontrado con ID: " + id);
        }
        productRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> searchProducts(String searchTerm) {
        List<Producto> productos = productRepository.findByNombreContainingIgnoreCaseOrDescripcionContainingIgnoreCase(
                searchTerm, searchTerm);
        return productos.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    // IMPLEMENTACIÓN DE NUEVOS MÉTODOS CON FILTROS (PERMANECEN IGUAL)

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> getProductsByPriceRange(Double minPrice, Double maxPrice, Pageable pageable) {
        Page<Producto> productos = productRepository.findByPrecioBetween(minPrice, maxPrice, pageable);
        return productos.map(this::convertToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> getProductsByMinPrice(Double minPrice, Pageable pageable) {
        Page<Producto> productos = productRepository.findByPrecioGreaterThanEqual(minPrice, pageable);
        return productos.map(this::convertToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> getProductsByMaxPrice(Double maxPrice, Pageable pageable) {
        Page<Producto> productos = productRepository.findByPrecioLessThanEqual(maxPrice, pageable);
        return productos.map(this::convertToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> getProductsByTipo(TipoProducto tipo, Pageable pageable) {
        Page<Producto> productos = productRepository.findByTipoAndEstadoActivo(tipo, pageable);
        return productos.map(this::convertToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> getProductsByUbicacion(String ubicacion, Pageable pageable) {
        String ubicacionPattern = "%" + ubicacion + "%";
        Page<Producto> productos = productRepository.findByUbicacionContaining(ubicacionPattern, pageable);
        return productos.map(this::convertToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> searchProductsWithPagination(String searchTerm, Pageable pageable) {
        String searchPattern = "%" + searchTerm + "%";
        Page<Producto> productos = productRepository.findByNombreOrDescripcionContaining(searchPattern, pageable);
        return productos.map(this::convertToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> getProductsWithFilters(Double minPrice, Double maxPrice, TipoProducto tipo,
            String searchTerm, String ubicacion, Boolean disponibilidad,
            Pageable pageable) {
        String searchPattern = searchTerm != null ? "%" + searchTerm + "%" : null;
        String ubicacionPattern = ubicacion != null ? "%" + ubicacion + "%" : null;

        Page<Producto> productos = productRepository.findWithFilters(minPrice, maxPrice, tipo, searchPattern,
                ubicacionPattern, disponibilidad, pageable);
        return productos.map(this::convertToResponseDTO);
    }

    private ProductResponseDTO convertToResponseDTO(Producto producto) {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setIdProducto(producto.getIdProducto());
        dto.setCodigo(producto.getCodigo());
        dto.setNombre(producto.getNombre());
        dto.setDescripcion(producto.getDescripcion());
        dto.setPrecio(producto.getPrecio());
        dto.setUbicacion(producto.getUbicacion());
        dto.setDisponibilidad(producto.getDisponibilidad());
        dto.setTipo(producto.getTipo().name());
        dto.setEstado(producto.getEstado().name());
        dto.setFechaPublicacion(producto.getFechaPublicacion());

        if (producto.getVendedor() != null) {
            dto.setIdVendedor(producto.getVendedor().getIdUsuario());
            dto.setNombreVendedor(producto.getVendedor().getNombre());
        }

        if (producto.getFotos() != null) {
            List<PhotoResponseDTO> fotosDTO = producto.getFotos().stream()
                    .map(foto -> new PhotoResponseDTO(foto.getIdFoto(), foto.getUrl(), producto.getIdProducto()))
                    .collect(Collectors.toList());
            dto.setFotos(fotosDTO);
        }

        return dto;
    }

    // ✅ MÉTODOS AUXILIARES PARA VALIDACIÓN DE CONTENIDO PROHIBIDO

    private boolean validarYProcesarContenido(Producto producto, ProductRequestDTO requestDTO) {
        // Combinar todo el texto del producto para análisis
        String contenidoCompleto = (requestDTO.getNombre() != null ? requestDTO.getNombre() : "") + " " +
                (requestDTO.getDescripcion() != null ? requestDTO.getDescripcion() : "");

        // Usar DetectorService para verificar palabras prohibidas
        return detectorService.containsProhibited(contenidoCompleto);
    }

    private void crearIncidenciaAutomatica(Producto producto) {
        try {
            // Usar el servicio para crear la incidencia automáticamente
            incidenciaService.crearPorDeteccion(
                    producto.getIdProducto(),
                    producto.getVendedor().getIdUsuario(),
                    "Contenido prohibido detectado automáticamente",
                    "El sistema detectó automáticamente contenido prohibido en el producto: " + producto.getNombre());
        } catch (Exception e) {
            // Log error pero no fallar la creación del producto
            System.err.println("Error al crear incidencia automática: " + e.getMessage());
        }
    }
}