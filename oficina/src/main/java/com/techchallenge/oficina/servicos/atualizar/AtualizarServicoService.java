package com.techchallenge.oficina.servicos.atualizar;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.techchallenge.oficina.servicos.dominio.ServicoNaoEncontradoException;
import com.techchallenge.oficina.servicos.dominio.ServicoRepository;
import com.techchallenge.oficina.servicos.dominio.ServicoResponse;
import com.techchallenge.oficina.servicos.entidades.Servico;

import jakarta.validation.Valid;

@Service
@Validated
public class AtualizarServicoService {

	private final ServicoRepository repository;

	public AtualizarServicoService(ServicoRepository repository) {
		this.repository = repository;
	}

	@Transactional
	public ServicoResponse atualizar(UUID id, @Valid AtualizarServicoRequest request) {
		
		Servico servico = repository.findById(id).orElseThrow(() -> new ServicoNaoEncontradoException(id));
		
		servico.atualizar(request.descricao(), request.valor());
		
		return ServicoResponse.from(repository.saveAndFlush(servico));
	}
}
