package com.techchallenge.oficina.servicos;

import com.techchallenge.oficina.servicos.atualizar.AtualizarServicoRequest;
import com.techchallenge.oficina.servicos.cadastrar.CadastrarServicoRequest;
import com.techchallenge.oficina.servicos.consultar.ConsultarServicoRequest;
import com.techchallenge.oficina.servicos.remover.RemoverServicoRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class ServicoRequestsTest {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    private static Set<String> camposInvalidos(Object request) {
        return VALIDATOR.validate(request).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

    @Test
    @DisplayName("Testa criação de ConsultarServicoRequest")
    void testConsultarServicoRequest() {
        ConsultarServicoRequest req = new ConsultarServicoRequest("servico-123");
        assertThat(req.idServico()).isEqualTo("servico-123");
    }

    @Test
    @DisplayName("Testa criação de RemoverServicoRequest")
    void testRemoverServicoRequest() {
        RemoverServicoRequest req = new RemoverServicoRequest("servico-456");
        assertThat(req.idServico()).isEqualTo("servico-456");
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.01", "150.00", "99999999.99"})
    @DisplayName("Aceita valor de zero até o limite de numeric(10,2)")
    void deveAceitarValorValido(String valor) {
        assertThat(camposInvalidos(new CadastrarServicoRequest("Troca de óleo", new BigDecimal(valor)))).isEmpty();
        assertThat(camposInvalidos(new AtualizarServicoRequest("Troca de óleo", new BigDecimal(valor)))).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"-0.01", "-50", "10.999", "100000000.00"})
    @DisplayName("Rejeita valor negativo, com mais de 2 casas ou acima de numeric(10,2)")
    void deveRejeitarValorInvalido(String valor) {
        assertThat(camposInvalidos(new CadastrarServicoRequest("Troca de óleo", new BigDecimal(valor)))).containsExactly("valor");
        assertThat(camposInvalidos(new AtualizarServicoRequest("Troca de óleo", new BigDecimal(valor)))).containsExactly("valor");
    }

    @Test
    @DisplayName("Rejeita descrição com mais de 255 caracteres e valor ausente")
    void deveRejeitarDescricaoLongaEValorAusente() {
        String longa = "x".repeat(256);
        assertThat(camposInvalidos(new CadastrarServicoRequest(longa, null))).containsExactlyInAnyOrder("descricao", "valor");
        assertThat(camposInvalidos(new AtualizarServicoRequest(longa, null))).containsExactlyInAnyOrder("descricao", "valor");
    }
}
