package br.edu.biblioteca.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Regra de negocio: multa de R$ 2,00 por dia de atraso. */
public final class CalculadoraMulta {

    public static final BigDecimal VALOR_POR_DIA = new BigDecimal("2.00");

    private CalculadoraMulta() {
    }

    public static BigDecimal calcular(LocalDate dataPrevista, LocalDate dataDevolucao) {
        if (dataPrevista == null || dataDevolucao == null) {
            throw new IllegalArgumentException("datas nao podem ser nulas");
        }
        long diasAtraso = ChronoUnit.DAYS.between(dataPrevista, dataDevolucao);
        if (diasAtraso <= 0) {
            return BigDecimal.ZERO;
        }
        return VALOR_POR_DIA.multiply(BigDecimal.valueOf(diasAtraso));
    }
}
