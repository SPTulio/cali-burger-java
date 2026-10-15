package com.caliburger.servico;

import com.caliburger.modelo.Ingrediente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EstoqueServicoTest {

    @TempDir
    Path pasta;

    private EstoqueServico estoque;

    @BeforeEach
    void preparar() {
        estoque = Servicos.abrir(pasta).estoque();
    }

    @Test
    @DisplayName("cadastra sem repetir nomes e lista em ordem alfabética")
    void cadastra() {
        estoque.cadastrar("Queijo", 30, 10);
        estoque.cadastrar("Alface", 5, 2);
        assertThrows(IllegalArgumentException.class, () -> estoque.cadastrar("queijo", 1, 0));
        assertEquals(List.of("Alface", "Queijo"), estoque.listar().stream().map(Ingrediente::getNome).toList());
    }

    @Test
    @DisplayName("entrada soma, baixa subtrai e o estoque nunca fica negativo")
    void movimenta() {
        Ingrediente pao = estoque.cadastrar("Pão", 10, 5);
        estoque.darEntrada(pao, 15);
        estoque.darBaixa(pao, 20);
        assertEquals(5, pao.getQuantidade());

        assertThrows(IllegalArgumentException.class, () -> estoque.darBaixa(pao, 6));
        assertThrows(IllegalArgumentException.class, () -> estoque.darEntrada(pao, -3));
        assertThrows(IllegalArgumentException.class, () -> estoque.cadastrar("Tomate", -1, 0));
        assertEquals(5, pao.getQuantidade());
    }

    @Test
    @DisplayName("aponta itens no estoque mínimo ou abaixo dele")
    void estoqueBaixo() {
        Ingrediente pao = estoque.cadastrar("Pão", 5, 5);
        estoque.cadastrar("Queijo", 30, 10);
        assertEquals(List.of(pao), estoque.listarEstoqueBaixo());

        estoque.darEntrada(pao, 1);
        assertTrue(estoque.listarEstoqueBaixo().isEmpty());
    }

    @Test
    @DisplayName("ajuste inválido não altera o ingrediente")
    void ajuste() {
        Ingrediente pao = estoque.cadastrar("Pão", 10, 5);
        estoque.ajustar(pao, 40, 8);
        assertThrows(IllegalArgumentException.class, () -> estoque.ajustar(pao, 50, -1));
        assertEquals(40, pao.getQuantidade());
        assertEquals(8, pao.getEstoqueMinimo());
    }

    @Test
    @DisplayName("salva as alterações em disco")
    void persiste() {
        Ingrediente pao = estoque.cadastrar("Pão", 10, 5);
        estoque.darBaixa(pao, 3);
        Ingrediente queijo = estoque.cadastrar("Queijo", 1, 0);
        estoque.excluir(queijo);

        EstoqueServico reaberto = Servicos.abrir(pasta).estoque();
        assertEquals(1, reaberto.listar().size());
        assertEquals(7, reaberto.buscar("pão").orElseThrow().getQuantidade());
        assertFalse(reaberto.buscar("Queijo").isPresent());
    }
}
