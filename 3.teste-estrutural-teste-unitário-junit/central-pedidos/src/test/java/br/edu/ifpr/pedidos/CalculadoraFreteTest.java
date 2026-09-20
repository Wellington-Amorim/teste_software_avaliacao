package br.edu.ifpr.pedidos;

import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CalculadoraFreteTest {
    private final CalculadoraFrete calculadora = new CalculadoraFrete();

    @Test
    void deveCalcularBaseDoParanaSemAdicionais() {
        Pedido pedido = pedido("PR", false, item("SKU", 10_000, 1, 1_000, false));
        Cliente cliente = new Cliente(false, false, 1);

        long frete = calculadora.calcular(pedido, cliente, 10_000);

        assertEquals(1_200, frete);
    }

    @Test
    void deveCalcularBaseDeSaoPauloERioDeJaneiro() {
        Cliente cliente = new Cliente(false, false, 1);
        Pedido saoPaulo = pedido("SP", false, item("SP", 10_000, 1, 1_000, false));
        Pedido rio = pedido("RJ", false, item("RJ", 10_000, 1, 1_000, false));

        assertAll(
            () -> assertEquals(2_000, calculadora.calcular(saoPaulo, cliente, 10_000)),
            () -> assertEquals(2_000, calculadora.calcular(rio, cliente, 10_000))
        );
    }

    @Test
    void deveCalcularBasePadraoParaOutrasUfs() {
        Pedido pedido = pedido("SC", false, item("SKU", 10_000, 1, 1_000, false));
        Cliente cliente = new Cliente(false, false, 1);

        long frete = calculadora.calcular(pedido, cliente, 10_000);

        assertEquals(3_000, frete);
    }

    @Test
    void deveAcrescentarPesoPorQuiloOuFracaoAcimaDeDoisQuilos() {
        Cliente cliente = new Cliente(false, false, 1);
        Pedido fracao = pedido("PR", false, item("FRACAO", 10_000, 1, 2_001, false));
        Pedido variasIteracoes = pedido("PR", false, item("PESO", 10_000, 1, 4_001, false));

        assertAll(
            () -> assertEquals(1_500, calculadora.calcular(fracao, cliente, 10_000)),
            () -> assertEquals(2_100, calculadora.calcular(variasIteracoes, cliente, 10_000))
        );
    }

    @Test
    void deveZerarFreteEmEntregaNormalComLiquidoMinimo() {
        Pedido pedido = pedido("SC", false, item("SKU", 40_000, 1, 5_000, false));
        Cliente cliente = new Cliente(false, false, 1);

        long frete = calculadora.calcular(pedido, cliente, 30_000);

        assertEquals(0, frete);
    }

    @Test
    void deveAplicarMetadeParaVipAntesDosAdicionaisExpressosEFragil() {
        Pedido pedido = pedido("SC", true, item("SKU", 10_000, 1, 3_000, true));
        Cliente cliente = new Cliente(true, false, 1);

        long frete = calculadora.calcular(pedido, cliente, 10_000);

        assertEquals(3_650, frete);
    }

    @Test
    void deveManterAdicionaisQuandoBaseFoiZerada() {
        Pedido pedido = pedido("PR", false, item("SKU", 40_000, 1, 1_000, true));
        Cliente cliente = new Cliente(false, false, 1);

        long frete = calculadora.calcular(pedido, cliente, 30_000);

        assertEquals(500, frete);
    }

    @Test
    void deveRejeitarLiquidoNegativo() {
        Pedido pedido = pedido("PR", false, item("SKU", 10_000, 1, 1_000, false));
        Cliente cliente = new Cliente(false, false, 1);

        assertThrows(IllegalArgumentException.class, () -> calculadora.calcular(pedido, cliente, -1));
    }

    private Pedido pedido(String uf, boolean expresso, ItemPedido item) {
        return new Pedido(List.of(item), uf, expresso, null);
    }

    private ItemPedido item(String sku, long preco, int quantidade, int peso, boolean fragil) {
        return new ItemPedido(sku, preco, quantidade, quantidade, peso, fragil);
    }
}
