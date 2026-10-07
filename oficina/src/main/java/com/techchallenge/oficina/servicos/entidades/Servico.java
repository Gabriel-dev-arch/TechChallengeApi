package com.techchallenge.oficina.servicos.entidades;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.techchallenge.oficina.clientes.entidades.Cliente;
import com.techchallenge.oficina.shared.persistence.BaseEntity;
import com.techchallenge.oficina.veiculos.entidades.Veiculo;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Entity
@Table(name = "servicos")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Servico extends BaseEntity {
	
	
	@OneToOne
	@JoinColumn(name = "veiculo_id")
	private Veiculo veiculo;
	
	@OneToOne
	@JoinColumn(name = "cliente_id")
	private Cliente cliente;
	//private Funcionario funcionario;
	@Enumerated(EnumType.STRING)
	private StatusServico status;
	private String diagnostico;
	private BigDecimal orcamentoTotal;
	private Instant orcamentoDecididoEm;
	private Instant delivered_at;
	private UUID codigoAcompanhamento;
	
	//codigo_acompanhamento · UQ
	//cliente_id · FK
	//veiculo_id · FK
	//funcionario_id · FK
	//status · enum (6 estados)
	//diagnostico · text
	//itens · jsonb
	//orcamento_total · numeric(12,2)

	


}
