package com.techchallenge.oficina.clientes.dominio;

import com.techchallenge.oficina.clientes.entidades.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ClienteRepository extends JpaRepository<Cliente, UUID> {

	boolean existsByDocumento(String documento);

	boolean existsByDocumentoAndIdNot(String documento, UUID id);

	Optional<Cliente> findByDocumento(String documento);
}
