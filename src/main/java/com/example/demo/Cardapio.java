package com.example.demo;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.util.ArrayList;

public class Cardapio implements Serializable {
    private String nome;
    private transient ObservableList<Produto> itens;
    private ArrayList<Produto> itensSerializados;

    public Cardapio(String nome) {
        this.nome = nome;
        this.itens = FXCollections.observableArrayList();
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public ObservableList<Produto> getItens() {
        if (itens == null) {
            itens = FXCollections.observableArrayList();
        }
        return itens;
    }

    public void adicionarProduto(Produto p) {
        getItens().add(p);
    }

    public void removerProduto(Produto p) {
        getItens().remove(p);
    }

    @Override
    public String toString() {
        return nome;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        itensSerializados = new ArrayList<>(getItens());
        out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        itens = FXCollections.observableArrayList();
        if (itensSerializados != null) {
            itens.addAll(itensSerializados);
        }
    }
}
