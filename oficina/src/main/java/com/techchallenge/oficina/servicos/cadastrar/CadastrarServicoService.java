package com.techchallenge.oficina.servicos.cadastrar;

import org.springframework.stereotype.Service;

import com.techchallenge.oficina.servicos.dominio.ServicoRepository;

@Service
public class CadastrarServicoService {

	private final ServicoRepository repository;

	public CadastrarServicoService(ServicoRepository repository) {
		this.repository = repository;
	}

	
}
