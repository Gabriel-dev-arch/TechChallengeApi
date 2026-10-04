package com.techchallenge.oficina.clientes.remover;

import com.techchallenge.oficina.clientes.dominio.ClienteNaoEncontradoException;
import com.techchallenge.oficina.clientes.dominio.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class RemoverClienteService {

	private final ClienteRepository repository;

	public RemoverClienteService(ClienteRepository repository) {
		this.repository = repository;
	}

	@Transactional
	public void remover(UUID id) {
		if (!repository.existsById(id)) {
			throw new ClienteNaoEncontradoException(id);
		}
		repository.deleteById(id);
		repository.flush();
	}
}
