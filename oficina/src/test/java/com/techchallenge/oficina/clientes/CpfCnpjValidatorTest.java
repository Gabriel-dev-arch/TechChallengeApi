package com.techchallenge.oficina.clientes;

import com.techchallenge.oficina.clientes.dominio.CpfCnpjValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class CpfCnpjValidatorTest {

	@ParameterizedTest
	@ValueSource(strings = {"529.982.247-25", "52998224725", "111.444.777-35"})
	void aceitaCpfValido(String cpf) {
		assertThat(CpfCnpjValidator.valido(cpf)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = {"529.982.247-24", "52998224726", "111.111.111-11", "000.000.000-00", "123.456.789-00",
			"5299822472", "529982247250"})
	void rejeitaCpfInvalido(String cpf) {
		assertThat(CpfCnpjValidator.valido(cpf)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = {"11.222.333/0001-81", "11222333000181", "11.444.777/0001-61"})
	void aceitaCnpjValido(String cnpj) {
		assertThat(CpfCnpjValidator.valido(cnpj)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = {"11.222.333/0001-82", "11222333000180", "00.000.000/0000-00", "11.111.111/1111-11",
			"1122233300018"})
	void rejeitaCnpjInvalido(String cnpj) {
		assertThat(CpfCnpjValidator.valido(cnpj)).isFalse();
	}

	@Test
	void aceitaCnpjAlfanumerico() {
		assertThat(CpfCnpjValidator.valido("12.ABC.345/01DE-35")).isTrue();
		assertThat(CpfCnpjValidator.valido("12abc34501de35")).isTrue();
		assertThat(CpfCnpjValidator.valido("12.ABC.345/01DE-36")).isFalse();
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {" ", "abc", "529.982.247-2A"})
	void rejeitaEntradaNaoDocumento(String valor) {
		assertThat(CpfCnpjValidator.valido(valor)).isFalse();
	}

	@Test
	void normalizaRemovendoMascara() {
		assertThat(CpfCnpjValidator.normalizar(" 529.982.247-25 ")).isEqualTo("52998224725");
		assertThat(CpfCnpjValidator.normalizar("12.abc.345/01de-35")).isEqualTo("12ABC34501DE35");
		assertThat(CpfCnpjValidator.normalizar(null)).isNull();
	}

	@Test
	void validatorDeixaVazioParaNotBlank() {
		var validator = new CpfCnpjValidator();
		assertThat(validator.isValid(null, null)).isTrue();
		assertThat(validator.isValid(" ", null)).isTrue();
		assertThat(validator.isValid("123", null)).isFalse();
	}
}
