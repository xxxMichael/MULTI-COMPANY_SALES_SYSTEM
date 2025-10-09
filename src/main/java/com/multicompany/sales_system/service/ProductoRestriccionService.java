package com.multicompany.sales_system.service;

import com.multicompany.sales_system.model.Producto;
import com.multicompany.sales_system.model.enums.EstadoProducto;
import com.multicompany.sales_system.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Arrays;

/**
 * Servicio para gestionar restricciones y caducidad de productos
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductoRestriccionService {

    private final ProductRepository productRepository;
    private final DetectorService detectorService;
    private final IncidenciaService incidenciaService;

    // Configuración de días por defecto para la expiración (configurable desde
    // properties)
    @Value("${app.producto.dias-expiracion:30}")
    private int diasExpiracionDefecto;

    // Lista de palabras prohibidas (además de las de la BD)
    private static final List<String> PALABRAS_PROHIBIDAS_SISTEMA = Arrays.asList(
            "droga", "drogas", "narcótico", "cocaína", "heroína", "marihuana", "cannabis",
            "arma", "armas", "pistola", "rifle", "explosivo", "bomba",
            "estafa", "fraude", "engaño", "timador", "piramidal",
            "sexual", "prostitución", "escort", "adulto", "pornografía",
            "robo", "robado", "hurto", "ilegal", "falsificado");

    /**
     * Establece la fecha de expiración por defecto para un producto nuevo
     */
    public void establecerFechaExpiracion(Producto producto) {
        if (producto.getFechaExpiracion() == null) {
            producto.setFechaExpiracion(
                    LocalDateTime.now().plusDays(diasExpiracionDefecto));
        }
    }

    /**
     * Verifica si un producto contiene contenido prohibido y lo marca como tal
     */
    @Transactional
    public boolean verificarYMarcarProhibidos(Producto producto) {
        String textoCompleto = construirTextoCompleto(producto);

        // Verificar con DetectorService (BD) y palabras del sistema
        boolean tieneProhibidas = detectorService.containsProhibited(textoCompleto) ||
                contieneProhibidasSistema(textoCompleto);

        if (tieneProhibidas) {
            producto.setEstado(EstadoProducto.PROHIBIDO);
            producto.setDisponibilidad(false);

            // Crear incidencia si el producto ya tiene ID (guardado en BD)
            if (producto.getIdProducto() != null) {
                crearIncidenciaProhibido(producto, textoCompleto);
            }

            log.warn("Producto marcado como prohibido: ID={}, Nombre='{}'",
                    producto.getIdProducto(), producto.getNombre());
            return true;
        }

        return false;
    }

    /**
     * Procesa productos expirados automáticamente
     * Se ejecuta cada día a las 2:00 AM
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void procesarProductosExpirados() {
        LocalDateTime ahora = LocalDateTime.now();

        List<Producto> productosExpirados = productRepository.findProductosExpirados(ahora);

        for (Producto producto : productosExpirados) {
            producto.setEstado(EstadoProducto.ELIMINADO);
            producto.setDisponibilidad(false);
            productRepository.save(producto);

            log.info("Producto expirado automáticamente: ID={}, Nombre='{}'",
                    producto.getIdProducto(), producto.getNombre());
        }

        if (!productosExpirados.isEmpty()) {
            log.info("Se procesaron {} productos expirados", productosExpirados.size());
        }
    }

    /**
     * Extiende la fecha de expiración de un producto
     */
    @Transactional
    public void extenderExpiracion(Long productoId, int diasAdicionales) {
        Producto producto = productRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        LocalDateTime nuevaFecha = producto.getFechaExpiracion().plusDays(diasAdicionales);
        producto.setFechaExpiracion(nuevaFecha);
        productRepository.save(producto);

        log.info("Fecha de expiración extendida para producto ID={}, nueva fecha: {}",
                productoId, nuevaFecha);
    }

    /**
     * Obtiene productos próximos a expirar (en los próximos N días)
     */
    @Transactional(readOnly = true)
    public List<Producto> getProductosProximosAExpirar(int diasAnticipacion) {
        LocalDateTime fechaLimite = LocalDateTime.now().plusDays(diasAnticipacion);
        return productRepository.findProductosProximosAExpirar(fechaLimite);
    }

    // === MÉTODOS PRIVADOS ===

    private String construirTextoCompleto(Producto producto) {
        return String.join(" ",
                producto.getNombre() != null ? producto.getNombre() : "",
                producto.getDescripcion() != null ? producto.getDescripcion() : "",
                producto.getUbicacion() != null ? producto.getUbicacion() : "").trim();
    }

    private boolean contieneProhibidasSistema(String texto) {
        if (texto == null || texto.isBlank())
            return false;

        String textoLower = texto.toLowerCase();
        return PALABRAS_PROHIBIDAS_SISTEMA.stream()
                .anyMatch(palabra -> textoLower.contains(palabra.toLowerCase()));
    }

    private void crearIncidenciaProhibido(Producto producto, String textoCompleto) {
        try {
            List<String> palabrasEncontradas = detectorService.findMatchedWords(textoCompleto);

            // Agregar palabras del sistema encontradas
            PALABRAS_PROHIBIDAS_SISTEMA.stream()
                    .filter(palabra -> textoCompleto.toLowerCase().contains(palabra.toLowerCase()))
                    .forEach(palabrasEncontradas::add);

            String motivo = "Contenido inapropiado detectado automáticamente";
            String descripcion = String.format(
                    "Se detectaron palabras prohibidas en el producto. " +
                            "Palabras encontradas: %s. " +
                            "Producto: %s (ID: %s). " +
                            "Fecha de detección: %s",
                    String.join(", ", palabrasEncontradas),
                    producto.getNombre(),
                    producto.getIdProducto(),
                    LocalDateTime.now());

            // Usuario del sistema para incidencias automáticas
            Long idUsuarioSistema = 1L;

            incidenciaService.crearPorDeteccion(
                    producto.getIdProducto(),
                    idUsuarioSistema,
                    motivo,
                    descripcion);

        } catch (Exception e) {
            log.error("Error al crear incidencia para producto prohibido ID={}: {}",
                    producto.getIdProducto(), e.getMessage());
        }
    }
}