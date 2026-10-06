package com.techchallenge.oficina.pecasinsumos.dominio;

import com.techchallenge.oficina.pecasinsumos.entidades.PecaInsumo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface PecaInsumoRepository extends JpaRepository<PecaInsumo, UUID> {

    boolean existsByCodigo(String codigo);

    boolean existsByCodigoAndIdNot(String codigo, UUID id);

    @Query("""
        SELECT p FROM PecaInsumo p
        WHERE p.ativo = true
          AND (:tipo IS NULL OR p.tipo = :tipo)
          AND LOWER(p.nome) LIKE LOWER(CONCAT('%', :nome, '%'))
        """)
    Page<PecaInsumo> listarAtivos(@Param("tipo") TipoItem tipo,
                                  @Param("nome") String nome,
                                  Pageable pageable);
}
