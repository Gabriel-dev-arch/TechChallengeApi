package com.techchallenge.oficina.ordem_servico.cadastrar;

import com.techchallenge.oficina.config.SecurityConfig;
import com.techchallenge.oficina.funcionario.dominio.FuncionarioResponse;
import com.techchallenge.oficina.ordem_servico.cadastrar.request.CadastrarOSRequest;
import com.techchallenge.oficina.ordem_servico.cadastrar.request.ItemQuantidadeRequest;
import com.techchallenge.oficina.ordem_servico.cadastrar.request.ServicosRequest;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CadastrarOSController.class)
@Import(SecurityConfig.class)
@WithMockUser(roles = "ADMIN")
class CadastrarOSControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CadastrarOSService service;

    private static final UUID ID = UUID.randomUUID();

    private OrdemServicoResponse criarResponse() {
        return new OrdemServicoResponse(ID,
                new OrcamentoResponse(new BigDecimal("150.00"), "RECEBIDA"),
                "Revisão preventiva",
                null,
                new ClienteResponse("Ana Silva", "ana@email.com", "12345678901", "CPF", "11999999999"),
                new VeiculoResponse("ABC1234", "Fiat", "Uno", 2020),
                new FuncionarioResponse(ID, "Ana Silva", "ana.silva@empresa.com"),
                List.of(),
                List.of());
    }

    @Test
    @DisplayName("Deve cadastrar ordem de serviço com sucesso")
    void deveCadastrarOrdemDeServicoComSucesso() throws Exception {
        when(service.cadastrar(any(CadastrarOSRequest.class))).thenReturn(criarResponse());

        mockMvc.perform(post("/ordens-servico")
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
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/ordens-servico/ABC1234")))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.diagnostico").value("Revisão preventiva"))
                .andExpect(jsonPath("$.orcamento.total").value(150.00))
                .andExpect(jsonPath("$.cliente.fullName").value("Ana Silva"))
                .andExpect(jsonPath("$.veiculo.placa").value("ABC1234"));

        verify(service).cadastrar(any(CadastrarOSRequest.class));
    }

    @Test
    @DisplayName("Deve incluir serviços na OS")
    void deveIncluirServicosOS() throws Exception {
        when(service.incluiServicosOS(eq(ID), any())).thenReturn(criarResponse());

        mockMvc.perform(post("/ordens-servico/{id}/servicos", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                [
                                  {
                                    "id": "11111111-1111-1111-1111-111111111111",
                                    "descricao": "Troca de óleo",
                                    "valor": 50.0
                                  }
                                ]
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ID.toString()));

        verify(service).incluiServicosOS(eq(ID), any());
    }

    @Test
    @DisplayName("Deve incluir peças na OS")
    void deveIncluirPecasOS() throws Exception {
        when(service.incluiPecasInsumosOS(eq(ID), any())).thenReturn(criarResponse());

        mockMvc.perform(post("/ordens-servico/{id}/pecas", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                [
                                  {
                                    "id": "22222222-2222-2222-2222-222222222222",
                                    "quantidade": 2
                                  }
                                ]
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ID.toString()));

        verify(service).incluiPecasInsumosOS(eq(ID), any());
    }

    @Test
    @DisplayName("Deve iniciar diagnóstico")
    void deveIniciarDiagnostico() throws Exception {
        when(service.iniciaDiagnostico(ID)).thenReturn(criarResponse());

        mockMvc.perform(post("/ordens-servico/{id}/iniciar-diagnostico", ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ID.toString()));

        verify(service).iniciaDiagnostico(ID);
    }

    @Test
    @DisplayName("Deve cadastrar orçamento")
    void deveIniciarOrcamento() throws Exception {
        when(service.cadastrarOrcamento(ID)).thenReturn(criarResponse());

        mockMvc.perform(post("/ordens-servico/{id}/orcamento", ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ID.toString()));

        verify(service).cadastrarOrcamento(ID);
    }

    @Test
    @DisplayName("Deve iniciar execução")
    void deveIniciarExecucao() throws Exception {
        when(service.iniciaExecucao(ID)).thenReturn(criarResponse());

        mockMvc.perform(post("/ordens-servico/{id}/iniciar-execucao", ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ID.toString()));

        verify(service).iniciaExecucao(ID);
    }
}
