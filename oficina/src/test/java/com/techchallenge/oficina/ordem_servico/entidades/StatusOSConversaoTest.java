package com.techchallenge.oficina.ordem_servico.entidades;

import com.techchallenge.oficina.ordem_servico.dominio.exception.StatusInvalidoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StatusOSConversaoTest {

    @ParameterizedTest
    @ValueSource(strings = {"AGUARDANDO_APROVACAO", "aguardando_aprovacao", "  Aguardando_Aprovacao "})
    @DisplayName("Converte o filtro de status sem diferenciar maiúsculas")
    void deveConverterSemDiferenciarMaiusculas(String valor) {
        assertThat(StatusOS.de(valor)).isEqualTo(StatusOS.AGUARDANDO_APROVACAO);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"XYZ", "Recebid"})
    @DisplayName("Status desconhecido lança StatusInvalidoException")
    void deveRejeitarStatusDesconhecido(String valor) {
        assertThatThrownBy(() -> StatusOS.de(valor))
                .isInstanceOf(StatusInvalidoException.class);
    }

    @Test
    @DisplayName("A mensagem lista os status aceitos")
    void mensagemListaValoresAceitos() {
        assertThatThrownBy(() -> StatusOS.de("XYZ"))
                .hasMessageContaining("'XYZ'")
                .hasMessageContaining("RECEBIDA, EM_DIAGNOSTICO");
    }
}
