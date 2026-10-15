package com.caliburger.servico;

import com.caliburger.modelo.Cardapio;
import com.caliburger.modelo.Produto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CardapioServicoTest {

    @TempDir
    Path pasta;

    private Servicos servicos;
    private CardapioServico cardapios;
    private Produto burger;

    @BeforeEach
    void preparar() {
        servicos = Servicos.abrir(pasta);
        cardapios = servicos.cardapios();
        burger = servicos.produtos().adicionar("X-Burger", new BigDecimal("25"));
    }

    @Test
    @DisplayName("cria, renomeia e exclui cardápios, sem nomes repetidos")
    void cicloDeVida() {
        Cardapio lanches = cardapios.criar("Lanches");
        assertThrows(IllegalArgumentException.class, () -> cardapios.criar("lanches"));
        assertThrows(IllegalArgumentException.class, () -> cardapios.criar(" "));

        Cardapio bebidas = cardapios.criar("Bebidas");
        assertThrows(IllegalArgumentException.class, () -> cardapios.renomear(bebidas, "Lanches"));
        cardapios.renomear(lanches, "Lanches da casa");
        assertEquals("Lanches da casa", lanches.getNome());

        cardapios.excluir(bebidas);
        assertEquals(List.of(lanches), cardapios.listar());
    }

    @Test
    @DisplayName("o mesmo produto não entra duas vezes no cardápio")
    void semDuplicados() {
        Cardapio lanches = cardapios.criar("Lanches");
        assertTrue(cardapios.adicionarProduto(lanches, burger));
        assertFalse(cardapios.adicionarProduto(lanches, burger));
        assertEquals(1, cardapios.produtosDo(lanches).size());
    }

    @Test
    @DisplayName("editar o produto atualiza o cardápio, inclusive depois de reabrir o sistema")
    void refleteEdicaoDoProduto() {
        Cardapio lanches = cardapios.criar("Lanches");
        cardapios.adicionarProduto(lanches, burger);

        servicos.produtos().editar(burger, "X-Burger Artesanal", new BigDecimal("32"));

        Servicos reaberto = Servicos.abrir(pasta);
        Produto noCardapio = reaberto.cardapios().produtosDo(reaberto.cardapios().listar().get(0)).get(0);
        assertEquals("X-Burger Artesanal", noCardapio.getNome());
        assertEquals(new BigDecimal("32.00"), noCardapio.getPreco());
    }

    @Test
    @DisplayName("remove produto do cardápio sem excluí-lo do cadastro")
    void removeProduto() {
        Cardapio lanches = cardapios.criar("Lanches");
        cardapios.adicionarProduto(lanches, burger);
        cardapios.removerProduto(lanches, burger);
        assertTrue(cardapios.produtosDo(lanches).isEmpty());
        assertEquals(1, servicos.produtos().listar().size());
    }
}
