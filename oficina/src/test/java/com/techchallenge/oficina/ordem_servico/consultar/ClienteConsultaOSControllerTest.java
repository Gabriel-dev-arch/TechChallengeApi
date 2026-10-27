package com.techchallenge.oficina.ordem_servico.consultar;

import com.techchallenge.oficina.config.SecurityConfig;
import com.techchallenge.oficina.funcionario.dominio.FuncionarioResponse;
import com.techchallenge.oficina.ordem_servico.dominio.response.ClienteResponse;
import com.techchallenge.oficina.ordem_servico.dominio.response.OrcamentoResponse;
import com.techchallenge.oficina.ordem_servico.dominio.response.OrdemServicoResponse;
import com.techchallenge.oficina.ordem_servico.dominio.response.VeiculoResponse;
import org.junit.jupiter.api.DisplayName;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClienteConsultaOSController.class)
@Import(SecurityConfig.class)
@WithMockUser(roles = "ADMIN")
class ClienteConsultaOSControllerTest {

    private static final UUID ID = UUID.randomUUID();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConsultarOSService service;

    @Test
    @DisplayName("Deve consultar status da OS com sucesso")
    void deveConsultarStatusOS() throws Exception {
        OrdemServicoResponse response = new OrdemServicoResponse(ID,
                new OrcamentoResponse(new BigDecimal("300.00"), "RECEBIDA"),
                "Diagnostico",
                null,
                new ClienteResponse("Carlos Santos", "carlos@email.com", "12345678901", "CPF", "11988888888"),
                new VeiculoResponse("XYZ9876", "Honda", "Civic", 2022),
                new FuncionarioResponse(ID, "Carlos Santos", "carlos@empresa.com"),
                List.of(),
                List.of());

        when(service.consultarStatusOS(ID)).thenReturn(response);

        mockMvc.perform(get("/public/os/{id}", ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ID.toString()))
                .andExpect(jsonPath("$.diagnostico").value("Diagnostico"))
                .andExpect(jsonPath("$.orcamento.total").value(300.00))
                .andExpect(jsonPath("$.veiculo.placa").value("XYZ9876"));

        verify(service).consultarStatusOS(ID);
    }
}
