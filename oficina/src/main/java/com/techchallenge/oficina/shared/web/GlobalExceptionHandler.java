package com.techchallenge.oficina.shared.web;

import com.techchallenge.oficina.shared.excecoes.ConflitoException;
import com.techchallenge.oficina.shared.excecoes.RecursoNaoEncontradoException;
import com.techchallenge.oficina.shared.excecoes.RegraInvalidaException;
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

	private Map<String, String> erro(FieldError e) {
		return Map.of("campo", e.getField(), "mensagem", String.valueOf(e.getDefaultMessage()));
	}
}
