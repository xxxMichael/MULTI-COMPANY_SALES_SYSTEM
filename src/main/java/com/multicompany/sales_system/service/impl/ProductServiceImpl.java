package com.multicompany.sales_system.service.impl;

import com.multicompany.sales_system.dto.photo.PhotoResponseDTO;
import com.multicompany.sales_system.dto.product.ProductRequestDTO;
import com.multicompany.sales_system.dto.product.ProductResponseDTO;
import com.multicompany.sales_system.model.Producto;
import com.multicompany.sales_system.model.Usuario;
import com.multicompany.sales_system.model.Categoria;
import com.multicompany.sales_system.model.FotoProducto;
import com.multicompany.sales_system.model.enums.TipoProducto;
import com.multicompany.sales_system.repository.ProductRepository;
import com.multicompany.sales_system.repository.UsuarioRepository;
import com.multicompany.sales_system.repository.CategoriaRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final UsuarioRepository usuarioRepository; // Agregado
    private final CategoriaRepository categoriaRepository; // Agregado
    private final DetectorService detectorService; // Agregado
    private final IncidenciaService incidenciaService; // Agregado

    @Override
    public ProductResponseDTO createProduct(ProductRequestDTO productRequestDTO) {
        // Buscar el vendedor real en la base de datos
        Usuario vendedor = usuarioRepository.findById(productRequestDTO.getIdVendedor())
                .orElseThrow(() -> new RuntimeException(
                        "Vendedor no encontrado con ID: " + productRequestDTO.getIdVendedor()));

        Categoria categoria = categoriaRepository.findById(productRequestDTO.getIdCategoria())
                .orElseThrow(() -> new RuntimeException(
                        "Categoría no encontrada con ID: " + productRequestDTO.getIdCategoria()));

        // Validar que la categoría esté activa
        if (!categoria.getActivo()) {
            throw new RuntimeException(
                    "La categoría '" + categoria.getNombre()
                            + "' está desactivada. No se pueden crear productos en categorías inactivas.");
        }

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
        producto.setCategoria(categoria);

        // Guardar el producto primero
        Producto savedProduct = productRepository.save(producto);

        boolean contieneProhibidas = validarYProcesarContenido(savedProduct, productRequestDTO);

        if (contieneProhibidas) {
            savedProduct.setEstado(EstadoProducto.PROHIBIDO);
            savedProduct.setDisponibilidad(false);
        } else {
            savedProduct.setEstado(EstadoProducto.ACTIVO);
        }

        // Guardar cambs de estado
        savedProduct = productRepository.save(savedProduct);

        if (contieneProhibidas) {
            crearIncidenciaAutomatica(savedProduct);
        }

        return convertToResponseDTO(savedProduct);
    }

    @Override
    public ProductResponseDTO updateProduct(Long id, ProductRequestDTO productRequestDTO) {
        Producto producto = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));

        if (productRequestDTO.getIdCategoria() != null) {
            Categoria categoria = categoriaRepository.findById(productRequestDTO.getIdCategoria())
                    .orElseThrow(() -> new RuntimeException(
                            "Categoría no encontrada con ID: " + productRequestDTO.getIdCategoria()));

            if (!categoria.getActivo()) {
                throw new RuntimeException(
                        "La categoría '" + categoria.getNombre()
                                + "' está desactivada. No se pueden asignar productos a categorías inactivas.");
            }

            producto.setCategoria(categoria);
        }

        producto.setCodigo(productRequestDTO.getCodigo());
        producto.setNombre(productRequestDTO.getNombre());
        producto.setDescripcion(productRequestDTO.getDescripcion());
        producto.setPrecio(productRequestDTO.getPrecio());
        producto.setUbicacion(productRequestDTO.getUbicacion());
        producto.setDisponibilidad(productRequestDTO.getDisponibilidad());
        producto.setTipo(TipoProducto.valueOf(productRequestDTO.getTipo().toUpperCase()));

        Producto updatedProduct = productRepository.save(producto);

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

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDTO getProductById(Long id) {
        // ✅ Usar el método con JOIN FETCH para cargar fotos
        Producto producto = productRepository.findByIdWithFotos(id)
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
    public void deleteProductLogico(Long id) {
        Producto producto = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));
        producto.setEstado(EstadoProducto.ELIMINADO);
        productRepository.save(producto);
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

        if (producto.getCategoria() != null) {
            dto.setIdCategoria(producto.getCategoria().getIdCategoria());
            dto.setNombreCategoria(producto.getCategoria().getNombre());
        }

        // ✅ Forzar la inicialización de fotos dentro de la transacción
        List<FotoProducto> fotos = producto.getFotos();
        if (fotos != null && !fotos.isEmpty()) {
            // Forzar la carga accediendo al tamaño
            fotos.size();
            List<PhotoResponseDTO> fotosDTO = fotos.stream()
                    .map(foto -> new PhotoResponseDTO(foto.getIdFoto(), foto.getUrl(), producto.getIdProducto()))
                    .collect(Collectors.toList());
            dto.setFotos(fotosDTO);
        } else {
            dto.setFotos(new ArrayList<>()); // Lista vacía en lugar de null
        }

        return dto;
    }

    private boolean validarYProcesarContenido(Producto producto, ProductRequestDTO requestDTO) {
        // Combinar todo el texto del producto para análisis
        String contenidoCompleto = (requestDTO.getNombre() != null ? requestDTO.getNombre() : "") + " " +
                (requestDTO.getDescripcion() != null ? requestDTO.getDescripcion() : "");

        // Usar DetectorService para verificar palabras prohibidas
        return detectorService.containsProhibited(contenidoCompleto);
    }

    private void crearIncidenciaAutomatica(Producto producto) {
        try {
            incidenciaService.crearPorDeteccion(
                    producto.getIdProducto(),
                    producto.getVendedor().getIdUsuario(),
                    "Contenido prohibido detectado automáticamente",
                    "El sistema detectó automáticamente contenido prohibido en el producto: " + producto.getNombre());
        } catch (Exception e) {
            System.err.println("Error al crear incidencia automática: " + e.getMessage());
        }
    }
}