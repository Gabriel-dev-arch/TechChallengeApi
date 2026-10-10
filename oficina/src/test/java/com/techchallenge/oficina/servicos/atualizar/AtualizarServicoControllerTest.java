package com.techchallenge.oficina.servicos.atualizar;

import com.techchallenge.oficina.config.SecurityConfig;
import com.techchallenge.oficina.servicos.dominio.ServicoResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AtualizarServicoController.class)
@Import(SecurityConfig.class)
class AtualizarServicoControllerTest {

    private static final UUID ID = UUID.fromString("11111111-2222-3333-4444-555555666666");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AtualizarServicoService service;

    @Test
    void deveAtualizarServicoComSucesso() throws Exception {
        ServicoResponse response = new ServicoResponse(ID, "Troca de óleo", new BigDecimal("180.00"));

        when(service.atualizar(eq(ID), any(AtualizarServicoRequest.class))).thenReturn(response);

        mockMvc.perform(put("/servicos/{id}", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "descricao": "Troca de óleo",
                                  "valor": 180.00
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(ID.toString())))
                .andExpect(jsonPath("$.descricao", is("Troca de óleo")))
                .andExpect(jsonPath("$.valor", is(180.00)));

        verify(service).atualizar(eq(ID), any(AtualizarServicoRequest.class));
    }

    @Test
    void deveRetornarBadRequestQuandoDadosForemInvalidos() throws Exception {
        mockMvc.perform(put("/servicos/{id}", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "descricao": "",
                                  "valor": null
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errors[*].campo", hasItems("descricao", "valor")))
                .andExpect(jsonPath("$.errors[*].mensagem", hasItems("must not be blank", "must not be null")));
    }
}
