package com.techchallenge.oficina.servicos.cadastrar;

import com.techchallenge.oficina.servicos.dominio.ServicoRepository;
import com.techchallenge.oficina.servicos.dominio.ServicoResponse;
import com.techchallenge.oficina.servicos.entidades.Servico;
import com.techchallenge.oficina.shared.persistence.BaseEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CadastrarServicoServiceTest {

    @Mock
    private ServicoRepository repository;

    @InjectMocks
    private CadastrarServicoService service;

    @Test
    void deveCadastrarServicoComDadosValidos() {
        CadastrarServicoRequest request = new CadastrarServicoRequest("Troca de óleo", new BigDecimal("180.00"));

        when(repository.saveAndFlush(any(Servico.class))).thenAnswer(invocation -> {
            Servico servico = invocation.getArgument(0);
            setId(servico, UUID.randomUUID());
            return servico;
        });

        ServicoResponse response = service.cadastrar(request);

        assertThat(response.descricao()).isEqualTo("Troca de óleo");
        assertThat(response.valor()).isEqualByComparingTo(new BigDecimal("180.00"));
        assertThat(response.id()).isNotNull();
        verify(repository).saveAndFlush(any(Servico.class));
    }

    private void setId(Servico servico, UUID id) {
        try {
            Field field = BaseEntity.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(servico, id);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Não foi possível configurar o id do serviço", e);
        }
    }
}
