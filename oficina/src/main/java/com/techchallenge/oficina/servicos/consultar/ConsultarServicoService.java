package com.techchallenge.oficina.servicos.consultar;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techchallenge.oficina.servicos.dominio.ServicoRepository;
import com.techchallenge.oficina.servicos.dominio.ServicoResponse;
import com.techchallenge.oficina.servicos.dominio.ServicoNaoEncontradoException;

@Service
public class ConsultarServicoService {

	private final ServicoRepository repository;

	public ConsultarServicoService(ServicoRepository repository) {
		this.repository = repository;
	}

	public ServicoResponse listar(UUID id) {
		return repository.findById(id).map(ServicoResponse::from)
		.orElseThrow(() -> new ServicoNaoEncontradoException(id));
	}

	public List<ServicoResponse> listarTodos() {
		return repository.findAll().stream().map(ServicoResponse::from).collect(Collectors.toUnmodifiableList());
	}
}
