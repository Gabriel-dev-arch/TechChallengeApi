package com.techchallenge.oficina.servicos.consultar;

import com.techchallenge.oficina.config.SecurityConfig;
import com.techchallenge.oficina.servicos.dominio.ServicoResponse;
import com.techchallenge.oficina.servicos.entidades.Servico;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ConsultarServicoController.class)
@Import(SecurityConfig.class)
class ConsultarServicoControllerTest {

    private static final UUID ID = UUID.fromString("44444444-5555-6666-7777-888888999999");
    private static final UUID ID_2 = UUID.fromString("77777777-5555-6666-7777-888888999999");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConsultarServicoService service;

    @Test
    void deveConsultarServicoPorId() throws Exception {
        ServicoResponse response = new ServicoResponse(ID, "Revisão geral", new BigDecimal("250.00"));

        when(service.listar(ID)).thenReturn(response);

        mockMvc.perform(get("/servicos/{id}", ID))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(ID.toString())))
                .andExpect(jsonPath("$.descricao", is("Revisão geral")))
                .andExpect(jsonPath("$.valor", is(250.00)));
    }

    @Test
    void deveListarTodosOsServicos() throws Exception {
        when(service.listarTodos()).thenReturn(List.of(
                new ServicoResponse(ID, "Revisão geral", new BigDecimal("250.00")),
                new ServicoResponse(UUID.randomUUID(), "Alinhamento", new BigDecimal("120.00"))
        ));

        mockMvc.perform(get("/servicos"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].descricao", is("Revisão geral")))
                .andExpect(jsonPath("$[1].descricao", is("Alinhamento")));
    }

    @Test
    void deveBuscarPorDescricao() throws Exception {
        Servico servico1 = new Servico("Revisão geral", new BigDecimal("250.00"));
        Servico servico2 = new Servico("Revisão leve", new BigDecimal("120.00"));

        when(service.buscarPorDescricao("revisão")).thenReturn(List.of(servico1, servico2));

        mockMvc.perform(get("/servicos/descricao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descricao\":\"revisão\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].descricao", is("Revisão geral")))
                .andExpect(jsonPath("$[1].descricao", is("Revisão leve")));
    }
}
