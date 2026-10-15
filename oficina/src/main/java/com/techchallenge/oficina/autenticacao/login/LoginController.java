package com.techchallenge.oficina.autenticacao.login;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Autenticação", description = "Emissão do token JWT para as APIs administrativas")
@RestController
@RequestMapping("/autenticacao")
public class LoginController {

	private final LoginService service;

	public LoginController(LoginService service) {
		this.service = service;
	}

	@PostMapping("/login")
	@SecurityRequirements
	@Operation(summary = "Gera token JWT", description = "Valida usuário e senha do administrador e retorna o token de acesso")
	public LoginResponse login(@Valid @RequestBody LoginRequest request) {
		return service.autenticar(request);
	}
}
