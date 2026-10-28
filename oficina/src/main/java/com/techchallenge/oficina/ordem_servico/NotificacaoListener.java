package com.techchallenge.oficina.ordem_servico;

import com.techchallenge.oficina.ordem_servico.cadastrar.eventos.OSCriadaEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class NotificacaoListener {

    @EventListener
    public void notificarClienteMudancaStatusOS(OSCriadaEvent evento) {
        System.out.println("Enviando notificação de alteração de status da OS para o cliente: " + evento.getOrdemServico().getStatus());

    }
}