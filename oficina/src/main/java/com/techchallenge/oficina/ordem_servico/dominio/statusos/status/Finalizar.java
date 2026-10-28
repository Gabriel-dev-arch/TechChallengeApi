package com.techchallenge.oficina.ordem_servico.dominio.statusos.status;

import com.techchallenge.oficina.ordem_servico.dominio.OrdemServicoRepository;
import com.techchallenge.oficina.ordem_servico.dominio.statusos.AvancarStatus;
import com.techchallenge.oficina.ordem_servico.dominio.statusos.AvancarStatusOSAbstract;
import com.techchallenge.oficina.ordem_servico.entidades.OrdemServico;
import com.techchallenge.oficina.ordem_servico.entidades.StatusOS;

public class Finalizar extends AvancarStatusOSAbstract {

    public Finalizar(OrdemServico os, OrdemServicoRepository repository) {
        super(os, repository);
    }

    @Override
    public OrdemServico avancar(OrdemServico os, OrdemServicoRepository repository) {

        if(!StatusOS.FINALIZADA.equals(os.getStatus())){
            throw new RuntimeException("Status inválido. A ordem de serviço deve estar com status RECEBIDA.");
        }
        OrdemServico ordemServico = os.atualizaStatus(StatusOS.ENTREGUE);
        return repository.saveAndFlush(ordemServico);
    }
}
