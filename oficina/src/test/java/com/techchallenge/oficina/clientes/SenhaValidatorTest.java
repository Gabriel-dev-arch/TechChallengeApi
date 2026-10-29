package com.techchallenge.oficina.clientes;

import com.techchallenge.oficina.clientes.cadastrar.CadastrarClienteRequest;
import com.techchallenge.oficina.clientes.atualizar.AtualizarClienteRequest;
import com.techchallenge.oficina.clientes.dominio.SenhaValidator;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

class SenhaValidatorTest {

    private final SenhaValidator validator = new SenhaValidator();

    @ParameterizedTest
    @ValueSource(strings = {"Senha123", "SenhaSegura123", "   senha válida   "})
    void aceitaSenhasValidasSemNormalizar(String senha) {
        assertThat(validator.isValid(senha, null)).isTrue();
    }

    @Test
    void validaLimitesEmBytesEDeixaNuloParaNotBlank() {
        assertThat(validator.isValid(null, null)).isTrue();
        assertThat(validator.isValid("", null)).isFalse();
        assertThat(validator.isValid(" ".repeat(8), null)).isFalse();
        assertThat(validator.isValid("1234567", null)).isFalse();
        assertThat(validator.isValid("a".repeat(72), null)).isTrue();
        assertThat(validator.isValid("a".repeat(73), null)).isFalse();
        assertThat(validator.isValid("á".repeat(36), null)).isTrue();
        assertThat(validator.isValid("á".repeat(37), null)).isFalse();
    }

    @Test
    void cadastroExigeSenhaEAtualizacaoPermiteOmitir() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var beanValidator = factory.getValidator();
            assertThat(beanValidator.validate(new CadastrarClienteRequest("Maria", "Silva", "maria@email.com",
                    "52998224725", null, null))).anyMatch(erro -> erro.getPropertyPath().toString().equals("senha"));
            assertThat(beanValidator.validate(new AtualizarClienteRequest("Maria", "Silva", "maria@email.com",
                    "52998224725", null, null))).isEmpty();
            assertThat(beanValidator.validate(new AtualizarClienteRequest("Maria", "Silva", "maria@email.com",
                    "52998224725", null, ""))).hasSize(1);
        }
    }

    @Test
    void bcryptUsaSaltEDistingueSenhaExata() {
        var encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode(" Senha123 ");
        assertThat(hash).isNotEqualTo(" Senha123 ").hasSize(60);
        assertThat(hash).isNotEqualTo(encoder.encode(" Senha123 "));
        assertThat(encoder.matches(" Senha123 ", hash)).isTrue();
        assertThat(encoder.matches("Senha123", hash)).isFalse();
    }

    @Test
    void senhaNuncaEhSerializadaNosRequestsOuEntidade() {
        var mapper = JsonMapper.builder().build();
        var request = new CadastrarClienteRequest("Maria", "Silva", "maria@email.com", "52998224725", null, "Senha123");
        var atualizacao = new AtualizarClienteRequest("Maria", "Silva", "maria@email.com", "52998224725", null, "Senha123");
        var cliente = new com.techchallenge.oficina.clientes.entidades.Cliente(
                "Maria", "Silva", "maria@email.com", "52998224725", null, "hash-secreto");
        assertThat(mapper.writeValueAsString(request)).doesNotContain("senha", "Senha123");
        assertThat(mapper.writeValueAsString(atualizacao)).doesNotContain("senha", "Senha123");
        assertThat(mapper.writeValueAsString(cliente)).doesNotContain("senha", "hash-secreto");
    }
}
