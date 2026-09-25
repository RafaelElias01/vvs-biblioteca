package br.edu.biblioteca.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.edu.biblioteca.model.CalculadoraMulta;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CalculadoraMultaTest {

    private static final LocalDate PREVISTA = LocalDate.of(2026, 3, 10);

    @Test
    void devolucaoNoPrazoNaoTemMulta() {
        assertEquals(BigDecimal.ZERO, CalculadoraMulta.calcular(PREVISTA, PREVISTA));
    }

    @Test
    void devolucaoAntecipadaNaoTemMulta() {
        assertEquals(BigDecimal.ZERO, CalculadoraMulta.calcular(PREVISTA, PREVISTA.minusDays(5)));
    }

    @ParameterizedTest
    @CsvSource({"1,2.00", "2,4.00", "10,20.00", "30,60.00"})
    void atrasoCobraDoisReaisPorDia(long dias, String esperado) {
        BigDecimal multa = CalculadoraMulta.calcular(PREVISTA, PREVISTA.plusDays(dias));
        assertEquals(new BigDecimal(esperado), multa);
    }

    @Test
    void dataPrevistaNulaLancaExcecao() {
        assertThrows(IllegalArgumentException.class, () -> CalculadoraMulta.calcular(null, PREVISTA));
    }

    @Test
    void dataDevolucaoNulaLancaExcecao() {
        assertThrows(IllegalArgumentException.class, () -> CalculadoraMulta.calcular(PREVISTA, null));
    }
}
