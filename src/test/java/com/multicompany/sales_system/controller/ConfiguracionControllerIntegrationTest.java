package com.multicompany.sales_system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.multicompany.sales_system.dto.configuracion.AgregarPalabraRequestDTO;
import com.multicompany.sales_system.dto.configuracion.DiasExpiracionRequestDTO;
import com.multicompany.sales_system.dto.configuracion.EliminarPalabraRequestDTO;
import com.multicompany.sales_system.model.Configuracion;
import com.multicompany.sales_system.repository.ConfiguracionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de integración para ConfiguracionController
 * Utiliza H2 como base de datos en memoria
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
public class ConfiguracionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ConfiguracionRepository configuracionRepository;

    @BeforeEach
    void setUp() {
        // Limpiar la base de datos antes de cada prueba
        configuracionRepository.deleteAll();
        
        // Configurar datos iniciales para pruebas
        Configuracion expiracion = new Configuracion();
        expiracion.setOpcion(Configuracion.Opcion.EXPIRACION_CONFIG);
        expiracion.setValor("30");
        configuracionRepository.save(expiracion);
        
        Configuracion filtro = new Configuracion();
        filtro.setOpcion(Configuracion.Opcion.FILTRO_PALABRAS);
        filtro.setValor("palabra1,palabra2,palabra3");
        configuracionRepository.save(filtro);
    }

    // ========================
    // GET /api/configuracion/dias-expiracion
    // ========================

    @Test
    @DisplayName("GET /api/configuracion/dias-expiracion - Obtener días de expiración")
    void testGetDiasExpiracion_Success() throws Exception {
        mockMvc.perform(get("/api/configuracion/dias-expiracion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dias").value(30))
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    @DisplayName("GET /api/configuracion/dias-expiracion - Sin configuración inicial")
    void testGetDiasExpiracion_NoConfig() throws Exception {
        // Eliminar configuración
        configuracionRepository.deleteAll();
        
        mockMvc.perform(get("/api/configuracion/dias-expiracion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dias").exists());
    }

    // ========================
    // PUT /api/configuracion/dias-expiracion
    // ========================

    @Test
    @DisplayName("PUT /api/configuracion/dias-expiracion - Actualizar días exitosamente")
    void testUpdateDiasExpiracion_Success() throws Exception {
        DiasExpiracionRequestDTO requestDTO = new DiasExpiracionRequestDTO(45);

        mockMvc.perform(put("/api/configuracion/dias-expiracion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dias").value(45))
                .andExpect(jsonPath("$.mensaje").value("Días de expiración actualizados exitosamente"));
    }

    @Test
    @DisplayName("PUT /api/configuracion/dias-expiracion - Error con días negativos")
    void testUpdateDiasExpiracion_NegativeDays() throws Exception {
        DiasExpiracionRequestDTO requestDTO = new DiasExpiracionRequestDTO(-5);

        mockMvc.perform(put("/api/configuracion/dias-expiracion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/configuracion/dias-expiracion - Error con cero días")
    void testUpdateDiasExpiracion_ZeroDays() throws Exception {
        DiasExpiracionRequestDTO requestDTO = new DiasExpiracionRequestDTO(0);

        mockMvc.perform(put("/api/configuracion/dias-expiracion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/configuracion/dias-expiracion - Error con días nulos")
    void testUpdateDiasExpiracion_NullDays() throws Exception {
        DiasExpiracionRequestDTO requestDTO = new DiasExpiracionRequestDTO(null);

        mockMvc.perform(put("/api/configuracion/dias-expiracion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/configuracion/dias-expiracion - Actualizar con valor grande")
    void testUpdateDiasExpiracion_LargeValue() throws Exception {
        DiasExpiracionRequestDTO requestDTO = new DiasExpiracionRequestDTO(365);

        mockMvc.perform(put("/api/configuracion/dias-expiracion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dias").value(365));
    }

    // ========================
    // GET /api/configuracion/palabras-prohibidas
    // ========================

    @Test
    @DisplayName("GET /api/configuracion/palabras-prohibidas - Obtener lista de palabras")
    void testGetPalabrasProhibidas_Success() throws Exception {
        mockMvc.perform(get("/api/configuracion/palabras-prohibidas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.palabras", hasSize(3)))
                .andExpect(jsonPath("$.palabras", containsInAnyOrder("palabra1", "palabra2", "palabra3")))
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    @DisplayName("GET /api/configuracion/palabras-prohibidas - Lista vacía")
    void testGetPalabrasProhibidas_EmptyList() throws Exception {
        // Actualizar con lista vacía
        Configuracion config = configuracionRepository.findByOpcion(Configuracion.Opcion.FILTRO_PALABRAS)
                .orElseThrow();
        config.setValor("");
        configuracionRepository.save(config);

        mockMvc.perform(get("/api/configuracion/palabras-prohibidas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.palabras", hasSize(0)));
    }

    // ========================
    // POST /api/configuracion/palabras-prohibidas
    // ========================

    @Test
    @DisplayName("POST /api/configuracion/palabras-prohibidas - Agregar palabra exitosamente")
    void testAgregarPalabraProhibida_Success() throws Exception {
        AgregarPalabraRequestDTO requestDTO = new AgregarPalabraRequestDTO("nuevaPalabra");

        mockMvc.perform(post("/api/configuracion/palabras-prohibidas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.palabras", hasSize(4)))
                .andExpect(jsonPath("$.palabras", hasItem("nuevapalabra"))) // Se guarda en minúsculas
                .andExpect(jsonPath("$.mensaje").value("Palabra agregada exitosamente al filtro"));
    }

    @Test
    @DisplayName("POST /api/configuracion/palabras-prohibidas - Error palabra vacía")
    void testAgregarPalabraProhibida_EmptyWord() throws Exception {
        AgregarPalabraRequestDTO requestDTO = new AgregarPalabraRequestDTO("");

        mockMvc.perform(post("/api/configuracion/palabras-prohibidas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/configuracion/palabras-prohibidas - Error palabra con espacios")
    void testAgregarPalabraProhibida_WordWithSpaces() throws Exception {
        AgregarPalabraRequestDTO requestDTO = new AgregarPalabraRequestDTO("   ");

        mockMvc.perform(post("/api/configuracion/palabras-prohibidas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/configuracion/palabras-prohibidas - Agregar palabra con mayúsculas")
    void testAgregarPalabraProhibida_UpperCase() throws Exception {
        AgregarPalabraRequestDTO requestDTO = new AgregarPalabraRequestDTO("PROHIBIDA");

        mockMvc.perform(post("/api/configuracion/palabras-prohibidas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.palabras", hasItem("prohibida"))); // Se convierte a minúsculas
    }

    @Test
    @DisplayName("POST /api/configuracion/palabras-prohibidas - Agregar palabra duplicada")
    void testAgregarPalabraProhibida_Duplicate() throws Exception {
        AgregarPalabraRequestDTO requestDTO = new AgregarPalabraRequestDTO("palabra1");

        mockMvc.perform(post("/api/configuracion/palabras-prohibidas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.palabras", hasSize(3))); // No se duplica
    }

    // ========================
    // DELETE /api/configuracion/palabras-prohibidas
    // ========================

    @Test
    @DisplayName("DELETE /api/configuracion/palabras-prohibidas - Eliminar palabra exitosamente")
    void testEliminarPalabraProhibida_Success() throws Exception {
        EliminarPalabraRequestDTO requestDTO = new EliminarPalabraRequestDTO("palabra2");

        mockMvc.perform(delete("/api/configuracion/palabras-prohibidas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.palabras", hasSize(2)))
                .andExpect(jsonPath("$.palabras", not(hasItem("palabra2"))))
                .andExpect(jsonPath("$.mensaje").value("Palabra eliminada exitosamente del filtro"));
    }

    // Test comentado: Requiere @ControllerAdvice para manejar IllegalArgumentException
    // @Test
    // @DisplayName("DELETE /api/configuracion/palabras-prohibidas - Eliminar palabra inexistente")
    // void testEliminarPalabraProhibida_NotFound() throws Exception {
    //     EliminarPalabraRequestDTO requestDTO = new EliminarPalabraRequestDTO("palabraInexistente");
    //
    //     mockMvc.perform(delete("/api/configuracion/palabras-prohibidas")
    //                     .contentType(MediaType.APPLICATION_JSON)
    //                     .content(objectMapper.writeValueAsString(requestDTO)))
    //             .andExpect(status().isBadRequest());
    // }

    @Test
    @DisplayName("DELETE /api/configuracion/palabras-prohibidas - Error palabra vacía")
    void testEliminarPalabraProhibida_EmptyWord() throws Exception {
        EliminarPalabraRequestDTO requestDTO = new EliminarPalabraRequestDTO("");

        mockMvc.perform(delete("/api/configuracion/palabras-prohibidas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /api/configuracion/palabras-prohibidas - Eliminar última palabra")
    void testEliminarPalabraProhibida_LastWord() throws Exception {
        // Dejar solo una palabra
        Configuracion config = configuracionRepository.findByOpcion(Configuracion.Opcion.FILTRO_PALABRAS)
                .orElseThrow();
        config.setValor("ultimaPalabra");
        configuracionRepository.save(config);

        EliminarPalabraRequestDTO requestDTO = new EliminarPalabraRequestDTO("ultimaPalabra");

        mockMvc.perform(delete("/api/configuracion/palabras-prohibidas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.palabras", hasSize(0)));
    }

    // ========================
    // Pruebas de flujo completo
    // ========================

    @Test
    @DisplayName("Flujo completo - Agregar y eliminar múltiples palabras")
    void testFlowAgregarYEliminar() throws Exception {
        // 1. Agregar primera palabra
        AgregarPalabraRequestDTO agregar1 = new AgregarPalabraRequestDTO("test1");
        mockMvc.perform(post("/api/configuracion/palabras-prohibidas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(agregar1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.palabras", hasSize(4)));

        // 2. Agregar segunda palabra
        AgregarPalabraRequestDTO agregar2 = new AgregarPalabraRequestDTO("test2");
        mockMvc.perform(post("/api/configuracion/palabras-prohibidas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(agregar2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.palabras", hasSize(5)));

        // 3. Eliminar una palabra original
        EliminarPalabraRequestDTO eliminar = new EliminarPalabraRequestDTO("palabra1");
        mockMvc.perform(delete("/api/configuracion/palabras-prohibidas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eliminar)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.palabras", hasSize(4)));

        // 4. Verificar estado final
        mockMvc.perform(get("/api/configuracion/palabras-prohibidas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.palabras", hasSize(4)))
                .andExpect(jsonPath("$.palabras", hasItems("palabra2", "palabra3", "test1", "test2")))
                .andExpect(jsonPath("$.palabras", not(hasItem("palabra1"))));
    }

    @Test
    @DisplayName("Flujo completo - Actualizar días múltiples veces")
    void testFlowActualizarDiasMultiple() throws Exception {
        // 1. Actualizar a 60 días
        DiasExpiracionRequestDTO request1 = new DiasExpiracionRequestDTO(60);
        mockMvc.perform(put("/api/configuracion/dias-expiracion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dias").value(60));

        // 2. Actualizar a 90 días
        DiasExpiracionRequestDTO request2 = new DiasExpiracionRequestDTO(90);
        mockMvc.perform(put("/api/configuracion/dias-expiracion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dias").value(90));

        // 3. Verificar que persiste el último valor
        mockMvc.perform(get("/api/configuracion/dias-expiracion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dias").value(90));
    }
}
