package com.multicompany.sales_system.controller;

import org.springframework.web.bind.annotation.*;

import com.multicompany.sales_system.model.Incidencia;
import com.multicompany.sales_system.service.IncidenciaService;

import java.util.List;

@RestController
@RequestMapping("/api/incidencias")
public class IncidenciaController {

    private final IncidenciaService incidenciaService;

    public IncidenciaController(IncidenciaService incidenciaService) {
        this.incidenciaService = incidenciaService;
    }

    // 🔹 Listar todas las incidencias pendientes
    @GetMapping("/pendientes")
    public List<Incidencia> listarPendientes() {
        return incidenciaService.listarPendientes();
    }

        // 🔹 Listar todas las incidencias pendientes
    @GetMapping("/atendidas")
    public List<Incidencia> listarAtendidas() {
        return incidenciaService.listarAtendidas();
    }

        // 🔹 Listar todas las incidencias pendientes
    @GetMapping("/descartadas")
    public List<Incidencia> listarDescartada() {
        return incidenciaService.listarDescartadas();
    }

    // 🔹 Marcar incidencia como atendida
    @PutMapping("/{id}/atender")
    public Incidencia marcarAtendida(@PathVariable Long id) {
        return incidenciaService.marcarAtendida(id);
    }

    // 🔹 Descartar incidencia
    @PutMapping("/{id}/descartar")
    public Incidencia descartar(@PathVariable Long id) {
        return incidenciaService.descartar(id);
    }

    // 🔹 Crear incidencia automáticamente (detección de producto prohibido)
    @PostMapping("/crear-automatico")
    public Incidencia crearPorDeteccion(
            @RequestParam Long idProducto,
            @RequestParam Long idUsuarioReporta,
            @RequestParam String motivo,
            @RequestParam String descripcion) {
        return incidenciaService.crearPorDeteccion(idProducto, idUsuarioReporta, motivo, descripcion);
    }
}