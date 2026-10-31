package com.techchallenge.oficina.ordem_servico.cadastrar;

import com.techchallenge.oficina.clientes.entidades.Cliente;
import com.techchallenge.oficina.funcionario.dominio.*;
import com.techchallenge.oficina.funcionario.entidades.Funcionario;
import com.techchallenge.oficina.ordem_servico.cadastrar.request.*;
import com.techchallenge.oficina.ordem_servico.dominio.OrdemServicoRepository;
import com.techchallenge.oficina.ordem_servico.dominio.exception.*;
import com.techchallenge.oficina.ordem_servico.dominio.statusos.EstadosOS;
import com.techchallenge.oficina.ordem_servico.entidades.*;
import com.techchallenge.oficina.ordem_servico.finalizar.FinalizarOSService;
import com.techchallenge.oficina.pecasinsumos.dominio.*;
import com.techchallenge.oficina.pecasinsumos.entidades.PecaInsumo;
import com.techchallenge.oficina.servicos.dominio.ServicoRepository;
import com.techchallenge.oficina.servicos.entidades.Servico;
import com.techchallenge.oficina.veiculos.dominio.VeiculoRepository;
import com.techchallenge.oficina.veiculos.entidades.Veiculo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CadastrarOSFluxoServiceTest {

    private static final UUID ID = UUID.randomUUID();
    @Mock OrdemServicoRepository repository;
    @Mock VeiculoRepository veiculoRepository;
    @Mock ServicoRepository servicoRepository;
    @Mock PecaInsumoRepository pecaInsumoRepository;
    @Mock FuncionarioRepository funcionarioRepository;
    @Mock EstadosOS estadosOS;
    @Mock FinalizarOSService finalizarOSService;
    @InjectMocks CadastrarOSService service;

    private OrdemServico os(StatusOS status) {
        var cliente = new Cliente("Maria", "Silva", "maria@email.com", "52998224725", null);
        return new OrdemServico(status, BigDecimal.ZERO, new ArrayList<>(), List.of(), List.of(), null,
                new Veiculo("ABC1234", "Fiat", "Uno", 2020, cliente), cliente,
                new Funcionario("Mecânico", "mecanico@email.com", (short) 1, false, true));
    }

    private CadastrarOSRequest request() {
        return new CadastrarOSRequest(List.of(), List.of(), null, "abc-1234", new FuncionarioRequest(ID));
    }

    @Test
    void cadastraComPlacaNormalizada() {
        var os = os(StatusOS.RECEBIDA);
        when(veiculoRepository.findByPlaca("ABC1234")).thenReturn(Optional.of(os.getVeiculo()));
        when(funcionarioRepository.findById(ID)).thenReturn(Optional.of(os.getFuncionario()));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        assertThat(service.cadastrar(request()).veiculo().placa()).isEqualTo("ABC1234");
        verify(repository).buscarPorPlacaComOSEmAberto("ABC1234");
    }

    @Test
    void cadastroImpedeOsAbertaVeiculoOuFuncionarioInexistentes() {
        var os = os(StatusOS.RECEBIDA);
        when(repository.buscarPorPlacaComOSEmAberto("ABC1234")).thenReturn(List.of(os));
        assertThatThrownBy(() -> service.cadastrar(request())).isInstanceOf(OrdemServicoEmAbertoException.class);
        when(repository.buscarPorPlacaComOSEmAberto("ABC1234")).thenReturn(List.of());
        assertThatThrownBy(() -> service.cadastrar(request())).isInstanceOf(VeiculoNaoEncontradoException.class);
        when(veiculoRepository.findByPlaca("ABC1234")).thenReturn(Optional.of(os.getVeiculo()));
        assertThatThrownBy(() -> service.cadastrar(request())).isInstanceOf(FuncionarioNaoEncontradoException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void transicoesValidasEDelegacaoDaFinalizacao() {
        var os = os(StatusOS.RECEBIDA);
        when(repository.findById(ID)).thenReturn(Optional.of(os));
        service.iniciaDiagnostico(ID);
        os.atualizaStatus(StatusOS.APROVADA);
        service.iniciaExecucao(ID);
        os.atualizaStatus(StatusOS.FINALIZADA);
        service.entregaVeiculo(ID);
        service.finalizaExecucao(ID);
        verify(estadosOS, times(3)).executa(os);
        verify(finalizarOSService).finalizar(ID);
    }

    @Test
    void rotasDeOutrasEtapasNaoPodemFinalizarOuBaixarEstoque() {
        var os = os(StatusOS.EM_EXECUCAO);
        when(repository.findById(ID)).thenReturn(Optional.of(os));
        assertThatThrownBy(() -> service.iniciaDiagnostico(ID)).isInstanceOf(TransicaoInvalidaException.class);
        assertThatThrownBy(() -> service.iniciaExecucao(ID)).isInstanceOf(TransicaoInvalidaException.class);
        assertThatThrownBy(() -> service.entregaVeiculo(ID)).isInstanceOf(TransicaoInvalidaException.class);
        assertThatThrownBy(() -> service.cadastrarOrcamento(ID)).isInstanceOf(TransicaoInvalidaException.class);
        verifyNoInteractions(estadosOS, finalizarOSService);
    }

    @Test
    void operacoesComOsInexistenteRetornamErro() {
        List<Consumer<UUID>> operacoes = List.of(service::iniciaDiagnostico, service::iniciaExecucao,
                service::entregaVeiculo, service::cadastrarOrcamento,
                id -> service.incluiServicosOS(id, List.of()), id -> service.incluiPecasInsumosOS(id, List.of()));
        operacoes.forEach(operacao -> assertThatThrownBy(() -> operacao.accept(ID))
                .isInstanceOf(OrdemServicoNaoEncontradaException.class));
    }

    @Test
    void incluiServicoPecaEOrcamento() {
        var os = os(StatusOS.RECEBIDA);
        when(repository.findById(ID)).thenReturn(Optional.of(os));
        when(repository.saveAndFlush(os)).thenReturn(os);
        when(repository.save(os)).thenReturn(os);
        when(servicoRepository.findAllById(List.of(ID))).thenReturn(List.of(new Servico("Troca de óleo", BigDecimal.TEN)));
        service.incluiServicosOS(ID, List.of(new ServicosRequest(ID)));
        var peca = new PecaInsumo(TipoItem.PECA, "FILTRO", "Filtro", "Filtro de óleo", UnidadeMedida.UN,
                BigDecimal.TEN, BigDecimal.TEN);
        ReflectionTestUtils.setField(peca, "id", ID);
        when(pecaInsumoRepository.findAllById(List.of(ID))).thenReturn(List.of(peca));
        service.incluiPecasInsumosOS(ID, List.of(new ItemQuantidadeRequest(ID, BigDecimal.ONE)));
        assertThat(peca.getQuantidadeReservada()).isEqualByComparingTo("1");
        service.cadastrarOrcamento(ID);
        assertThat(os.getStatus()).isEqualTo(StatusOS.AGUARDANDO_APROVACAO);
        assertThat(os.getOrcamento()).isEqualByComparingTo("20");
    }

    @Test
    void orcamentoDuranteDiagnosticoTambemEhPermitido() {
        var os = os(StatusOS.EM_DIAGNOSTICO);
        when(repository.findById(ID)).thenReturn(Optional.of(os));
        when(repository.saveAndFlush(os)).thenReturn(os);
        service.cadastrarOrcamento(ID);
        assertThat(os.getStatus()).isEqualTo(StatusOS.AGUARDANDO_APROVACAO);
    }
}
