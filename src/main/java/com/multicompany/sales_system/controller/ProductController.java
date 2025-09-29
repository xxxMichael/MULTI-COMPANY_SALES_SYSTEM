package com.multicompany.sales_system.controller;

import com.multicompany.sales_system.dto.product.ProductRequestDTO;
import com.multicompany.sales_system.dto.product.ProductResponseDTO;
import com.multicompany.sales_system.model.enums.TipoProducto;
import com.multicompany.sales_system.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponseDTO> createProduct(@Valid @RequestBody ProductRequestDTO productRequestDTO) {
        ProductResponseDTO createdProduct = productService.createProduct(productRequestDTO);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable Long id) {
        ProductResponseDTO product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @GetMapping
    public ResponseEntity<Page<ProductResponseDTO>> getAllProducts(Pageable pageable) {
        Page<ProductResponseDTO> products = productService.getAllProducts(pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/vendedor/{vendedorId}")
    public ResponseEntity<List<ProductResponseDTO>> getProductsByVendedor(@PathVariable Long vendedorId) {
        List<ProductResponseDTO> products = productService.getProductsByVendedor(vendedorId);
        return ResponseEntity.ok(products);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequestDTO productRequestDTO) {
        ProductResponseDTO updatedProduct = productService.updateProduct(id, productRequestDTO);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductResponseDTO>> searchProducts(@RequestParam String searchTerm) {
        List<ProductResponseDTO> products = productService.searchProducts(searchTerm);
        return ResponseEntity.ok(products);
    }

    // ENDPOINTS DE FILTRADO AVANZADO

    // Endpoint para filtrar productos con múltiples criterios
    @GetMapping("/filter")
    public ResponseEntity<Page<ProductResponseDTO>> getProductsWithFilters(
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) String ubicacion,
            @RequestParam(required = false) Boolean disponibilidad,
            Pageable pageable) {

        // Convertir string a enum si es necesario
        TipoProducto tipoEnum = null;
        if (tipo != null && !tipo.isEmpty()) {
            try {
                tipoEnum = TipoProducto.valueOf(tipo.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(null);
            }
        }

        Page<ProductResponseDTO> products = productService.getProductsWithFilters(
                minPrice, maxPrice, tipoEnum, searchTerm, ubicacion, disponibilidad, pageable);
        return ResponseEntity.ok(products);
    }

    // Endpoint específico para filtrar por rango de precio
    @GetMapping("/filter/price")
    public ResponseEntity<Page<ProductResponseDTO>> getProductsByPriceRange(
            @RequestParam Double minPrice,
            @RequestParam Double maxPrice,
            Pageable pageable) {
        Page<ProductResponseDTO> products = productService.getProductsByPriceRange(minPrice, maxPrice, pageable);
        return ResponseEntity.ok(products);
    }

    // Endpoint específico para filtrar por tipo (PRODUCTO o SERVICIO)
    @GetMapping("/filter/type")
    public ResponseEntity<Page<ProductResponseDTO>> getProductsByType(
            @RequestParam String tipo,
            Pageable pageable) {
        try {
            TipoProducto tipoEnum = TipoProducto.valueOf(tipo.toUpperCase());
            Page<ProductResponseDTO> products = productService.getProductsByTipo(tipoEnum, pageable);
            return ResponseEntity.ok(products);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Endpoint específico para filtrar por ubicación
    @GetMapping("/filter/location")
    public ResponseEntity<Page<ProductResponseDTO>> getProductsByLocation(
            @RequestParam String ubicacion,
            Pageable pageable) {
        Page<ProductResponseDTO> products = productService.getProductsByUbicacion(ubicacion, pageable);
        return ResponseEntity.ok(products);
    }

    // Endpoint para búsqueda con paginación
    @GetMapping("/search/paginated")
    public ResponseEntity<Page<ProductResponseDTO>> searchProductsWithPagination(
            @RequestParam String searchTerm,
            Pageable pageable) {
        Page<ProductResponseDTO> products = productService.searchProductsWithPagination(searchTerm, pageable);
        return ResponseEntity.ok(products);
    }

    // Endpoints adicionales para filtros de precio individuales
    @GetMapping("/filter/min-price")
    public ResponseEntity<Page<ProductResponseDTO>> getProductsByMinPrice(
            @RequestParam Double minPrice,
            Pageable pageable) {
        Page<ProductResponseDTO> products = productService.getProductsByMinPrice(minPrice, pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/filter/max-price")
    public ResponseEntity<Page<ProductResponseDTO>> getProductsByMaxPrice(
            @RequestParam Double maxPrice,
            Pageable pageable) {
        Page<ProductResponseDTO> products = productService.getProductsByMaxPrice(maxPrice, pageable);
        return ResponseEntity.ok(products);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}