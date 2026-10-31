package com.techchallenge.oficina.ordem_servico.cadastrar.eventos;

import com.techchallenge.oficina.ordem_servico.entidades.OrdemServico;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OSCriadaEvent {

    private final OrdemServico ordemServico;

    public OSCriadaEvent(OrdemServico ordemServico) {
        this.ordemServico = ordemServico;
    }
}
