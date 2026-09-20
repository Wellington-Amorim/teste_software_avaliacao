package br.edu.ifpr.boletim;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BoletimTest {

    @Test
    void deveAprovarAlunoComMediaOito() {
        Boletim boletim = new Boletim();

        String resultado = boletim.verificarSituacao(8);

        assertEquals("APROVADO", resultado);
    }

    @Test
    void deveAprovarAlunoComMediaSete() {
        Boletim boletim = new Boletim();

        String resultado = boletim.verificarSituacao(7);

        assertEquals("APROVADO", resultado);
    }

    @Test
    void deveRecuperarAlunoComMediaQuatro() {
        Boletim boletim = new Boletim();

        String resultado = boletim.verificarSituacao(4);

        assertEquals("RECUPERACAO", resultado);
    }

    @Test
    void deveReprovarAlunoComMediaDois() {
        Boletim boletim = new Boletim();

        String resultado = boletim.verificarSituacao(2);

        assertEquals("REPROVADO", resultado);
    }

    @Test
    void deveCalcularMediaIgualCinco() {
        Boletim boletim = new Boletim();

        double resultado = boletim.calcularMedia(5, 5);

        assertEquals(5, resultado, 0.0001);
    }

    @Test
    void deveCalcularMediaComParteDecimal() {
        Boletim boletim = new Boletim();

        double resultado = boletim.calcularMedia(7, 8);

        assertEquals(7.5, resultado, 0.0001);
    }

    @Test
    void deveContarZeroAprovadosEmArrayVazio() {
        Boletim boletim = new Boletim();

        int resultado = boletim.contarAprovados(new double[] {});

        assertEquals(0, resultado);
    }

    @Test
    void deveContarUmAprovadoEmArrayComUmElemento() {
        Boletim boletim = new Boletim();

        int resultado = boletim.contarAprovados(new double[] {7});

        assertEquals(1, resultado);
    }

    @Test
    void deveContarAprovadosEmArrayComVariasMedias() {
        Boletim boletim = new Boletim();

        int resultado = boletim.contarAprovados(new double[] {8, 5, 7, 3.5, 9.5});

        assertEquals(3, resultado);
    }
}
