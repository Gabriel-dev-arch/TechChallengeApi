package com.techchallenge.oficina.autenticacao.login;

import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Autenticação")
@RestController
@RequestMapping("/auth")
public class LoginController {

	private final LoginService service;

	public LoginController(LoginService service) {
		this.service = service;
	}

	/** Rota pública: o {@code @SecurityRequirements} vazio tira o cadeado global no Swagger. */
	@SecurityRequirements
	@PostMapping("/login")
	public TokenResponse login(@Valid @RequestBody LoginRequest request) {
		return service.login(request);
	}
}
