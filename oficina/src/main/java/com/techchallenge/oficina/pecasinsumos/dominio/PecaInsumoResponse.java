package com.techchallenge.oficina.pecasinsumos.dominio;

import com.techchallenge.oficina.pecasinsumos.entidades.PecaInsumo;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;

public record PecaInsumoResponse(
        UUID id,
        TipoItem tipo,
        String codigo,
        String nome,
        String descricao,
        UnidadeMedida unidadeMedida,
        BigDecimal precoUnitario,
        BigDecimal quantidadeTotal,
        BigDecimal quantidadeReservada,
        BigDecimal quantidadeDisponivel,
        boolean ativo,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

    public static PecaInsumoResponse from(PecaInsumo item) {
        return new PecaInsumoResponse(item.getId(), item.getTipo(), item.getCodigo(),
                                      item.getNome(), item.getDescricao(), item.getUnidadeMedida(),
                                      item.getPrecoUnitario(), item.getQuantidadeTotal(), item.getQuantidadeReservada(),
                                      item.getQuantidadeDisponivel(), item.isAtivo(), paraFusoLocal(item.getCreatedAt()),
                                      paraFusoLocal(item.getUpdatedAt()));
    }

    /** Converte o instante (UTC) para o horário de Brasília; null enquanto a entidade não foi persistida. */
    private static OffsetDateTime paraFusoLocal(Instant instante) {
        return instante == null ? null : instante.atZone(FUSO).toOffsetDateTime();
    }
}
