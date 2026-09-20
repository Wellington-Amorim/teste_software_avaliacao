package br.edu.ifpr.pedidos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PedidoTest {
    @Test
    void deveSomarSubtotalPesoEFragilidadeApenasQuandoAplicavel() {
        ItemPedido ativo = new ItemPedido("ATIVO", 2_000, 2, 2, 700, true);
        ItemPedido inativo = new ItemPedido("INATIVO", 5_000, 0, 0, 900, true);
        Pedido pedido = new Pedido(List.of(ativo, inativo), "PR", false, null);

        assertAll(
            () -> assertEquals(4_000, pedido.subtotalCentavos()),
            () -> assertEquals(1_400, pedido.pesoGramas()),
            () -> assertTrue(pedido.temFragil())
        );
    }

    @Test
    void itemFragilInativoNaoTornaPedidoFragil() {
        ItemPedido inativo = new ItemPedido("INATIVO", 5_000, 0, 0, 900, true);
        Pedido pedido = new Pedido(List.of(inativo), "PR", false, null);

        assertFalse(pedido.temFragil());
    }

    @Test
    void deveAvaliarEstoquePorLinhaEPararNaPrimeiraFalta() {
        ItemPedido indisponivel = new ItemPedido("A", 1_000, 2, 1, 100, false);
        ItemPedido disponivel = new ItemPedido("B", 1_000, 1, 1, 100, false);
        Pedido pedido = new Pedido(List.of(indisponivel, disponivel), "PR", false, null);

        assertFalse(pedido.estoqueSuficiente());
    }

    @Test
    void deveCopiarListaDefensivamente() {
        List<ItemPedido> itens = new ArrayList<>();
        itens.add(new ItemPedido("A", 1_000, 1, 1, 100, false));
        Pedido pedido = new Pedido(itens, "PR", false, null);

        itens.clear();

        assertAll(
            () -> assertEquals(1, pedido.itens().size()),
            () -> assertThrows(UnsupportedOperationException.class,
                () -> pedido.itens().add(new ItemPedido("B", 1_000, 1, 1, 100, false)))
        );
    }

    @Test
    void deveAceitarListaVaziaNaConstrucao() {
        Pedido pedido = new Pedido(List.of(), "SC", false, "EXTRA10");

        assertAll(
            () -> assertEquals(0, pedido.subtotalCentavos()),
            () -> assertEquals("SC", pedido.uf()),
            () -> assertEquals("EXTRA10", pedido.cupom())
        );
    }

    @Test
    void deveRejeitarListaUfOuElementoInvalidos() {
        List<ItemPedido> muitosItens = Collections.nCopies(101, new ItemPedido("SKU", 1, 0, 0, 1, false));

        assertAll(
            () -> assertThrows(IllegalArgumentException.class, () -> new Pedido(null, "PR", false, null)),
            () -> assertThrows(IllegalArgumentException.class, () -> new Pedido(muitosItens, "PR", false, null)),
            () -> assertThrows(IllegalArgumentException.class, () -> new Pedido(List.of(), null, false, null)),
            () -> assertThrows(IllegalArgumentException.class, () -> new Pedido(List.of(), "pr", false, null)),
            () -> assertThrows(IllegalArgumentException.class, () -> new Pedido(List.of(), "PAR", false, null)),
            () -> assertThrows(NullPointerException.class, () -> new Pedido(Collections.singletonList(null), "PR", false, null))
        );
    }
}
