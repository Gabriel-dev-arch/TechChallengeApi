package com.techchallenge.oficina.veiculos.dominio;

import com.techchallenge.oficina.veiculos.entidades.Veiculo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VeiculoRepository extends JpaRepository<Veiculo, UUID> {

	boolean existsByPlaca(String placa);

	boolean existsByPlacaAndIdNot(String placa, UUID id);

	Optional<Veiculo> findByPlaca(String placa);
}
