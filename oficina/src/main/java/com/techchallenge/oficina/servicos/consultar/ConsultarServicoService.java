package com.techchallenge.oficina.servicos.consultar;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techchallenge.oficina.servicos.dominio.ServicoRepository;
import com.techchallenge.oficina.servicos.dominio.ServicoResponse;
import com.techchallenge.oficina.servicos.dominio.VeiculoNaoEncontradoException;

@Service
public class ConsultarServicoService {

	private final ServicoRepository repository;

	public ConsultarServicoService(ServicoRepository repository) {
		this.repository = repository;
	}

	@Transactional
	public ServicoResponse listar(UUID id) {
		
		return repository.findById(id).map(ServicoResponse::from)
		.orElseThrow(() -> new VeiculoNaoEncontradoException(id.toString()));
		
	}
}
