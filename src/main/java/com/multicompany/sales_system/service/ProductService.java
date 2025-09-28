package com.multicompany.sales_system.service;

import com.multicompany.sales_system.dto.product.ProductRequestDTO;
import com.multicompany.sales_system.dto.product.ProductResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ProductService {

    ProductResponseDTO createProduct(ProductRequestDTO productRequestDTO);

    ProductResponseDTO getProductById(Long id);

    Page<ProductResponseDTO> getAllProducts(Pageable pageable);

    List<ProductResponseDTO> getProductsByVendedor(Long vendedorId);

    ProductResponseDTO updateProduct(Long id, ProductRequestDTO productRequestDTO);

    void deleteProduct(Long id);

    List<ProductResponseDTO> searchProducts(String searchTerm);
}
