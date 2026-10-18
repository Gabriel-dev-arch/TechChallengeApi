package com.techchallenge.oficina.veiculos.cadastrar;

import com.techchallenge.oficina.clientes.dominio.ClienteNaoEncontradoException;
import com.techchallenge.oficina.clientes.dominio.ClienteRepository;
import com.techchallenge.oficina.clientes.entidades.Cliente;
import com.techchallenge.oficina.veiculos.dominio.VeiculoRepository;
import com.techchallenge.oficina.veiculos.dominio.VeiculoResponse;
import com.techchallenge.oficina.veiculos.dominio.PlacaValidator;
import com.techchallenge.oficina.veiculos.dominio.PlacaJaCadastradaException;
import com.techchallenge.oficina.veiculos.entidades.Veiculo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CadastrarVeiculoService {

	private final VeiculoRepository repository;

	private final ClienteRepository clienteRepository;

	public CadastrarVeiculoService(VeiculoRepository repository, ClienteRepository clienteRepository) {
		this.repository = repository;
		this.clienteRepository = clienteRepository;
	}

	@Transactional
	public VeiculoResponse cadastrar(CadastrarVeiculoRequest request) {
		String placa = PlacaValidator.normalizar(request.placa());
		if (repository.existsByPlaca(placa)) {
			throw new PlacaJaCadastradaException(placa);
		}
		Cliente cliente = clienteRepository.findById(request.clienteId())
				.orElseThrow(() -> new ClienteNaoEncontradoException(request.clienteId()));
		Veiculo veiculo = new Veiculo(placa, request.marca().trim(),
				request.modelo().trim(), request.ano(), cliente);
		return VeiculoResponse.from(repository.saveAndFlush(veiculo));
	}
}
