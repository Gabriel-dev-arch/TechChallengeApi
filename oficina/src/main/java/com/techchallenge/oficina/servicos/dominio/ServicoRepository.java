package com.techchallenge.oficina.servicos.dominio;

import com.techchallenge.oficina.servicos.entidades.Servico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ServicoRepository extends JpaRepository<Servico, UUID> {

        List<Servico> findByDescricaoContaining(String descricao);
}
