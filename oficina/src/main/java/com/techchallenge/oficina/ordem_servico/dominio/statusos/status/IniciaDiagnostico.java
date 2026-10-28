package com.techchallenge.oficina.ordem_servico.dominio.statusos.status;

import com.techchallenge.oficina.ordem_servico.dominio.OrdemServicoRepository;
import com.techchallenge.oficina.ordem_servico.dominio.statusos.AvancarStatus;
import com.techchallenge.oficina.ordem_servico.dominio.statusos.AvancarStatusOSAbstract;
import com.techchallenge.oficina.ordem_servico.entidades.OrdemServico;
import com.techchallenge.oficina.ordem_servico.entidades.StatusOS;

public class IniciaDiagnostico extends AvancarStatusOSAbstract {

    public IniciaDiagnostico(OrdemServico os, OrdemServicoRepository repository) {
        super(os, repository);
    }

    public OrdemServico avancar(OrdemServico os, OrdemServicoRepository repository) {

        if(!StatusOS.RECEBIDA.equals(os.getStatus())){
            throw new RuntimeException("Status inválido. A ordem de serviço deve estar com status RECEBIDA.");
        }
        OrdemServico ordemServico = os.atualizaStatus(StatusOS.EM_DIAGNOSTICO);
        return repository.save(ordemServico);
    }
}
