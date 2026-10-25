package com.techchallenge.oficina.ordem_servico.consultar;

import com.techchallenge.oficina.ordem_servico.dominio.OrdemServicoRepository;
import com.techchallenge.oficina.ordem_servico.dominio.exception.OrdemServicoNaoEncontradaException;
import com.techchallenge.oficina.ordem_servico.dominio.exception.VeiculoNaoEncontradoException;
import com.techchallenge.oficina.ordem_servico.dominio.response.OrdemServicoResponse;
import com.techchallenge.oficina.ordem_servico.entidades.OrdemServico;
import com.techchallenge.oficina.ordem_servico.entidades.StatusOS;
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

	public OrdemServicoResponse consultarStatusOS(UUID id) {

		return repository.findById(id)
				.map(f -> OrdemServicoResponse.from(f, f.getStatus()))
				.orElseThrow(() -> new OrdemServicoNaoEncontradaException(id));
		
	}

	public PageImpl<OrdemServicoResponse> listarOrdemServicoEmAberto(StatusOS statusOs, Pageable pageable) {
		List<OrdemServicoResponse> encontrado = repository.buscarPorStatus(statusOs, pageable)
				.stream()
				.map(OrdemServicoResponse::from).toList();
		return new PageImpl<>(encontrado, pageable, encontrado.size());
	}

	public OrdemServicoResponse buscarOSPeloID(UUID id) {

		return repository.findById(id).stream().findFirst().map(OrdemServicoResponse::from)
				.orElseThrow(() -> new OrdemServicoNaoEncontradaException(id));
	}
}
