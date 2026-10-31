package com.techchallenge.oficina.clientes.cadastrar;

import com.techchallenge.oficina.clientes.dominio.ClienteRepository;
import com.techchallenge.oficina.clientes.dominio.ClienteResponse;
import com.techchallenge.oficina.clientes.dominio.CpfCnpjValidator;
import com.techchallenge.oficina.clientes.dominio.DocumentoJaCadastradoException;
import com.techchallenge.oficina.clientes.entidades.Cliente;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CadastrarClienteService {

	private final ClienteRepository repository;
	private final PasswordEncoder passwordEncoder;

	public CadastrarClienteService(ClienteRepository repository, PasswordEncoder passwordEncoder) {
		this.repository = repository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public ClienteResponse cadastrar(CadastrarClienteRequest request) {
		String documento = CpfCnpjValidator.normalizar(request.documento());
		if (repository.existsByDocumento(documento)) {
			throw new DocumentoJaCadastradoException(documento);
		}
		Cliente cliente = new Cliente(request.firstName().trim(), request.lastName().trim(),
				request.email().trim(), documento, request.telefone(), passwordEncoder.encode(request.senha()));
		return ClienteResponse.from(repository.saveAndFlush(cliente));
	}
}
