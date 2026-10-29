package com.techchallenge.oficina.ordem_servico.cadastrar;

import com.techchallenge.oficina.clientes.entidades.Cliente;
import com.techchallenge.oficina.funcionario.dominio.FuncionarioNaoEncontradoException;
import com.techchallenge.oficina.funcionario.dominio.FuncionarioRepository;
import com.techchallenge.oficina.funcionario.entidades.Funcionario;
import com.techchallenge.oficina.ordem_servico.cadastrar.request.CadastrarOSRequest;
import com.techchallenge.oficina.ordem_servico.cadastrar.request.FuncionarioRequest;
import com.techchallenge.oficina.ordem_servico.cadastrar.request.ItemQuantidadeRequest;
import com.techchallenge.oficina.ordem_servico.cadastrar.request.ServicosRequest;
import com.techchallenge.oficina.ordem_servico.dominio.OrdemServicoRepository;
import com.techchallenge.oficina.ordem_servico.dominio.exception.OrdemServicoEmAbertoException;
import com.techchallenge.oficina.ordem_servico.dominio.exception.OrdemServicoNaoEncontradaException;
import com.techchallenge.oficina.ordem_servico.dominio.exception.VeiculoNaoEncontradoException;
import com.techchallenge.oficina.ordem_servico.dominio.response.OrdemServicoResponse;
import com.techchallenge.oficina.ordem_servico.dominio.statusos.EstadosOS;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CadastrarOSServiceTest {

    private static final String PLACA = "ABC1234";
    private static final UUID OS_ID = UUID.randomUUID();
    private static final UUID FUNCIONARIO_ID = UUID.randomUUID();

    @Mock
    private OrdemServicoRepository repository;
    @Mock
    private VeiculoRepository veiculoRepository;
    @Mock
    private ServicoRepository servicoRepository;
    @Mock
    private PecaInsumoRepository pecaInsumoRepository;
    @Mock
    private FuncionarioRepository funcionarioRepository;
    @Mock
    private EstadosOS estadosOS;

    @InjectMocks
    private CadastrarOSService service;

    @Test
    void naoCadastraQuandoVeiculoJaTemOSAberta() {
        when(repository.buscarPorPlacaComOSEmAberto(PLACA)).thenReturn(List.of(criarOS(StatusOS.RECEBIDA)));

        assertThatThrownBy(() -> service.cadastrar(criarRequest(PLACA)))
                .isInstanceOf(OrdemServicoEmAbertoException.class);

        verifyNoInteractions(veiculoRepository, funcionarioRepository);
        verify(repository, never()).save(any(OrdemServico.class));
    }

    @Test
    void naoCadastraQuandoVeiculoNaoExiste() {
        when(repository.buscarPorPlacaComOSEmAberto(PLACA)).thenReturn(List.of());
        when(veiculoRepository.findByPlaca(PLACA)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cadastrar(criarRequest(PLACA)))
                .isInstanceOf(VeiculoNaoEncontradoException.class);

        verifyNoInteractions(funcionarioRepository);
        verify(repository, never()).save(any(OrdemServico.class));
    }

    @Test
    void naoCadastraQuandoFuncionarioNaoExiste() {
        when(repository.buscarPorPlacaComOSEmAberto(PLACA)).thenReturn(List.of());
        when(veiculoRepository.findByPlaca(PLACA)).thenReturn(Optional.of(criarVeiculo()));
        when(funcionarioRepository.findById(FUNCIONARIO_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cadastrar(criarRequest(PLACA)))
                .isInstanceOf(FuncionarioNaoEncontradoException.class);

        verify(repository, never()).save(any(OrdemServico.class));
    }

    @Test
    void cadastraOSComSucesso() {
        when(repository.buscarPorPlacaComOSEmAberto(PLACA)).thenReturn(List.of());
        when(veiculoRepository.findByPlaca(PLACA)).thenReturn(Optional.of(criarVeiculo()));
        when(funcionarioRepository.findById(FUNCIONARIO_ID)).thenReturn(Optional.of(criarFuncionario()));
        when(repository.save(any(OrdemServico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrdemServicoResponse response = service.cadastrar(criarRequest(PLACA));

        assertThat(response.veiculo().placa()).isEqualTo(PLACA);
        verify(repository).save(argThat(os -> os.getStatus() == StatusOS.RECEBIDA));
    }

    @Test
    void normalizaPlacaAntesDeBuscarVeiculo() {
        when(repository.buscarPorPlacaComOSEmAberto(PLACA)).thenReturn(List.of());
        when(veiculoRepository.findByPlaca(PLACA)).thenReturn(Optional.of(criarVeiculo()));
        when(funcionarioRepository.findById(FUNCIONARIO_ID)).thenReturn(Optional.of(criarFuncionario()));
        when(repository.save(any(OrdemServico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrdemServicoResponse response = service.cadastrar(criarRequest(" abc-1234 "));

        assertThat(response.veiculo().placa()).isEqualTo(PLACA);
        verify(repository).buscarPorPlacaComOSEmAberto(PLACA);
        verify(veiculoRepository).findByPlaca(PLACA);
    }

    @Test
    void iniciaDiagnosticoOSExistente() {
        OrdemServico os = criarOS(StatusOS.RECEBIDA);
        when(repository.findById(OS_ID)).thenReturn(Optional.of(os));

        assertThat(service.iniciaDiagnostico(OS_ID)).isNotNull();

        verify(estadosOS).executa(os, StatusOS.RECEBIDA);
    }

    @Test
    void iniciaDiagnosticoOSInexistente() {
        when(repository.findById(OS_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.iniciaDiagnostico(OS_ID))
                .isInstanceOf(OrdemServicoNaoEncontradaException.class);
    }

    @Test
    void iniciaExecucaoOSExistente() {
        OrdemServico os = criarOS(StatusOS.APROVADA);
        when(repository.findById(OS_ID)).thenReturn(Optional.of(os));

        assertThat(service.iniciaExecucao(OS_ID)).isNotNull();

        verify(estadosOS).executa(os, StatusOS.EM_EXECUCAO);
    }

    @Test
    void iniciaExecucaoOSInexistente() {
        when(repository.findById(OS_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.iniciaExecucao(OS_ID))
                .isInstanceOf(OrdemServicoNaoEncontradaException.class);
    }

    @Test
    void finalizaExecucaoOSExistente() {
        OrdemServico os = criarOS(StatusOS.EM_EXECUCAO);
        when(repository.findById(OS_ID)).thenReturn(Optional.of(os));

        assertThat(service.finalizaExecucao(OS_ID)).isNotNull();

        verify(estadosOS).executa(os, StatusOS.FINALIZADA);
    }

    @Test
    void finalizaExecucaoOSInexistente() {
        when(repository.findById(OS_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.finalizaExecucao(OS_ID))
                .isInstanceOf(OrdemServicoNaoEncontradaException.class);
    }

    @Test
    void entregaVeiculoOSExistente() {
        OrdemServico os = criarOS(StatusOS.ENTREGUE);
        when(repository.findById(OS_ID)).thenReturn(Optional.of(os));

        assertThat(service.entregaVeiculo(OS_ID)).isNotNull();

        verify(estadosOS).executa(os, StatusOS.ENTREGUE);
    }

    @Test
    void entregaVeiculoOSInexistente() {
        when(repository.findById(OS_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.entregaVeiculo(OS_ID))
                .isInstanceOf(OrdemServicoNaoEncontradaException.class);
    }

    @Test
    void incluiServicosNaOS() throws ReflectiveOperationException {
        OrdemServico os = criarOS(StatusOS.RECEBIDA);
        UUID servicoId = UUID.randomUUID();
        Servico servico = new Servico("Troca de óleo", new BigDecimal("50.00"));
        setId(servico, servicoId);
        when(repository.findById(OS_ID)).thenReturn(Optional.of(os));
        when(servicoRepository.findAllById(List.of(servicoId))).thenReturn(List.of(servico));
        when(repository.saveAndFlush(os)).thenReturn(os);

        assertThat(service.incluiServicosOS(OS_ID, List.of(new ServicosRequest(servicoId)))).isNotNull();

        assertThat(os.getOrcamento()).isEqualByComparingTo("50.00");
        verify(repository).saveAndFlush(os);
    }

    @Test
    void incluiServicosEmOSInexistente() {
        when(repository.findById(OS_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.incluiServicosOS(OS_ID, List.of()))
                .isInstanceOf(OrdemServicoNaoEncontradaException.class);

        verifyNoInteractions(servicoRepository);
    }

    @Test
    void cadastraOrcamentoDaOS() {
        OrdemServico os = criarOS(StatusOS.EM_DIAGNOSTICO);
        when(repository.findById(OS_ID)).thenReturn(Optional.of(os));
        when(repository.saveAndFlush(os)).thenReturn(os);

        OrdemServicoResponse response = service.cadastrarOrcamento(OS_ID);

        assertThat(response).isNotNull();
        assertThat(os.getStatus()).isEqualTo(StatusOS.AGUARDANDO_APROVACAO);
        verify(repository).saveAndFlush(os);
    }

    @Test
    void cadastraOrcamentoParaOSInexistente() {
        when(repository.findById(OS_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cadastrarOrcamento(OS_ID))
                .isInstanceOf(OrdemServicoNaoEncontradaException.class);

        verify(repository, never()).saveAndFlush(any(OrdemServico.class));
    }

    @Test
    void incluiPecasEInsumosNaOS() throws ReflectiveOperationException {
        OrdemServico os = criarOS(StatusOS.RECEBIDA);
        UUID pecaId = UUID.randomUUID();
        PecaInsumo peca = new PecaInsumo(TipoItem.PECA, "FILTRO", "Filtro", "Filtro de óleo",
                UnidadeMedida.UN, new BigDecimal("30.00"), BigDecimal.TEN);
        setId(peca, pecaId);
        when(repository.findById(OS_ID)).thenReturn(Optional.of(os));
        when(pecaInsumoRepository.findAllById(List.of(pecaId))).thenReturn(List.of(peca));
        when(repository.save(os)).thenReturn(os);

        assertThat(service.incluiPecasInsumosOS(OS_ID,
                List.of(new ItemQuantidadeRequest(pecaId, BigDecimal.valueOf(2))))).isNotNull();

        assertThat(os.getOrcamento()).isEqualByComparingTo("60.00");
        verify(repository).save(os);
    }

    @Test
    void incluiPecasEmOSInexistente() {
        when(repository.findById(OS_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.incluiPecasInsumosOS(OS_ID, List.of()))
                .isInstanceOf(OrdemServicoNaoEncontradaException.class);

        verifyNoInteractions(pecaInsumoRepository);
    }

    private CadastrarOSRequest criarRequest(String placa) {
        return new CadastrarOSRequest(List.of(), List.of(), "Diagnóstico", placa,
                new FuncionarioRequest(FUNCIONARIO_ID));
    }

    private OrdemServico criarOS(StatusOS status) {
        Cliente cliente = new Cliente("Ana", "Silva", "ana@email.com", "12345678901", "11999999999");
        Veiculo veiculo = new Veiculo(PLACA, "Fiat", "Uno", 2020, cliente);
        return new OrdemServico(status, BigDecimal.ZERO, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                "Diagnóstico", veiculo, cliente, criarFuncionario());
    }

    private Veiculo criarVeiculo() {
        Cliente cliente = new Cliente("Ana", "Silva", "ana@email.com", "12345678901", "11999999999");
        return new Veiculo(PLACA, "Fiat", "Uno", 2020, cliente);
    }

    private Funcionario criarFuncionario() {
        return new Funcionario("Ana Silva", "ana.silva@empresa.com", (short) 1234, false, true);
    }

    private void setId(Object entity, UUID id) throws ReflectiveOperationException {
        var field = BaseEntity.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(entity, id);
    }
}
