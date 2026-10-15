package com.caliburger.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MoedaTest {

    @Test
    @DisplayName("formata no padrão brasileiro")
    void formata() {
        assertEquals("R$ 25,90", Moeda.formatar(new BigDecimal("25.9")));
        assertEquals("R$ 1.234,50", Moeda.formatar(new BigDecimal("1234.5")));
        assertEquals("R$ 0,00", Moeda.formatar(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("aceita vírgula, ponto, milhar e o prefixo R$")
    void converteFormatosComuns() {
        assertEquals(new BigDecimal("12.50"), Moeda.converter("12,50"));
        assertEquals(new BigDecimal("12.50"), Moeda.converter("12.50"));
        assertEquals(new BigDecimal("12.00"), Moeda.converter("12"));
        assertEquals(new BigDecimal("12.50"), Moeda.converter(" R$ 12,5 "));
        assertEquals(new BigDecimal("1234.50"), Moeda.converter("1.234,50"));
        assertEquals(new BigDecimal("1234.00"), Moeda.converter("1.234"));
    }

    @Test
    @DisplayName("rejeita textos que não são valores")
    void rejeitaInvalidos() {
        for (String invalido : new String[] {"", "  ", "abc", "12,345", "1,2,3", "-5", "12.5.1", null}) {
            assertThrows(IllegalArgumentException.class, () -> Moeda.converter(invalido), "aceitou: " + invalido);
        }
    }
}
