package com.example.demo;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

class Pedido implements Serializable {
    private static final long serialVersionUID = 1L;

    private ArrayList<Produto> produtos = new ArrayList<>();
    private int numPedido;
    private String status = "Em preparação";
    private String dataHora = "";

    public void pedirProdutos(Produto p) {
        produtos.add(p);
    }

    public ArrayList<Produto> getProdutos() {
        return produtos;
    }

    public void setProdutos(ArrayList<Produto> produtos) {
        this.produtos = produtos;
    }

    public double calcularValorTotal() {
        double total = 0;
        for (Produto p : produtos) {
            total += p.getValor();
        }
        return total;
    }

    public String getProdutosComoTexto() {
        String texto = "";
        for (Produto p : produtos) {
            texto += p.getNome() + " (R$ " + String.format("%.2f", p.getValor()) + "), ";
        }
        //tira a virgula do final
        if (texto.length() > 2) {
            texto = texto.substring(0, texto.length() - 2);
        }
        return texto;
    }

    public int getNumPedido() {
        return numPedido;
    }

    public void setNumPedido(int numPedido) {
        this.numPedido = numPedido;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String registrarDataHora() {
        dataHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        return dataHora;
    }

    public String getDataHora() {
        return dataHora;
    }

    public double getValorTotal() {
        return calcularValorTotal();
    }
}

