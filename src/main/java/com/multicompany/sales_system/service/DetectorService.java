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
        List<String> palabras = configuracionService.getPalabrasProhibidas();
        log.info("🔄 [DETECTOR] Cargando patrones de palabras prohibidas...");
        log.info("📋 [DETECTOR] Total palabras prohibidas: {}", palabras.size());
        log.info("📋 [DETECTOR] Palabras: {}", palabras);
        
        List<Pattern> patrones = palabras.stream()
                .filter(s -> !s.isBlank())
                .map(p -> "\\b" + Pattern.quote(p) + "\\b")
                .map(regex -> Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE))
                .collect(Collectors.toList());
        patronesCache = patrones;
        
        log.info("✅ [DETECTOR] Patrones cargados exitosamente: {}", patronesCache.size());
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
        cargarPatrones();
    }
}