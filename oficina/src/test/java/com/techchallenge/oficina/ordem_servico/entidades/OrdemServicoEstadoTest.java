package com.techchallenge.oficina.ordem_servico.entidades;

class OrdemServicoEstadoTest {

    /*@Test
    void deveAvancarPelosEstadosDaOrdemDeServico() {
        OrdemServico ordemServico = criarOrdemServico();

        for (StatusOS status : StatusOS.values()) {
            assertThat(ordemServico.getStatus()).isEqualTo(status);
            if (status != StatusOS.ENTREGUE) {
                ordemServico.avancarStatus();
            }
        }
    }

    @Test
    void naoDeveAvancarDepoisDeEntregue() {
        OrdemServico ordemServico = criarOrdemServico();
        for (int i = 0; i < StatusOS.values().length - 1; i++) {
            ordemServico.avancarStatus();
        }

        assertThatThrownBy(ordemServico::avancarStatus)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Uma ordem de serviço entregue não pode avançar de status.");
        assertThat(ordemServico.getStatus()).isEqualTo(StatusOS.ENTREGUE);
    }

    private OrdemServico criarOrdemServico() {
        return new OrdemServico(StatusOS.RECEBIDA, null, List.of(), List.of(), List.of(),
                null, null, null);
    }*/
}
