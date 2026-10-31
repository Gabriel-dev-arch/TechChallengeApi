package com.techchallenge.oficina.ordem_servico;

import com.techchallenge.oficina.ordem_servico.cadastrar.eventos.OSCriadaEvent;
import com.techchallenge.oficina.ordem_servico.entidades.OrdemServico;
import com.techchallenge.oficina.ordem_servico.entidades.StatusOS;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NotificacaoListenerTest {

    @Test
    void deveNotificarMudancaDeStatusDaOS() {
        OrdemServico os = new OrdemServico(StatusOS.APROVADA, BigDecimal.ZERO,
                List.of(), List.of(), List.of(), null, null, null, null);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));

            new NotificacaoListener().notificarClienteMudancaStatusOS(new OSCriadaEvent(os));
        } finally {
            System.setOut(originalOut);
        }

        assertThat(output.toString(StandardCharsets.UTF_8))
                .contains("Enviando notificação de alteração de status da OS para o cliente: APROVADA");
    }
}
