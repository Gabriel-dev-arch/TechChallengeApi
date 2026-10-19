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
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@Entity
@Table(name = "ordem_servico")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrdemServico extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;
    @ManyToOne
    @JoinColumn(name = "veiculo_id")
    private Veiculo veiculo;
    private String diagnostico;
    @ManyToMany
    @JoinTable(name = "ordem_servico_itens",
            joinColumns = @JoinColumn(name = "ordem_servico_id"),
            inverseJoinColumns = @JoinColumn(name = "itens_id"))
    private List<PecaInsumo> itens;
    @ManyToMany
    @JoinTable(name = "ordem_servico_servicos",
            joinColumns = @JoinColumn(name = "ordem_servico_id"),
            inverseJoinColumns = @JoinColumn(name = "servicos_id"))
    private List<Servico> servicos;
    private BigDecimal orcamento;
    @Enumerated(value = EnumType.STRING)
    private StatusOS status;

    public OrdemServico(StatusOS status, BigDecimal orcamento, List<Servico> servicos, List<PecaInsumo> itens, String diagnostico, Veiculo veiculo, Cliente cliente) {
        this.status = status;
        this.orcamento = orcamento;
        this.servicos = servicos;
        this.itens = itens;
        this.diagnostico = diagnostico;
        this.veiculo = veiculo;
        this.cliente = cliente;
    }

    public String formatDate(){

        if(this.getCreatedAt() != null){
            return this.getCreatedAt().atOffset(ZoneOffset.of("-03:00")).format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm"));
        }
        return null;
    }
}