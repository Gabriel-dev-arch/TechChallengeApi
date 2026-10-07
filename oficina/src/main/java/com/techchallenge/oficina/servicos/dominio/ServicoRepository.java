package com.techchallenge.oficina.servicos.dominio;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techchallenge.oficina.servicos.entidades.Servico;

public interface ServicoRepository extends JpaRepository<Servico, UUID> {
	

	
}
