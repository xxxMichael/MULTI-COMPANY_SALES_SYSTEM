package com.multicompany.sales_system.controller;

import com.multicompany.sales_system.dto.valoracion.CrearValoracionRequest;
import com.multicompany.sales_system.dto.valoracion.EstadisticasVendedorResponse;
import com.multicompany.sales_system.dto.valoracion.ValoracionResponse;
import com.multicompany.sales_system.security.JwtService;
import com.multicompany.sales_system.service.ValoracionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/valoraciones")
@RequiredArgsConstructor
@Tag(name = "Valoraciones", description = "Gestión de valoraciones de vendedores")
public class ValoracionController {

    private final ValoracionService valoracionService;
    private final JwtService jwtService;

    /**
     * Crear una nueva valoración
     * Solo usuarios autenticados pueden valorar
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('USER', 'MODERATOR', 'ADMIN')")
    @Operation(
        summary = "Crear valoración",
        description = "Permite a un usuario valorar a un vendedor. Solo se puede valorar una vez por vendedor.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> crearValoracion(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody CrearValoracionRequest request) {
        try {
            // Extraer el ID del usuario del token
            String jwt = token.replace("Bearer ", "");
            Long compradorId = jwtService.getUserId(jwt);

            ValoracionResponse response = valoracionService.crearValoracion(compradorId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al crear la valoración: " + e.getMessage()));
        }
    }

    /**
     * Obtener todas las valoraciones de un vendedor
     * Endpoint público
     */
    @GetMapping("/vendedor/{vendedorId}")
    @Operation(
        summary = "Obtener valoraciones de un vendedor",
        description = "Lista todas las valoraciones recibidas por un vendedor específico"
    )
    public ResponseEntity<?> obtenerValoracionesVendedor(
            @Parameter(description = "ID del vendedor") 
            @PathVariable Long vendedorId) {
        try {
            List<ValoracionResponse> valoraciones = valoracionService
                    .obtenerValoracionesDeVendedor(vendedorId);
            return ResponseEntity.ok(valoraciones);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Obtener estadísticas de un vendedor
     * Endpoint público
     */
    @GetMapping("/vendedor/{vendedorId}/estadisticas")
    @Operation(
        summary = "Obtener estadísticas de valoración de un vendedor",
        description = "Retorna el promedio, total y distribución de valoraciones de un vendedor"
    )
    public ResponseEntity<?> obtenerEstadisticasVendedor(
            @Parameter(description = "ID del vendedor") 
            @PathVariable Long vendedorId) {
        try {
            EstadisticasVendedorResponse stats = valoracionService
                    .obtenerEstadisticasVendedor(vendedorId);
            return ResponseEntity.ok(stats);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Obtener las últimas N valoraciones de un vendedor
     */
    @GetMapping("/vendedor/{vendedorId}/ultimas")
    @Operation(
        summary = "Obtener últimas valoraciones de un vendedor",
        description = "Retorna las últimas N valoraciones de un vendedor"
    )
    public ResponseEntity<?> obtenerUltimasValoraciones(
            @Parameter(description = "ID del vendedor") 
            @PathVariable Long vendedorId,
            @Parameter(description = "Número de valoraciones a obtener") 
            @RequestParam(defaultValue = "5") int limit) {
        try {
            List<ValoracionResponse> valoraciones = valoracionService
                    .obtenerUltimasValoraciones(vendedorId, limit);
            return ResponseEntity.ok(valoraciones);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Obtener valoraciones realizadas por el usuario autenticado
     */
    @GetMapping("/mis-valoraciones")
    @PreAuthorize("hasAnyAuthority('USER', 'MODERATOR', 'ADMIN')")
    @Operation(
        summary = "Obtener mis valoraciones",
        description = "Lista todas las valoraciones realizadas por el usuario autenticado",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> obtenerMisValoraciones(
            @RequestHeader("Authorization") String token) {
        try {
            String jwt = token.replace("Bearer ", "");
            Long compradorId = jwtService.getUserId(jwt);

            List<ValoracionResponse> valoraciones = valoracionService
                    .obtenerValoracionesRealizadas(compradorId);
            return ResponseEntity.ok(valoraciones);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al obtener valoraciones: " + e.getMessage()));
        }
    }

    /**
     * Verificar si el usuario ya valoró a un vendedor
     */
    @GetMapping("/verificar/{vendedorId}")
    @PreAuthorize("hasAnyAuthority('USER', 'MODERATOR', 'ADMIN')")
    @Operation(
        summary = "Verificar si ya se valoró a un vendedor",
        description = "Verifica si el usuario autenticado ya valoró a un vendedor específico",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> verificarSiYaValorado(
            @RequestHeader("Authorization") String token,
            @Parameter(description = "ID del vendedor") 
            @PathVariable Long vendedorId) {
        try {
            String jwt = token.replace("Bearer ", "");
            Long compradorId = jwtService.getUserId(jwt);

            boolean yaValorado = valoracionService.yaValorado(compradorId, vendedorId);
            return ResponseEntity.ok(Map.of("yaValorado", yaValorado));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al verificar valoración: " + e.getMessage()));
        }
    }

    /**
     * Eliminar una valoración (solo administradores)
     */
    @DeleteMapping("/{valoracionId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(
        summary = "Eliminar valoración",
        description = "Permite a un administrador eliminar una valoración. Solo para ADMIN.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> eliminarValoracion(
            @Parameter(description = "ID de la valoración") 
            @PathVariable Long valoracionId) {
        try {
            valoracionService.eliminarValoracion(valoracionId);
            return ResponseEntity.ok(Map.of("message", "Valoración eliminada exitosamente"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al eliminar valoración: " + e.getMessage()));
        }
    }
}
