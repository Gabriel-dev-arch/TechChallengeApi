package com.techchallenge.oficina.veiculos.remover;

import com.techchallenge.oficina.veiculos.dominio.VeiculoNaoEncontradoException;
import com.techchallenge.oficina.veiculos.dominio.VeiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class RemoverVeiculoService {

	private final VeiculoRepository repository;

	public RemoverVeiculoService(VeiculoRepository repository) {
		this.repository = repository;
	}

	@Transactional
	public void remover(UUID id) {
		if (!repository.existsById(id)) {
			throw new VeiculoNaoEncontradoException(id);
		}
		repository.deleteById(id);
		repository.flush();
	}
}
