package com.techchallenge.oficina.ordem_servico.consultar;

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
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ConsultarOSController.class)
@Import(SecurityConfig.class)
@WithMockUser(roles = "ADMIN")
class ConsultarOSControllerTest {

    private static final UUID ID = UUID.fromString("33333333-4444-5555-6666-777777888888");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConsultarOSService service;

    @Test
    void deveListarOrdensDeServicoPeloId() throws Exception {
        when(service.listar(ID)).thenReturn(List.of(criarResponse()));

        mockMvc.perform(get("/os").param("id", ID.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].diagnostico").value("Ruído no motor"))
                .andExpect(jsonPath("$[0].orcamento.total").value(250.00))
                .andExpect(jsonPath("$[0].veiculo.placa").value("ABC1234"));

        verify(service).listar(ID);
    }

    @Test
    void deveBuscarOrdemDeServicoPelaPlaca() throws Exception {
        when(service.buscar("ABC1234")).thenReturn(criarResponse());

        mockMvc.perform(get("/os/{placa}", "ABC1234"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.diagnostico").value("Ruído no motor"))
                .andExpect(jsonPath("$.cliente.fullName").value("Ana Silva"));

        verify(service).buscar("ABC1234");
    }

    private OrdemServicoResponse criarResponse() {
        return new OrdemServicoResponse(
                new OrcamentoResponse(new BigDecimal("250.00"), "Recebida"),
                "Ruído no motor",
                null,
                new ClienteResponse("Ana Silva", "ana@email.com", "12345678901", "CPF", "11999999999"),
                new VeiculoResponse("ABC1234", "Fiat", "Uno", 2020),
                List.of(),
                List.of());
    }
}
