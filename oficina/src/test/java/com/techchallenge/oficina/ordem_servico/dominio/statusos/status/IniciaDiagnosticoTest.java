package com.techchallenge.oficina.ordem_servico.dominio.statusos.status;

import com.techchallenge.oficina.ordem_servico.dominio.OrdemServicoRepository;
import com.techchallenge.oficina.ordem_servico.entidades.OrdemServico;
import com.techchallenge.oficina.ordem_servico.entidades.StatusOS;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IniciaDiagnosticoTest {

    @Mock
    private OrdemServicoRepository repository;

    @Test
    void deveSalvarOrdemAoIniciarDiagnostico() {
        OrdemServico os = new OrdemServico(StatusOS.RECEBIDA, BigDecimal.ZERO,
                List.of(), List.of(), List.of(), "Diagnostico", null, null, null);
        when(repository.save(any(OrdemServico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        new IniciaDiagnostico(os, repository);

        assertThat(os.getStatus()).isEqualTo(StatusOS.EM_DIAGNOSTICO);
        verify(repository).save(os);
    }
}
