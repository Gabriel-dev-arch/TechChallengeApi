package com.techchallenge.oficina.ordem_servico.consultar;

import com.techchallenge.oficina.clientes.entidades.Cliente;
import com.techchallenge.oficina.ordem_servico.dominio.OrdemServicoRepository;
import com.techchallenge.oficina.ordem_servico.dominio.exception.OrdemServicoNaoEncontradaException;
import com.techchallenge.oficina.ordem_servico.dominio.response.OrdemServicoResponse;
import com.techchallenge.oficina.ordem_servico.entidades.OrdemServico;
import com.techchallenge.oficina.ordem_servico.entidades.StatusOS;
import com.techchallenge.oficina.pecasinsumos.entidades.PecaInsumo;
import com.techchallenge.oficina.servicos.entidades.Servico;
import com.techchallenge.oficina.veiculos.entidades.Veiculo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultarOSServiceTest {

    private static final UUID ID = UUID.fromString("33333333-4444-5555-6666-777777888888");
    private static final String PLACA = "ABC1234";

    @Mock
    private OrdemServicoRepository repository;

    @InjectMocks
    private ConsultarOSService service;

    @Test
    void deveListarOrdemDeServicoPorId() {
        OrdemServico ordem = criarOrdem();
        when(repository.findById(ID)).thenReturn(Optional.of(ordem));

        List<OrdemServicoResponse> responses = service.listar(ID);

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().diagnostico()).isEqualTo("Ruído no motor");
        assertThat(responses.getFirst().orcamento().total()).isEqualByComparingTo("250.00");
        assertThat(responses.getFirst().cliente().fullName()).isEqualTo("Ana Silva");
        assertThat(responses.getFirst().veiculo().placa()).isEqualTo(PLACA);
    }

    @Test
    void deveRetornarListaVaziaQuandoIdNaoForEncontrado() {
        when(repository.findById(ID)).thenReturn(Optional.empty());

        assertThat(service.listar(ID)).isEmpty();
    }

    @Test
    void deveBuscarOrdemDeServicoPorPlaca() {
        when(repository.buscarPorPlaca(PLACA)).thenReturn(List.of(criarOrdem()));

        OrdemServicoResponse response = service.buscar(PLACA);

        assertThat(response.diagnostico()).isEqualTo("Ruído no motor");
        assertThat(response.veiculo().placa()).isEqualTo(PLACA);
    }

    @Test
    void deveLancarExcecaoQuandoOrdemNaoExisteParaPlaca() {
        when(repository.buscarPorPlaca(PLACA)).thenReturn(List.of());

        assertThatThrownBy(() -> service.buscar(PLACA))
                .isInstanceOf(OrdemServicoNaoEncontradaException.class)
                .hasMessage("Ordem de serviço não encontrado pela placa: " + PLACA);
    }

    private OrdemServico criarOrdem() {
        Cliente cliente = new Cliente("Ana", "Silva", "ana@email.com", "12345678901", "11999999999");
        Veiculo veiculo = new Veiculo(PLACA, "Fiat", "Uno", 2020, cliente);
        Servico servico = new Servico("Revisão", new BigDecimal("250.00"));
        PecaInsumo peca = new PecaInsumo(
                com.techchallenge.oficina.pecasinsumos.dominio.TipoItem.PECA,
                "OLEO", "Óleo", "Óleo do motor",
                com.techchallenge.oficina.pecasinsumos.dominio.UnidadeMedida.UN,
                new BigDecimal("20.00"), new BigDecimal("10"));
        return new OrdemServico(StatusOS.RECEBIDA, new BigDecimal("250.00"),
                List.of(servico), List.of(peca), "Ruído no motor", veiculo, cliente);
    }
}
