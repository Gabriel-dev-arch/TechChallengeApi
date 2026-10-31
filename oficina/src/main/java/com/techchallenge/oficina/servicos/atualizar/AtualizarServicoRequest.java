package com.techchallenge.oficina.servicos.atualizar;

import java.math.BigDecimal;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

// Os limites seguem as colunas varchar(255) e numeric(10,2); sem eles o banco recusaria ou arredondaria em silêncio.
public record AtualizarServicoRequest(
		@NotBlank @Size(max = 255) String descricao,
		@NotNull @PositiveOrZero
		@Digits(integer = 8, fraction = 2, message = "deve ter no máximo {integer} dígitos inteiros e {fraction} casas decimais")
		BigDecimal valor) {
}
