package com.techchallenge.oficina.ordem_servico.cadastrar;

import com.techchallenge.oficina.ordem_servico.dominio.*;
import com.techchallenge.oficina.ordem_servico.entidades.OrdemServico;
import com.techchallenge.oficina.ordem_servico.entidades.StatusOS;
import com.techchallenge.oficina.pecasinsumos.dominio.PecaInsumoRepository;
import com.techchallenge.oficina.pecasinsumos.entidades.PecaInsumo;
import com.techchallenge.oficina.servicos.dominio.ServicoRepository;
import com.techchallenge.oficina.servicos.entidades.Servico;
import com.techchallenge.oficina.veiculos.dominio.PlacaJaCadastradaException;
import com.techchallenge.oficina.veiculos.dominio.VeiculoRepository;
import com.techchallenge.oficina.veiculos.entidades.Veiculo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CadastrarOSService {

	private final OrdemServicoRepository repository;
	private final VeiculoRepository veiculoRepository;
	private final ServicoRepository servicoRepository;
	private final PecaInsumoRepository pecaInsumoRepository;

	public CadastrarOSService(OrdemServicoRepository repository, VeiculoRepository veiculoRepository,
			ServicoRepository servicoRepository, PecaInsumoRepository pecaInsumoRepository) {
		this.repository = repository;
		this.veiculoRepository = veiculoRepository;
		this.servicoRepository = servicoRepository;
		this.pecaInsumoRepository = pecaInsumoRepository;
	}

	@Transactional
	public OrdemServicoResponse cadastrar(CadastrarOSRequest request) {

		var veiculoComOSEmAberto = repository.buscarPorPlacaComOSEmAberto(request.placa());

		if (!veiculoComOSEmAberto.isEmpty()) {
			throw new OrdemServicoEmAbertoException(request.placa());
		}

		Veiculo veiculo = veiculoRepository.findByPlaca(request.placa()).orElseThrow(() -> new VeiculoNaoEncontradoException(request.placa()));

		List<Servico> servicos = servicoRepository.findAllById(request.servicos());
		List<PecaInsumo> itens = pecaInsumoRepository.findAllById(request.itens());

		OrdemServico ordemServico = new OrdemServico(StatusOS.RECEBIDA,
				Orcamento.from(itens, servicos),
				servicos,
				itens,
				request.diagnostico(),
				veiculo,
				veiculo.getCliente());

		return OrdemServicoResponse.from(repository.save(ordemServico));
	}
}
