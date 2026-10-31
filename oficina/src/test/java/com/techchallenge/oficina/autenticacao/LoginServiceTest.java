package com.techchallenge.oficina.autenticacao;

import com.techchallenge.oficina.autenticacao.login.*;
import com.techchallenge.oficina.autenticacao.dominio.ControleDeAcesso;
import com.techchallenge.oficina.clientes.dominio.ClienteRepository;
import com.techchallenge.oficina.clientes.entidades.Cliente;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock JwtEncoder jwtEncoder;
    @Mock ClienteRepository repository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private LoginService service() {
        return new LoginService(jwtEncoder, repository, passwordEncoder,
                "admin", "admin123", "legado", "legado123", "52998224725", 60);
    }

    private void prepararToken() {
        var agora = Instant.now();
        when(jwtEncoder.encode(any())).thenReturn(new Jwt("token-assinado", agora, agora.plusSeconds(3600),
                Map.of("alg", "HS256"), Map.of("sub", "cliente")));
    }

    @Test
    void autenticaClienteComBcryptEDocumentoNormalizado() {
        var cliente = new Cliente("Maria", "Silva", "maria@email.com", "52998224725", null,
                passwordEncoder.encode("Senha123"));
        when(repository.findByDocumento("52998224725")).thenReturn(Optional.of(cliente));
        prepararToken();
        var response = service().autenticar(new LoginRequest("529.982.247-25", "Senha123"));
        assertThat(response.accessToken()).isEqualTo("token-assinado");
        assertThat(response.expiresIn()).isEqualTo(3600);
        var captor = ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(jwtEncoder).encode(captor.capture());
        var claims = captor.getValue().getClaims();
        assertThat(claims.getSubject()).isEqualTo("52998224725");
        assertThat(claims.getClaimAsString(ControleDeAcesso.CLAIM_DOCUMENTO)).isEqualTo("52998224725");
        assertThat(claims.getClaims().toString()).doesNotContain("Senha123", cliente.getSenhaHash());
    }

    @Test
    void senhaErradaNaoEmiteToken() {
        var cliente = new Cliente("Maria", "Silva", "maria@email.com", "52998224725", null,
                passwordEncoder.encode("Senha123"));
        when(repository.findByDocumento("52998224725")).thenReturn(Optional.of(cliente));
        assertThatThrownBy(() -> service().autenticar(new LoginRequest("52998224725", "errada")))
                .isInstanceOf(CredenciaisInvalidasException.class);
        assertThatThrownBy(() -> service().autenticar(new LoginRequest("52998224725", "á".repeat(37))))
                .isInstanceOf(CredenciaisInvalidasException.class);
        verifyNoInteractions(jwtEncoder);
    }

    @Test
    void clienteSemSenhaOuInexistenteNaoAutentica() {
        assertThatThrownBy(() -> service().autenticar(new LoginRequest("52998224725", "Senha123")))
                .isInstanceOf(CredenciaisInvalidasException.class);
        when(repository.findByDocumento("52998224725")).thenReturn(Optional.of(
                new Cliente("Maria", "Silva", "maria@email.com", "52998224725", null)));
        assertThatThrownBy(() -> service().autenticar(new LoginRequest("52998224725", "Senha123")))
                .isInstanceOf(CredenciaisInvalidasException.class);
        verifyNoInteractions(jwtEncoder);
    }

    @Test
    void preservaLoginAdministrativoEClienteLegadoSemSenhaCadastrada() {
        prepararToken();
        service().autenticar(new LoginRequest("admin", "admin123"));
        service().autenticar(new LoginRequest("legado", "legado123"));
        verify(jwtEncoder, times(2)).encode(any());
    }

    @Test
    void senhaConfiguradaNaoContornaSenhaDoClienteCadastrado() {
        when(repository.findByDocumento("52998224725")).thenReturn(Optional.of(
                new Cliente("Maria", "Silva", "maria@email.com", "52998224725", null, "hash")));
        assertThatThrownBy(() -> service().autenticar(new LoginRequest("legado", "legado123")))
                .isInstanceOf(CredenciaisInvalidasException.class);
        verifyNoInteractions(jwtEncoder);
    }

    @Test
    void rejeitaUsuarioOuSenhaConfiguradosInvalidos() {
        assertThatThrownBy(() -> service().autenticar(new LoginRequest("desconhecido", "admin123")))
                .isInstanceOf(CredenciaisInvalidasException.class);
        assertThatThrownBy(() -> service().autenticar(new LoginRequest("admin", "errada")))
                .isInstanceOf(CredenciaisInvalidasException.class);
        verifyNoInteractions(jwtEncoder);
    }

    @Test
    void validaDocumentoDeConfiguracao() {
        assertThatThrownBy(() -> new LoginService(jwtEncoder, repository, passwordEncoder,
                "admin", "admin123", "legado", "legado123", "invalido", 60))
                .isInstanceOf(IllegalStateException.class);
    }
}
