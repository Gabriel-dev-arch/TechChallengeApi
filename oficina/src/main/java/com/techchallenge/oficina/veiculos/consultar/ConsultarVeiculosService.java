package com.techchallenge.oficina.veiculos.consultar;

import com.techchallenge.oficina.veiculos.dominio.VeiculoNaoEncontradoException;
import com.techchallenge.oficina.veiculos.dominio.VeiculoRepository;
import com.techchallenge.oficina.veiculos.dominio.VeiculoResponse;
import com.techchallenge.oficina.veiculos.dominio.PlacaValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ConsultarVeiculosService {

	private final VeiculoRepository repository;

	public ConsultarVeiculosService(VeiculoRepository repository) {
		this.repository = repository;
	}

	public Page<VeiculoResponse> listar(String placa, Pageable pageable) {
		if (placa != null && !placa.isBlank()) {
			List<VeiculoResponse> encontrado = repository.findByPlaca(PlacaValidator.normalizar(placa))
					.map(VeiculoResponse::from).stream().toList();
			return new PageImpl<>(encontrado, pageable, encontrado.size());
		}
		return repository.findAll(pageable).map(VeiculoResponse::from);
	}

	public VeiculoResponse buscar(UUID id) {
		return repository.findById(id).map(VeiculoResponse::from)
				.orElseThrow(() -> new VeiculoNaoEncontradoException(id));
	}
}
