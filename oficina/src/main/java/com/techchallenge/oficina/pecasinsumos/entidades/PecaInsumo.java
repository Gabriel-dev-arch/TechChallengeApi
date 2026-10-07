package com.techchallenge.oficina.pecasinsumos.entidades;

import com.techchallenge.oficina.pecasinsumos.dominio.*;
import com.techchallenge.oficina.shared.persistence.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Objects;

@Getter
@Entity
@Table(name = "pecas_insumos")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PecaInsumo extends BaseEntity {

    @Enumerated(EnumType.STRING)
    private TipoItem tipo;

    private String codigo;
    private String nome;
    private String descricao;

    @Enumerated(EnumType.STRING)
    private UnidadeMedida unidadeMedida;

    private BigDecimal precoUnitario;
    private BigDecimal quantidadeTotal;
    private BigDecimal quantidadeReservada;
    private boolean ativo;

    public PecaInsumo(TipoItem tipo, String codigo, String nome, String descricao, UnidadeMedida unidadeMedida, BigDecimal precoUnitario, BigDecimal quantidadeInicial) {
        this.tipo = Objects.requireNonNull(tipo, "Tipo não pode ser nulo");
        this.unidadeMedida = Objects.requireNonNull(unidadeMedida, "Unidade de Medida não pode ser nula");
        atualizarDados(codigo, nome, descricao, precoUnitario);

        BigDecimal inicial = quantidadeInicial;
        if (inicial == null) {
            inicial = BigDecimal.ZERO;
        }
        if (inicial.signum() < 0) {
            throw new QuantidadeInvalidaException("quantidade inicial não pode ser negativa");
        }
        validarUnidade(inicial);

        this.quantidadeTotal = inicial;
        this.quantidadeReservada = BigDecimal.ZERO;
        this.ativo = true;
    }

    public void atualizarDados(String codigo, String nome, String descricao, BigDecimal precoUnitario) {
        if(precoUnitario == null){
            throw new PrecoInvalidoException("Preço não pode ser nulo");
        }
        if(precoUnitario.signum() < 0){
            throw new PrecoInvalidoException("Preço não pode ser menor que zero");
        }
        this.codigo = normalizarCodigo(codigo);
        this.nome = nome.strip();
        if (descricao == null || descricao.isBlank()) {
            this.descricao = null;
        } else{
            this.descricao = descricao.strip();
        }
        this.precoUnitario = precoUnitario;
    }

    public static String normalizarCodigo(String codigo){
        return codigo.strip().toUpperCase(Locale.ROOT);
    }

    public BigDecimal getQuantidadeDisponivel(){
        return quantidadeTotal.subtract(quantidadeReservada);
    }

    public void registrarEntrada(BigDecimal quantidade){
        exigirAtivo();
        validarQuantidade(quantidade);

        this.quantidadeTotal = quantidadeTotal.add(quantidade);
    }

    public void registrarSaida(BigDecimal quantidade){
        exigirAtivo();
        validarQuantidade(quantidade);

        if(getQuantidadeDisponivel().compareTo(quantidade) < 0){
            throw new EstoqueInsuficienteException(getId(), quantidade, this.getQuantidadeDisponivel());
        }

        this.quantidadeTotal = quantidadeTotal.subtract(quantidade);
    }

    public void reservar(BigDecimal quantidade){
        exigirAtivo();
        validarQuantidade(quantidade);

        if(getQuantidadeDisponivel().compareTo(quantidade) < 0){
            throw new EstoqueInsuficienteException(getId(), quantidade, this.getQuantidadeDisponivel());
        }

        this.quantidadeReservada = quantidadeReservada.add(quantidade);
    }

    public void liberar(BigDecimal quantidade){
        validarQuantidade(quantidade);

        exigirReservado(quantidade);

        this.quantidadeReservada = quantidadeReservada.subtract(quantidade);
    }

    public void consumir(BigDecimal quantidade){
        liberar(quantidade);
        this.quantidadeTotal = this.quantidadeTotal.subtract(quantidade);
    }

    public void desativar(){
        exigirAtivo();

        if (this.quantidadeReservada.signum() > 0) {
            throw new PecaInsumoComReservaException(getId());
        }

        this.ativo = false;
    }

    private void validarUnidade(BigDecimal quantidade) {
        if(!unidadeMedida.aceita(quantidade)){
            throw new QuantidadeInvalidaException("A unidade: " + unidadeMedida + " não aceita ser fracionada");
        }
    }

    private void validarQuantidade(BigDecimal quantidade) {
        if (quantidade == null) {
            throw new QuantidadeInvalidaException("A quantidade não pode ser nula");
        }
        if (quantidade.signum() <= 0){
            throw new QuantidadeInvalidaException("A quantidade não pode ser menor ou igual a zero");
        }
        validarUnidade(quantidade);
    }

    private void exigirAtivo(){
        if (!ativo){
            throw new PecaInsumoInativoException(getId());
        }
    }

    private void exigirReservado(BigDecimal quantidade){
        if(quantidadeReservada.compareTo(quantidade) < 0){
            throw new ReservaInsuficienteException(getId(), quantidade, quantidadeReservada);
        }
    }
}
