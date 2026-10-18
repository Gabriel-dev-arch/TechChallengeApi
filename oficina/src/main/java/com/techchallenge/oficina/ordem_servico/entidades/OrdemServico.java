package com.techchallenge.oficina.ordem_servico.entidades;

import com.techchallenge.oficina.clientes.entidades.Cliente;
import com.techchallenge.oficina.pecasinsumos.entidades.PecaInsumo;
import com.techchallenge.oficina.servicos.entidades.Servico;
import com.techchallenge.oficina.shared.persistence.BaseEntity;
import com.techchallenge.oficina.veiculos.entidades.Veiculo;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Entity
@Table(name = "ordem_servico")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrdemServico extends BaseEntity {

    private String firstName;
    private String lastName;
    @OneToOne
    private Cliente cliente;
    @OneToOne
    private Veiculo veiculo;
    private String diagnostico;
    @OneToMany
    private List<PecaInsumo> itens;
    @OneToMany
    private List<Servico> servicos;
    private BigDecimal orcamento;
    @Enumerated(value = EnumType.STRING)
    private StatusOS status;
}