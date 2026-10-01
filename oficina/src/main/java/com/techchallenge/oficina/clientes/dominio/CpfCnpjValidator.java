package com.techchallenge.oficina.clientes.dominio;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Locale;

public class CpfCnpjValidator implements ConstraintValidator<CpfCnpj, String> {

	private static final int[] PESOS_CNPJ = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

	/** Remove máscara (. - / e espaços) e coloca em maiúsculas. Retorna null se a entrada for null. */
	public static String normalizar(String documento) {
		if (documento == null) {
			return null;
		}
		return documento.replaceAll("[.\\-/\\s]", "").toUpperCase(Locale.ROOT);
	}

	/** Valida CPF (11 dígitos) ou CNPJ (14 caracteres, incluindo o formato alfanumérico), com ou sem máscara. */
	public static boolean valido(String documento) {
		String d = normalizar(documento);
		if (d == null || d.chars().distinct().count() == 1) {
			return false;
		}
		if (d.matches("\\d{11}")) {
			return cpfValido(d);
		}
		if (d.matches("[0-9A-Z]{12}\\d{2}")) {
			return cnpjValido(d);
		}
		return false;
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		// null/vazio é responsabilidade de @NotBlank
		return value == null || value.isBlank() || valido(value);
	}

	private static boolean cpfValido(String cpf) {
		return digitoCpf(cpf, 9) == valor(cpf.charAt(9)) && digitoCpf(cpf, 10) == valor(cpf.charAt(10));
	}

	private static int digitoCpf(String cpf, int tamanho) {
		int soma = 0;
		for (int i = 0; i < tamanho; i++) {
			soma += valor(cpf.charAt(i)) * (tamanho + 1 - i);
		}
		return digito(soma);
	}

	private static boolean cnpjValido(String cnpj) {
		return digitoCnpj(cnpj, 12) == valor(cnpj.charAt(12)) && digitoCnpj(cnpj, 13) == valor(cnpj.charAt(13));
	}

	private static int digitoCnpj(String cnpj, int tamanho) {
		int soma = 0;
		int deslocamento = PESOS_CNPJ.length - tamanho;
		for (int i = 0; i < tamanho; i++) {
			soma += valor(cnpj.charAt(i)) * PESOS_CNPJ[i + deslocamento];
		}
		return digito(soma);
	}

	private static int digito(int soma) {
		int resto = soma % 11;
		return resto < 2 ? 0 : 11 - resto;
	}

	/** Valor do caractere no cálculo: '0'..'9' = 0..9 e 'A'..'Z' = 17..42 (CNPJ alfanumérico). */
	private static int valor(char c) {
		return c - '0';
	}
}
