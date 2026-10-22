package com.techchallenge.oficina.ordem_servico.cadastrar;

import com.techchallenge.oficina.config.SecurityConfig;
import com.techchallenge.oficina.ordem_servico.dominio.response.ClienteResponse;
import com.techchallenge.oficina.ordem_servico.dominio.response.OrcamentoResponse;
import com.techchallenge.oficina.ordem_servico.dominio.response.OrdemServicoResponse;
import com.techchallenge.oficina.ordem_servico.dominio.response.VeiculoResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CadastrarOSController.class)
@Import(SecurityConfig.class)
@WithMockUser(roles = "ADMIN")
class CadastrarOSControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CadastrarOSService service;

    @Test
    void deveCadastrarOrdemDeServicoComSucesso() throws Exception {
        OrdemServicoResponse response = new OrdemServicoResponse(
                new OrcamentoResponse(new BigDecimal("150.00"), "Recebida"),
                "Revisão preventiva",
                null,
                new ClienteResponse("Ana Silva", "ana@email.com", "12345678901", "CPF", "11999999999"),
                new VeiculoResponse("ABC1234", "Fiat", "Uno", 2020),
                List.of(),
                List.of());
        when(service.cadastrar(any(CadastrarOSRequest.class))).thenReturn(response);

        mockMvc.perform(post("/os")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "servicos": [],
                                  "itens": [],
                                  "diagnostico": "Revisão preventiva",
                                  "placa": "ABC1234"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/os/ABC1234")))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.diagnostico").value("Revisão preventiva"))
                .andExpect(jsonPath("$.orcamento.total").value(150.00))
                .andExpect(jsonPath("$.cliente.fullName").value("Ana Silva"))
                .andExpect(jsonPath("$.veiculo.placa").value("ABC1234"));

        verify(service).cadastrar(any(CadastrarOSRequest.class));
    }
}
