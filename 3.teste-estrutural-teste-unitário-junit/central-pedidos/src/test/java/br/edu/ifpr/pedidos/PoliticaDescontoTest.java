package br.edu.ifpr.pedidos;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PoliticaDescontoTest {
    private final PoliticaDesconto politica = new PoliticaDesconto();

    @Test
    void deveAplicarDezPorCentoParaVipComTruncamento() {
        Cliente cliente = new Cliente(true, false, 1);

        long desconto = politica.calcular(cliente, 10_005, null);

        assertEquals(1_000, desconto);
    }

    @Test
    void deveAplicarCincoPorCentoParaClienteComumNoSubtotalMinimo() {
        Cliente cliente = new Cliente(false, false, 1);

        long desconto = politica.calcular(cliente, 50_000, " ");

        assertEquals(2_500, desconto);
    }

    @Test
    void deveRetornarZeroParaClienteComumAbaixoDoSubtotalMinimo() {
        Cliente cliente = new Cliente(false, false, 1);

        long desconto = politica.calcular(cliente, 49_999, null);

        assertEquals(0, desconto);
    }

    @Test
    void deveAplicarCupomBemVindoNormalizadoQuandoElegivel() {
        Cliente cliente = new Cliente(false, false, 0);

        long desconto = politica.calcular(cliente, 10_000, "  bemvindo ");

        assertEquals(2_000, desconto);
    }

    @Test
    void naoDeveAplicarBemVindoQuandoClienteTemHistoricoOuSubtotalInsuficiente() {
        Cliente comHistorico = new Cliente(false, false, 1);
        Cliente semHistorico = new Cliente(false, false, 0);

        assertAll(
            () -> assertEquals(0, politica.calcular(comHistorico, 10_000, "BEMVINDO")),
            () -> assertEquals(0, politica.calcular(semHistorico, 9_999, "BEMVINDO"))
        );
    }

    @Test
    void deveAplicarExtra10QuandoElegivel() {
        Cliente cliente = new Cliente(false, false, 1);

        long desconto = politica.calcular(cliente, 20_000, "EXTRA10");

        assertEquals(2_000, desconto);
    }

    @Test
    void deveLimitarDescontoCombinadoAVintePorCentoDoSubtotal() {
        Cliente cliente = new Cliente(true, false, 0);

        long desconto = politica.calcular(cliente, 10_000, "BEMVINDO");

        assertEquals(2_000, desconto);
    }

    @Test
    void deveRejeitarSubtotalNegativoECupomDesconhecido() {
        Cliente cliente = new Cliente(false, false, 1);

        assertAll(
            () -> assertThrows(IllegalArgumentException.class, () -> politica.calcular(cliente, -1, null)),
            () -> assertThrows(IllegalArgumentException.class, () -> politica.calcular(cliente, 10_000, "NATAL"))
        );
    }
}
