package com.techchallenge.oficina.ordem_servico.cadastrar.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record FuncionarioRequest(@NotNull UUID id) {
}
