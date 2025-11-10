package com.multicompany.sales_system.controller;

import com.multicompany.sales_system.dto.configuracion.DiasExpiracionRequestDTO;
import com.multicompany.sales_system.dto.configuracion.DiasExpiracionResponseDTO;
import com.multicompany.sales_system.service.ConfiguracionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador para gestionar configuraciones del sistema.
 * La configuración CORS se maneja globalmente desde CorsConfig.
 */
@RestController
@RequestMapping("/api/configuracion")
@RequiredArgsConstructor
public class ConfiguracionController {

    private final ConfiguracionService configuracionService;

    /**
     * Obtiene la cantidad de días configurados para la expiración de productos.
     * 
     * GET /api/configuracion/dias-expiracion
     * 
     * @return DiasExpiracionResponseDTO con los días actuales
     */
    @GetMapping("/dias-expiracion")
    public ResponseEntity<DiasExpiracionResponseDTO> getDiasExpiracion() {
        int dias = configuracionService.getDiasExpiracion();
        DiasExpiracionResponseDTO response = new DiasExpiracionResponseDTO(
            dias,
            "Días de expiración de productos"
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Actualiza la cantidad de días para la expiración de productos.
     * 
     * PUT /api/configuracion/dias-expiracion
     * Body: { "dias": 45 }
     * 
     * @param request DTO con los nuevos días (debe ser >= 1)
     * @return DiasExpiracionResponseDTO con confirmación
     */
    @PutMapping("/dias-expiracion")
    public ResponseEntity<DiasExpiracionResponseDTO> updateDiasExpiracion(
            @Valid @RequestBody DiasExpiracionRequestDTO request) {
        
        int diasActualizados = configuracionService.updateDiasExpiracion(request.getDias());
        
        DiasExpiracionResponseDTO response = new DiasExpiracionResponseDTO(
            diasActualizados,
            "Días de expiración actualizados exitosamente"
        );
        
        return ResponseEntity.ok(response);
    }

}
