package com.techchallenge.oficina.ordem_servico.cadastrar.request;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RequestsTest {

    @Test
    @DisplayName("Testa criação de ItemQuantidadeRequest")
    void testItemQuantidadeRequest() {
        UUID id = UUID.randomUUID();
        ItemQuantidadeRequest req = new ItemQuantidadeRequest(id, BigDecimal.valueOf(5));

        assertThat(req.id()).isEqualTo(id);
        assertThat(req.quantidade()).isEqualTo(BigDecimal.valueOf(5));
    }

    @Test
    @DisplayName("Testa criação de ServicosRequest")
    void testServicosRequest() {
        UUID id = UUID.randomUUID();
        ServicosRequest req = new ServicosRequest(id);

        assertThat(req.id()).isEqualTo(id);
    }

    @Test
    @DisplayName("Testa criação de FuncionarioRequest")
    void testFuncionarioRequest() {
        UUID id = UUID.randomUUID();
        FuncionarioRequest req = new FuncionarioRequest(id);

        assertThat(req.id()).isEqualTo(id);
    }
}
