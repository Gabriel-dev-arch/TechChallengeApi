package com.techchallenge.oficina.autenticacao;

import com.jayway.jsonpath.JsonPath;
import com.techchallenge.oficina.autenticacao.dominio.Perfil;
import com.techchallenge.oficina.autenticacao.dominio.UsuarioRepository;
import com.techchallenge.oficina.autenticacao.entidades.Usuario;
import com.techchallenge.oficina.config.AdministradorInicialRunner;
import com.techchallenge.oficina.config.JwtConfig;
import com.techchallenge.oficina.support.PostgresTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresTestConfiguration.class)
class AutenticacaoIntegrationTest {

	private static final String TOKEN_INVALIDO = "Token inválido ou expirado. Faça login novamente em POST /auth/login.";

	@Autowired
	MockMvc mockMvc;

	@Autowired
	JwtEncoder jwtEncoder;

	@Autowired
	UsuarioRepository usuarioRepository;

	@Autowired
	PasswordEncoder passwordEncoder;

	@Autowired
	AdministradorInicialRunner administradorInicial;

	private ResultActions login(String username, String senha) throws Exception {
		return mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"username":"%s","senha":"%s"}""".formatted(username, senha)));
	}

	private String tokenDoAdmin() throws Exception {
		String corpo = login("admin", "admin123").andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		return JsonPath.read(corpo, "$.accessToken");
	}

	private ResultActions listarClientesCom(String token) throws Exception {
		return mockMvc.perform(get("/clientes").header(HttpHeaders.AUTHORIZATION, "Bearer " + token));
	}

	/** Token assinado com o mesmo encoder da aplicação, mas com claims escolhidas pelo teste. */
	private String token(String emissor, List<String> perfis, Instant emitidoEm, Duration validade) {
		JwtClaimsSet claims = JwtClaimsSet.builder().issuer(emissor).subject("admin")
				.issuedAt(emitidoEm).expiresAt(emitidoEm.plus(validade))
				.claim(JwtConfig.CLAIM_PERFIS, perfis).build();
		return jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
				.getTokenValue();
	}

	private static void esperaProblemJson401(ResultActions resposta, String detalhe) throws Exception {
		resposta.andExpect(status().isUnauthorized())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.status").value(401))
				.andExpect(jsonPath("$.detail").value(detalhe))
				.andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, containsString("Bearer")));
	}

	@Test
	void loginComCredenciaisDeDemonstracaoDevolveToken() throws Exception {
		login("admin", "admin123").andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").value(not(emptyOrNullString())))
				.andExpect(jsonPath("$.tipo").value("Bearer"))
				.andExpect(jsonPath("$.expiraEm").value(endsWith("-03:00")));
	}

	@Test
	void loginComSenhaErradaOuUsuarioInexistenteRetorna401ComAMesmaMensagem() throws Exception {
		for (ResultActions resposta : List.of(login("admin", "senha-errada"), login("nao-existe", "admin123"))) {
			resposta.andExpect(status().isUnauthorized())
					.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
					.andExpect(jsonPath("$.detail").value("Usuário ou senha inválidos"))
					.andExpect(jsonPath("$.accessToken").doesNotExist());
		}
	}

	@Test
	void loginSemCamposRetorna400ComErrosPorCampo() throws Exception {
		login("", "").andExpect(status().isBadRequest())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.errors[?(@.campo=='username')]").exists())
				.andExpect(jsonPath("$.errors[?(@.campo=='senha')]").exists());
	}

	@Test
	void rotaAdministrativaSemTokenRetorna401EmProblemJson() throws Exception {
		esperaProblemJson401(mockMvc.perform(get("/clientes")),
				"Autenticação necessária. Envie o header 'Authorization: Bearer <token>' obtido em POST /auth/login.");
	}

	@Test
	void tokenObtidoNoLoginDaAcessoAsRotasAdministrativas() throws Exception {
		listarClientesCom(tokenDoAdmin()).andExpect(status().isOk()).andExpect(jsonPath("$.content").isArray());
	}

	@Test
	void tokenAdulteradoRetorna401() throws Exception {
		String token = tokenDoAdmin();
		// troca um caractere no meio da assinatura (o último pode carregar só bits de preenchimento)
		int posicao = token.length() - 10;
		char trocado = token.charAt(posicao) == 'A' ? 'B' : 'A';
		String adulterado = token.substring(0, posicao) + trocado + token.substring(posicao + 1);

		esperaProblemJson401(listarClientesCom(adulterado), TOKEN_INVALIDO);
	}

	@Test
	void tokenAssinadoComOutraChaveRetorna401() throws Exception {
		SecretKeySpec outraChave = new SecretKeySpec(
				"outra-chave-que-nao-e-a-da-aplicacao-1234567890".getBytes(StandardCharsets.UTF_8), "HmacSHA256");
		JwtClaimsSet claims = JwtClaimsSet.builder().issuer(JwtConfig.EMISSOR).subject("admin")
				.issuedAt(Instant.now()).expiresAt(Instant.now().plus(Duration.ofHours(1)))
				.claim(JwtConfig.CLAIM_PERFIS, List.of("ADMIN")).build();
		String token = NimbusJwtEncoder.withSecretKey(outraChave).build()
				.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();

		esperaProblemJson401(listarClientesCom(token), TOKEN_INVALIDO);
	}

	@Test
	void tokenExpiradoRetorna401() throws Exception {
		// expirado há 1h: bem além da tolerância de relógio do validador (60s)
		String expirado = token(JwtConfig.EMISSOR, List.of("ADMIN"), Instant.now().minus(Duration.ofHours(2)),
				Duration.ofHours(1));

		esperaProblemJson401(listarClientesCom(expirado), TOKEN_INVALIDO);
	}

	@Test
	void tokenDeOutroEmissorRetorna401() throws Exception {
		String token = token("outro-emissor", List.of("ADMIN"), Instant.now(), Duration.ofHours(1));

		esperaProblemJson401(listarClientesCom(token), TOKEN_INVALIDO);
	}

	@Test
	void tokenValidoSemPerfilAdminRetorna403EmProblemJson() throws Exception {
		String token = token(JwtConfig.EMISSOR, List.of("CLIENTE"), Instant.now(), Duration.ofHours(1));

		listarClientesCom(token).andExpect(status().isForbidden())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.status").value(403))
				.andExpect(jsonPath("$.detail").value("Seu perfil não tem permissão para acessar este recurso."));
	}

	@Test
	void swaggerEOpenApiSaoPublicos() throws Exception {
		mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
		mockMvc.perform(get("/swagger-ui.html")).andExpect(status().is3xxRedirection());
		mockMvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
	}

	@Test
	void openApiDeclaraEsquemaBearerJwtGlobalELoginSemCadeado() throws Exception {
		mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
				.andExpect(jsonPath("$.components.securitySchemes.bearerAuth.type").value("http"))
				.andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
				.andExpect(jsonPath("$.components.securitySchemes.bearerAuth.bearerFormat").value("JWT"))
				.andExpect(jsonPath("$.security[0].bearerAuth").exists())
				.andExpect(jsonPath("$.paths['/auth/login'].post.security", hasSize(0)));
	}

	@Test
	void rotasSobPublicoNaoExigemToken() throws Exception {
		// o endpoint da OS ainda não existe: basta não ser barrado pela segurança
		int status = mockMvc.perform(get("/publico/ordens-servico/" + UUID.randomUUID()))
				.andReturn().getResponse().getStatus();

		assertThat(status).isNotIn(401, 403);
	}

	@Test
	void administradorDeDemonstracaoECriadoUmaVezComSenhaEmBcrypt() {
		usuarioRepository.deleteAll();

		administradorInicial.run(new DefaultApplicationArguments());
		administradorInicial.run(new DefaultApplicationArguments());

		List<Usuario> usuarios = usuarioRepository.findAll();
		assertThat(usuarios).hasSize(1);
		Usuario admin = usuarios.getFirst();
		assertThat(admin.getUsername()).isEqualTo("admin");
		assertThat(admin.getPerfil()).isEqualTo(Perfil.ADMIN);
		assertThat(admin.isAtivo()).isTrue();
		assertThat(admin.getSenhaHash()).startsWith("$2").isNotEqualTo("admin123");
		assertThat(passwordEncoder.matches("admin123", admin.getSenhaHash())).isTrue();
	}
}
