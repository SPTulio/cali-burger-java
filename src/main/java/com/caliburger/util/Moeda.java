package com.caliburger.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.regex.Pattern;

/** Conversão entre texto e valores em reais. */
public final class Moeda {

    private static final Locale BRASIL = Locale.of("pt", "BR");
    private static final Pattern FORMATO_VALIDO = Pattern.compile("\\d{1,3}(\\.\\d{3})*(,\\d{1,2})?|\\d+([.,]\\d{1,2})?");

    private Moeda() {
    }

    /** Formata no padrão brasileiro: 1234.5 → "R$ 1.234,50". */
    public static String formatar(BigDecimal valor) {
        DecimalFormat formato = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(BRASIL));
        return "R$ " + formato.format(valor.setScale(2, RoundingMode.HALF_UP));
    }

    /**
     * Lê um valor digitado pelo usuário. Aceita "12,50", "12.50", "1.234,50" e "R$ 12,50".
     *
     * @throws IllegalArgumentException se o texto não for um valor válido
     */
    public static BigDecimal converter(String texto) {
        String limpo = texto == null ? "" : texto.replace("R$", "").replace(' ', ' ').trim();
        if (!FORMATO_VALIDO.matcher(limpo).matches()) {
            throw new IllegalArgumentException("Valor inválido: use o formato 12,50.");
        }
        String normalizado;
        if (limpo.contains(",")) {
            normalizado = limpo.replace(".", "").replace(',', '.');   // 1.234,50 → 1234.50
        } else if (limpo.matches("\\d{1,3}(\\.\\d{3})+")) {
            normalizado = limpo.replace(".", "");                     // 1.234 → 1234
        } else {
            normalizado = limpo;                                      // 12.50 ou 12
        }
        return new BigDecimal(normalizado).setScale(2, RoundingMode.HALF_UP);
    }
}
