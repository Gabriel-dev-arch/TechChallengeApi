package com.techchallenge.oficina.funcionario.entidades;

import com.techchallenge.oficina.shared.persistence.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Entity
@Table(name = "funcionarios", schema = "public")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Funcionario extends BaseEntity {

    @Column(name = "nome", nullable = false, length = 255)
    private String nome;

    @Column(name = "email", length = 50)
    private String email;

    @Column(name = "senha_hash", nullable = false)
    private Short senhaHash;

    @Column(name = "admin", nullable = false)
    private Boolean admin;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo;

    public Funcionario(String nome, String email, Short senhaHash, Boolean admin, Boolean ativo) {
        this.nome = nome;
        this.email = email;
        this.senhaHash = senhaHash;
        this.admin = admin;
        this.ativo = ativo;
    }

}

