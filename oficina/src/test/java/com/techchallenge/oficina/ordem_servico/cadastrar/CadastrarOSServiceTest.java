package com.techchallenge.oficina.ordem_servico.cadastrar;

import com.techchallenge.oficina.clientes.entidades.Cliente;
import com.techchallenge.oficina.ordem_servico.dominio.OrdemServicoRepository;
import com.techchallenge.oficina.ordem_servico.dominio.exception.OrdemServicoEmAbertoException;
import com.techchallenge.oficina.ordem_servico.dominio.exception.VeiculoNaoEncontradoException;
import com.techchallenge.oficina.ordem_servico.dominio.response.OrdemServicoResponse;
import com.techchallenge.oficina.ordem_servico.entidades.OrdemServico;
import com.techchallenge.oficina.ordem_servico.entidades.StatusOS;
import com.techchallenge.oficina.pecasinsumos.dominio.PecaInsumoRepository;
import com.techchallenge.oficina.pecasinsumos.dominio.TipoItem;
import com.techchallenge.oficina.pecasinsumos.dominio.UnidadeMedida;
import com.techchallenge.oficina.pecasinsumos.entidades.PecaInsumo;
import com.techchallenge.oficina.servicos.dominio.ServicoRepository;
import com.techchallenge.oficina.servicos.entidades.Servico;
import com.techchallenge.oficina.shared.persistence.BaseEntity;
import com.techchallenge.oficina.veiculos.dominio.VeiculoRepository;
import com.techchallenge.oficina.veiculos.entidades.Veiculo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CadastrarOSServiceTest {

    private static final String PLACA = "ABC1234";

    @Mock
    private OrdemServicoRepository repository;

    @Mock
    private VeiculoRepository veiculoRepository;

    @Mock
    private ServicoRepository servicoRepository;

    @Mock
    private PecaInsumoRepository pecaInsumoRepository;

    @InjectMocks
    private CadastrarOSService service;

    @Test
    void deveCadastrarOrdemDeServicoComOrcamentoCalculado() {
        Cliente cliente = new Cliente("Ana", "Silva", "ana@email.com", "12345678901", "11999999999");
        Veiculo veiculo = new Veiculo(PLACA, "Fiat", "Uno", 2020, cliente);
        PecaInsumo peca = new PecaInsumo(TipoItem.PECA, "OLEO", "Óleo", "Óleo do motor",
                UnidadeMedida.UN, new BigDecimal("15.00"), new BigDecimal("10"));
        setId(peca, UUID.randomUUID());
        Servico servico = new Servico("Revisão", new BigDecimal("120.00"));
        CadastrarOSRequest request = new CadastrarOSRequest(
                List.of(UUID.randomUUID()),
                List.of(new ItemQuantidade(peca.getId(), new BigDecimal("2"))),
                "Revisão preventiva",
                PLACA);

        when(repository.buscarPorPlacaComOSEmAberto(PLACA)).thenReturn(List.of());
        when(veiculoRepository.findByPlaca(PLACA)).thenReturn(Optional.of(veiculo));
        when(servicoRepository.findAllById(request.servicos())).thenReturn(List.of(servico));
        when(pecaInsumoRepository.findAllById(List.of(peca.getId()))).thenReturn(List.of(peca));
        when(repository.save(any(OrdemServico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrdemServicoResponse response = service.cadastrar(request);

        assertThat(response.diagnostico()).isEqualTo("Revisão preventiva");
        assertThat(response.orcamento().total()).isEqualByComparingTo("150.00");
        assertThat(response.orcamento().status()).isEqualTo("Recebida");
        assertThat(response.cliente().fullName()).isEqualTo("Ana Silva");
        assertThat(response.veiculo().placa()).isEqualTo(PLACA);
        assertThat(response.itens()).hasSize(1);
        assertThat(response.servicos()).extracting("descricao").containsExactly("Revisão");

        ArgumentCaptor<OrdemServico> ordemCaptor = ArgumentCaptor.forClass(OrdemServico.class);
        verify(repository).save(ordemCaptor.capture());
        assertThat(ordemCaptor.getValue().getStatus()).isEqualTo(StatusOS.RECEBIDA);
        assertThat(ordemCaptor.getValue().getOrcamento()).isEqualByComparingTo("150.00");
    }

    @Test
    void deveImpedirCadastroQuandoVeiculoJaTemOrdemEmAberto() {
        CadastrarOSRequest request = new CadastrarOSRequest(List.of(), List.of(), "Diagnóstico", PLACA);
        when(repository.buscarPorPlacaComOSEmAberto(PLACA)).thenReturn(List.of(
                new OrdemServico(StatusOS.RECEBIDA, BigDecimal.ZERO, List.of(), List.of(), "Aberta", null, null)));

        assertThatThrownBy(() -> service.cadastrar(request))
                .isInstanceOf(OrdemServicoEmAbertoException.class)
                .hasMessage("Veículo com ordem de serviço em aberto: " + PLACA);

        verify(veiculoRepository, never()).findByPlaca(any());
        verify(repository, never()).save(any(OrdemServico.class));
    }

    @Test
    void deveLancarExcecaoQuandoVeiculoNaoExiste() {
        CadastrarOSRequest request = new CadastrarOSRequest(List.of(), List.of(), "Diagnóstico", PLACA);
        when(repository.buscarPorPlacaComOSEmAberto(PLACA)).thenReturn(List.of());
        when(veiculoRepository.findByPlaca(PLACA)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cadastrar(request))
                .isInstanceOf(VeiculoNaoEncontradoException.class)
                .hasMessage("Veículo não encontrado: " + PLACA);

        verify(repository, never()).save(any(OrdemServico.class));
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
