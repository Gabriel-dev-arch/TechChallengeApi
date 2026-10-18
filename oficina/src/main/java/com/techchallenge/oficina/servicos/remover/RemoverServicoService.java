package com.techchallenge.oficina.servicos.remover;

import com.techchallenge.oficina.clientes.dominio.ClienteNaoEncontradoException;
import com.techchallenge.oficina.servicos.dominio.ServicoNaoEncontradoException;
import com.techchallenge.oficina.servicos.dominio.ServicoRepository;
import com.techchallenge.oficina.servicos.dominio.ServicoResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RemoverServicoService {

	private final ServicoRepository repository;

	public RemoverServicoService(ServicoRepository repository) {
		this.repository = repository;
	}

	@Transactional
	public void remover(UUID id) {
		if (!repository.existsById(id)) {
			throw new ServicoNaoEncontradoException(id);
		}
		repository.deleteById(id);
		repository.flush();
	}
}
