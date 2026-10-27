package com.techchallenge.oficina.funcionario.dominio;

import com.techchallenge.oficina.funcionario.entidades.Funcionario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FuncionarioRepository extends JpaRepository<Funcionario, UUID> {

}
