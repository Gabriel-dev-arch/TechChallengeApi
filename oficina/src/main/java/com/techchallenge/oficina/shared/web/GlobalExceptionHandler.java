package com.techchallenge.oficina.shared.web;

import com.techchallenge.oficina.clientes.dominio.ClienteNaoEncontradoException;
import com.techchallenge.oficina.clientes.dominio.DocumentoJaCadastradoException;
import com.techchallenge.oficina.servicos.dominio.ServicoNaoEncontradoException;
import com.techchallenge.oficina.veiculos.dominio.PlacaJaCadastradaException;
import com.techchallenge.oficina.veiculos.dominio.VeiculoNaoEncontradoException;
import com.techchallenge.oficina.shared.excecoes.ConflitoException;
import com.techchallenge.oficina.shared.excecoes.RecursoNaoEncontradoException;
import com.techchallenge.oficina.shared.excecoes.RegraInvalidaException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.exc.InvalidFormatException;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Map;

/**
 * Converte exceções em respostas {@code application/problem+json} (RFC 9457).
 *
 * <p>As exceções de domínio de cada módulo estendem uma das bases de {@code shared.excecoes}
 * ({@link RecursoNaoEncontradoException}, {@link ConflitoException} ou {@link RegraInvalidaException}),
 * então módulos novos não precisam alterar esta classe.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler(RecursoNaoEncontradoException.class)
	ProblemDetail naoEncontrado(RecursoNaoEncontradoException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	@ExceptionHandler(ConflitoException.class)
	ProblemDetail conflito(ConflitoException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
	}

	@ExceptionHandler(RegraInvalidaException.class)
	ProblemDetail regraInvalida(RegraInvalidaException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
	}

	@ExceptionHandler(ServicoNaoEncontradoException.class)
	ProblemDetail naoEncontrado(ServicoNaoEncontradoException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	@ExceptionHandler({DocumentoJaCadastradoException.class, PlacaJaCadastradaException.class,
			DataIntegrityViolationException.class, ObjectOptimisticLockingFailureException.class})
	ProblemDetail conflito(Exception ex) {
		String detalhe = ex instanceof DocumentoJaCadastradoException || ex instanceof PlacaJaCadastradaException
				? ex.getMessage()
				: "A operação conflita com o estado atual dos dados. Tente novamente.";
		return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, detalhe);
		/** Violação de constraint no banco ou concorrência (lock otimista): mensagem genérica, sem detalhes internos. */
		@ExceptionHandler({DataIntegrityViolationException.class, ObjectOptimisticLockingFailureException.class})
		ProblemDetail conflitoDeDados(Exception ex) {
			return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
					"A operação conflita com o estado atual dos dados. Tente novamente.");
		}

		@Override
		protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
				HttpHeaders headers, HttpStatusCode status, WebRequest request) {
			ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Dados inválidos");
			List<Map<String, String>> erros = ex.getBindingResult().getFieldErrors().stream()
					.map(this::erro).toList();
			problema.setProperty("errors", erros);
			return handleExceptionInternal(ex, problema, headers, status, request);
		}

		@Override
		protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
				HttpHeaders headers, HttpStatusCode status, WebRequest request) {
			ProblemDetail problema;
			if (ex.getCause() instanceof InvalidFormatException formato) {
				String campo = nomeDoCampo(formato);
				problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
						"Valor inválido para o campo '" + campo + "'");
				problema.setProperty("errors", List.of(Map.of("campo", campo, "mensagem", mensagemDeFormato(formato))));
			} else {
				problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "JSON malformado ou ilegível");
			}
			return handleExceptionInternal(ex, problema, headers, status, request);
		}

		/** Caminho do campo no JSON, ex.: "tipo" ou "itens.quantidade" em objetos aninhados. */
		private String nomeDoCampo(InvalidFormatException ex) {
			return ex.getPath().stream()
					.map(JacksonException.Reference::getPropertyName)
					.filter(Objects::nonNull)
					.collect(Collectors.joining("."));
		}

		/** Para enums, lista os valores aceitos; para outros tipos (número, data…), só aponta o valor recebido. */
		private String mensagemDeFormato(InvalidFormatException ex) {
			String valor = String.valueOf(ex.getValue());
			Class<?> tipo = ex.getTargetType();
			if (tipo.isEnum()) {
				String aceitos = Arrays.stream(tipo.getEnumConstants())
						.map(Object::toString)
						.collect(Collectors.joining(", "));
				return "valor '" + valor + "' não é aceito. Valores aceitos: " + aceitos;
			}
			return "valor '" + valor + "' não é válido para este campo";
		}

		private Map<String, String> erro(FieldError e) {
			return Map.of("campo", e.getField(), "mensagem", String.valueOf(e.getDefaultMessage()));
		}
	}
