package com.example.demo;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;

public class ArquivoProduto {
    private static final String CAMINHO_ARQUIVO = "produtos.dat";
    private final ArrayList<Produto> produtos;

    public ArquivoProduto() {
        produtos = lerLista();
    }

    public ArrayList<Produto> listarProdutos() {
        return produtos;
    }

    public void adicionarProduto(Produto produto) {
        produtos.add(produto);
        salvarLista();
    }

    public boolean editarProduto(String nomeAntigo, String novoNome, double novoValor) {
        Produto p = buscarProdutoPorNome(nomeAntigo);
        if (p != null) {
            p.setNome(novoNome);
            p.setValor(novoValor);
            salvarLista();
            return true;
        }
        return false;
    }

    public boolean excluirProduto(String nome) {
        Produto p = buscarProdutoPorNome(nome);
        if (p != null) {
            produtos.remove(p);
            salvarLista();
            return true;
        }
        return false;
    }

    public Produto buscarProdutoPorNome(String nome) {
        for (Produto p : produtos) {
            if (p.getNome().equalsIgnoreCase(nome)) return p;
        }
        return null;
    }

    public void ordenarPorNome() {
        produtos.sort(Comparator.comparing(Produto::getNome));
        salvarLista();
    }

    public void ordenarPorValor() {
        produtos.sort(Comparator.comparingDouble(Produto::getValor));
        salvarLista();
    }

    public void limparProdutos() {
        produtos.clear();
        salvarLista();
    }

    private void salvarLista() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(CAMINHO_ARQUIVO))) {
            oos.writeObject(produtos);
            System.out.println("Lista de produtos salva com sucesso.");
        } catch (IOException e) {
            System.err.println("Erro ao salvar lista: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private ArrayList<Produto> lerLista() {
        ArrayList<Produto> lista = new ArrayList<>();
        try {
            File arq = new File(CAMINHO_ARQUIVO);
            if (arq.exists()) {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(CAMINHO_ARQUIVO));
                lista = (ArrayList<Produto>) ois.readObject();
                ois.close();
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erro ao ler lista: " + e.getMessage());
        }
        return lista;
    }
}
