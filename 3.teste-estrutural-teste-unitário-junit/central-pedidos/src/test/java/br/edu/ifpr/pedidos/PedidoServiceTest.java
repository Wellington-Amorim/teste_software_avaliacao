package br.edu.ifpr.pedidos;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PedidoServiceTest {
    @Test
    void deveFecharPedidoDeClienteComumComFreteDoParanaEPagamentoAprovado() {
        Cliente cliente = new Cliente(false, false, 1);
        ItemPedido item = new ItemPedido("LIVRO-JAVA", 10_000, 1, 5, 1_000, false);
        Pedido pedido = new Pedido(List.of(item), "PR", false, null);

        List<Long> cobrancas = new ArrayList<>();
        PedidoService service = new PedidoService(total -> {
            cobrancas.add(total);
            return true;
        });

        ResultadoPedido resultado = service.fechar(pedido, cliente);

        assertAll(
            () -> assertEquals("PAGO", resultado.status()),
            () -> assertEquals(10_000L, resultado.subtotalCentavos()),
            () -> assertEquals(0L, resultado.descontoCentavos()),
            () -> assertEquals(1_200L, resultado.freteCentavos()),
            () -> assertEquals(11_200L, resultado.totalCentavos()),
            () -> assertEquals(List.of(11_200L), cobrancas)
        );
    }

    @Test
    void deveRetornarBloqueadoAntesDeAvaliarItensECupom() {
        Cliente cliente = new Cliente(false, true, 0);
        ItemPedido itemSemEstoque = new ItemPedido("SKU", 10_000, 2, 0, 1_000, false);
        Pedido pedido = new Pedido(List.of(itemSemEstoque), "PR", false, "DESCONHECIDO");
        List<Long> cobrancas = new ArrayList<>();
        PedidoService service = new PedidoService(total -> {
            cobrancas.add(total);
            return true;
        });

        ResultadoPedido resultado = service.fechar(pedido, cliente);

        assertAll(
            () -> assertEquals("BLOQUEADO", resultado.status()),
            () -> assertEquals(0, resultado.totalCentavos()),
            () -> assertTrue(cobrancas.isEmpty())
        );
    }

    @Test
    void deveRejeitarPedidoSemItensAtivos() {
        Cliente cliente = new Cliente(false, false, 1);
        Pedido pedido = new Pedido(List.of(new ItemPedido("SKU", 10_000, 0, 0, 1_000, false)), "PR", false, null);
        PedidoService service = new PedidoService(total -> true);

        assertThrows(IllegalArgumentException.class, () -> service.fechar(pedido, cliente));
    }

    @Test
    void deveRetornarSemEstoqueAntesDeAvaliarCupom() {
        Cliente cliente = new Cliente(false, false, 1);
        Pedido pedido = new Pedido(List.of(new ItemPedido("SKU", 10_000, 2, 1, 1_000, false)), "PR", false, "NATAL");
        List<Long> cobrancas = new ArrayList<>();
        PedidoService service = new PedidoService(total -> {
            cobrancas.add(total);
            return true;
        });

        ResultadoPedido resultado = service.fechar(pedido, cliente);

        assertAll(
            () -> assertEquals("SEM_ESTOQUE", resultado.status()),
            () -> assertEquals(0, resultado.totalCentavos()),
            () -> assertTrue(cobrancas.isEmpty())
        );
    }

    @Test
    void deveRetornarRevisaoSemCobrarQuandoRiscoNaoAprova() {
        Cliente cliente = new Cliente(false, false, 0);
        Pedido pedido = new Pedido(List.of(new ItemPedido("NOTEBOOK", 120_000, 1, 1, 1_000, false)), "PR", false, null);
        List<Long> cobrancas = new ArrayList<>();
        PedidoService service = new PedidoService(total -> {
            cobrancas.add(total);
            return true;
        });

        ResultadoPedido resultado = service.fechar(pedido, cliente);

        assertAll(
            () -> assertEquals("REVISAO", resultado.status()),
            () -> assertEquals(120_000, resultado.subtotalCentavos()),
            () -> assertEquals(6_000, resultado.descontoCentavos()),
            () -> assertEquals(0, resultado.freteCentavos()),
            () -> assertEquals(114_000, resultado.totalCentavos()),
            () -> assertTrue(cobrancas.isEmpty())
        );
    }

    @Test
    void deveRetornarPagamentoRecusadoComValoresCalculados() {
        Cliente cliente = new Cliente(false, false, 1);
        Pedido pedido = new Pedido(List.of(new ItemPedido("SKU", 20_000, 1, 1, 1_000, false)), "PR", false, "EXTRA10");
        List<Long> cobrancas = new ArrayList<>();
        PedidoService service = new PedidoService(total -> {
            cobrancas.add(total);
            return false;
        });

        ResultadoPedido resultado = service.fechar(pedido, cliente);

        assertAll(
            () -> assertEquals("PAGAMENTO_RECUSADO", resultado.status()),
            () -> assertEquals(20_000, resultado.subtotalCentavos()),
            () -> assertEquals(2_000, resultado.descontoCentavos()),
            () -> assertEquals(1_200, resultado.freteCentavos()),
            () -> assertEquals(19_200, resultado.totalCentavos()),
            () -> assertEquals(List.of(19_200L), cobrancas)
        );
    }

    @Test
    void deveRepetirPagamentoTemporariamenteIndisponivelNoFechamento() {
        Cliente cliente = new Cliente(false, false, 1);
        Pedido pedido = new Pedido(List.of(new ItemPedido("SKU", 10_000, 1, 1, 1_000, false)), "PR", false, null);
        List<Long> cobrancas = new ArrayList<>();
        PedidoService service = new PedidoService(total -> {
            cobrancas.add(total);
            if (cobrancas.size() < 3) throw new IllegalStateException("indisponivel");
            return true;
        });

        ResultadoPedido resultado = service.fechar(pedido, cliente);

        assertAll(
            () -> assertEquals("PAGO", resultado.status()),
            () -> assertEquals(List.of(11_200L, 11_200L, 11_200L), cobrancas)
        );
    }

    @Test
    void devePropagarCupomDesconhecidoSemCobrar() {
        Cliente cliente = new Cliente(false, false, 1);
        Pedido pedido = new Pedido(List.of(new ItemPedido("SKU", 10_000, 1, 1, 1_000, false)), "PR", false, "NATAL");
        List<Long> cobrancas = new ArrayList<>();
        PedidoService service = new PedidoService(total -> {
            cobrancas.add(total);
            return true;
        });

        assertAll(
            () -> assertThrows(IllegalArgumentException.class, () -> service.fechar(pedido, cliente)),
            () -> assertTrue(cobrancas.isEmpty())
        );
    }

    @Test
    void deveValidarReferenciasObrigatorias() {
        Pedido pedido = new Pedido(List.of(new ItemPedido("SKU", 10_000, 1, 1, 1_000, false)), "PR", false, null);
        Cliente cliente = new Cliente(false, false, 1);

        assertAll(
            () -> assertThrows(NullPointerException.class, () -> new PedidoService(null)),
            () -> assertThrows(NullPointerException.class, () -> new PedidoService(total -> true).fechar(null, cliente)),
            () -> assertThrows(NullPointerException.class, () -> new PedidoService(total -> true).fechar(pedido, null))
        );
    }
}
