package com.techchallenge.oficina.ordem_servico.consultar;

import com.techchallenge.oficina.config.SecurityConfig;
import com.techchallenge.oficina.funcionario.dominio.FuncionarioResponse;
import com.techchallenge.oficina.ordem_servico.dominio.response.ClienteResponse;
import com.techchallenge.oficina.ordem_servico.dominio.response.OrcamentoResponse;
import com.techchallenge.oficina.ordem_servico.dominio.response.OrdemServicoResponse;
import com.techchallenge.oficina.ordem_servico.dominio.response.VeiculoResponse;
import com.techchallenge.oficina.ordem_servico.entidades.StatusOS;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
    @DisplayName("Deve buscar ordem de serviço pelo ID")
    void deveBuscarOrdemDeServicoPeloId() throws Exception {
        OrdemServicoResponse response = criarResponse();
        when(service.buscarOSPeloID(ID)).thenReturn(response);

        mockMvc.perform(get("/ordens-servico/{id}", ID))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(response.id().toString()))
                .andExpect(jsonPath("$.diagnostico").value("Ruído no motor"))
                .andExpect(jsonPath("$.orcamento.total").value(250.00))
                .andExpect(jsonPath("$.veiculo.placa").value("ABC1234"));

        verify(service).buscarOSPeloID(ID);
    }

    @Test
    @DisplayName("Deve listar ordens de serviço por status com paginação")
    void deveListarOrdensDeServicoPorStatus() throws Exception {
        OrdemServicoResponse response = criarResponse();
        PageImpl<OrdemServicoResponse> page = new PageImpl<>(List.of(response));
        when(service.listarOrdemServicoEmAberto(eq(StatusOS.RECEBIDA), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/ordens-servico")
                        .param("status", "RECEBIDA")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].diagnostico").value("Ruído no motor"));

        verify(service).listarOrdemServicoEmAberto(eq(StatusOS.RECEBIDA), any(Pageable.class));
    }

    @Test
    @DisplayName("Aceita o status em minúsculas")
    void deveAceitarStatusEmMinusculas() throws Exception {
        when(service.listarOrdemServicoEmAberto(eq(StatusOS.RECEBIDA), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(criarResponse())));

        mockMvc.perform(get("/ordens-servico").param("status", "recebida"))
                .andExpect(status().isOk());

        verify(service).listarOrdemServicoEmAberto(eq(StatusOS.RECEBIDA), any(Pageable.class));
    }

    @Test
    @DisplayName("Status desconhecido devolve 400 com os valores aceitos")
    void deveRejeitarStatusDesconhecido() throws Exception {
        mockMvc.perform(get("/ordens-servico").param("status", "XYZ"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("Valores aceitos")));
    }

    private OrdemServicoResponse criarResponse() {
        return new OrdemServicoResponse(UUID.randomUUID(),
                new OrcamentoResponse(new BigDecimal("250.00"), "Recebida"),
                "Ruído no motor",
                null,
                new ClienteResponse("Ana Silva", "ana@email.com", "12345678901", "CPF", "11999999999"),
                new VeiculoResponse("ABC1234", "Fiat", "Uno", 2020),
                new FuncionarioResponse(null, "Ana Silva", "ana.silva@empresa.com"),
                List.of(),
                List.of());
    }
}
