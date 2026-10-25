package com.techchallenge.oficina.ordem_servico.cadastrar;

import com.techchallenge.oficina.ordem_servico.dominio.OrdemServicoRepository;
import com.techchallenge.oficina.ordem_servico.dominio.exception.OrdemServicoEmAbertoException;
import com.techchallenge.oficina.ordem_servico.dominio.exception.OrdemServicoNaoEncontradaException;
import com.techchallenge.oficina.ordem_servico.dominio.exception.VeiculoNaoEncontradoException;
import com.techchallenge.oficina.ordem_servico.dominio.response.OrdemServicoResponse;
import com.techchallenge.oficina.ordem_servico.dominio.statusos.EstadosOS;
import com.techchallenge.oficina.ordem_servico.entidades.OrdemServico;
import com.techchallenge.oficina.ordem_servico.entidades.StatusOS;
import com.techchallenge.oficina.pecasinsumos.dominio.PecaInsumoRepository;
import com.techchallenge.oficina.pecasinsumos.entidades.PecaInsumo;
import com.techchallenge.oficina.servicos.dominio.ServicoRepository;
import com.techchallenge.oficina.servicos.entidades.Servico;
import com.techchallenge.oficina.veiculos.dominio.VeiculoRepository;
import com.techchallenge.oficina.veiculos.entidades.Veiculo;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CadastrarOSService {

    private final OrdemServicoRepository repository;
    private final VeiculoRepository veiculoRepository;
    private final ServicoRepository servicoRepository;
    private final PecaInsumoRepository pecaInsumoRepository;
    private final EstadosOS estadosOS;

    public CadastrarOSService(OrdemServicoRepository repository, VeiculoRepository veiculoRepository,
                              ServicoRepository servicoRepository, PecaInsumoRepository pecaInsumoRepository, EstadosOS estadosOS) {
        this.repository = repository;
        this.veiculoRepository = veiculoRepository;
        this.servicoRepository = servicoRepository;
        this.pecaInsumoRepository = pecaInsumoRepository;
        this.estadosOS = estadosOS;
    }

    @Transactional
    public OrdemServicoResponse cadastrar(CadastrarOSRequest request) {

        var veiculoComOSEmAberto = repository.buscarPorPlacaComOSEmAberto(request.placa());

        if (!veiculoComOSEmAberto.isEmpty()) {
            throw new OrdemServicoEmAbertoException(request.placa());
        }

        Veiculo veiculo = veiculoRepository.findByPlaca(request.placa()).orElseThrow(() -> new VeiculoNaoEncontradoException(request.placa()));

        OrdemServico ordemServico = new OrdemServico(StatusOS.RECEBIDA,
                veiculo,
                veiculo.getCliente());


        return OrdemServicoResponse.from(repository.save(ordemServico));
    }

    @Transactional
    public OrdemServicoResponse incluiServicosOS(UUID id, @Valid List<ServicosRequest> request) {

        OrdemServico osCadastrada = repository.findById(id).orElseThrow(() -> new OrdemServicoNaoEncontradaException(id));

        List<Servico> servicos = servicoRepository.findAllById(request.stream()
                        .map(ServicosRequest::id)
                        .collect(Collectors.toUnmodifiableList()))
                .stream().toList();

        osCadastrada.incluirServicos(servicos);

        return OrdemServicoResponse.from(repository.saveAndFlush(osCadastrada));
    }

    @Transactional
    public OrdemServicoResponse incluiPecasInsumosOS(UUID id, @Valid List<ItemQuantidadeRequest> request) {

        OrdemServico osCadastrada = repository.findById(id).orElseThrow(() -> new OrdemServicoNaoEncontradaException(id));

        List<ItemQuantidade> itensQuantidade = request.stream()
                .map(f -> new ItemQuantidade(f.id(), f.quantidade()))
                .collect(Collectors.toList());

        List<PecaInsumo> itens = pecaInsumoRepository.findAllById(request.stream().map(ItemQuantidadeRequest::id).toList());

        osCadastrada.incluirPecaInsumos(itens, itensQuantidade);

        return OrdemServicoResponse.from(repository.save(osCadastrada));
    }

    @Transactional
    public OrdemServicoResponse iniciaDiagnostico(UUID id) {

        OrdemServico osCadastrada = repository.findById(id).orElseThrow(() -> new OrdemServicoNaoEncontradaException(id));

        estadosOS.executa(osCadastrada);

        return OrdemServicoResponse.from(osCadastrada);
    }

    @Transactional
    public OrdemServicoResponse cadastrarOrcamento(UUID id) {

        OrdemServico osCadastrada = repository.findById(id).orElseThrow(() -> new OrdemServicoNaoEncontradaException(id));

        estadosOS.executa(osCadastrada);

        return OrdemServicoResponse.from(osCadastrada);
    }

    @Transactional
    public OrdemServicoResponse iniciaExecucao(UUID id) {

        OrdemServico osCadastrada = repository.findById(id).orElseThrow(() -> new OrdemServicoNaoEncontradaException(id));

        estadosOS.executa(osCadastrada);

        return OrdemServicoResponse.from(osCadastrada);
    }

    @Transactional
    public OrdemServicoResponse finalizaExecucao(UUID id) {

        OrdemServico osCadastrada = repository.findById(id).orElseThrow(() -> new OrdemServicoNaoEncontradaException(id));

        estadosOS.executa(osCadastrada);

        return OrdemServicoResponse.from(osCadastrada);
    }
}
