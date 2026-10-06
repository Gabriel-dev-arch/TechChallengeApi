package com.techchallenge.oficina.pecasinsumos;

import com.techchallenge.oficina.pecasinsumos.dominio.EstoqueInsuficienteException;
import com.techchallenge.oficina.pecasinsumos.dominio.PecaInsumoComReservaException;
import com.techchallenge.oficina.pecasinsumos.dominio.PecaInsumoInativoException;
import com.techchallenge.oficina.pecasinsumos.dominio.PrecoInvalidoException;
import com.techchallenge.oficina.pecasinsumos.dominio.QuantidadeInvalidaException;
import com.techchallenge.oficina.pecasinsumos.dominio.ReservaInsuficienteException;
import com.techchallenge.oficina.pecasinsumos.dominio.TipoItem;
import com.techchallenge.oficina.pecasinsumos.dominio.UnidadeMedida;
import com.techchallenge.oficina.pecasinsumos.entidades.PecaInsumo;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Regras de domínio da entidade PecaInsumo (Java puro, sem Spring). */
class PecaInsumoTest {

    private static BigDecimal bd(String valor) {
        return new BigDecimal(valor);
    }

    /** Insumo em litros (fracionável) com o saldo inicial informado. */
    private static PecaInsumo oleo(String quantidadeInicial) {
        return new PecaInsumo(TipoItem.INSUMO, "OLEO-5W30", "Óleo de motor 5W30", "Frasco de 1 L",
                UnidadeMedida.L, bd("45.00"), quantidadeInicial == null ? null : bd(quantidadeInicial));
    }

    /** Peça em unidades (não fracionável) com o saldo inicial informado. */
    private static PecaInsumo filtro(String quantidadeInicial) {
        return new PecaInsumo(TipoItem.PECA, "FILTRO-OLEO", "Filtro de óleo", null,
                UnidadeMedida.UN, bd("35.00"), bd(quantidadeInicial));
    }

    private static void assertSaldo(PecaInsumo item, String total, String reservada, String disponivel) {
        assertThat(item.getQuantidadeTotal()).isEqualByComparingTo(total);
        assertThat(item.getQuantidadeReservada()).isEqualByComparingTo(reservada);
        assertThat(item.getQuantidadeDisponivel()).isEqualByComparingTo(disponivel);
    }

    @Nested
    class Criacao {

        @Test
        void criaAtivoSemReservaComSaldoInicial() {
            PecaInsumo item = oleo("20");

            assertThat(item.isAtivo()).isTrue();
            assertThat(item.getTipo()).isEqualTo(TipoItem.INSUMO);
            assertThat(item.getUnidadeMedida()).isEqualTo(UnidadeMedida.L);
            assertThat(item.getPrecoUnitario()).isEqualByComparingTo("45.00");
            assertSaldo(item, "20", "0", "20");
        }

        @Test
        void quantidadeInicialNulaViraZero() {
            assertSaldo(oleo(null), "0", "0", "0");
        }

        @Test
        void quantidadeInicialZeroEhPermitida() {
            assertSaldo(oleo("0"), "0", "0", "0");
        }

        @Test
        void quantidadeInicialNegativaFalha() {
            assertThatThrownBy(() -> oleo("-1")).isInstanceOf(QuantidadeInvalidaException.class);
        }

        @Test
        void unidadeInteiraRejeitaQuantidadeInicialFracionada() {
            assertThatThrownBy(() -> filtro("1.5")).isInstanceOf(QuantidadeInvalidaException.class);
        }

        @Test
        void unidadeInteiraAceitaInteiroComZerosDecimais() {
            assertSaldo(filtro("3.000"), "3", "0", "3");
        }

        @Test
        void normalizaCodigoNomeEDescricao() {
            PecaInsumo item = new PecaInsumo(TipoItem.PECA, "  vela-ign ", "  Vela de ignição  ", "  NGK  ",
                    UnidadeMedida.UN, bd("28.00"), null);

            assertThat(item.getCodigo()).isEqualTo("VELA-IGN");
            assertThat(item.getNome()).isEqualTo("Vela de ignição");
            assertThat(item.getDescricao()).isEqualTo("NGK");
        }

