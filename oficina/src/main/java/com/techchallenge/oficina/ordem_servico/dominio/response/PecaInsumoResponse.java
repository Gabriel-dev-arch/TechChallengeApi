package com.techchallenge.oficina.ordem_servico.dominio.response;

import com.techchallenge.oficina.pecasinsumos.dominio.TipoItem;
import com.techchallenge.oficina.pecasinsumos.dominio.UnidadeMedida;
import com.techchallenge.oficina.pecasinsumos.entidades.PecaInsumo;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

public record PecaInsumoResponse(
        TipoItem tipo,
        String codigo,
        String nome,
        String descricao,
        UnidadeMedida unidadeMedida,
        BigDecimal precoUnitario) {

    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

    public static PecaInsumoResponse from(PecaInsumo item) {
        return new PecaInsumoResponse(item.getTipo(), item.getCodigo(),
                                      item.getNome(), item.getDescricao(), item.getUnidadeMedida(),
                                      item.getPrecoUnitario());
    }

    public static List<PecaInsumoResponse> from(List<PecaInsumo> itens) {

        return itens.stream().map(PecaInsumoResponse::from).collect(Collectors.toList());
    }

    /** Converte o instante (UTC) para o horário de Brasília; null enquanto a entidade não foi persistida. */
    private static OffsetDateTime paraFusoLocal(Instant instante) {
        return instante == null ? null : instante.atZone(FUSO).toOffsetDateTime();
    }
}
