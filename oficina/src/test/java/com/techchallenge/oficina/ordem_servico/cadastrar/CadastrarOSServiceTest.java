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
import org.junit.jupiter.api.DisplayName;
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

    /*
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
    @DisplayName("Deve impedir cadastro quando veículo já tem ordem em aberto")
    void deveImpedirCadastroQuandoVeiculoJaTemOrdemEmAberto() {
        CadastrarOSRequest request = new CadastrarOSRequest(List.of(), List.of(), "Diagnóstico", PLACA,
            new FuncionarioRequest(FUNCIONARIO_ID));
        when(repository.buscarPorPlacaComOSEmAberto(PLACA)).thenReturn(List.of(
                new OrdemServico(StatusOS.RECEBIDA, BigDecimal.ZERO, List.of(), List.of(), List.of(),
                "Aberta", null, null, null)));

        assertThatThrownBy(() -> service.cadastrar(request))
                .isInstanceOf(OrdemServicoEmAbertoException.class)
                .hasMessage("Veículo com ordem de serviço em aberto: " + PLACA);

        verify(veiculoRepository, never()).findByPlaca(any());
        verify(repository, never()).save(any(OrdemServico.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando veículo não existe")
    void deveLancarExcecaoQuandoVeiculoNaoExiste() {
        CadastrarOSRequest request = new CadastrarOSRequest(List.of(), List.of(), "Diagnóstico", PLACA,
            new FuncionarioRequest(FUNCIONARIO_ID));
        when(repository.buscarPorPlacaComOSEmAberto(PLACA)).thenReturn(List.of());
        when(veiculoRepository.findByPlaca(PLACA)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cadastrar(request))
                .isInstanceOf(VeiculoNaoEncontradoException.class)
                .hasMessage("Veículo não encontrado: " + PLACA);

        verify(repository, never()).save(any(OrdemServico.class));
    }

    @Test
    @DisplayName("Deve cadastrar ordem de serviço com sucesso")
    void deveCadastrarComSucesso() {
        CadastrarOSRequest request = new CadastrarOSRequest(List.of(), List.of(), "Diagnóstico", PLACA,
            new FuncionarioRequest(FUNCIONARIO_ID));
        Cliente cliente = new Cliente("Ana", "Silva", "ana@email.com", "12345678901", "11999999999");
        Veiculo veiculo = new Veiculo(PLACA, "Fiat", "Uno", 2020, cliente);
        Funcionario funcionario = criarFuncionario();

        when(repository.buscarPorPlacaComOSEmAberto(PLACA)).thenReturn(List.of());
        when(veiculoRepository.findByPlaca(PLACA)).thenReturn(Optional.of(veiculo));
        when(funcionarioRepository.findById(FUNCIONARIO_ID)).thenReturn(Optional.of(funcionario));
        when(repository.save(any(OrdemServico.class))).thenAnswer(i -> i.getArgument(0));

        OrdemServicoResponse response = service.cadastrar(request);

        assertThat(response).isNotNull();
        assertThat(response.veiculo().placa()).isEqualTo(PLACA);
        verify(repository).save(any(OrdemServico.class));
    }

    @Test
    @DisplayName("Deve iniciar diagnóstico com sucesso")
    void deveIniciarDiagnostico() {
        OrdemServico os = criarOS(StatusOS.RECEBIDA);
        when(repository.findById(OS_ID)).thenReturn(Optional.of(os));

        OrdemServicoResponse response = service.iniciaDiagnostico(OS_ID);

        assertThat(response).isNotNull();
        verify(estadosOS).executa(os);
    }

    @Test
    @DisplayName("Deve lançar exceção ao iniciar diagnóstico de OS inexistente")
    void deveLancarExcecaoAoIniciarDiagnosticoInexistente() {
        when(repository.findById(OS_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.iniciaDiagnostico(OS_ID))
                .isInstanceOf(OrdemServicoNaoEncontradaException.class);
    }

    @Test
    @DisplayName("Deve iniciar execução com sucesso")
    void deveIniciarExecucao() {
        OrdemServico os = criarOS(StatusOS.APROVADA);
        when(repository.findById(OS_ID)).thenReturn(Optional.of(os));

        OrdemServicoResponse response = service.iniciaExecucao(OS_ID);

        assertThat(response).isNotNull();
        verify(estadosOS).executa(os);
    }

    @Test
    @DisplayName("Deve lançar exceção ao iniciar execução de OS inexistente")
    void deveLancarExcecaoAoIniciarExecucaoInexistente() {
        when(repository.findById(OS_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.iniciaExecucao(OS_ID))
                .isInstanceOf(OrdemServicoNaoEncontradaException.class);
    }

    @Test
    @DisplayName("Deve finalizar execução com sucesso")
    void deveFinalizarExecucao() {
        OrdemServico os = criarOS(StatusOS.EM_EXECUCAO);
        when(repository.findById(OS_ID)).thenReturn(Optional.of(os));

        OrdemServicoResponse response = service.finalizaExecucao(OS_ID);

        assertThat(response).isNotNull();
        verify(estadosOS).executa(os);
    }

    @Test
    @DisplayName("Deve lançar exceção ao finalizar execução de OS inexistente")
    void deveLancarExcecaoAoFinalizarExecucaoInexistente() {
        when(repository.findById(OS_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.finalizaExecucao(OS_ID))
                .isInstanceOf(OrdemServicoNaoEncontradaException.class);
    }

    @Test
    @DisplayName("Deve cadastrar orçamento com sucesso")
    void deveCadastrarOrcamento() {
        OrdemServico os = criarOS(StatusOS.EM_DIAGNOSTICO);
        when(repository.findById(OS_ID)).thenReturn(Optional.of(os));

        OrdemServicoResponse response = service.cadastrarOrcamento(OS_ID);

        assertThat(response).isNotNull();
        verify(estadosOS).executa(os);
    }

    @Test
    @DisplayName("Deve incluir serviços na OS com sucesso")
    void deveIncluirServicosOS() {
        OrdemServico os = criarOS(StatusOS.RECEBIDA);
        UUID servicoId = UUID.randomUUID();
        Servico servico = new Servico("Troca de óleo", new BigDecimal("50.00"));
        setId(servico, servicoId);

        when(repository.findById(OS_ID)).thenReturn(Optional.of(os));
        when(servicoRepository.findAllById(List.of(servicoId))).thenReturn(List.of(servico));
        when(repository.saveAndFlush(os)).thenReturn(os);

        OrdemServicoResponse response = service.incluiServicosOS(OS_ID, List.of(new ServicosRequest(servicoId, "Troca de óleo", new BigDecimal("50.00"))));

        assertThat(response).isNotNull();
        verify(servicoRepository).findAllById(List.of(servicoId));
        verify(repository).saveAndFlush(os);
    }

    @Test
    @DisplayName("Deve incluir peças e insumos na OS com sucesso")
    void deveIncluirPecasInsumosOS() {
        OrdemServico os = criarOS(StatusOS.RECEBIDA);
        UUID pecaId = UUID.randomUUID();
        PecaInsumo peca = new PecaInsumo(TipoItem.PECA, "FILTRO", "Filtro", "Filtro óleo",
                UnidadeMedida.UN, new BigDecimal("30.00"), new BigDecimal("10"));
        setId(peca, pecaId);

        when(repository.findById(OS_ID)).thenReturn(Optional.of(os));
        when(pecaInsumoRepository.findAllById(List.of(pecaId))).thenReturn(List.of(peca));
        when(repository.save(os)).thenReturn(os);

        OrdemServicoResponse response = service.incluiPecasInsumosOS(OS_ID, List.of(new ItemQuantidadeRequest(pecaId, new BigDecimal("2"))));

        assertThat(response).isNotNull();
        verify(pecaInsumoRepository).findAllById(List.of(pecaId));
        verify(repository).save(os);
    }

    @Test
    @DisplayName("Deve normalizar a placa (minúsculas e hífen) antes de buscar o veículo")
    void deveNormalizarPlacaNaAbertura() {
        CadastrarOSRequest request = new CadastrarOSRequest(List.of(), List.of(), null, " abc-1234 ",
                new FuncionarioRequest(FUNCIONARIO_ID));
        Cliente cliente = new Cliente("Ana", "Silva", "ana@email.com", "12345678901", "11999999999");
        when(repository.buscarPorPlacaComOSEmAberto(PLACA)).thenReturn(List.of());
        when(veiculoRepository.findByPlaca(PLACA)).thenReturn(Optional.of(new Veiculo(PLACA, "Fiat", "Uno", 2020, cliente)));
        when(funcionarioRepository.findById(FUNCIONARIO_ID)).thenReturn(Optional.of(criarFuncionario()));
        when(repository.save(any(OrdemServico.class))).thenAnswer(i -> i.getArgument(0));

        OrdemServicoResponse response = service.cadastrar(request);

        assertThat(response.veiculo().placa()).isEqualTo(PLACA);
    }

    @Test
    @DisplayName("Deve lançar FuncionarioNaoEncontradoException quando o funcionário não existe")
    void deveLancarExcecaoQuandoFuncionarioNaoExiste() {
        CadastrarOSRequest request = new CadastrarOSRequest(List.of(), List.of(), null, PLACA,
                new FuncionarioRequest(FUNCIONARIO_ID));
        Cliente cliente = new Cliente("Ana", "Silva", "ana@email.com", "12345678901", "11999999999");
        when(repository.buscarPorPlacaComOSEmAberto(PLACA)).thenReturn(List.of());
        when(veiculoRepository.findByPlaca(PLACA)).thenReturn(Optional.of(new Veiculo(PLACA, "Fiat", "Uno", 2020, cliente)));
        when(funcionarioRepository.findById(FUNCIONARIO_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cadastrar(request))
                .isInstanceOf(FuncionarioNaoEncontradoException.class);

        verify(repository, never()).save(any(OrdemServico.class));
    }

    private OrdemServico criarOS(StatusOS status) {
        Cliente cliente = new Cliente("Ana", "Silva", "ana@email.com", "12345678901", "11999999999");
        Veiculo veiculo = new Veiculo(PLACA, "Fiat", "Uno", 2020, cliente);
        return new OrdemServico(status, BigDecimal.ZERO, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                "Diagnóstico", veiculo, cliente, criarFuncionario());
    }

    private Funcionario criarFuncionario() {
        return new Funcionario("Ana Silva", "ana.silva@empresa.com", (short) 1234, false, true);
    }

    private void setId(Object entity, UUID id) {
        try {
            var field = BaseEntity.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Não foi possível configurar id", e);
        }
    }*/
}
