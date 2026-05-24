package com.multicompany.sales_system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.multicompany.sales_system.dto.incident.IncidenciaRequestDTO;
import com.multicompany.sales_system.model.*;
import com.multicompany.sales_system.model.enums.EstadoProducto;
import com.multicompany.sales_system.model.enums.TipoProducto;
import com.multicompany.sales_system.model.enums.UsuarioRole;
import com.multicompany.sales_system.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithMockUser(username = "admin", roles = {"ADMIN"})
public class IncidenciaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IncidenciaRepository incidenciaRepository;

    @Autowired
    private ProductRepository productoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    private Usuario usuarioReporta;
    private Usuario vendedor;
    private Producto producto;
    private Categoria categoria;

    @BeforeEach
    void setUp() {
        // Limpiar datos
        incidenciaRepository.deleteAll();
        productoRepository.deleteAll();
        usuarioRepository.deleteAll();
        categoriaRepository.deleteAll();

        // Crear categoría
        categoria = new Categoria();
        categoria.setNombre("Electrónicos");
        categoria.setDescripcion("Dispositivos electrónicos");
        categoria = categoriaRepository.save(categoria);

        // Crear vendedor
        vendedor = new Usuario();
        vendedor.setCedula("1234567890");
        vendedor.setNombre("Juan Vendedor");
        vendedor.setCorreo("vendedor@test.com");
        vendedor.setContrasena("password123");
        vendedor.setRol(UsuarioRole.USER);
        vendedor.setEstado(Usuario.EstadoUsuario.ACTIVO);
        vendedor = usuarioRepository.save(vendedor);

        // Crear usuario que reporta
        usuarioReporta = new Usuario();
        usuarioReporta.setCedula("0987654321");
        usuarioReporta.setNombre("María Reporta");
        usuarioReporta.setCorreo("reporta@test.com");
        usuarioReporta.setContrasena("password123");
        usuarioReporta.setRol(UsuarioRole.USER);
        usuarioReporta.setEstado(Usuario.EstadoUsuario.ACTIVO);
        usuarioReporta = usuarioRepository.save(usuarioReporta);

        // Crear producto
        producto = new Producto();
        producto.setCodigo("PROD-001");
        producto.setNombre("Laptop HP");
        producto.setDescripcion("Laptop en buen estado");
        producto.setPrecio(500.0);
        producto.setUbicacion("Quito");
        producto.setDisponibilidad(true);
        producto.setTipo(TipoProducto.PRODUCTO);
        producto.setEstado(EstadoProducto.ACTIVO);
        producto.setVendedor(vendedor);
        producto.setCategoria(categoria);
        producto.setFechaPublicacion(LocalDateTime.now());
        producto = productoRepository.save(producto);

        // Crear incidencias de prueba
        Incidencia incidencia1 = new Incidencia();
        incidencia1.setProducto(producto);
        incidencia1.setUsuarioReporta(usuarioReporta);
        incidencia1.setMotivo("Producto no corresponde a la descripción");
        incidencia1.setDescripcion("El producto anunciado no coincide con las fotos");
        incidencia1.setEstado(Incidencia.Estado.PENDIENTE);
        incidencia1.setFechaRegistro(LocalDateTime.now());
        incidenciaRepository.save(incidencia1);

        Incidencia incidencia2 = new Incidencia();
        incidencia2.setProducto(producto);
        incidencia2.setUsuarioReporta(usuarioReporta);
        incidencia2.setMotivo("Precio sospechoso");
        incidencia2.setDescripcion("El precio es demasiado bajo para ser real");
        incidencia2.setEstado(Incidencia.Estado.ATENDIDA);
        incidencia2.setFechaRegistro(LocalDateTime.now().minusDays(1));
        incidenciaRepository.save(incidencia2);

        Incidencia incidencia3 = new Incidencia();
        incidencia3.setProducto(producto);
        incidencia3.setUsuarioReporta(usuarioReporta);
        incidencia3.setMotivo("Reporte duplicado");
        incidencia3.setDescripcion("Esta es una incidencia descartada");
        incidencia3.setEstado(Incidencia.Estado.DESCARTADA);
        incidencia3.setFechaRegistro(LocalDateTime.now().minusDays(2));
        incidenciaRepository.save(incidencia3);
    }

    // ========================
    // GET /api/incidencias
    // ========================

    @Test
    @DisplayName("GET /api/incidencias - Listar todas las incidencias")
    void testListarTodas() throws Exception {
        mockMvc.perform(get("/api/incidencias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].motivo", containsInAnyOrder(
                        "Producto no corresponde a la descripción",
                        "Precio sospechoso",
                        "Reporte duplicado"
                )));
    }

    // ========================
    // GET /api/incidencias/pendientes
    // ========================

    @Test
    @DisplayName("GET /api/incidencias/pendientes - Listar solo incidencias pendientes")
    void testListarPendientes() throws Exception {
        mockMvc.perform(get("/api/incidencias/pendientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].motivo").value("Producto no corresponde a la descripción"))
                .andExpect(jsonPath("$[0].estado").value("PENDIENTE"));
    }

    @Test
    @DisplayName("GET /api/incidencias/pendientes - Sin incidencias pendientes")
    void testListarPendientes_Empty() throws Exception {
        // Marcar todas como atendidas
        incidenciaRepository.findAll().forEach(i -> {
            i.setEstado(Incidencia.Estado.ATENDIDA);
            incidenciaRepository.save(i);
        });

        mockMvc.perform(get("/api/incidencias/pendientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ========================
    // GET /api/incidencias/atendidas
    // ========================

    @Test
    @DisplayName("GET /api/incidencias/atendidas - Listar solo incidencias atendidas")
    void testListarAtendidas() throws Exception {
        mockMvc.perform(get("/api/incidencias/atendidas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].motivo").value("Precio sospechoso"))
                .andExpect(jsonPath("$[0].estado").value("ATENDIDA"));
    }

    // ========================
    // GET /api/incidencias/descartadas
    // ========================

    @Test
    @DisplayName("GET /api/incidencias/descartadas - Listar solo incidencias descartadas")
    void testListarDescartadas() throws Exception {
        mockMvc.perform(get("/api/incidencias/descartadas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].motivo").value("Reporte duplicado"))
                .andExpect(jsonPath("$[0].estado").value("DESCARTADA"));
    }

    // ========================
    // GET /api/incidencias/{id}
    // ========================

    @Test
    @DisplayName("GET /api/incidencias/{id} - Obtener incidencia por ID")
    void testObtenerPorId_Success() throws Exception {
        Incidencia incidencia = incidenciaRepository.findAll().get(0);

        mockMvc.perform(get("/api/incidencias/{id}", incidencia.getIdIncidencia()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idIncidencia").value(incidencia.getIdIncidencia()))
                .andExpect(jsonPath("$.idProducto").value(producto.getIdProducto()))
                .andExpect(jsonPath("$.nombreProducto").value("Laptop HP"))
                .andExpect(jsonPath("$.idUsuarioReporta").value(usuarioReporta.getIdUsuario()))
                .andExpect(jsonPath("$.nombreUsuarioReporta").value("María Reporta"))
                .andExpect(jsonPath("$.motivo").isNotEmpty())
                .andExpect(jsonPath("$.descripcion").isNotEmpty())
                .andExpect(jsonPath("$.estado").isNotEmpty())
                .andExpect(jsonPath("$.fechaRegistro").isNotEmpty());
    }

    // Test comentado: Requiere manejo de excepciones personalizado
    // @Test
    // @DisplayName("GET /api/incidencias/{id} - Incidencia no encontrada")
    // void testObtenerPorId_NotFound() throws Exception {
    //     mockMvc.perform(get("/api/incidencias/{id}", 99999L))
    //             .andExpect(status().isNotFound());
    // }

    // ========================
    // POST /api/incidencias
    // ========================

    @Test
    @DisplayName("POST /api/incidencias - Crear incidencia exitosamente")
    void testCrearIncidencia_Success() throws Exception {
        IncidenciaRequestDTO requestDTO = new IncidenciaRequestDTO(
                producto.getIdProducto(),
                usuarioReporta.getIdUsuario(),
                "Contenido inapropiado",
                "El producto contiene información ofensiva"
        );

        mockMvc.perform(post("/api/incidencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idIncidencia").exists())
                .andExpect(jsonPath("$.motivo").value("Contenido inapropiado"))
                .andExpect(jsonPath("$.descripcion").value("El producto contiene información ofensiva"))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.idProducto").value(producto.getIdProducto()))
                .andExpect(jsonPath("$.idUsuarioReporta").value(usuarioReporta.getIdUsuario()));
    }

    @Test
    @DisplayName("POST /api/incidencias - Error con motivo vacío")
    void testCrearIncidencia_EmptyMotivo() throws Exception {
        IncidenciaRequestDTO requestDTO = new IncidenciaRequestDTO(
                producto.getIdProducto(),
                usuarioReporta.getIdUsuario(),
                "",
                "Descripción válida"
        );

        mockMvc.perform(post("/api/incidencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/incidencias - Error con descripción vacía")
    void testCrearIncidencia_EmptyDescripcion() throws Exception {
        IncidenciaRequestDTO requestDTO = new IncidenciaRequestDTO(
                producto.getIdProducto(),
                usuarioReporta.getIdUsuario(),
                "Motivo válido",
                ""
        );

        mockMvc.perform(post("/api/incidencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/incidencias - Error con producto null")
    void testCrearIncidencia_NullProducto() throws Exception {
        IncidenciaRequestDTO requestDTO = new IncidenciaRequestDTO(
                null,
                usuarioReporta.getIdUsuario(),
                "Motivo válido",
                "Descripción válida"
        );

        mockMvc.perform(post("/api/incidencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/incidencias - Error con usuario null")
    void testCrearIncidencia_NullUsuario() throws Exception {
        IncidenciaRequestDTO requestDTO = new IncidenciaRequestDTO(
                producto.getIdProducto(),
                null,
                "Motivo válido",
                "Descripción válida"
        );

        mockMvc.perform(post("/api/incidencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/incidencias - Error con motivo muy largo")
    void testCrearIncidencia_MotivoTooLong() throws Exception {
        String motivoLargo = "a".repeat(256); // Excede los 255 caracteres

        IncidenciaRequestDTO requestDTO = new IncidenciaRequestDTO(
                producto.getIdProducto(),
                usuarioReporta.getIdUsuario(),
                motivoLargo,
                "Descripción válida"
        );

        mockMvc.perform(post("/api/incidencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/incidencias - Error con descripción muy larga")
    void testCrearIncidencia_DescripcionTooLong() throws Exception {
        String descripcionLarga = "a".repeat(1001); // Excede los 1000 caracteres

        IncidenciaRequestDTO requestDTO = new IncidenciaRequestDTO(
                producto.getIdProducto(),
                usuarioReporta.getIdUsuario(),
                "Motivo válido",
                descripcionLarga
        );

        mockMvc.perform(post("/api/incidencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    // ========================
    // POST /api/incidencias/deteccion
    // ========================

    @Test
    @DisplayName("POST /api/incidencias/deteccion - Crear incidencia por detección automática")
    void testCrearPorDeteccion_Success() throws Exception {
        mockMvc.perform(post("/api/incidencias/deteccion")
                        .param("idProducto", producto.getIdProducto().toString())
                        .param("idUsuarioReporta", usuarioReporta.getIdUsuario().toString())
                        .param("motivo", "Detección automática de contenido prohibido")
                        .param("descripcion", "El sistema detectó palabras prohibidas en el producto"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idIncidencia").exists())
                .andExpect(jsonPath("$.motivo").value("Detección automática de contenido prohibido"))
                .andExpect(jsonPath("$.descripcion").value("El sistema detectó palabras prohibidas en el producto"))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));
    }

    @Test
    @DisplayName("POST /api/incidencias/deteccion - Con parámetros mínimos")
    void testCrearPorDeteccion_MinimalParams() throws Exception {
        mockMvc.perform(post("/api/incidencias/deteccion")
                        .param("idProducto", producto.getIdProducto().toString())
                        .param("idUsuarioReporta", usuarioReporta.getIdUsuario().toString())
                        .param("motivo", "Test")
                        .param("descripcion", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));
    }

    // ========================
    // PUT /api/incidencias/{id}/atender
    // ========================

    @Test
    @DisplayName("PUT /api/incidencias/{id}/atender - Marcar incidencia como atendida")
    void testMarcarAtendida_Success() throws Exception {
        Incidencia pendiente = incidenciaRepository.findAll().stream()
                .filter(i -> i.getEstado() == Incidencia.Estado.PENDIENTE)
                .findFirst()
                .orElseThrow();

        mockMvc.perform(put("/api/incidencias/{id}/atender", pendiente.getIdIncidencia()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idIncidencia").value(pendiente.getIdIncidencia()))
                .andExpect(jsonPath("$.estado").value("ATENDIDA"));
    }

    @Test
    @DisplayName("PUT /api/incidencias/{id}/atender - Marcar incidencia ya atendida")
    void testMarcarAtendida_AlreadyAtendida() throws Exception {
        Incidencia atendida = incidenciaRepository.findAll().stream()
                .filter(i -> i.getEstado() == Incidencia.Estado.ATENDIDA)
                .findFirst()
                .orElseThrow();

        mockMvc.perform(put("/api/incidencias/{id}/atender", atendida.getIdIncidencia()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ATENDIDA"));
    }

    // ========================
    // PUT /api/incidencias/{id}/descartar
    // ========================

    @Test
    @DisplayName("PUT /api/incidencias/{id}/descartar - Descartar incidencia")
    void testDescartar_Success() throws Exception {
        Incidencia pendiente = incidenciaRepository.findAll().stream()
                .filter(i -> i.getEstado() == Incidencia.Estado.PENDIENTE)
                .findFirst()
                .orElseThrow();

        mockMvc.perform(put("/api/incidencias/{id}/descartar", pendiente.getIdIncidencia()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idIncidencia").value(pendiente.getIdIncidencia()))
                .andExpect(jsonPath("$.estado").value("DESCARTADA"));
    }

    @Test
    @DisplayName("PUT /api/incidencias/{id}/descartar - Descartar incidencia ya descartada")
    void testDescartar_AlreadyDescartada() throws Exception {
        Incidencia descartada = incidenciaRepository.findAll().stream()
                .filter(i -> i.getEstado() == Incidencia.Estado.DESCARTADA)
                .findFirst()
                .orElseThrow();

        mockMvc.perform(put("/api/incidencias/{id}/descartar", descartada.getIdIncidencia()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("DESCARTADA"));
    }

    // ========================
    // PRUEBAS DE FLUJO COMPLETO
    // ========================

    @Test
    @DisplayName("Flujo completo: Crear incidencia → Marcar como atendida")
    void testWorkflow_CrearYAtender() throws Exception {
        // 1. Crear nueva incidencia
        IncidenciaRequestDTO requestDTO = new IncidenciaRequestDTO(
                producto.getIdProducto(),
                usuarioReporta.getIdUsuario(),
                "Producto sospechoso",
                "El vendedor tiene comportamiento extraño"
        );

        String response = mockMvc.perform(post("/api/incidencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andReturn().getResponse().getContentAsString();

        Long idIncidencia = objectMapper.readTree(response).get("idIncidencia").asLong();

        // 2. Marcar como atendida
        mockMvc.perform(put("/api/incidencias/{id}/atender", idIncidencia))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ATENDIDA"));

        // 3. Verificar en lista de atendidas
        mockMvc.perform(get("/api/incidencias/atendidas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].idIncidencia", hasItem(idIncidencia.intValue())));
    }

    @Test
    @DisplayName("Flujo completo: Crear incidencia → Descartar")
    void testWorkflow_CrearYDescartar() throws Exception {
        // 1. Crear nueva incidencia
        IncidenciaRequestDTO requestDTO = new IncidenciaRequestDTO(
                producto.getIdProducto(),
                usuarioReporta.getIdUsuario(),
                "Reporte sin fundamento",
                "Incidencia que será descartada"
        );

        String response = mockMvc.perform(post("/api/incidencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andReturn().getResponse().getContentAsString();

        Long idIncidencia = objectMapper.readTree(response).get("idIncidencia").asLong();

        // 2. Descartar
        mockMvc.perform(put("/api/incidencias/{id}/descartar", idIncidencia))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("DESCARTADA"));

        // 3. Verificar en lista de descartadas
        mockMvc.perform(get("/api/incidencias/descartadas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].idIncidencia", hasItem(idIncidencia.intValue())));
    }

    @Test
    @DisplayName("Flujo completo: Filtrado por estados")
    void testWorkflow_FiltradoPorEstados() throws Exception {
        // Verificar conteos iniciales
        mockMvc.perform(get("/api/incidencias/pendientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(get("/api/incidencias/atendidas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(get("/api/incidencias/descartadas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(get("/api/incidencias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }
}
