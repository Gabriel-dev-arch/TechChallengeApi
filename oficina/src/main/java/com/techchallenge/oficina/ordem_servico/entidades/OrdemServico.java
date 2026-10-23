package com.techchallenge.oficina.ordem_servico.entidades;

import com.techchallenge.oficina.clientes.entidades.Cliente;
import com.techchallenge.oficina.ordem_servico.cadastrar.ItemQuantidade;
import com.techchallenge.oficina.pecasinsumos.entidades.PecaInsumo;
import com.techchallenge.oficina.servicos.entidades.Servico;
import com.techchallenge.oficina.shared.persistence.BaseEntity;
import com.techchallenge.oficina.veiculos.entidades.Veiculo;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    @LastModifiedDate
    private Instant updatedAt;

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

    public void incluirPecaInsumos(List<PecaInsumo> pecasInsumos, List<ItemQuantidade> itens) {
        this.itens.addAll(pecasInsumos);
        this.realizaCalculoItensInsumosOrcamento(pecasInsumos, itens);
    }

    public void incluirServicos(List<Servico> servicos) {
        this.servicos.addAll(servicos);
        this.realizaCalculoServicos(servicos);
    }

    private void realizaCalculoItensInsumosOrcamento(List<PecaInsumo> pecasInsumos, List<ItemQuantidade> itens){

        Map<UUID, PecaInsumo> pecasPorId = this.itens.stream()
                .collect(Collectors.toMap(PecaInsumo::getId,
                        Function.identity(),
                        (existente, substituto) -> existente));

        BigDecimal valorTotalPecasInsumos = itens.stream()
                .map(item -> {
                    return pecasPorId.get(item.itens()).getPrecoUnitario().multiply(item.quantidade());
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add).add(this.getOrcamento());

        this.orcamento = valorTotalPecasInsumos;
    }

    private void realizaCalculoServicos(List<Servico> servicos){

        var valorTotalServicos = servicos.stream()
                .map(Servico::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add).add(this.getOrcamento());

        this.orcamento = valorTotalServicos;
    }
}