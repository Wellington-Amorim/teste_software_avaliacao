package br.edu.ifpr.pedidos;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AnaliseRiscoTest {
    private final AnaliseRisco analise = new AnaliseRisco();

    @Test
    void deveRecusarClienteBloqueadoAntesDasDemaisRegras() {
        Cliente cliente = new Cliente(false, true, 0);

        String resultado = analise.avaliar(cliente, 200_000, true);

        assertEquals("RECUSADO", resultado);
    }

    @Test
    void deveEnviarClienteSemHistoricoParaRevisaoQuandoTotalPassaDeMilReais() {
        Cliente cliente = new Cliente(false, false, 0);

        String resultado = analise.avaliar(cliente, 100_001, false);

        assertEquals("REVISAO", resultado);
    }

    @Test
    void deveEnviarClienteSemHistoricoParaRevisaoQuandoEntregaExpressa() {
        Cliente cliente = new Cliente(false, false, 0);

        String resultado = analise.avaliar(cliente, 50_000, true);

        assertEquals("REVISAO", resultado);
    }

    @Test
    void deveAprovarClienteSemHistoricoNoLimiteComEntregaNormal() {
        Cliente cliente = new Cliente(false, false, 0);

        String resultado = analise.avaliar(cliente, 100_000, false);

        assertEquals("APROVADO", resultado);
    }

    @Test
    void deveEnviarClienteComHistoricoNaoVipParaRevisaoQuandoTotalPassaDeCincoMilReais() {
        Cliente cliente = new Cliente(false, false, 2);

        String resultado = analise.avaliar(cliente, 500_001, false);

        assertEquals("REVISAO", resultado);
    }

    @Test
    void deveAprovarClienteVipComHistoricoMesmoAcimaDeCincoMilReais() {
        Cliente cliente = new Cliente(true, false, 2);

        String resultado = analise.avaliar(cliente, 600_000, false);

        assertEquals("APROVADO", resultado);
    }

    @Test
    void deveRejeitarTotalNegativo() {
        Cliente cliente = new Cliente(false, false, 1);

        assertThrows(IllegalArgumentException.class, () -> analise.avaliar(cliente, -1, false));
    }
}