        @Test
        void descricaoEmBrancoViraNula() {
            PecaInsumo item = new PecaInsumo(TipoItem.PECA, "VELA-IGN", "Vela", "   ",
                    UnidadeMedida.UN, bd("28.00"), null);

            assertThat(item.getDescricao()).isNull();
        }

        @Test
        void tipoOuUnidadeNulosFalham() {
            assertThatThrownBy(() -> new PecaInsumo(null, "X", "X", null, UnidadeMedida.UN, bd("1"), null))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> new PecaInsumo(TipoItem.PECA, "X", "X", null, null, bd("1"), null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void precoNuloOuNegativoFalha() {
            assertThatThrownBy(() -> new PecaInsumo(TipoItem.PECA, "X", "X", null, UnidadeMedida.UN, null, null))
                    .isInstanceOf(PrecoInvalidoException.class);
            assertThatThrownBy(() -> new PecaInsumo(TipoItem.PECA, "X", "X", null, UnidadeMedida.UN, bd("-0.01"), null))
                    .isInstanceOf(PrecoInvalidoException.class);
        }

        @Test
        void precoZeroEhPermitido() {
            PecaInsumo item = new PecaInsumo(TipoItem.INSUMO, "BRINDE", "Cortesia", null, UnidadeMedida.UN, bd("0"), null);

            assertThat(item.getPrecoUnitario()).isEqualByComparingTo("0");
        }
    }

    @Nested
    class AtualizacaoDeDados {

        @Test
        void atualizaDadosCadastraisSemMexerNoSaldo() {
            PecaInsumo item = oleo("20");
            item.reservar(bd("5"));

            item.atualizarDados(" oleo-5w40 ", "Óleo 5W40", null, bd("50.00"));

            assertThat(item.getCodigo()).isEqualTo("OLEO-5W40");
            assertThat(item.getNome()).isEqualTo("Óleo 5W40");
            assertThat(item.getDescricao()).isNull();
            assertThat(item.getPrecoUnitario()).isEqualByComparingTo("50.00");
            assertThat(item.getTipo()).isEqualTo(TipoItem.INSUMO);
            assertThat(item.getUnidadeMedida()).isEqualTo(UnidadeMedida.L);
            assertSaldo(item, "20", "5", "15");
        }

        @Test
        void precoInvalidoNaoAlteraNada() {
            PecaInsumo item = oleo("20");

            assertThatThrownBy(() -> item.atualizarDados("OUTRO", "Outro nome", null, bd("-1")))
                    .isInstanceOf(PrecoInvalidoException.class);

            assertThat(item.getCodigo()).isEqualTo("OLEO-5W30");
            assertThat(item.getNome()).isEqualTo("Óleo de motor 5W30");
            assertThat(item.getPrecoUnitario()).isEqualByComparingTo("45.00");
        }
    }

    @Nested
    class EntradaDeEstoque {

        @Test
        void somaAoTotal() {
            PecaInsumo item = oleo("20");

            item.registrarEntrada(bd("5"));

            assertSaldo(item, "25", "0", "25");
        }

        @Test
        void aceitaFracaoEmUnidadeFracionavel() {
            PecaInsumo item = oleo("20");

            item.registrarEntrada(bd("3.5"));

            assertSaldo(item, "23.5", "0", "23.5");
        }

        @ParameterizedTest
        @ValueSource(strings = {"0", "-1", "-0.5"})
        void quantidadeZeroOuNegativaFalha(String quantidade) {
            PecaInsumo item = oleo("20");

            assertThatThrownBy(() -> item.registrarEntrada(bd(quantidade)))
                    .isInstanceOf(QuantidadeInvalidaException.class);
            assertSaldo(item, "20", "0", "20");
        }

        @Test
        void quantidadeNulaFalha() {
            assertThatThrownBy(() -> oleo("20").registrarEntrada(null))
                    .isInstanceOf(QuantidadeInvalidaException.class);
        }

        @Test
        void unidadeInteiraRejeitaFracao() {
            PecaInsumo item = filtro("10");

            assertThatThrownBy(() -> item.registrarEntrada(bd("0.5")))
                    .isInstanceOf(QuantidadeInvalidaException.class);
            assertSaldo(item, "10", "0", "10");
        }

        @Test
        void itemInativoFalha() {
            PecaInsumo item = oleo("20");
            item.desativar();

            assertThatThrownBy(() -> item.registrarEntrada(bd("5")))
                    .isInstanceOf(PecaInsumoInativoException.class);
        }
    }

    @Nested
    class Reserva {

        @Test
        void reservaBloqueiaSemAlterarTotal() {
            PecaInsumo item = oleo("20");

            item.reservar(bd("5"));

            assertSaldo(item, "20", "5", "15");
        }

        @Test
        void reservaTodoODisponivel() {
            PecaInsumo item = filtro("3");

            item.reservar(bd("3"));

            assertSaldo(item, "3", "3", "0");
        }

        @Test
        void reservasSomam() {
            PecaInsumo item = oleo("20");

            item.reservar(bd("5"));
            item.reservar(bd("7"));

            assertSaldo(item, "20", "12", "8");
        }

        @Test
        void maisQueODisponivelFalhaENaoAlteraSaldo() {
            PecaInsumo item = oleo("10");
            item.reservar(bd("8"));

            assertThatThrownBy(() -> item.reservar(bd("5")))
                    .isInstanceOf(EstoqueInsuficienteException.class)
                    .hasMessageContaining("solicitado 5")
                    .hasMessageContaining("disponível 2");
            assertSaldo(item, "10", "8", "2");
        }

        @Test
        void semEstoqueFalha() {
            assertThatThrownBy(() -> oleo(null).reservar(bd("1")))
                    .isInstanceOf(EstoqueInsuficienteException.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {"0", "-2"})
        void quantidadeZeroOuNegativaFalha(String quantidade) {
            assertThatThrownBy(() -> oleo("20").reservar(bd(quantidade)))
                    .isInstanceOf(QuantidadeInvalidaException.class);
        }

        @Test
        void unidadeInteiraRejeitaFracao() {
            assertThatThrownBy(() -> filtro("10").reservar(bd("1.5")))
                    .isInstanceOf(QuantidadeInvalidaException.class);
        }

        @Test
        void itemInativoFalha() {
            PecaInsumo item = oleo("20");
            item.desativar();

            assertThatThrownBy(() -> item.reservar(bd("1")))
                    .isInstanceOf(PecaInsumoInativoException.class);
        }
    }

    @Nested
    class Liberacao {

        @Test
        void devolveAoDisponivelSemAlterarTotal() {
            PecaInsumo item = oleo("20");
            item.reservar(bd("8"));

            item.liberar(bd("5"));

            assertSaldo(item, "20", "3", "17");
        }

        @Test
        void liberaTudoQueEstavaReservado() {
            PecaInsumo item = oleo("20");
            item.reservar(bd("8"));

            item.liberar(bd("8"));

            assertSaldo(item, "20", "0", "20");
        }

        @Test
        void maisQueOReservadoFalhaENaoAlteraSaldo() {
            PecaInsumo item = oleo("20");
            item.reservar(bd("2"));

            assertThatThrownBy(() -> item.liberar(bd("5")))
                    .isInstanceOf(ReservaInsuficienteException.class);
            assertSaldo(item, "20", "2", "18");
        }

        @Test
        void semReservaFalha() {
            assertThatThrownBy(() -> oleo("20").liberar(bd("1")))
                    .isInstanceOf(ReservaInsuficienteException.class);
        }

        @Test
        void quantidadeInvalidaFalha() {
            PecaInsumo item = oleo("20");
            item.reservar(bd("5"));

            assertThatThrownBy(() -> item.liberar(bd("0"))).isInstanceOf(QuantidadeInvalidaException.class);
            assertThatThrownBy(() -> item.liberar(null)).isInstanceOf(QuantidadeInvalidaException.class);
        }
    }

    @Nested
    class Consumo {

        @Test
        void baixaReservadoETotal() {
            PecaInsumo item = oleo("20");
            item.reservar(bd("5"));

            item.consumir(bd("5"));

            assertSaldo(item, "15", "0", "15");
        }

        @Test
        void consumoParcialMantemORestoReservado() {
            PecaInsumo item = oleo("20");
            item.reservar(bd("5"));

            item.consumir(bd("3.5"));

            assertSaldo(item, "16.5", "1.5", "15");
        }

        @Test
        void maisQueOReservadoFalhaENaoAlteraSaldo() {
            PecaInsumo item = oleo("20");
            item.reservar(bd("2"));

            assertThatThrownBy(() -> item.consumir(bd("5")))
                    .isInstanceOf(ReservaInsuficienteException.class);
            assertSaldo(item, "20", "2", "18");
        }

        @Test
        void naoConsomeOQueNaoFoiReservado() {
            PecaInsumo item = oleo("20");

            assertThatThrownBy(() -> item.consumir(bd("1")))
                    .isInstanceOf(ReservaInsuficienteException.class);
            assertSaldo(item, "20", "0", "20");
        }
    }

    @Nested
    class CicloCompletoDaReserva {

        @Test
        void reservarRecusarEDepoisReservarEConsumir() {
            PecaInsumo item = filtro("4");

            item.reservar(bd("3"));    // peça vinculada à OS 1
            item.liberar(bd("3"));     // orçamento da OS 1 recusado
            item.reservar(bd("4"));    // peça vinculada à OS 2
            item.consumir(bd("4"));    // orçamento da OS 2 aprovado

            assertSaldo(item, "0", "0", "0");
        }
    }

    @Nested
    class Desativacao {

        @Test
        void desativaSemReservaMesmoComEstoque() {
            PecaInsumo item = oleo("20");

            item.desativar();

            assertThat(item.isAtivo()).isFalse();
            assertSaldo(item, "20", "0", "20");
        }

        @Test
        void comReservaFalhaEContinuaAtivo() {
            PecaInsumo item = oleo("20");
            item.reservar(bd("1"));

            assertThatThrownBy(item::desativar).isInstanceOf(PecaInsumoComReservaException.class);
            assertThat(item.isAtivo()).isTrue();
        }

        @Test
        void desativaDepoisDeLiberarAReserva() {
            PecaInsumo item = oleo("20");
            item.reservar(bd("1"));
            item.liberar(bd("1"));

            item.desativar();

            assertThat(item.isAtivo()).isFalse();
        }

        @Test
        void jaInativoFalha() {
            PecaInsumo item = oleo("20");
            item.desativar();

            assertThatThrownBy(item::desativar).isInstanceOf(PecaInsumoInativoException.class);
        }
    }

    @Nested
    class UnidadeDeMedida {

        @ParameterizedTest
        @EnumSource(value = UnidadeMedida.class, names = {"L", "ML", "KG", "G"})
        void fracionaveisAceitamFracao(UnidadeMedida unidade) {
            assertThat(unidade.aceita(bd("1.5"))).isTrue();
        }

        @ParameterizedTest
        @EnumSource(value = UnidadeMedida.class, names = {"UN", "JOGO"})
        void inteirasRejeitamFracaoEAceitamInteiro(UnidadeMedida unidade) {
            assertThat(unidade.aceita(bd("1.5"))).isFalse();
            assertThat(unidade.aceita(bd("2"))).isTrue();
            assertThat(unidade.aceita(bd("2.000"))).isTrue();
            assertThat(unidade.aceita(bd("10"))).isTrue();
        }
    }
}
