package com.techchallenge.oficina.veiculos;

import com.techchallenge.oficina.veiculos.atualizar.AtualizarVeiculoRequest;
import com.techchallenge.oficina.veiculos.atualizar.AtualizarVeiculoService;
import com.techchallenge.oficina.veiculos.cadastrar.CadastrarVeiculoRequest;
import com.techchallenge.oficina.veiculos.cadastrar.CadastrarVeiculoService;
import com.techchallenge.oficina.veiculos.consultar.ConsultarVeiculosService;
import com.techchallenge.oficina.veiculos.dominio.PlacaJaCadastradaException;
import com.techchallenge.oficina.veiculos.dominio.VeiculoNaoEncontradoException;
import com.techchallenge.oficina.veiculos.dominio.VeiculoRepository;
import com.techchallenge.oficina.veiculos.entidades.Veiculo;
import com.techchallenge.oficina.veiculos.remover.RemoverVeiculoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VeiculoServicesTest {

	@Mock
	VeiculoRepository repository;

	@InjectMocks
	CadastrarVeiculoService cadastrar;

	@InjectMocks
	AtualizarVeiculoService atualizar;

	@InjectMocks
	ConsultarVeiculosService consultar;

	@InjectMocks
	RemoverVeiculoService remover;

	@Test
	void cadastraNormalizandoPlacaEMarcaEModelo() {
		when(repository.saveAndFlush(any(Veiculo.class))).thenAnswer(i -> i.getArgument(0));

		var resposta = cadastrar.cadastrar(new CadastrarVeiculoRequest(" abc-1234 ", " Fiat ", " Uno ", 2010));

		assertThat(resposta.placa()).isEqualTo("ABC1234");
		assertThat(resposta.marca()).isEqualTo("Fiat");
		assertThat(resposta.modelo()).isEqualTo("Uno");
		assertThat(resposta.ano()).isEqualTo(2010);
		verify(repository).existsByPlaca("ABC1234");
	}

	@Test
	void cadastroDuplicadoNaoSalva() {
		when(repository.existsByPlaca("ABC1234")).thenReturn(true);

		assertThatThrownBy(() -> cadastrar.cadastrar(new CadastrarVeiculoRequest("abc-1234", "Fiat", "Uno", 2010)))
				.isInstanceOf(PlacaJaCadastradaException.class);
		verify(repository, never()).saveAndFlush(any());
	}

	@Test
	void atualizaVeiculoExistente() {
		UUID id = UUID.randomUUID();
		Veiculo existente = new Veiculo("ABC1234", "Fiat", "Uno", 2010);
		when(repository.findById(id)).thenReturn(Optional.of(existente));
		when(repository.saveAndFlush(existente)).thenReturn(existente);

		var resposta = atualizar.atualizar(id, new AtualizarVeiculoRequest("abc1d23", " Ford ", " Ka ", 2020));

		assertThat(resposta.placa()).isEqualTo("ABC1D23");
		assertThat(resposta.marca()).isEqualTo("Ford");
		assertThat(resposta.modelo()).isEqualTo("Ka");
		assertThat(resposta.ano()).isEqualTo(2020);
		verify(repository).existsByPlacaAndIdNot("ABC1D23", id);
	}

	@Test
	void atualizarInexistenteLanca404() {
		UUID id = UUID.randomUUID();
		assertThatThrownBy(() -> atualizar.atualizar(id, new AtualizarVeiculoRequest("ABC1234", "Fiat", "Uno", 2010)))
				.isInstanceOf(VeiculoNaoEncontradoException.class);
		verify(repository, never()).saveAndFlush(any());
	}

	@Test
	void atualizarComPlacaDeOutroVeiculoNaoAlteraEntidade() {
		UUID id = UUID.randomUUID();
		Veiculo existente = new Veiculo("DEF5678", "Fiat", "Uno", 2010);
		when(repository.findById(id)).thenReturn(Optional.of(existente));
		when(repository.existsByPlacaAndIdNot("ABC1234", id)).thenReturn(true);

		assertThatThrownBy(() -> atualizar.atualizar(id, new AtualizarVeiculoRequest("abc-1234", "Ford", "Ka", 2020)))
				.isInstanceOf(PlacaJaCadastradaException.class);
		assertThat(existente.getPlaca()).isEqualTo("DEF5678");
		verify(repository, never()).saveAndFlush(any());
	}

	@Test
	void buscaPorId() {
		UUID id = UUID.randomUUID();
		when(repository.findById(id)).thenReturn(Optional.of(new Veiculo("ABC1234", "Fiat", "Uno", 2010)));
		assertThat(consultar.buscar(id).placa()).isEqualTo("ABC1234");
	}

	@Test
	void buscaInexistenteLanca404() {
		assertThatThrownBy(() -> consultar.buscar(UUID.randomUUID())).isInstanceOf(VeiculoNaoEncontradoException.class);
	}

	@Test
	void listaFiltrandoPorPlacaNormalizada() {
		when(repository.findByPlaca("ABC1234")).thenReturn(Optional.of(new Veiculo("ABC1234", "Fiat", "Uno", 2010)));
		var pagina = consultar.listar("abc-1234", PageRequest.of(0, 20));
		assertThat(pagina.getContent()).hasSize(1);
		assertThat(pagina.getContent().get(0).placa()).isEqualTo("ABC1234");
	}

	@Test
	void listaSemFiltro() {
		var pageable = PageRequest.of(0, 20);
		when(repository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(new Veiculo("ABC1234", "Fiat", "Uno", 2010))));
		assertThat(consultar.listar(null, pageable).getContent()).hasSize(1);
	}

	@Test
	void removeVeiculoExistente() {
		UUID id = UUID.randomUUID();
		when(repository.existsById(id)).thenReturn(true);
		remover.remover(id);
		verify(repository).deleteById(id);
		verify(repository).flush();
	}

	@Test
	void removerInexistenteLanca404() {
		assertThatThrownBy(() -> remover.remover(UUID.randomUUID())).isInstanceOf(VeiculoNaoEncontradoException.class);
		verify(repository, never()).deleteById(any());
	}
}
