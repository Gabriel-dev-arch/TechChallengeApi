package com.techchallenge.oficina.ordem_servico.consultar;

import com.techchallenge.oficina.clientes.dominio.ClienteNaoEncontradoException;
import com.techchallenge.oficina.clientes.dominio.ClienteRepository;
import com.techchallenge.oficina.clientes.dominio.ClienteResponse;
import com.techchallenge.oficina.clientes.dominio.CpfCnpjValidator;
import com.techchallenge.oficina.ordem_servico.dominio.OrdemServicoException;
import com.techchallenge.oficina.ordem_servico.dominio.OrdemServicoRepository;
import com.techchallenge.oficina.ordem_servico.dominio.OrdemServicoResponse;
import com.techchallenge.oficina.ordem_servico.dominio.VeiculoNaoEncontradoException;
import com.techchallenge.oficina.veiculos.dominio.VeiculoRepository;
import com.techchallenge.oficina.veiculos.dominio.VeiculoResponse;
import com.techchallenge.oficina.veiculos.entidades.Veiculo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ConsultarOSService {

	private final OrdemServicoRepository repository;
	private final VeiculoRepository veiculoRepository;

	public ConsultarOSService(OrdemServicoRepository repository, VeiculoRepository veiculoRepository) {
		this.repository = repository;
        this.veiculoRepository = veiculoRepository;
    }

	public PageImpl<OrdemServicoResponse> listar(UUID uid, Pageable pageable) {
			List<OrdemServicoResponse> encontrado = repository.findById(uid)
					.map(OrdemServicoResponse::from).stream().toList();
			return new PageImpl<>(encontrado, pageable, encontrado.size());
	}

	public OrdemServicoResponse buscar(String placa) {

		 return repository.buscarPorPlaca(placa).stream().findFirst().map(OrdemServicoResponse::from)
				 .orElseThrow(() -> new OrdemServicoException(placa));
	}
}
