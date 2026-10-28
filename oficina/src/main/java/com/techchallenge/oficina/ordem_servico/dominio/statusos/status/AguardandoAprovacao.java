package com.techchallenge.oficina.ordem_servico.dominio.statusos.status;

import com.techchallenge.oficina.ordem_servico.dominio.OrdemServicoRepository;
import com.techchallenge.oficina.ordem_servico.dominio.statusos.AvancarStatus;
import com.techchallenge.oficina.ordem_servico.dominio.statusos.AvancarStatusOSAbstract;
import com.techchallenge.oficina.ordem_servico.entidades.OrdemServico;
import com.techchallenge.oficina.ordem_servico.entidades.StatusOS;

public class AguardandoAprovacao extends AvancarStatusOSAbstract {

    public AguardandoAprovacao(OrdemServico os, OrdemServicoRepository repository) {
        super(os, repository);
    }

    public OrdemServico avancar(OrdemServico osCadastrada, OrdemServicoRepository repository) {

        if(!StatusOS.EM_DIAGNOSTICO.equals(osCadastrada.getStatus())){
            throw new RuntimeException("Status inválido. A ordem de serviço deve estar com status EM_DIAGNOSTICO.");
        }

        OrdemServico ordemServico = osCadastrada.atualizaStatus(StatusOS.AGUARDANDO_APROVACAO);

        return repository.saveAndFlush(ordemServico);
    }
}
