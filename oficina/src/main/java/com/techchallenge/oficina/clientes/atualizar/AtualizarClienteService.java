package com.techchallenge.oficina.clientes.atualizar;

import com.techchallenge.oficina.clientes.dominio.*;
import com.techchallenge.oficina.clientes.entidades.Cliente;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AtualizarClienteService {

	private final ClienteRepository repository;

	public AtualizarClienteService(ClienteRepository repository) {
		this.repository = repository;
	}

	@Transactional
	public ClienteResponse atualizar(UUID id, AtualizarClienteRequest request) {
		Cliente cliente = repository.findById(id).orElseThrow(() -> new ClienteNaoEncontradoException(id));
		String documento = CpfCnpjValidator.normalizar(request.documento());
		if (repository.existsByDocumentoAndIdNot(documento, id)) {
			throw new DocumentoJaCadastradoException(documento);
		}
		cliente.atualizar(request.firstName().trim(), request.lastName().trim(), request.email().trim(),
				documento, request.telefone());
		return ClienteResponse.from(repository.saveAndFlush(cliente));
	}
}
