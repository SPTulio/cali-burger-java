package com.caliburger.modelo;

import java.io.Serial;
import java.io.Serializable;

/** Item do estoque (ex.: "Pão de hambúrguer", 120 unidades). */
public final class Ingrediente implements Serializable {

    @Serial
    private static final long serialVersionUID = 2L;

    private final String nome;
    private int quantidade;
    private int estoqueMinimo;

    public Ingrediente(String nome, int quantidade, int estoqueMinimo) {
        this.nome = Validacao.textoObrigatorio(nome, "O nome do ingrediente");
        definirQuantidade(quantidade);
        definirEstoqueMinimo(estoqueMinimo);
    }

    public String getNome() {
        return nome;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public int getEstoqueMinimo() {
        return estoqueMinimo;
    }

    public void definirQuantidade(int novaQuantidade) {
        this.quantidade = Validacao.quantidadeNaoNegativa(novaQuantidade, "A quantidade");
    }

    public void definirEstoqueMinimo(int minimo) {
        this.estoqueMinimo = Validacao.quantidadeNaoNegativa(minimo, "O estoque mínimo");
    }

    /** Soma uma entrada de mercadoria ao estoque. */
    public void darEntrada(int quantidadeRecebida) {
        if (quantidadeRecebida <= 0) {
            throw new IllegalArgumentException("A quantidade recebida deve ser maior que zero.");
        }
        this.quantidade = Math.addExact(quantidade, quantidadeRecebida);
    }

    /** Retira itens do estoque (ex.: uso na cozinha ou perda). */
    public void darBaixa(int quantidadeUsada) {
        if (quantidadeUsada <= 0) {
            throw new IllegalArgumentException("A quantidade retirada deve ser maior que zero.");
        }
        if (quantidadeUsada > quantidade) {
            throw new IllegalArgumentException(
                    "Estoque insuficiente de " + nome + ": há apenas " + quantidade + " unidade(s).");
        }
        this.quantidade -= quantidadeUsada;
    }

    public boolean isEstoqueBaixo() {
        return quantidade <= estoqueMinimo;
    }

    @Override
    public String toString() {
        return nome + ": " + quantidade + " unidade(s)";
    }
}
