package com.techchallenge.oficina.ordem_servico.dominio.statusos;

import com.techchallenge.oficina.ordem_servico.dominio.OrdemServicoRepository;
import com.techchallenge.oficina.ordem_servico.entidades.OrdemServico;
import com.techchallenge.oficina.ordem_servico.entidades.StatusOS;

import java.math.BigDecimal;
import java.util.Collections;

public class Finalizada implements AvancarStatus{

    private final OrdemServicoRepository repository;


    public Finalizada(OrdemServico os, OrdemServicoRepository repository) {
        this.repository = repository;
    }

    @Override
    public OrdemServico avancar(OrdemServico os) {


        return null;
    }
}
