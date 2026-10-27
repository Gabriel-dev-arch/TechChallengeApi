package com.techchallenge.oficina.servicos.consultar;

import com.techchallenge.oficina.servicos.dominio.ServicoNaoEncontradoException;
import com.techchallenge.oficina.servicos.dominio.ServicoRepository;
import com.techchallenge.oficina.servicos.dominio.ServicoResponse;
import com.techchallenge.oficina.servicos.entidades.Servico;
import jakarta.validation.constraints.NotBlank;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

	public List<Servico> buscarPorDescricao(@NotBlank String descricao) {
		return repository.findByDescricaoContaining(descricao);
	}
}
