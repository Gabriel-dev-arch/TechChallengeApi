package com.techchallenge.oficina.servicos.consultar;

import com.techchallenge.oficina.servicos.dominio.ServicoNaoEncontradoException;
import com.techchallenge.oficina.servicos.dominio.ServicoRepository;
import com.techchallenge.oficina.servicos.dominio.ServicoResponse;
import com.techchallenge.oficina.servicos.entidades.Servico;
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
class ConsultarServicoServiceTest {

    private static final UUID ID = UUID.fromString("33333333-4444-5555-6666-777777888888");

    @Mock
    private ServicoRepository repository;

    @InjectMocks
    private ConsultarServicoService service;

    @Test
    void deveListarServicoPorId() {
        Servico servico = new Servico("Revisão geral", new BigDecimal("250.00"));
        setId(servico, ID);

        when(repository.findById(ID)).thenReturn(Optional.of(servico));

        ServicoResponse response = service.listar(ID);

        assertThat(response.id()).isEqualTo(ID);
        assertThat(response.descricao()).isEqualTo("Revisão geral");
        assertThat(response.valor()).isEqualByComparingTo(new BigDecimal("250.00"));
    }

    @Test
    void deveLancarExcecaoQuandoServicoNaoExiste() {
        when(repository.findById(ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.listar(ID))
                .isInstanceOf(ServicoNaoEncontradoException.class)
                .hasMessage("Servico não encontrado: " + ID);
    }

    @Test
    void deveListarTodosOsServicos() {
        Servico servico1 = new Servico("Troca de óleo", new BigDecimal("180.00"));
        Servico servico2 = new Servico("Alinhamento", new BigDecimal("120.00"));
        when(repository.findAll()).thenReturn(List.of(servico1, servico2));

        List<ServicoResponse> response = service.listarTodos();

        assertThat(response).hasSize(2);
        assertThat(response).extracting(ServicoResponse::descricao)
                .containsExactlyInAnyOrder("Troca de óleo", "Alinhamento");
    }

    @Test
    void deveBuscarPorDescricao() {
        Servico servico1 = new Servico("Revisão geral", new BigDecimal("250.00"));
        Servico servico2 = new Servico("Revisão leve", new BigDecimal("120.00"));


        when(repository.findByDescricaoContaining("revisão")).thenReturn(List.of(servico1, servico2));

        List<Servico> response = service.buscarPorDescricao("revisão");

        assertThat(response).hasSize(2);
        assertThat(response).extracting(Servico::getDescricao)
                .containsExactlyInAnyOrder("Revisão geral", "Revisão leve");
    }

    private void setId(Servico servico, UUID id) {
        try {
            var field = com.techchallenge.oficina.shared.persistence.BaseEntity.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(servico, id);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Não foi possível configurar o id do serviço", e);
        }
    }
}
