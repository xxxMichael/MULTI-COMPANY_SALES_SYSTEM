package com.multicompany.sales_system.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DetectorService {

    private final ConfiguracionService configuracionService;
    // Cache simple de patrones para evitar recompilar en cada llamada.
    private volatile List<Pattern> patronesCache = null;

    private void cargarPatrones() {
        log.info("🔄 [DETECTOR] ==================== INICIANDO CARGA DE PATRONES ====================");
        
        try {
            List<String> palabras = configuracionService.getPalabrasProhibidas();
            
            log.info("📋 [DETECTOR] Palabras obtenidas de BD: {}", palabras);
            log.info("📋 [DETECTOR] Total palabras prohibidas: {}", palabras.size());
            
            if (palabras.isEmpty()) {
                log.error("❌ [DETECTOR] ¡ADVERTENCIA! No se encontraron palabras prohibidas en la BD");
                log.error("❌ [DETECTOR] El filtro de contenido NO funcionará hasta que se configuren palabras");
                patronesCache = new ArrayList<>();
                return;
            }
            
            List<Pattern> patrones = palabras.stream()
                    .filter(s -> !s.isBlank())
                    .peek(palabra -> log.debug("   - Compilando patrón para: '{}'", palabra))
                    .map(p -> "\\b" + Pattern.quote(p) + "\\b")
                    .map(regex -> Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE))
                    .collect(Collectors.toList());
            
            patronesCache = patrones;
            
            log.info("✅ [DETECTOR] Patrones cargados exitosamente: {}", patronesCache.size());
            log.info("✅ [DETECTOR] ==================== CARGA COMPLETADA ====================");
            
        } catch (Exception e) {
            log.error("❌ [DETECTOR] ERROR al cargar patrones: {}", e.getMessage(), e);
            patronesCache = new ArrayList<>();
        }
    }

    private List<Pattern> getPatrones() {
        if (patronesCache == null) {
            synchronized (this) {
                if (patronesCache == null) cargarPatrones();
            }
        }
        return patronesCache;
    }

    /** Indica si el texto contiene al menos una palabra prohibida. */
    public boolean containsProhibited(String texto) {
        if (texto == null || texto.isBlank()) {
            log.debug("⚠️ [DETECTOR] Texto vacío o nulo, retornando false");
            return false;
        }
        
        List<Pattern> patrones = getPatrones();
        
        if (patrones.isEmpty()) {
            log.warn("⚠️ [DETECTOR] No hay patrones cargados. El filtro no está activo.");
            return false;
        }
        
        log.debug("🔍 [DETECTOR] Verificando texto: '{}'", texto);
        log.debug("🔍 [DETECTOR] Patrones disponibles: {}", patrones.size());
        
        for (Pattern p : patrones) {
            if (p.matcher(texto).find()) {
                log.warn("❌ [DETECTOR] ¡MATCH! Patrón encontrado: {}", p.pattern());
                return true;
            }
        }
        
        log.debug("✅ [DETECTOR] No se encontraron coincidencias");
        return false;
    }

    /** Devuelve las palabras encontradas (sin duplicados). */
    public List<String> findMatchedWords(String texto) {
        if (texto == null || texto.isBlank()) return List.of();
        LinkedHashSet<String> found = new LinkedHashSet<>();
        for (Pattern p : getPatrones()) {
            Matcher m = p.matcher(texto);
            if (m.find()) {
                found.add(m.group().toLowerCase());
            }
        }
        return new ArrayList<>(found);
    }

    /** Forzar recarga (por ejemplo admin actualizó el banco de palabras) */
    public void reload() {
        log.warn("🔄 [DETECTOR] Forzando recarga de patrones...");
        synchronized (this) {
            patronesCache = null;
        }
        cargarPatrones();
        log.info("✅ [DETECTOR] Recarga completada. Nuevos patrones: {}", patronesCache != null ? patronesCache.size() : 0);
    }
    
    /** Obtener información del estado actual del detector */
    public Map<String, Object> getEstado() {
        Map<String, Object> estado = new HashMap<>();
        estado.put("patronesCargados", patronesCache != null ? patronesCache.size() : 0);
        estado.put("cacheInicializado", patronesCache != null);
        estado.put("palabrasProhibidas", configuracionService.getPalabrasProhibidas());
        return estado;
    }
}