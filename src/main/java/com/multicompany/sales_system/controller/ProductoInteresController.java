package com.multicompany.sales_system.controller;

import com.multicompany.sales_system.dto.interes.ProductoInteresadoResponseDTO;
import com.multicompany.sales_system.service.ProductoInteresadoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador para gestionar la funcionalidad "Me interesa"
 */
@RestController
@RequestMapping("/api/productos-interes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProductoInteresController {

    private final ProductoInteresadoService productoInteresadoService;

    /**
     * Agregar un producto a la lista de intereses
     * POST /api/productos-interes/usuario/{idUsuario}/producto/{idProducto}
     */
    @PostMapping("/usuario/{idUsuario}/producto/{idProducto}")
    public ResponseEntity<ProductoInteresadoResponseDTO> agregarInteres(
            @PathVariable Long idUsuario,
            @PathVariable Long idProducto) {
        ProductoInteresadoResponseDTO respuesta = productoInteresadoService.agregarInteres(idUsuario, idProducto);
        return new ResponseEntity<>(respuesta, HttpStatus.CREATED);
    }

    /**
     * Retirar un producto de la lista de intereses
     * DELETE /api/productos-interes/usuario/{idUsuario}/producto/{idProducto}
     */
    @DeleteMapping("/usuario/{idUsuario}/producto/{idProducto}")
    public ResponseEntity<Map<String, String>> retirarInteres(
            @PathVariable Long idUsuario,
            @PathVariable Long idProducto) {
        productoInteresadoService.retirarInteres(idUsuario, idProducto);
        return ResponseEntity.ok(Map.of("mensaje", "Interés retirado exitosamente"));
    }

    /**
     * Verificar si un usuario tiene interés en un producto
     * GET /api/productos-interes/usuario/{idUsuario}/producto/{idProducto}/existe
     */
    @GetMapping("/usuario/{idUsuario}/producto/{idProducto}/existe")
    public ResponseEntity<Map<String, Boolean>> verificarInteres(
            @PathVariable Long idUsuario,
            @PathVariable Long idProducto) {
        boolean tieneInteres = productoInteresadoService.tieneInteres(idUsuario, idProducto);
        return ResponseEntity.ok(Map.of("tieneInteres", tieneInteres));
    }

    /**
     * Obtener productos de interés de un usuario con paginación
     * GET /api/productos-interes/usuario/{idUsuario}
     */
    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<Page<ProductoInteresadoResponseDTO>> getProductosDeInteres(
            @PathVariable Long idUsuario,
            Pageable pageable) {
        Page<ProductoInteresadoResponseDTO> productos = productoInteresadoService.getProductosDeInteres(idUsuario,
                pageable);
        return ResponseEntity.ok(productos);
    }

    /**
     * Obtener productos de interés de un usuario sin paginación
     * GET /api/productos-interes/usuario/{idUsuario}/lista
     */
    @GetMapping("/usuario/{idUsuario}/lista")
    public ResponseEntity<List<ProductoInteresadoResponseDTO>> getProductosDeInteresList(
            @PathVariable Long idUsuario) {
        List<ProductoInteresadoResponseDTO> productos = productoInteresadoService.getProductosDeInteresList(idUsuario);
        return ResponseEntity.ok(productos);
    }

    /**
     * Contar productos de interés de un usuario
     * GET /api/productos-interes/usuario/{idUsuario}/count
     */
    @GetMapping("/usuario/{idUsuario}/count")
    public ResponseEntity<Map<String, Long>> contarProductosDeInteres(
            @PathVariable Long idUsuario) {
        long count = productoInteresadoService.contarProductosDeInteres(idUsuario);
        return ResponseEntity.ok(Map.of("totalProductosInteres", count));
    }

    /**
     * Obtener usuarios interesados en un producto específico
     * GET /api/productos-interes/producto/{idProducto}/usuarios
     */
    @GetMapping("/producto/{idProducto}/usuarios")
    public ResponseEntity<Page<ProductoInteresadoResponseDTO>> getUsuariosInteresados(
            @PathVariable Long idProducto,
            Pageable pageable) {
        Page<ProductoInteresadoResponseDTO> usuarios = productoInteresadoService.getUsuariosInteresados(idProducto,
                pageable);
        return ResponseEntity.ok(usuarios);
    }

    /**
     * Contar usuarios interesados en un producto
     * GET /api/productos-interes/producto/{idProducto}/count
     */
    @GetMapping("/producto/{idProducto}/count")
    public ResponseEntity<Map<String, Long>> contarUsuariosInteresados(
            @PathVariable Long idProducto) {
        long count = productoInteresadoService.contarUsuariosInteresados(idProducto);
        return ResponseEntity.ok(Map.of("totalUsuariosInteresados", count));
    }

    /**
     * Obtener productos más populares (con más intereses)
     * GET /api/productos-interes/populares
     */
    @GetMapping("/populares")
    public ResponseEntity<Page<Object[]>> getProductosMasPopulares(Pageable pageable) {
        Page<Object[]> productos = productoInteresadoService.getProductosMasPopulares(pageable);
        return ResponseEntity.ok(productos);
    }

    /**
     * Alternar interés (agregar si no existe, retirar si existe)
     * PUT /api/productos-interes/usuario/{idUsuario}/producto/{idProducto}/toggle
     */
    @PutMapping("/usuario/{idUsuario}/producto/{idProducto}/toggle")
    public ResponseEntity<Map<String, Object>> toggleInteres(
            @PathVariable Long idUsuario,
            @PathVariable Long idProducto) {

        boolean tieneInteres = productoInteresadoService.tieneInteres(idUsuario, idProducto);

        if (tieneInteres) {
            productoInteresadoService.retirarInteres(idUsuario, idProducto);
            return ResponseEntity.ok(Map.of(
                    "accion", "retirado",
                    "mensaje", "Interés retirado exitosamente",
                    "tieneInteres", false));
        } else {
            ProductoInteresadoResponseDTO respuesta = productoInteresadoService.agregarInteres(idUsuario, idProducto);
            return ResponseEntity.ok(Map.of(
                    "accion", "agregado",
                    "mensaje", "Interés agregado exitosamente",
                    "tieneInteres", true,
                    "interes", respuesta));
        }
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}