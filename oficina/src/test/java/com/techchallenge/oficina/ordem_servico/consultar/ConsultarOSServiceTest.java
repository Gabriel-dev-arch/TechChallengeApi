package com.techchallenge.oficina.ordem_servico.consultar;

import com.techchallenge.oficina.autenticacao.dominio.ControleDeAcesso;
import com.techchallenge.oficina.clientes.entidades.Cliente;
import com.techchallenge.oficina.ordem_servico.cadastrar.request.ItemQuantidade;
import com.techchallenge.oficina.ordem_servico.dominio.OrdemServicoRepository;
import com.techchallenge.oficina.ordem_servico.dominio.exception.OrdemServicoNaoEncontradaException;
import com.techchallenge.oficina.ordem_servico.dominio.response.OrdemServicoResponse;
import com.techchallenge.oficina.ordem_servico.entidades.OrdemServico;
import com.techchallenge.oficina.ordem_servico.entidades.StatusOS;
import com.techchallenge.oficina.pecasinsumos.entidades.PecaInsumo;
import com.techchallenge.oficina.servicos.entidades.Servico;
import com.techchallenge.oficina.shared.persistence.BaseEntity;
import com.techchallenge.oficina.veiculos.entidades.Veiculo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultarOSServiceTest {

    private static final UUID ID = UUID.fromString("33333333-4444-5555-6666-777777888888");
    private static final String PLACA = "ABC1234";

    @Mock
    private OrdemServicoRepository repository;

    @Mock
    private ControleDeAcesso controleDeAcesso;

    @InjectMocks
    private ConsultarOSService service;

    @Test
    @DisplayName("Deve buscar ordem de serviço por ID com sucesso")
    void deveListarOrdemDeServicoPorId() {
        OrdemServico ordem = criarOrdem();
        when(repository.findById(ID)).thenReturn(Optional.of(ordem));

        List<OrdemServicoResponse> responses = Collections.singletonList(service.buscarOSPeloID(ID));

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().diagnostico()).isEqualTo("Ruído no motor");
        assertThat(responses.getFirst().orcamento().total()).isEqualByComparingTo("250.00");
        assertThat(responses.getFirst().cliente().fullName()).isEqualTo("Ana Silva");
        assertThat(responses.getFirst().veiculo().placa()).isEqualTo(PLACA);
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar OS por ID inexistente")
    void deveLancarExcecaoQuandoNaoEncontrarPorId() {
        when(repository.findById(ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarOSPeloID(ID))
                .isInstanceOf(OrdemServicoNaoEncontradaException.class);
    }

    @Test
    @DisplayName("Deve consultar status da OS por ID")
    void deveConsultarStatusOS() {
        OrdemServico ordem = criarOrdem();
        when(repository.findById(ID)).thenReturn(Optional.of(ordem));

        OrdemServicoResponse response = service.consultarStatusOS(ID);

        assertThat(response).isNotNull();
        assertThat(response.diagnostico()).isEqualTo("Ruído no motor");
        verify(repository).findById(ID);
    }

    @Test
    @DisplayName("Deve lançar exceção ao consultar status de OS inexistente")
    void deveLancarExcecaoAoConsultarStatusInexistente() {
        when(repository.findById(ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.consultarStatusOS(ID))
                .isInstanceOf(OrdemServicoNaoEncontradaException.class);
    }

    @Test
    @DisplayName("Deve listar ordens de serviço por status com paginação")
    void deveListarPorStatus() {
        OrdemServico ordem = criarOrdem();
        Pageable pageable = PageRequest.of(0, 10);
        when(repository.buscarPorStatus(StatusOS.RECEBIDA, pageable)).thenReturn(List.of(ordem));

        PageImpl<OrdemServicoResponse> pagina = service.listarOrdemServicoEmAberto(StatusOS.RECEBIDA, pageable);

        assertThat(pagina.getContent()).hasSize(1);
        assertThat(pagina.getContent().getFirst().veiculo().placa()).isEqualTo(PLACA);
        verify(repository).buscarPorStatus(StatusOS.RECEBIDA, pageable);
    }

    private OrdemServico criarOrdem() {
        Cliente cliente = new Cliente("Ana", "Silva", "ana@email.com", "12345678901", "11999999999");
        Veiculo veiculo = new Veiculo(PLACA, "Fiat", "Uno", 2020, cliente);
        Servico servico = new Servico("Revisão", new BigDecimal("250.00"));

        PecaInsumo peca = new PecaInsumo(
                com.techchallenge.oficina.pecasinsumos.dominio.TipoItem.PECA,
                "OLEO", "Óleo", "Óleo do motor",
                com.techchallenge.oficina.pecasinsumos.dominio.UnidadeMedida.UN,
                new BigDecimal("20.00"), new BigDecimal("10"));

        setId(peca, UUID.randomUUID());

        return new OrdemServico(StatusOS.RECEBIDA, new BigDecimal("250.00"),
                List.of(servico), List.of(peca),
                List.of(new ItemQuantidade(peca.getId(), BigDecimal.ONE)),
                "Ruído no motor", veiculo, cliente,
                new Funcionario("Ana Silva", "ana.silva@empresa.com", (short) 1234, false, true));
    }

    private void setId(PecaInsumo peca, UUID id) {
        try {
            var field = BaseEntity.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(peca, id);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Não foi possível configurar o id da peça", e);
        }
    }
}