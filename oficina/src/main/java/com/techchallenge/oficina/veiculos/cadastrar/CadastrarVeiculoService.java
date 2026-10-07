package com.techchallenge.oficina.veiculos.cadastrar;

import com.techchallenge.oficina.veiculos.dominio.VeiculoRepository;
import com.techchallenge.oficina.veiculos.dominio.VeiculoResponse;
import com.techchallenge.oficina.veiculos.dominio.PlacaValidator;
import com.techchallenge.oficina.veiculos.dominio.PlacaJaCadastradaException;
import com.techchallenge.oficina.veiculos.entidades.Veiculo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CadastrarVeiculoService {

	private final VeiculoRepository repository;

	public CadastrarVeiculoService(VeiculoRepository repository) {
		this.repository = repository;
	}

	@Transactional
	public VeiculoResponse cadastrar(CadastrarVeiculoRequest request) {
		String placa = PlacaValidator.normalizar(request.placa());
		if (repository.existsByPlaca(placa)) {
			throw new PlacaJaCadastradaException(placa);
		}
		Veiculo veiculo = new Veiculo(placa, request.marca().trim(),
				request.modelo().trim(), request.ano());
		return VeiculoResponse.from(repository.saveAndFlush(veiculo));
	}
}
