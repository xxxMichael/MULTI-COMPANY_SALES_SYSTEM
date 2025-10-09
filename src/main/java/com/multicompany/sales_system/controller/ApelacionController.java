package com.multicompany.sales_system.controller;

import com.multicompany.sales_system.dto.apelacion.ApelacionRequestDTO;
import com.multicompany.sales_system.dto.apelacion.ApelacionResponseDTO;
import com.multicompany.sales_system.service.ApelacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador para gestionar el módulo de apelaciones de productos rechazados
 */
@RestController
@RequestMapping("/api/apelaciones")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ApelacionController {

    private final ApelacionService apelacionService;

    /**
     * Crear una nueva apelación para un producto prohibido
     * POST /api/apelaciones
     */
    @PostMapping
    public ResponseEntity<ApelacionResponseDTO> crearApelacion(
            @Valid @RequestBody ApelacionRequestDTO apelacionDTO) {
        ApelacionResponseDTO respuesta = apelacionService.crearApelacion(apelacionDTO);
        return new ResponseEntity<>(respuesta, HttpStatus.CREATED);
    }

    /**
     * Procesar una apelación (aprobar o rechazar) - Solo para moderadores
     * PUT /api/apelaciones/producto/{idProducto}/procesar
     */
    @PutMapping("/producto/{idProducto}/procesar")
    public ResponseEntity<ApelacionResponseDTO> procesarApelacion(
            @PathVariable Long idProducto,
            @RequestParam Long idModerador,
            @RequestParam boolean aprobar,
            @RequestParam String razonDecision) {

        ApelacionResponseDTO respuesta = apelacionService.procesarApelacion(
                idProducto, idModerador, aprobar, razonDecision);
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Aprobar una apelación - Endpoint específico para aprobar
     * PUT /api/apelaciones/producto/{idProducto}/aprobar
     */
    @PutMapping("/producto/{idProducto}/aprobar")
    public ResponseEntity<ApelacionResponseDTO> aprobarApelacion(
            @PathVariable Long idProducto,
            @RequestParam Long idModerador,
            @RequestParam(required = false, defaultValue = "Apelación aprobada por moderador") String razon) {

        ApelacionResponseDTO respuesta = apelacionService.procesarApelacion(
                idProducto, idModerador, true, razon);
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Rechazar una apelación - Endpoint específico para rechazar
     * PUT /api/apelaciones/producto/{idProducto}/rechazar
     */
    @PutMapping("/producto/{idProducto}/rechazar")
    public ResponseEntity<ApelacionResponseDTO> rechazarApelacion(
            @PathVariable Long idProducto,
            @RequestParam Long idModerador,
            @RequestParam String razon) {

        ApelacionResponseDTO respuesta = apelacionService.procesarApelacion(
                idProducto, idModerador, false, razon);
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Obtener apelaciones pendientes - Para moderadores
     * GET /api/apelaciones/pendientes
     */
    @GetMapping("/pendientes")
    public ResponseEntity<Page<ApelacionResponseDTO>> getApelacionesPendientes(Pageable pageable) {
        Page<ApelacionResponseDTO> apelaciones = apelacionService.getApelacionesPendientes(pageable);
        return ResponseEntity.ok(apelaciones);
    }

    /**
     * Obtener historial de apelaciones de un vendedor
     * GET /api/apelaciones/vendedor/{idVendedor}/historial
     */
    @GetMapping("/vendedor/{idVendedor}/historial")
    public ResponseEntity<List<ApelacionResponseDTO>> getHistorialApelaciones(
            @PathVariable Long idVendedor) {
        List<ApelacionResponseDTO> historial = apelacionService.getHistorialApelaciones(idVendedor);
        return ResponseEntity.ok(historial);
    }

    /**
     * Verificar si un producto puede ser apelado
     * GET /api/apelaciones/producto/{idProducto}/puede-apelar
     */
    @GetMapping("/producto/{idProducto}/puede-apelar")
    public ResponseEntity<Map<String, Boolean>> verificarPuedeApelar(
            @PathVariable Long idProducto,
            @RequestParam Long idVendedor) {
        boolean puedeApelar = apelacionService.puedeSerApelado(idProducto, idVendedor);
        return ResponseEntity.ok(Map.of("puedeApelar", puedeApelar));
    }

    /**
     * Endpoint combinado para crear apelación directamente desde producto y
     * vendedor
     * POST /api/apelaciones/producto/{idProducto}/vendedor/{idVendedor}
     */
    @PostMapping("/producto/{idProducto}/vendedor/{idVendedor}")
    public ResponseEntity<ApelacionResponseDTO> crearApelacionDirecta(
            @PathVariable Long idProducto,
            @PathVariable Long idVendedor,
            @RequestParam String justificacion,
            @RequestParam(required = false) String comentariosAdicionales) {

        ApelacionRequestDTO apelacionDTO = new ApelacionRequestDTO();
        apelacionDTO.setIdProducto(idProducto);
        apelacionDTO.setIdVendedor(idVendedor);
        apelacionDTO.setJustificacion(justificacion);
        apelacionDTO.setComentariosAdicionales(comentariosAdicionales);

        ApelacionResponseDTO respuesta = apelacionService.crearApelacion(apelacionDTO);
        return new ResponseEntity<>(respuesta, HttpStatus.CREATED);
    }

    /**
     * Obtener estadísticas de apelaciones - Para dashboard de moderadores
     * GET /api/apelaciones/estadisticas
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> getEstadisticasApelaciones() {
        // En una implementación completa, esto obtendría estadísticas reales
        Map<String, Object> estadisticas = Map.of(
                "pendientes", "Se requiere implementación con queries específicas",
                "aprobadas_mes", "Se requiere implementación con queries específicas",
                "rechazadas_mes", "Se requiere implementación con queries específicas",
                "tiempo_promedio_resolucion", "Se requiere implementación con análisis temporal");
        return ResponseEntity.ok(estadisticas);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}