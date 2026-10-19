package com.techchallenge.oficina.veiculos.dominio;

import com.techchallenge.oficina.veiculos.entidades.Veiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VeiculoRepository extends JpaRepository<Veiculo, UUID> {

	boolean existsByPlaca(String placa);

	boolean existsByPlacaAndIdNot(String placa, UUID id);

	Optional<Veiculo> findByPlaca(String placa);
}
