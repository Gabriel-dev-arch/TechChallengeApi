package com.techchallenge.oficina.servicos.cadastrar;

import com.techchallenge.oficina.clientes.dominio.ClienteResponse;
import com.techchallenge.oficina.servicos.dominio.ServicoResponse;
import com.techchallenge.oficina.servicos.entidades.Servico;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import com.techchallenge.oficina.servicos.dominio.ServicoRepository;

@Service
public class CadastrarServicoService {

	private final ServicoRepository repository;

	public CadastrarServicoService(ServicoRepository repository) {
		this.repository = repository;
	}

	public ServicoResponse cadastrar(@Valid CadastrarServicoRequest request) {

		Servico servico = new Servico(request.descricao(), request.valor());
		return ServicoResponse.from(repository.saveAndFlush(servico));
	}
}
