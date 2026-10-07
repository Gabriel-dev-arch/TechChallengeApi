package com.techchallenge.oficina.veiculos;

import com.techchallenge.oficina.veiculos.dominio.PlacaValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class PlacaValidatorTest {

	@ParameterizedTest
	@ValueSource(strings = {"ABC1234", "ABC-1234", "abc1234", "abc-1234", "ABC1D23", "abc1d23", " ABC-1234 "})
	void aceitaPlacasBrasileiras(String placa) {
		assertThat(PlacaValidator.valido(placa)).isTrue();
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {" ", "AB12345", "ABCD123", "ABC123", "ABC12345", "123ABCD", "ABC1DD3", "ABC-1D23",
			"A-BC1234", "ABC--1234", "ABC 1234", "ÁBC1234", "ABC1D2!"})
	void rejeitaPlacasInvalidas(String placa) {
		assertThat(PlacaValidator.valido(placa)).isFalse();
	}

	@Test
	void normalizaPlaca() {
		assertThat(PlacaValidator.normalizar(" abc-1234 ")).isEqualTo("ABC1234");
		assertThat(PlacaValidator.normalizar("abc1d23")).isEqualTo("ABC1D23");
		assertThat(PlacaValidator.normalizar(null)).isNull();
	}

	@Test
	void validatorDeixaVazioParaNotBlank() {
		var validator = new PlacaValidator();
		assertThat(validator.isValid(null, null)).isTrue();
		assertThat(validator.isValid(" ", null)).isTrue();
		assertThat(validator.isValid("123", null)).isFalse();
		assertThat(validator.isValid("abc-1234", null)).isTrue();
	}
}
