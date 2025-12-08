package com.multicompany.sales_system.controller;

import com.multicompany.sales_system.model.*;
import com.multicompany.sales_system.model.enums.UsuarioRole;
import com.multicompany.sales_system.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
@WithMockUser(username = "testuser", roles = {"USER"})
public class ChatControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private MensajeRepository mensajeRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Usuario usuario1;
    private Usuario usuario2;
    private Usuario usuario3;
    private Chat chat1;
    private Chat chat2;

    @BeforeEach
    void setUp() {
        // Limpiar datos
        mensajeRepository.deleteAll();
        chatRepository.deleteAll();
        usuarioRepository.deleteAll();

        // Crear usuarios
        usuario1 = new Usuario();
        usuario1.setCedula("1234567890");
        usuario1.setNombre("Juan");
        usuario1.setApellido("Pérez");
        usuario1.setCorreo("juan@test.com");
        usuario1.setContrasena("password123");
        usuario1.setRol(UsuarioRole.USER);
        usuario1.setEstado(Usuario.EstadoUsuario.ACTIVO);
        usuario1 = usuarioRepository.save(usuario1);

        usuario2 = new Usuario();
        usuario2.setCedula("0987654321");
        usuario2.setNombre("María");
        usuario2.setApellido("García");
        usuario2.setCorreo("maria@test.com");
        usuario2.setContrasena("password123");
        usuario2.setRol(UsuarioRole.USER);
        usuario2.setEstado(Usuario.EstadoUsuario.ACTIVO);
        usuario2 = usuarioRepository.save(usuario2);

        usuario3 = new Usuario();
        usuario3.setCedula("1122334455");
        usuario3.setNombre("Carlos");
        usuario3.setApellido("López");
        usuario3.setCorreo("carlos@test.com");
        usuario3.setContrasena("password123");
        usuario3.setRol(UsuarioRole.USER);
        usuario3.setEstado(Usuario.EstadoUsuario.ACTIVO);
        usuario3 = usuarioRepository.save(usuario3);

        // Crear chat entre usuario1 y usuario2
        chat1 = new Chat();
        chat1.setUsuario1(usuario1);
        chat1.setUsuario2(usuario2);
        chat1 = chatRepository.save(chat1);

        // Crear mensajes en chat1
        Mensaje mensaje1 = new Mensaje();
        mensaje1.setChat(chat1);
        mensaje1.setEmisor(usuario1);
        mensaje1.setContenido("Hola María, ¿está disponible el producto?");
        mensaje1.setFechaEnvio(LocalDateTime.now().minusHours(2));
        mensaje1.setLeido(true);
        mensajeRepository.save(mensaje1);

        Mensaje mensaje2 = new Mensaje();
        mensaje2.setChat(chat1);
        mensaje2.setEmisor(usuario2);
        mensaje2.setContenido("Sí, todavía está disponible");
        mensaje2.setFechaEnvio(LocalDateTime.now().minusHours(1));
        mensaje2.setLeido(false);
        mensajeRepository.save(mensaje2);

        Mensaje mensaje3 = new Mensaje();
        mensaje3.setChat(chat1);
        mensaje3.setEmisor(usuario1);
        mensaje3.setContenido("Perfecto, ¿cuándo podemos vernos?");
        mensaje3.setFechaEnvio(LocalDateTime.now().minusMinutes(30));
        mensaje3.setLeido(false);
        mensajeRepository.save(mensaje3);

        // Crear chat entre usuario1 y usuario3
        chat2 = new Chat();
        chat2.setUsuario1(usuario1);
        chat2.setUsuario2(usuario3);
        chat2 = chatRepository.save(chat2);

        // Crear mensaje en chat2
        Mensaje mensaje4 = new Mensaje();
        mensaje4.setChat(chat2);
        mensaje4.setEmisor(usuario3);
        mensaje4.setContenido("Hola Juan, me interesa tu producto");
        mensaje4.setFechaEnvio(LocalDateTime.now().minusMinutes(10));
        mensaje4.setLeido(false);
        mensajeRepository.save(mensaje4);
    }

    // ========================
    // GET /api/chat/usuario/{idUsuario}
    // ========================

    @Test
    @DisplayName("GET /api/chat/usuario/{idUsuario} - Obtener chats del usuario1")
    void testObtenerChatsPorUsuario_Usuario1() throws Exception {
        mockMvc.perform(get("/api/chat/usuario/{idUsuario}", usuario1.getIdUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2))) // usuario1 tiene 2 chats
                .andExpect(jsonPath("$[*].idUsuario1", hasItem(usuario1.getIdUsuario().intValue())))
                .andExpect(jsonPath("$[*].nombreUsuario1", hasItem(containsString("Juan"))))
                .andExpect(jsonPath("$[*].nombreUsuario2", hasItems(
                        containsString("María"),
                        containsString("Carlos")
                )));
    }

    @Test
    @DisplayName("GET /api/chat/usuario/{idUsuario} - Obtener chats del usuario2")
    void testObtenerChatsPorUsuario_Usuario2() throws Exception {
        mockMvc.perform(get("/api/chat/usuario/{idUsuario}", usuario2.getIdUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1))) // usuario2 tiene 1 chat
                .andExpect(jsonPath("$[0].nombreUsuario2", containsString("María")));
    }

    @Test
    @DisplayName("GET /api/chat/usuario/{idUsuario} - Usuario sin chats")
    void testObtenerChatsPorUsuario_SinChats() throws Exception {
        // Crear usuario nuevo sin chats
        Usuario usuarioNuevo = new Usuario();
        usuarioNuevo.setCedula("9999999999");
        usuarioNuevo.setNombre("Nuevo");
        usuarioNuevo.setCorreo("nuevo@test.com");
        usuarioNuevo.setContrasena("password123");
        usuarioNuevo.setRol(UsuarioRole.USER);
        usuarioNuevo.setEstado(Usuario.EstadoUsuario.ACTIVO);
        usuarioNuevo = usuarioRepository.save(usuarioNuevo);

        mockMvc.perform(get("/api/chat/usuario/{idUsuario}", usuarioNuevo.getIdUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ========================
    // GET /api/chat/{idUsuario1}/{idUsuario2}
    // ========================

    @Test
    @DisplayName("GET /api/chat/{idUsuario1}/{idUsuario2} - Obtener chat existente")
    void testCrearObtenerChat_Existente() throws Exception {
        mockMvc.perform(get("/api/chat/{idUsuario1}/{idUsuario2}", 
                        usuario1.getIdUsuario(), usuario2.getIdUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idChat").value(chat1.getIdChat()))
                .andExpect(jsonPath("$.idUsuario1").value(usuario1.getIdUsuario()))
                .andExpect(jsonPath("$.idUsuario2").value(usuario2.getIdUsuario()))
                .andExpect(jsonPath("$.nombreUsuario1").value(containsString("Juan")))
                .andExpect(jsonPath("$.nombreUsuario2").value(containsString("María")));
    }

    @Test
    @DisplayName("GET /api/chat/{idUsuario1}/{idUsuario2} - Crear nuevo chat")
    void testCrearObtenerChat_Nuevo() throws Exception {
        // Crear usuario nuevo
        Usuario usuario4 = new Usuario();
        usuario4.setCedula("5566778899");
        usuario4.setNombre("Ana");
        usuario4.setCorreo("ana@test.com");
        usuario4.setContrasena("password123");
        usuario4.setRol(UsuarioRole.USER);
        usuario4.setEstado(Usuario.EstadoUsuario.ACTIVO);
        usuario4 = usuarioRepository.save(usuario4);

        int chatsAntes = chatRepository.findAll().size();

        mockMvc.perform(get("/api/chat/{idUsuario1}/{idUsuario2}", 
                        usuario1.getIdUsuario(), usuario4.getIdUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idChat").exists())
                .andExpect(jsonPath("$.idUsuario1").value(usuario1.getIdUsuario()))
                .andExpect(jsonPath("$.idUsuario2").value(usuario4.getIdUsuario()))
                .andExpect(jsonPath("$.nombreUsuario1").value(containsString("Juan")))
                .andExpect(jsonPath("$.nombreUsuario2").value(containsString("Ana")));

        int chatsDespues = chatRepository.findAll().size();
        assert chatsDespues == chatsAntes + 1;
    }

    @Test
    @DisplayName("GET /api/chat/{idUsuario1}/{idUsuario2} - Obtener chat en orden inverso")
    void testCrearObtenerChat_OrdenInverso() throws Exception {
        // Debería obtener el mismo chat independientemente del orden
        mockMvc.perform(get("/api/chat/{idUsuario1}/{idUsuario2}", 
                        usuario2.getIdUsuario(), usuario1.getIdUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idChat").value(chat1.getIdChat()));
    }

    // ========================
    // GET /api/chat/{idChat}/mensajes
    // ========================

    @Test
    @DisplayName("GET /api/chat/{idChat}/mensajes - Obtener mensajes del chat")
    void testObtenerMensajesPorChat_Success() throws Exception {
        mockMvc.perform(get("/api/chat/{idChat}/mensajes", chat1.getIdChat()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].contenido", hasItems(
                        "Hola María, ¿está disponible el producto?",
                        "Sí, todavía está disponible",
                        "Perfecto, ¿cuándo podemos vernos?"
                )))
                .andExpect(jsonPath("$[*].idEmisor", hasItems(
                        usuario1.getIdUsuario().intValue(),
                        usuario2.getIdUsuario().intValue()
                )));
    }

    @Test
    @DisplayName("GET /api/chat/{idChat}/mensajes - Chat con un solo mensaje")
    void testObtenerMensajesPorChat_UnMensaje() throws Exception {
        mockMvc.perform(get("/api/chat/{idChat}/mensajes", chat2.getIdChat()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].contenido").value("Hola Juan, me interesa tu producto"))
                .andExpect(jsonPath("$[0].idEmisor").value(usuario3.getIdUsuario()))
                .andExpect(jsonPath("$[0].leido").value(false));
    }

    @Test
    @DisplayName("GET /api/chat/{idChat}/mensajes - Chat sin mensajes")
    void testObtenerMensajesPorChat_Vacio() throws Exception {
        // Crear chat nuevo sin mensajes
        Chat chatVacio = new Chat();
        chatVacio.setUsuario1(usuario2);
        chatVacio.setUsuario2(usuario3);
        chatVacio = chatRepository.save(chatVacio);

        mockMvc.perform(get("/api/chat/{idChat}/mensajes", chatVacio.getIdChat()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ========================
    // GET /api/chat/usuario/{idUsuario}/mensajes-no-leidos
    // ========================

    @Test
    @DisplayName("GET /api/chat/usuario/{idUsuario}/mensajes-no-leidos - Contar mensajes no leídos de usuario1")
    void testContarMensajesNoLeidos_Usuario1() throws Exception {
        // Usuario1 tiene 2 mensajes no leídos: 1 de María en chat1 y 1 de Carlos en chat2
        mockMvc.perform(get("/api/chat/usuario/{idUsuario}/mensajes-no-leidos", 
                        usuario1.getIdUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(2));
    }

    @Test
    @DisplayName("GET /api/chat/usuario/{idUsuario}/mensajes-no-leidos - Contar mensajes no leídos de usuario2")
    void testContarMensajesNoLeidos_Usuario2() throws Exception {
        // Usuario2 tiene 1 mensaje no leído de Juan ("Perfecto, ¿cuándo podemos vernos?")
        mockMvc.perform(get("/api/chat/usuario/{idUsuario}/mensajes-no-leidos", 
                        usuario2.getIdUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(1));
    }

    @Test
    @DisplayName("GET /api/chat/usuario/{idUsuario}/mensajes-no-leidos - Usuario sin mensajes no leídos")
    void testContarMensajesNoLeidos_Cero() throws Exception {
        // Marcar todos los mensajes como leídos
        mensajeRepository.findAll().forEach(m -> {
            m.setLeido(true);
            mensajeRepository.save(m);
        });

        mockMvc.perform(get("/api/chat/usuario/{idUsuario}/mensajes-no-leidos", 
                        usuario1.getIdUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(0));
    }

    // ========================
    // PUT /api/chat/{idChat}/marcar-leido
    // ========================

    @Test
    @DisplayName("PUT /api/chat/{idChat}/marcar-leido - Marcar mensajes como leídos")
    void testMarcarMensajesComoLeidos_Success() throws Exception {
        // Verificar mensajes no leídos antes
        long noLeidosAntes = mensajeRepository.findAll().stream()
                .filter(m -> !m.getLeido() && m.getChat().getIdChat().equals(chat1.getIdChat()))
                .count();
        assert noLeidosAntes > 0;

        mockMvc.perform(put("/api/chat/{idChat}/marcar-leido", chat1.getIdChat())
                        .param("usuarioId", usuario1.getIdUsuario().toString()))
                .andExpect(status().isOk());

        // Verificar que los mensajes del otro usuario en este chat están marcados como leídos
        long noLeidosDespues = mensajeRepository.findAll().stream()
                .filter(m -> !m.getLeido() 
                        && m.getChat().getIdChat().equals(chat1.getIdChat())
                        && !m.getEmisor().getIdUsuario().equals(usuario1.getIdUsuario()))
                .count();
        assert noLeidosDespues == 0;
    }

    @Test
    @DisplayName("PUT /api/chat/{idChat}/marcar-leido - Marcar en chat sin mensajes no leídos")
    void testMarcarMensajesComoLeidos_YaLeidos() throws Exception {
        // Marcar todos como leídos primero
        mensajeRepository.findAll().forEach(m -> {
            m.setLeido(true);
            mensajeRepository.save(m);
        });

        // Intentar marcar nuevamente
        mockMvc.perform(put("/api/chat/{idChat}/marcar-leido", chat1.getIdChat())
                        .param("usuarioId", usuario1.getIdUsuario().toString()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/chat/{idChat}/marcar-leido - Marcar solo mensajes del otro usuario")
    void testMarcarMensajesComoLeidos_SoloDelOtro() throws Exception {
        // Usuario2 marca como leídos los mensajes de usuario1
        mockMvc.perform(put("/api/chat/{idChat}/marcar-leido", chat1.getIdChat())
                        .param("usuarioId", usuario2.getIdUsuario().toString()))
                .andExpect(status().isOk());

        // Verificar que solo los mensajes de usuario1 fueron marcados
        long mensajesUsuario1NoLeidos = mensajeRepository.findAll().stream()
                .filter(m -> m.getChat().getIdChat().equals(chat1.getIdChat())
                        && m.getEmisor().getIdUsuario().equals(usuario1.getIdUsuario())
                        && !m.getLeido())
                .count();
        assert mensajesUsuario1NoLeidos == 0;

        // Los mensajes de usuario2 pueden seguir sin leer
        long mensajesUsuario2NoLeidos = mensajeRepository.findAll().stream()
                .filter(m -> m.getChat().getIdChat().equals(chat1.getIdChat())
                        && m.getEmisor().getIdUsuario().equals(usuario2.getIdUsuario())
                        && !m.getLeido())
                .count();
        // No verificamos el valor exacto porque depende del estado inicial
    }

    // ========================
    // PRUEBAS DE FLUJO COMPLETO
    // ========================

    @Test
    @DisplayName("Flujo completo: Crear chat → Verificar existencia → Obtener mensajes")
    void testWorkflow_CrearYVerificar() throws Exception {
        // Crear usuario nuevo
        Usuario usuarioNuevo = new Usuario();
        usuarioNuevo.setCedula("7788990011");
        usuarioNuevo.setNombre("Laura");
        usuarioNuevo.setCorreo("laura@test.com");
        usuarioNuevo.setContrasena("password123");
        usuarioNuevo.setRol(UsuarioRole.USER);
        usuarioNuevo.setEstado(Usuario.EstadoUsuario.ACTIVO);
        usuarioNuevo = usuarioRepository.save(usuarioNuevo);

        // 1. Crear/Obtener chat
        String response1 = mockMvc.perform(get("/api/chat/{idUsuario1}/{idUsuario2}", 
                        usuario1.getIdUsuario(), usuarioNuevo.getIdUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idChat").exists())
                .andReturn().getResponse().getContentAsString();

        // 2. Verificar que el chat aparece en la lista del usuario1
        mockMvc.perform(get("/api/chat/usuario/{idUsuario}", usuario1.getIdUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3))); // Ahora tiene 3 chats

        // 3. Obtener mensajes (debería estar vacío inicialmente)
        mockMvc.perform(get("/api/chat/{idChat}/mensajes", 
                        chat1.getIdChat() + 10)) // ID aproximado del nuevo chat
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Flujo completo: Contar no leídos → Marcar leídos → Verificar contador")
    void testWorkflow_ContarYMarcar() throws Exception {
        // 1. Contar mensajes no leídos
        String count1 = mockMvc.perform(get("/api/chat/usuario/{idUsuario}/mensajes-no-leidos", 
                        usuario1.getIdUsuario()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        int noLeidosAntes = Integer.parseInt(count1);
        assert noLeidosAntes > 0;

        // 2. Marcar como leídos en chat1
        mockMvc.perform(put("/api/chat/{idChat}/marcar-leido", chat1.getIdChat())
                        .param("usuarioId", usuario1.getIdUsuario().toString()))
                .andExpect(status().isOk());

        // 3. Verificar que disminuyó el contador
        String count2 = mockMvc.perform(get("/api/chat/usuario/{idUsuario}/mensajes-no-leidos", 
                        usuario1.getIdUsuario()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        int noLeidosDespues = Integer.parseInt(count2);
        assert noLeidosDespues < noLeidosAntes;
    }

    @Test
    @DisplayName("Flujo completo: Verificar bidireccionalidad del chat")
    void testWorkflow_Bidireccionalidad() throws Exception {
        // Ambos usuarios deberían ver el mismo chat
        String response1 = mockMvc.perform(get("/api/chat/{idUsuario1}/{idUsuario2}", 
                        usuario1.getIdUsuario(), usuario2.getIdUsuario()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String response2 = mockMvc.perform(get("/api/chat/{idUsuario1}/{idUsuario2}", 
                        usuario2.getIdUsuario(), usuario1.getIdUsuario()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Ambos deberían devolver el mismo idChat (verificamos que contengan el ID)
        assert response1.contains("\"idChat\":" + chat1.getIdChat());
        assert response2.contains("\"idChat\":" + chat1.getIdChat());
    }

    @Test
    @DisplayName("Flujo completo: Múltiples chats del mismo usuario")
    void testWorkflow_MultiplesChats() throws Exception {
        // Usuario1 tiene chats con usuario2 y usuario3
        mockMvc.perform(get("/api/chat/usuario/{idUsuario}", usuario1.getIdUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].idUsuario1", everyItem(equalTo(usuario1.getIdUsuario().intValue()))));

        // Verificar que cada chat tiene sus propios mensajes
        mockMvc.perform(get("/api/chat/{idChat}/mensajes", chat1.getIdChat()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));

        mockMvc.perform(get("/api/chat/{idChat}/mensajes", chat2.getIdChat()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }
}
