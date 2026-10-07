package com.techchallenge.oficina.servicos.cadastrar;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techchallenge.oficina.servicos.dominio.ServicoRepository;
import com.techchallenge.oficina.servicos.dominio.ServicoResponse;

@Service
public class CadastrarServicoService {

	private final ServicoRepository repository;

	public CadastrarServicoService(ServicoRepository repository) {
		this.repository = repository;
	}

	@Transactional
	public ServicoResponse cadastrar(CadastrarServicoRequest request) {
		
		return new ServicoResponse(UUID.randomUUID());
	}
}
