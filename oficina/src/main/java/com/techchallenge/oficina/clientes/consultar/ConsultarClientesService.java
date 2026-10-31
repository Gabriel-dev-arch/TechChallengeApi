package com.techchallenge.oficina.clientes.consultar;

import com.techchallenge.oficina.clientes.dominio.ClienteNaoEncontradoException;
import com.techchallenge.oficina.clientes.dominio.ClienteRepository;
import com.techchallenge.oficina.clientes.dominio.ClienteResponse;
import com.techchallenge.oficina.clientes.dominio.CpfCnpjValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ConsultarClientesService {

	private final ClienteRepository repository;

	public ConsultarClientesService(ClienteRepository repository) {
		this.repository = repository;
	}

	public Page<ClienteResponse> listar(String documento, Pageable pageable) {
		if (documento != null && !documento.isBlank()) {
			List<ClienteResponse> encontrado = repository.findByDocumento(CpfCnpjValidator.normalizar(documento))
					.map(ClienteResponse::from).stream().toList();
			return new PageImpl<>(encontrado, pageable, encontrado.size());
		}
		return repository.findAll(pageable).map(ClienteResponse::from);
	}

	public ClienteResponse buscar(UUID id) {
		return repository.findById(id).map(ClienteResponse::from)
				.orElseThrow(() -> new ClienteNaoEncontradoException(id));
	}
}
