package com.techchallenge.oficina.autenticacao.dominio;

import com.techchallenge.oficina.autenticacao.entidades.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

	Optional<Usuario> findByUsername(String username);
}
