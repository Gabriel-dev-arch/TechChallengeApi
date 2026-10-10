package com.techchallenge.oficina.servicos.cadastrar;

import com.techchallenge.oficina.config.SecurityConfig;
import com.techchallenge.oficina.servicos.dominio.ServicoResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CadastrarServicoController.class)
@Import(SecurityConfig.class)
class CadastrarServicoControllerTest {

    private static final UUID ID = UUID.fromString("22222222-3333-4444-5555-666666777777");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CadastrarServicoService service;

    @Test
    void deveCadastrarServicoComSucesso() throws Exception {
        ServicoResponse response = new ServicoResponse(ID, "Troca de óleo", new BigDecimal("180.00"));

        when(service.cadastrar(any(CadastrarServicoRequest.class))).thenReturn(response);

        mockMvc.perform(post("/servicos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "descricao": "Troca de óleo",
                                  "valor": 180.00
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/servicos/")))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(ID.toString())))
                .andExpect(jsonPath("$.descricao", is("Troca de óleo")))
                .andExpect(jsonPath("$.valor", is(180.00)));

        verify(service).cadastrar(any(CadastrarServicoRequest.class));
    }

    @Test
    void deveRetornarBadRequestQuandoDadosForemInvalidos() throws Exception {
        mockMvc.perform(post("/servicos")
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
