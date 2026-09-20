package br.edu.ifpr.pedidos;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ItemPedidoTest {
    @Test
    void deveCalcularTotalEDisponibilidade() {
        ItemPedido item = new ItemPedido("SKU", 1_999, 3, 3, 500, false);

        assertAll(
            () -> assertEquals(5_997, item.totalCentavos()),
            () -> assertTrue(item.disponivel())
        );
    }

    @Test
    void deveIdentificarIndisponibilidadeQuandoQuantidadePassaDoEstoque() {
        ItemPedido item = new ItemPedido("SKU", 1_000, 4, 3, 500, false);

        assertFalse(item.disponivel());
    }

    @Test
    void deveAceitarValoresNosLimitesValidos() {
        ItemPedido item = new ItemPedido("SKU", 1_000_000, 100, 0, 100_000, true);

        assertAll(
            () -> assertEquals("SKU", item.sku()),
            () -> assertEquals(100_000_000, item.totalCentavos()),
            () -> assertFalse(item.disponivel())
        );
    }

    @Test
    void deveRejeitarCamposInvalidos() {
        assertAll(
            () -> assertThrows(IllegalArgumentException.class, () -> new ItemPedido(null, 1, 0, 0, 1, false)),
            () -> assertThrows(IllegalArgumentException.class, () -> new ItemPedido(" ", 1, 0, 0, 1, false)),
            () -> assertThrows(IllegalArgumentException.class, () -> new ItemPedido("SKU", 0, 0, 0, 1, false)),
            () -> assertThrows(IllegalArgumentException.class, () -> new ItemPedido("SKU", 1_000_001, 0, 0, 1, false)),
            () -> assertThrows(IllegalArgumentException.class, () -> new ItemPedido("SKU", 1, -1, 0, 1, false)),
            () -> assertThrows(IllegalArgumentException.class, () -> new ItemPedido("SKU", 1, 101, 0, 1, false)),
            () -> assertThrows(IllegalArgumentException.class, () -> new ItemPedido("SKU", 1, 0, -1, 1, false)),
            () -> assertThrows(IllegalArgumentException.class, () -> new ItemPedido("SKU", 1, 0, 0, 0, false)),
            () -> assertThrows(IllegalArgumentException.class, () -> new ItemPedido("SKU", 1, 0, 0, 100_001, false))
        );
    }
}
