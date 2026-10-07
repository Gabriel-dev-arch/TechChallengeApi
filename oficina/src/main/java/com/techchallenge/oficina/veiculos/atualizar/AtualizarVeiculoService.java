package com.techchallenge.oficina.veiculos.atualizar;

import com.techchallenge.oficina.veiculos.dominio.VeiculoNaoEncontradoException;
import com.techchallenge.oficina.veiculos.dominio.VeiculoRepository;
import com.techchallenge.oficina.veiculos.dominio.VeiculoResponse;
import com.techchallenge.oficina.veiculos.dominio.PlacaValidator;
import com.techchallenge.oficina.veiculos.dominio.PlacaJaCadastradaException;
import com.techchallenge.oficina.veiculos.entidades.Veiculo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AtualizarVeiculoService {

	private final VeiculoRepository repository;

	public AtualizarVeiculoService(VeiculoRepository repository) {
		this.repository = repository;
	}

	@Transactional
	public VeiculoResponse atualizar(UUID id, AtualizarVeiculoRequest request) {
		Veiculo veiculo = repository.findById(id).orElseThrow(() -> new VeiculoNaoEncontradoException(id));
		String placa = PlacaValidator.normalizar(request.placa());
		if (repository.existsByPlacaAndIdNot(placa, id)) {
			throw new PlacaJaCadastradaException(placa);
		}
		veiculo.atualizar(placa, request.marca().trim(), request.modelo().trim(),
				request.ano());
		return VeiculoResponse.from(repository.saveAndFlush(veiculo));
	}
}
