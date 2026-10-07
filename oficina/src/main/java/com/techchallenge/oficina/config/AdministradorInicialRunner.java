package com.techchallenge.oficina.config;

import com.techchallenge.oficina.autenticacao.dominio.Perfil;
import com.techchallenge.oficina.autenticacao.dominio.UsuarioRepository;
import com.techchallenge.oficina.autenticacao.entidades.Usuario;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Cria o administrador de demonstração ({@code ADMIN_USERNAME}/{@code ADMIN_PASSWORD}) na inicialização,
 * só quando a tabela de usuários está vazia. A senha é gravada com BCrypt.
 */
@Slf4j
@Component
public class AdministradorInicialRunner implements ApplicationRunner {

	private final UsuarioRepository repository;
	private final PasswordEncoder passwordEncoder;
	private final String username;
	private final String senha;

	public AdministradorInicialRunner(UsuarioRepository repository, PasswordEncoder passwordEncoder,
			@Value("${app.seguranca.admin.username:}") String username,
			@Value("${app.seguranca.admin.senha:}") String senha) {
		this.repository = repository;
		this.passwordEncoder = passwordEncoder;
		this.username = username;
		this.senha = senha;
	}

	@Override
	public void run(ApplicationArguments args) {
		if (repository.count() > 0) {
			return;
		}
		if (!StringUtils.hasText(username) || !StringUtils.hasText(senha)) {
			log.warn("Nenhum usuário cadastrado e ADMIN_USERNAME/ADMIN_PASSWORD não definidos: "
					+ "o administrador inicial não foi criado. Veja o .env.example.");
			return;
		}
		repository.save(new Usuario(username, passwordEncoder.encode(senha), Perfil.ADMIN));
		log.info("Administrador inicial '{}' criado.", username);
	}
}
