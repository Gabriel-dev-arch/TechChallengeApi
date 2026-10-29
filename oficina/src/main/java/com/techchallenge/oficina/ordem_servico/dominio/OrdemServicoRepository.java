package com.techchallenge.oficina.ordem_servico.dominio;

import com.techchallenge.oficina.ordem_servico.entidades.OrdemServico;
import com.techchallenge.oficina.ordem_servico.entidades.StatusOS;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface OrdemServicoRepository extends JpaRepository<OrdemServico, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT os FROM OrdemServico os WHERE os.id = :id")
    Optional<OrdemServico> buscarParaFinalizar(@Param("id") UUID id);

    List<OrdemServico> findByVeiculoPlaca(String placa);

    @Query("SELECT os FROM OrdemServico os WHERE os.veiculo.placa = :placa AND os.status NOT IN (" +
            "com.techchallenge.oficina.ordem_servico.entidades.StatusOS.ENTREGUE)")
    List<OrdemServico> buscarPorPlaca(@Param("placa") String placa);

    @Query("SELECT os FROM OrdemServico os WHERE os.veiculo.placa = :placa AND os.status NOT IN (" +
            "com.techchallenge.oficina.ordem_servico.entidades.StatusOS.ENTREGUE, " +
            "com.techchallenge.oficina.ordem_servico.entidades.StatusOS.FINALIZADA)")
    List<OrdemServico> buscarPorPlacaComOSEmAberto(@Param("placa") String placa);


    @Query("SELECT os FROM OrdemServico os WHERE os.status = :status")
    List<OrdemServico> buscarPorStatus(@Param("status") StatusOS status, Pageable pageable);
}
