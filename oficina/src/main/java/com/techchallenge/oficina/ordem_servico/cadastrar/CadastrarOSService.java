package com.techchallenge.oficina.ordem_servico.cadastrar;

import com.techchallenge.oficina.funcionario.dominio.FuncionarioNaoEncontradoException;
import com.techchallenge.oficina.funcionario.dominio.FuncionarioRepository;
import com.techchallenge.oficina.funcionario.entidades.Funcionario;
import com.techchallenge.oficina.ordem_servico.cadastrar.request.CadastrarOSRequest;
import com.techchallenge.oficina.ordem_servico.cadastrar.request.ItemQuantidade;
import com.techchallenge.oficina.ordem_servico.cadastrar.request.ItemQuantidadeRequest;
import com.techchallenge.oficina.ordem_servico.cadastrar.request.ServicosRequest;
import com.techchallenge.oficina.ordem_servico.dominio.OrdemServicoRepository;
import com.techchallenge.oficina.ordem_servico.dominio.exception.OrdemServicoEmAbertoException;
import com.techchallenge.oficina.ordem_servico.dominio.exception.OrdemServicoNaoEncontradaException;
import com.techchallenge.oficina.ordem_servico.dominio.exception.VeiculoNaoEncontradoException;
import com.techchallenge.oficina.ordem_servico.dominio.response.OrdemServicoResponse;
import com.techchallenge.oficina.ordem_servico.dominio.statusos.EstadosOS;
import com.techchallenge.oficina.ordem_servico.finalizar.FinalizarOSService;
import com.techchallenge.oficina.ordem_servico.dominio.exception.TransicaoInvalidaException;
import com.techchallenge.oficina.ordem_servico.entidades.OrdemServico;
import com.techchallenge.oficina.ordem_servico.entidades.StatusOS;
import com.techchallenge.oficina.pecasinsumos.dominio.PecaInsumoRepository;
import com.techchallenge.oficina.pecasinsumos.entidades.PecaInsumo;
import com.techchallenge.oficina.servicos.dominio.ServicoRepository;
import com.techchallenge.oficina.servicos.entidades.Servico;
import com.techchallenge.oficina.veiculos.dominio.PlacaValidator;
import com.techchallenge.oficina.veiculos.dominio.VeiculoRepository;
import com.techchallenge.oficina.veiculos.entidades.Veiculo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CadastrarOSService {

    private final OrdemServicoRepository repository;
    private final VeiculoRepository veiculoRepository;
    private final ServicoRepository servicoRepository;
    private final PecaInsumoRepository pecaInsumoRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final EstadosOS estadosOS;
    private final FinalizarOSService finalizarOSService;

    public CadastrarOSService(OrdemServicoRepository repository, VeiculoRepository veiculoRepository,
                              ServicoRepository servicoRepository, PecaInsumoRepository pecaInsumoRepository, FuncionarioRepository funcionarioRepository, EstadosOS estadosOS, FinalizarOSService finalizarOSService) {
        this.repository = repository;
        this.veiculoRepository = veiculoRepository;
        this.servicoRepository = servicoRepository;
        this.pecaInsumoRepository = pecaInsumoRepository;
        this.funcionarioRepository = funcionarioRepository;
        this.estadosOS = estadosOS;
        this.finalizarOSService = finalizarOSService;
        }

    @Transactional
    public OrdemServicoResponse cadastrar(CadastrarOSRequest request) {

        // mesma normalização do cadastro de veículos: aceita minúsculas e hífen
        String placa = PlacaValidator.normalizar(request.placa());
        var veiculoComOSEmAberto = repository.buscarPorPlacaComOSEmAberto(placa);

        if (!veiculoComOSEmAberto.isEmpty()) {
            throw new OrdemServicoEmAbertoException(placa);
        }

        Veiculo veiculo = veiculoRepository.findByPlaca(placa).orElseThrow(() -> new VeiculoNaoEncontradoException(placa));
        UUID funcionarioId = request.funcionario().id();
        Funcionario funcionario = funcionarioRepository.findById(funcionarioId)
                .orElseThrow(() -> new FuncionarioNaoEncontradoException(funcionarioId));

        OrdemServico ordemServico = new OrdemServico(StatusOS.RECEBIDA,
                veiculo,
                veiculo.getCliente(),
                funcionario);


        return OrdemServicoResponse.from(repository.save(ordemServico));
    }

    @Transactional
    public OrdemServicoResponse iniciaDiagnostico(UUID id) {

        OrdemServico osCadastrada = repository.findById(id).orElseThrow(() -> new OrdemServicoNaoEncontradaException(id));

        exigirStatus(osCadastrada, StatusOS.RECEBIDA);

        estadosOS.executa(osCadastrada);

        return OrdemServicoResponse.from(osCadastrada);
    }

    @Transactional
    public OrdemServicoResponse iniciaExecucao(UUID id) {

        OrdemServico osCadastrada = repository.findById(id).orElseThrow(() -> new OrdemServicoNaoEncontradaException(id));

        exigirStatus(osCadastrada, StatusOS.APROVADA);

        estadosOS.executa(osCadastrada);

        return OrdemServicoResponse.from(osCadastrada);
    }

    @Transactional
    public OrdemServicoResponse finalizaExecucao(UUID id) {
        return finalizarOSService.finalizar(id);
    }

    @Transactional
    public OrdemServicoResponse entregaVeiculo(UUID id) {

        OrdemServico osCadastrada = repository.findById(id).orElseThrow(() -> new OrdemServicoNaoEncontradaException(id));

        exigirStatus(osCadastrada, StatusOS.FINALIZADA);

        estadosOS.executa(osCadastrada);

        return OrdemServicoResponse.from(osCadastrada, osCadastrada.getStatus());
    }

    @Transactional
    public OrdemServicoResponse incluiServicosOS(UUID id, List<ServicosRequest> request) {

        OrdemServico osCadastrada = repository.findById(id).orElseThrow(() -> new OrdemServicoNaoEncontradaException(id));

        List<Servico> servicos = servicoRepository.findAllById(request.stream()
                        .map(ServicosRequest::id)
                        .collect(Collectors.toUnmodifiableList()))
                .stream().toList();

        osCadastrada.incluirServicos(servicos);

        return OrdemServicoResponse.from(repository.saveAndFlush(osCadastrada));
    }

    @Transactional
    public OrdemServicoResponse cadastrarOrcamento(UUID id) {

        OrdemServico osCadastrada = repository.findById(id).orElseThrow(() -> new OrdemServicoNaoEncontradaException(id));
        if (osCadastrada.getStatus() != StatusOS.RECEBIDA && osCadastrada.getStatus() != StatusOS.EM_DIAGNOSTICO) {
            throw new TransicaoInvalidaException("O orçamento só pode ser enviado durante o diagnóstico");
        }
        osCadastrada.incluirDiagnostico();
        osCadastrada.atualizaStatus(StatusOS.AGUARDANDO_APROVACAO);
        return OrdemServicoResponse.from(repository.saveAndFlush(osCadastrada));
    }

    @Transactional
    public OrdemServicoResponse incluiPecasInsumosOS(UUID id, List<ItemQuantidadeRequest> request) {

        OrdemServico osCadastrada = repository.findById(id).orElseThrow(() -> new OrdemServicoNaoEncontradaException(id));

        List<ItemQuantidade> itensQuantidade = request.stream()
                .map(f -> new ItemQuantidade(f.id(), f.quantidade()))
                .collect(Collectors.toList());

        List<PecaInsumo> itens = pecaInsumoRepository.findAllById(request.stream().map(ItemQuantidadeRequest::id).toList());

        osCadastrada.incluirPecaInsumos(itens, itensQuantidade);

        return OrdemServicoResponse.from(repository.save(osCadastrada));
    }

    private void exigirStatus(OrdemServico os, StatusOS esperado) {
        if (os.getStatus() != esperado) {
            throw new TransicaoInvalidaException("A ordem de serviço deve estar com status " + esperado);
        }
    }
}
