package com.techchallenge.oficina.shared.web;

import com.techchallenge.oficina.clientes.dominio.ClienteNaoEncontradoException;
import com.techchallenge.oficina.clientes.dominio.DocumentoJaCadastradoException;
import com.techchallenge.oficina.shared.excecoes.NaoAutenticadoException;
import com.techchallenge.oficina.veiculos.dominio.PlacaJaCadastradaException;
import com.techchallenge.oficina.veiculos.dominio.VeiculoNaoEncontradoException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
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

	@ExceptionHandler(NaoAutenticadoException.class)
	ProblemDetail naoAutenticado(NaoAutenticadoException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
	}

	/** Token ausente ou inválido/expirado; chega aqui pelo entry point configurado no SecurityConfig. */
	@ExceptionHandler(AuthenticationException.class)
	ProblemDetail tokenAusenteOuInvalido(AuthenticationException ex) {
		String detalhe = ex instanceof OAuth2AuthenticationException
				? "Token inválido ou expirado. Faça login novamente em POST /auth/login."
				: "Autenticação necessária. Envie o header 'Authorization: Bearer <token>' obtido em POST /auth/login.";
		return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, detalhe);
	}

	/** Token válido, mas sem o perfil exigido; chega aqui pelo access denied handler configurado no SecurityConfig. */
	@ExceptionHandler(AccessDeniedException.class)
	ProblemDetail acessoNegado(AccessDeniedException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN,
				"Seu perfil não tem permissão para acessar este recurso.");
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
