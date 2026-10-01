package com.techchallenge.oficina.clientes;

import com.techchallenge.oficina.clientes.atualizar.AtualizarClienteRequest;
import com.techchallenge.oficina.clientes.atualizar.AtualizarClienteService;
import com.techchallenge.oficina.clientes.cadastrar.CadastrarClienteRequest;
import com.techchallenge.oficina.clientes.cadastrar.CadastrarClienteService;
import com.techchallenge.oficina.clientes.consultar.ConsultarClientesService;
import com.techchallenge.oficina.clientes.dominio.ClienteNaoEncontradoException;
import com.techchallenge.oficina.clientes.dominio.ClienteRepository;
import com.techchallenge.oficina.clientes.dominio.ClienteResponse;
import com.techchallenge.oficina.clientes.dominio.DocumentoJaCadastradoException;
import com.techchallenge.oficina.clientes.entidades.Cliente;
import com.techchallenge.oficina.clientes.remover.RemoverClienteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteServicesTest {

	private static final String CPF = "52998224725";

	@Mock
	ClienteRepository repository;

	@InjectMocks
	CadastrarClienteService cadastrar;

	@InjectMocks
	AtualizarClienteService atualizar;

	@InjectMocks
	ConsultarClientesService consultar;

	@InjectMocks
	RemoverClienteService remover;

	@Test
	void cadastraNormalizandoDocumentoEMontandoFullName() {
		when(repository.existsByDocumento(CPF)).thenReturn(false);
		when(repository.saveAndFlush(any(Cliente.class))).thenAnswer(i -> i.getArgument(0));

		ClienteResponse resposta = cadastrar.cadastrar(
				new CadastrarClienteRequest(" Maria ", "Silva", "maria@email.com", "529.982.247-25", "11999990000"));

		assertThat(resposta.documento()).isEqualTo(CPF);
		assertThat(resposta.tipoDocumento()).isEqualTo("CPF");
		assertThat(resposta.fullName()).isEqualTo("Maria Silva");
	}

	@Test
	void cadastroComDocumentoDuplicadoLancaExcecaoESemSalvar() {
		when(repository.existsByDocumento(CPF)).thenReturn(true);

		assertThatThrownBy(() -> cadastrar.cadastrar(
				new CadastrarClienteRequest("Maria", "Silva", "maria@email.com", CPF, null)))
				.isInstanceOf(DocumentoJaCadastradoException.class);
		verify(repository, never()).saveAndFlush(any());
	}

	@Test
	void atualizaClienteExistente() {
		UUID id = UUID.randomUUID();
		Cliente existente = new Cliente("Maria", "Silva", "maria@email.com", CPF, null);
		when(repository.findById(id)).thenReturn(Optional.of(existente));
		when(repository.existsByDocumentoAndIdNot("11222333000181", id)).thenReturn(false);
		when(repository.saveAndFlush(existente)).thenReturn(existente);

		ClienteResponse resposta = atualizar.atualizar(id,
				new AtualizarClienteRequest("Empresa", "LTDA", "contato@empresa.com", "11.222.333/0001-81", "1133334444"));

		assertThat(resposta.fullName()).isEqualTo("Empresa LTDA");
		assertThat(resposta.tipoDocumento()).isEqualTo("CNPJ");
		assertThat(resposta.telefone()).isEqualTo("1133334444");
	}

	@Test
	void atualizarInexistenteLanca404() {
		UUID id = UUID.randomUUID();
		when(repository.findById(id)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> atualizar.atualizar(id,
				new AtualizarClienteRequest("A", "B", "a@b.com", CPF, null)))
				.isInstanceOf(ClienteNaoEncontradoException.class);
	}

	@Test
	void atualizarComDocumentoDeOutroClienteLancaConflito() {
		UUID id = UUID.randomUUID();
		when(repository.findById(id)).thenReturn(Optional.of(new Cliente("A", "B", "a@b.com", "11144477735", null)));
		when(repository.existsByDocumentoAndIdNot(CPF, id)).thenReturn(true);

		assertThatThrownBy(() -> atualizar.atualizar(id,
				new AtualizarClienteRequest("A", "B", "a@b.com", CPF, null)))
				.isInstanceOf(DocumentoJaCadastradoException.class);
	}

	@Test
	void buscaPorIdInexistenteLanca404() {
		UUID id = UUID.randomUUID();
		when(repository.findById(id)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> consultar.buscar(id)).isInstanceOf(ClienteNaoEncontradoException.class);
	}

	@Test
	void listaFiltrandoPorDocumentoComMascara() {
		when(repository.findByDocumento(CPF))
				.thenReturn(Optional.of(new Cliente("Maria", "Silva", "maria@email.com", CPF, null)));

		var pagina = consultar.listar("529.982.247-25", PageRequest.of(0, 20));

		assertThat(pagina.getContent()).hasSize(1);
		assertThat(pagina.getContent().get(0).documento()).isEqualTo(CPF);
	}

	@Test
	void removeClienteExistente() {
		UUID id = UUID.randomUUID();
		when(repository.existsById(id)).thenReturn(true);

		remover.remover(id);

		verify(repository).deleteById(id);
	}

	@Test
	void removerInexistenteLanca404() {
		UUID id = UUID.randomUUID();
		when(repository.existsById(id)).thenReturn(false);

		assertThatThrownBy(() -> remover.remover(id)).isInstanceOf(ClienteNaoEncontradoException.class);
		verify(repository, never()).deleteById(any());
	}
}
