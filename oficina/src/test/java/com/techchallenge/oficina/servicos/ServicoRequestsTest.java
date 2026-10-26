package com.techchallenge.oficina.servicos;

import com.techchallenge.oficina.servicos.consultar.ConsultarServicoRequest;
import com.techchallenge.oficina.servicos.remover.RemoverServicoRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ServicoRequestsTest {

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
}
