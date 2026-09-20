package br.edu.ifpr.pedidos;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PagamentoServiceTest {
    @Test
    void deveAprovarNaPrimeiraTentativa() {
        List<Long> cobrancas = new ArrayList<>();
        PagamentoService service = new PagamentoService(total -> {
            cobrancas.add(total);
            return true;
        });

        boolean pago = service.pagar(10_000, 3);

        assertAll(
            () -> assertTrue(pago),
            () -> assertEquals(List.of(10_000L), cobrancas)
        );
    }

    @Test
    void deveRecusarSemRepetirQuandoProcessadorRetornaFalse() {
        List<Long> cobrancas = new ArrayList<>();
        PagamentoService service = new PagamentoService(total -> {
            cobrancas.add(total);
            return false;
        });

        boolean pago = service.pagar(10_000, 3);

        assertAll(
            () -> assertFalse(pago),
            () -> assertEquals(List.of(10_000L), cobrancas)
        );
    }

    @Test
    void deveRepetirIndisponibilidadeTemporariaAteSucesso() {
        class ProcessadorInstavel implements ProcessadorPagamento {
            private int chamadas;

            @Override
            public boolean autorizar(long totalCentavos) {
                chamadas++;
                if (chamadas < 3) throw new IllegalStateException("indisponivel");
                return true;
            }
        }
        ProcessadorInstavel processador = new ProcessadorInstavel();
        PagamentoService service = new PagamentoService(processador);

        boolean pago = service.pagar(10_000, 3);

        assertAll(
            () -> assertTrue(pago),
            () -> assertEquals(3, processador.chamadas)
        );
    }

    @Test
    void deveRetornarFalseQuandoEsgotaTentativasTemporarias() {
        List<Long> cobrancas = new ArrayList<>();
        PagamentoService service = new PagamentoService(total -> {
            cobrancas.add(total);
            throw new IllegalStateException("indisponivel");
        });

        boolean pago = service.pagar(10_000, 2);

        assertAll(
            () -> assertFalse(pago),
            () -> assertEquals(List.of(10_000L, 10_000L), cobrancas)
        );
    }

    @Test
    void devePropagarExcecoesDiferentesDeIndisponibilidade() {
        PagamentoService service = new PagamentoService(total -> {
            throw new IllegalArgumentException("erro definitivo");
        });

        assertThrows(IllegalArgumentException.class, () -> service.pagar(10_000, 3));
    }

    @Test
    void deveValidarDependenciaTotalELimiteDeTentativas() {
        assertAll(
            () -> assertThrows(NullPointerException.class, () -> new PagamentoService(null)),
            () -> assertThrows(IllegalArgumentException.class, () -> new PagamentoService(total -> true).pagar(0, 1)),
            () -> assertThrows(IllegalArgumentException.class, () -> new PagamentoService(total -> true).pagar(1, 0)),
            () -> assertThrows(IllegalArgumentException.class, () -> new PagamentoService(total -> true).pagar(1, 4))
        );
    }
}
