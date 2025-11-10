package com.multicompany.sales_system.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.multicompany.sales_system.model.Configuracion;
import com.multicompany.sales_system.repository.ConfiguracionRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConfiguracionService {
    private final ConfiguracionRepository configuracionRepository;

    /**
     * Devuelve la lista de palabras prohibidas, limpias y en minúsculas.
     */
    public List<String> getPalabrasProhibidas() {
        return configuracionRepository.findByOpcion(Configuracion.Opcion.FILTRO_PALABRAS) // ✅ Usar enum
                .map(Configuracion::getValor)
                .map(v -> Arrays.stream(v.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(String::toLowerCase)
                        .collect(Collectors.toList()))
                .orElse(List.of());
    }

    /**
     * Devuelve la cantidad de días configurados para la expiración de productos.
     * Por defecto retorna 30 días si no hay configuración regfistrada.
     */
    public int getDiasExpiracion() {
        return configuracionRepository.findByOpcion(Configuracion.Opcion.EXPIRACION_CONFIG)
                .map(Configuracion::getValor)
                .map(valor -> {
                    try {
                        return Integer.parseInt(valor.trim());
                    } catch (NumberFormatException e) {
                        return 30; 
                    }
                })
                .orElse(30); 
    }

    /**
     * Actualiza la cantidad de días para la expiración de productos.
     * Si no existe una configuración previa, crea una nueva.
     * 
     * @param dias cantidad de días (debe ser mayor a 0)
     * @return los días configurados
     * @throws IllegalArgumentException si los días son menores o iguales a 0
     */
    public int updateDiasExpiracion(int dias) {
        if (dias <= 0) {
            throw new IllegalArgumentException("Los días de expiración deben ser mayor a 0");
        }

        Configuracion config = configuracionRepository.findByOpcion(Configuracion.Opcion.EXPIRACION_CONFIG)
                .orElseGet(() -> {
                    Configuracion nuevaConfig = new Configuracion();
                    nuevaConfig.setOpcion(Configuracion.Opcion.EXPIRACION_CONFIG);
                    return nuevaConfig;
                });
        
        config.setValor(String.valueOf(dias));
        configuracionRepository.save(config);
        
        return dias;
    }
}