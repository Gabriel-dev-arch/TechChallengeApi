package com.techchallenge.oficina.veiculos.atualizar;

import com.techchallenge.oficina.veiculos.dominio.Placa;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AtualizarVeiculoRequest(
		@NotBlank @Placa String placa,
		@NotBlank @Size(max = 75) String marca,
		@NotBlank @Size(max = 75) String modelo,
		@NotNull @Min(1000) @Max(9999) Integer ano) {
}
