package com.techchallenge.oficina.ordem_servico.entidades;

import com.techchallenge.oficina.clientes.entidades.Cliente;
import com.techchallenge.oficina.funcionario.entidades.Funcionario;
import com.techchallenge.oficina.ordem_servico.cadastrar.request.ItemQuantidade;
import com.techchallenge.oficina.ordem_servico.dominio.exception.OrcamentoFechadoException;
import com.techchallenge.oficina.pecasinsumos.dominio.PecaInsumoNaoEncontradoException;
import com.techchallenge.oficina.pecasinsumos.dominio.QuantidadeInvalidaException;
import com.techchallenge.oficina.pecasinsumos.entidades.PecaInsumo;
import com.techchallenge.oficina.servicos.entidades.Servico;
import com.techchallenge.oficina.shared.persistence.BaseEntity;
import com.techchallenge.oficina.veiculos.entidades.Veiculo;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Audited;
import org.hibernate.envers.AuditTable;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@Entity
@Audited
@AuditTable(value = "ordem_servicos_audit")
@Table(name = "ordem_servico")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrdemServico extends BaseEntity {

    private static final Set<StatusOS> STATUS_COM_ORCAMENTO_ABERTO = EnumSet.of(StatusOS.RECEBIDA, StatusOS.EM_DIAGNOSTICO);

    @ManyToOne(optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "veiculo_id")
    private Veiculo veiculo;

    private String diagnostico;

    @ElementCollection
    @CollectionTable(name = "ordem_servico_itens", joinColumns = @JoinColumn(name = "ordem_servico_id"))
    private List<OrdemServicoItem> itensQuantidade = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "ordem_servico_servicos", joinColumns = @JoinColumn(name = "ordem_servico_id"), inverseJoinColumns = @JoinColumn(name = "servicos_id"))
    private List<Servico> servicos;

    private BigDecimal orcamento;

    @Enumerated(value = EnumType.STRING)
    private StatusOS status;

    @ManyToOne
    @JoinColumn(name = "funcionario_id")
    private Funcionario funcionario;

    @LastModifiedDate
    private Instant updatedAt;

    public OrdemServico(StatusOS status, BigDecimal orcamento, List<Servico> servicos,
            List<PecaInsumo> pecasInsumos, List<ItemQuantidade> itensQuantidade,
            String diagnostico, Veiculo veiculo, Cliente cliente, Funcionario funcionario) {
        this.status = status;
        this.orcamento = orcamento;
        this.servicos = servicos;
        this.itensQuantidade = criarItens(pecasInsumos, itensQuantidade);
        this.diagnostico = diagnostico;
        this.veiculo = veiculo;
        this.cliente = cliente;
        this.funcionario = funcionario;
    }

    public OrdemServico(StatusOS status, Veiculo veiculo, Cliente cliente, Funcionario funcionario) {
        this.status = status;
        this.veiculo = veiculo;
        this.cliente = cliente;
        this.funcionario = funcionario;
    }


    public String formatDate() {

        if (this.getCreatedAt() != null) {
            return this.getCreatedAt().atOffset(ZoneOffset.of("-03:00"))
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm"));
        }
        return null;
    }

    public void incluirPecaInsumos(List<PecaInsumo> pecasInsumos, List<ItemQuantidade> itens) {
        exigirOrcamentoAberto();
        Map<UUID, PecaInsumo> pecasPorId = pecasInsumos.stream()
                .collect(Collectors.toMap(PecaInsumo::getId, Function.identity()));
        // valida tudo antes de alterar a OS, para não deixar a inclusão pela metade
        for (ItemQuantidade item : itens) {
            exigirQuantidadePositiva(item.quantidade());
            if (!pecasPorId.containsKey(item.itens())) {
                throw new PecaInsumoNaoEncontradoException(item.itens());
            }
        }

        for (ItemQuantidade itemQuantidade : itens) {
            PecaInsumo peca = pecasPorId.get(itemQuantidade.itens());
            // a reserva confere se o item está ativo, se há saldo disponível e se a unidade aceita a quantidade
            peca.reservar(itemQuantidade.quantidade());
            OrdemServicoItem itemExistente = this.itensQuantidade.stream()
                    .filter(item -> item.getItem().getId().equals(itemQuantidade.itens()))
                    .findFirst()
                    .orElse(null);
            if (itemExistente == null) {
                this.itensQuantidade.add(new OrdemServicoItem(peca, itemQuantidade.quantidade()));
            } else {
                itemExistente.adicionarQuantidade(itemQuantidade.quantidade());
            }
        }
        this.realizaCalculoItensInsumosOrcamento(pecasInsumos, itens);
    }

    public void incluirServicos(List<Servico> servicos) {
        exigirOrcamentoAberto();
        this.servicos.addAll(servicos);
        this.realizaCalculoServicos(servicos);
    }

    /** Orçamento recusado: as peças reservadas para a OS voltam a ficar disponíveis no estoque. */
    public void liberarReservas() {
        this.itensQuantidade.forEach(item -> item.getItem().liberar(item.getQuantidade()));
    }

    /** Serviço executado: as peças reservadas para a OS saem do estoque. */
    public void consumirReservas() {
        this.itensQuantidade.forEach(item -> item.getItem().consumir(item.getQuantidade()));
    }

    /** Depois que o orçamento é enviado ao cliente, serviços e peças não mudam mais o valor aprovado. */
    private void exigirOrcamentoAberto() {
        if (!STATUS_COM_ORCAMENTO_ABERTO.contains(this.status)) {
            throw new OrcamentoFechadoException(getId(), this.status);
        }
    }

    private static void exigirQuantidadePositiva(BigDecimal quantidade) {
        if (quantidade == null || quantidade.signum() <= 0) {
            throw new QuantidadeInvalidaException("a quantidade deve ser maior que zero");
        }
    }

    private void realizaCalculoItensInsumosOrcamento(List<PecaInsumo> pecasInsumos, List<ItemQuantidade> itens) {

        Map<UUID, PecaInsumo> pecasPorId = this.itensQuantidade.stream()
                .map(OrdemServicoItem::getItem)
                .collect(Collectors.toMap(PecaInsumo::getId,
                        Function.identity(),
                        (existente, substituto) -> existente));

        BigDecimal valorTotalPecasInsumos = itens.stream()
                .map(item -> {
                    return pecasPorId.get(item.itens()).getPrecoUnitario().multiply(item.quantidade());
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add).add(this.getOrcamento() == null ? BigDecimal.ZERO : this.getOrcamento());

        this.orcamento = valorTotalPecasInsumos;
    }

    public List<PecaInsumo> getItens() {
        return itensQuantidade.stream().map(OrdemServicoItem::getItem).toList();
    }

    private static List<OrdemServicoItem> criarItens(List<PecaInsumo> pecasInsumos,
            List<ItemQuantidade> itensQuantidade) {
        Map<UUID, PecaInsumo> pecasPorId = pecasInsumos.stream()
                .collect(Collectors.toMap(PecaInsumo::getId, Function.identity()));

        Map<UUID, OrdemServicoItem> itensPorId = new LinkedHashMap<>();

        for (ItemQuantidade itemQuantidade : itensQuantidade) {

            OrdemServicoItem itemExistente = itensPorId.get(itemQuantidade.itens());

            if (itemExistente == null) {
                itensPorId.put(itemQuantidade.itens(),
                        new OrdemServicoItem(pecasPorId.get(itemQuantidade.itens()), itemQuantidade.quantidade()));
            } else {
                itemExistente.adicionarQuantidade(itemQuantidade.quantidade());
            }
        }
        return new ArrayList<>(itensPorId.values());
    }

    private void realizaCalculoServicos(List<Servico> servicos) {

        var valorTotalServicos = servicos.stream()
                .map(Servico::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add).add(this.getOrcamento() == null ? BigDecimal.ZERO : this.getOrcamento());

        this.orcamento = valorTotalServicos;
    }

    public void incluirDiagnostico() {

       var servicos = this.getServicos().stream()
                            .map(f -> f.getDescricao())
                            .collect(Collectors.joining("\n"));

        var itens = this.getItens().stream()
                            .map(f -> f.getDescricao())
                            .collect(Collectors.joining("\n"));

       this.diagnostico = String.valueOf(servicos).concat("\n").concat(itens);
    }

    public OrdemServico atualizaStatus(StatusOS status){
        this.status = status;

        return this;
    }
}