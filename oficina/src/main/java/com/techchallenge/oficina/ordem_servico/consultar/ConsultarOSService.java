package com.techchallenge.oficina.ordem_servico.consultar;

import com.techchallenge.oficina.ordem_servico.dominio.OrdemServicoRepository;
import com.techchallenge.oficina.ordem_servico.dominio.exception.OrdemServicoNaoEncontradaException;
import com.techchallenge.oficina.ordem_servico.dominio.response.OrdemServicoResponse;
import com.techchallenge.oficina.veiculos.dominio.VeiculoRepository;
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

	public ConsultarOSService(OrdemServicoRepository repository) {
		this.repository = repository;
    }

	public List<OrdemServicoResponse> listar(UUID uid) {
			List<OrdemServicoResponse> encontrado = repository.findById(uid)
					.map(OrdemServicoResponse::from).stream().toList();
			return encontrado;
	}

	public OrdemServicoResponse buscar(String placa) {

		 return repository.buscarPorPlaca(placa).stream().findFirst().map(OrdemServicoResponse::from)
				 .orElseThrow(() -> new OrdemServicoNaoEncontradaException(placa));
	}
}
