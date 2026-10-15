package com.caliburger.modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Regras de validação compartilhadas pelas classes de modelo. */
final class Validacao {

    static final int TAMANHO_MAXIMO_NOME = 60;

    private Validacao() {
    }

    static String textoObrigatorio(String texto, String campo) {
        String limpo = texto == null ? "" : texto.trim().replaceAll("\\s+", " ");
        if (limpo.isEmpty()) {
            throw new IllegalArgumentException(campo + " não pode ficar vazio.");
        }
        if (limpo.length() > TAMANHO_MAXIMO_NOME) {
            throw new IllegalArgumentException(
                    campo + " pode ter no máximo " + TAMANHO_MAXIMO_NOME + " caracteres.");
        }
        return limpo;
    }

    static BigDecimal valorPositivo(BigDecimal valor, String campo) {
        if (valor == null || valor.signum() <= 0) {
            throw new IllegalArgumentException(campo + " deve ser maior que zero.");
        }
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    static int quantidadeNaoNegativa(int quantidade, String campo) {
        if (quantidade < 0) {
            throw new IllegalArgumentException(campo + " não pode ser negativa.");
        }
        return quantidade;
    }
}
