package com.caliburger.servico;

import com.caliburger.modelo.Cardapio;
import com.caliburger.modelo.Produto;
import com.caliburger.servico.ProdutoServico.Ordenacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProdutoServicoTest {

    @TempDir
    Path pasta;

    private Servicos servicos;
    private ProdutoServico produtos;

    @BeforeEach
    void preparar() {
        servicos = Servicos.abrir(pasta);
        produtos = servicos.produtos();
    }

    private static BigDecimal reais(String valor) {
        return new BigDecimal(valor);
    }

    private static List<String> nomes(List<Produto> lista) {
        return lista.stream().map(Produto::getNome).toList();
    }

    @Test
    @DisplayName("adiciona produto limpando espaços extras do nome")
    void adiciona() {
        Produto p = produtos.adicionar("  X-Burger   Duplo ", reais("31.5"));
        assertEquals("X-Burger Duplo", p.getNome());
        assertEquals(reais("31.50"), p.getPreco());
        assertEquals(1, produtos.listar().size());
    }

    @Test
    @DisplayName("rejeita nome vazio e preço zero ou negativo")
    void validaDados() {
        assertThrows(IllegalArgumentException.class, () -> produtos.adicionar("  ", reais("10")));
        assertThrows(IllegalArgumentException.class, () -> produtos.adicionar("Suco", reais("0")));
        assertThrows(IllegalArgumentException.class, () -> produtos.adicionar("Suco", reais("-3")));
        assertTrue(produtos.listar().isEmpty());
    }

    @Test
    @DisplayName("não permite dois produtos com o mesmo nome (ignorando maiúsculas e acentos)")
    void nomeUnico() {
        produtos.adicionar("Água", reais("4"));
        assertThrows(IllegalArgumentException.class, () -> produtos.adicionar("agua", reais("5")));
    }

    @Test
    @DisplayName("editar altera somente o produto escolhido e não o deixa pela metade se for inválido")
    void edita() {
        Produto burger = produtos.adicionar("X-Burger", reais("25"));
        Produto salada = produtos.adicionar("X-Salada", reais("27"));

        produtos.editar(burger, "X-Bacon", reais("29.9"));
        assertEquals("X-Bacon", burger.getNome());
        assertEquals("X-Salada", salada.getNome());

        assertThrows(IllegalArgumentException.class, () -> produtos.editar(burger, "X-Salada", reais("1")));
        assertThrows(IllegalArgumentException.class, () -> produtos.editar(burger, "Outro nome", reais("0")));
        assertEquals("X-Bacon", burger.getNome());
        assertEquals(reais("29.90"), burger.getPreco());
    }

    @Test
    @DisplayName("os dados continuam lá ao abrir o sistema de novo")
    void persiste() {
        Produto burger = produtos.adicionar("X-Burger", reais("25"));
        produtos.editar(burger, "X-Burger Especial", reais("28"));

        Produto lido = Servicos.abrir(pasta).produtos().listar().get(0);
        assertEquals(burger.getId(), lido.getId());
        assertEquals("X-Burger Especial", lido.getNome());
        assertEquals(reais("28.00"), lido.getPreco());
    }

    @Test
    @DisplayName("pesquisa ignora maiúsculas e acentos")
    void pesquisa() {
        produtos.adicionar("Pão de Queijo", reais("6"));
        produtos.adicionar("X-Burger", reais("25"));
        assertEquals(List.of("Pão de Queijo"), nomes(produtos.pesquisar("PAO", Ordenacao.NOME)));
        assertEquals(2, produtos.pesquisar("  ", Ordenacao.NOME).size());
    }

    @Test
    @DisplayName("ordena por nome em ordem alfabética do português e por preço")
    void ordena() {
        produtos.adicionar("suco", reais("8"));
        produtos.adicionar("Água", reais("4"));
        produtos.adicionar("Batata", reais("12"));

        assertEquals(List.of("Água", "Batata", "suco"), nomes(produtos.pesquisar("", Ordenacao.NOME)));
        assertEquals(List.of("Água", "suco", "Batata"), nomes(produtos.pesquisar("", Ordenacao.MENOR_PRECO)));
        assertEquals(List.of("Batata", "suco", "Água"), nomes(produtos.pesquisar("", Ordenacao.MAIOR_PRECO)));
    }

    @Test
    @DisplayName("excluir um produto também o retira dos cardápios")
    void excluirRetiraDosCardapios() {
        Produto burger = produtos.adicionar("X-Burger", reais("25"));
        Produto suco = produtos.adicionar("Suco", reais("8"));
        Cardapio lanches = servicos.cardapios().criar("Lanches");
        servicos.cardapios().adicionarProduto(lanches, burger);
        servicos.cardapios().adicionarProduto(lanches, suco);

        produtos.excluir(burger);

        assertEquals(List.of(suco), servicos.cardapios().produtosDo(lanches));
        Servicos reaberto = Servicos.abrir(pasta);
        assertEquals(List.of("Suco"), nomes(reaberto.cardapios().produtosDo(reaberto.cardapios().listar().get(0))));
    }

    @Test
    @DisplayName("excluir todos limpa produtos e cardápios")
    void excluirTodos() {
        Produto burger = produtos.adicionar("X-Burger", reais("25"));
        Cardapio lanches = servicos.cardapios().criar("Lanches");
        servicos.cardapios().adicionarProduto(lanches, burger);

        produtos.excluirTodos();

        assertTrue(produtos.listar().isEmpty());
        assertTrue(servicos.cardapios().produtosDo(lanches).isEmpty());
    }
}
