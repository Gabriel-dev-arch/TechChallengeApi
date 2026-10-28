package com.techchallenge.oficina.ordem_servico.dominio.statusos;

import com.techchallenge.oficina.ordem_servico.dominio.OrdemServicoRepository;
import com.techchallenge.oficina.ordem_servico.entidades.OrdemServico;

public abstract class AvancarStatusOSAbstract {

    public AvancarStatusOSAbstract(OrdemServico os,OrdemServicoRepository repository){
        avancar(os, repository);
    }

    public abstract OrdemServico avancar(OrdemServico os, OrdemServicoRepository repository);
}
