package com.techchallenge.oficina.shared.web;

import com.techchallenge.oficina.clientes.dominio.ClienteNaoEncontradoException;
import com.techchallenge.oficina.clientes.dominio.DocumentoJaCadastradoException;
import com.techchallenge.oficina.veiculos.dominio.PlacaJaCadastradaException;
import com.techchallenge.oficina.veiculos.dominio.VeiculoNaoEncontradoException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.Map;

/** Converte exceções em respostas {@code application/problem+json} (RFC 9457). */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler(ClienteNaoEncontradoException.class)
	ProblemDetail naoEncontrado(ClienteNaoEncontradoException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	@ExceptionHandler(VeiculoNaoEncontradoException.class)
	ProblemDetail naoEncontrado(VeiculoNaoEncontradoException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	@ExceptionHandler({DocumentoJaCadastradoException.class, PlacaJaCadastradaException.class,
			DataIntegrityViolationException.class, ObjectOptimisticLockingFailureException.class})
	ProblemDetail conflito(Exception ex) {
		String detalhe = ex instanceof DocumentoJaCadastradoException || ex instanceof PlacaJaCadastradaException
				? ex.getMessage()
				: "A operação conflita com o estado atual dos dados. Tente novamente.";
		return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, detalhe);
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

	private Map<String, String> erro(FieldError e) {
		return Map.of("campo", e.getField(), "mensagem", String.valueOf(e.getDefaultMessage()));
	}
}
