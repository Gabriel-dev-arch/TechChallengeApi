package com.techchallenge.oficina.veiculos.dominio;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Locale;

public class PlacaValidator implements ConstraintValidator<Placa, String> {

	public static String normalizar(String placa) {
		if (placa == null) {
			return null;
		}
		return placa.trim().replace("-", "").toUpperCase(Locale.ROOT);
	}

	public static boolean valido(String placa) {
		if (placa == null) {
			return false;
		}
		String valor = placa.trim().toUpperCase(Locale.ROOT);
		return valor.matches("[A-Z]{3}-?[0-9]{4}|[A-Z]{3}[0-9][A-Z][0-9]{2}");
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		return value == null || value.isBlank() || valido(value);
	}
}
