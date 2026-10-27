package com.techchallenge.oficina.funcionario.dominio;

import com.techchallenge.oficina.funcionario.entidades.Funcionario;
import com.techchallenge.oficina.ordem_servico.entidades.StatusOS;
import jakarta.persistence.Column;

import java.math.BigDecimal;
import java.util.UUID;

public record FuncionarioResponse(
        UUID id, String nome, String email) {

    public static FuncionarioResponse from(Funcionario funcionario) {

        return new FuncionarioResponse(funcionario.getId(), funcionario.getNome(), funcionario.getEmail());
    }
}