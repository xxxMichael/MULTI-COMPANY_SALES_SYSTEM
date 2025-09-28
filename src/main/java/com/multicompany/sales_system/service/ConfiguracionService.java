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
        return configuracionRepository.findByOpcion("FILTRO_PALABRAS")
                .map(Configuracion::getValor)
                .map(v -> Arrays.stream(v.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(String::toLowerCase)
                        .collect(Collectors.toList()))
                .orElse(List.of());
    }
}
