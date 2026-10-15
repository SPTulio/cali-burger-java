package com.example.demo;

import java.io.Serializable;

public class Ingrediente implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nome;
    private int quantidade;

    public Ingrediente(String nome, int quantidade) {
        this.nome = nome;
        this.quantidade = quantidade;
    }

    public void adicionar(int qtd) {
        this.quantidade += qtd;
    }

    public void atualizar(int qtd) {
        this.quantidade = qtd;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public String getNome() {
        return nome;
    }

    public void deletar() {
        this.quantidade = 0;
    }

    @Override
    public String toString() {
        return nome + ": " + quantidade + " unidades";
    }
}
