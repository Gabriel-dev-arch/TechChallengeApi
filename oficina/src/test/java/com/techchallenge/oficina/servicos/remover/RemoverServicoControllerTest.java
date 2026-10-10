package com.techchallenge.oficina.servicos.remover;

import com.techchallenge.oficina.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RemoverServicoController.class)
@Import(SecurityConfig.class)
class RemoverServicoControllerTest {

    private static final UUID ID = UUID.fromString("66666666-7777-8888-9999-000000111111");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RemoverServicoService service;

    @Test
    void deveRemoverServicoComSucesso() throws Exception {
        mockMvc.perform(delete("/servicos/{id}", ID))
                .andExpect(status().isNoContent());

        verify(service).remover(ID);
    }

    @Test
    void deveRetornarNotFoundQuandoServicoNaoExiste() throws Exception {
        doThrow(new com.techchallenge.oficina.servicos.dominio.ServicoNaoEncontradoException(ID))
                .when(service).remover(ID);

        mockMvc.perform(delete("/servicos/{id}", ID))
                .andExpect(status().isNotFound());
    }
}
