package com.example.demo;


import java.io.*;
import java.util.ArrayList;
import java.util.List;

class ArquivoPedido {
    //criar pasta .dat
    private static final String PASTA = ".dat";
    private static final String ARQUIVO = PASTA + File.separator + "pedidos.dat";

    public static void salvarPedidos(List<Pedido> pedidos) {
        File pasta = new File(PASTA);
        // criar a pasta se nao existir
        if (!pasta.exists()) {
            pasta.mkdir();
        }

        // troca de lista para arraylist pra pode r serializar
        ArrayList<Pedido> listaSerializavel = new ArrayList<>(pedidos);

        //
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ARQUIVO))) {
            oos.writeObject(listaSerializavel); //grava em ARQUIVO
            System.out.println("Pedidos salvos em: " + ARQUIVO);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erro ao salvar pedidos.");
        }
    }

    // carregar lista de pedidos
    public static List<Pedido> carregarPedidos() {
        File file = new File(ARQUIVO);
        //retorna uma lista vazia se nao tiver arquivo para carregar
        if (!file.exists()) {
            System.out.println("Arquivo de pedidos não encontrado: " + ARQUIVO);
            return new ArrayList<>();
        }

        //tenta ler o arquivo
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            List<Pedido> pedidos = (List<Pedido>) ois.readObject(); //converte o obj serializado para lista
            System.out.println("Pedidos carregados de: " + ARQUIVO);
            return pedidos;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace(); //mostra onde foi o erro
            System.err.println("Erro ao carregar pedidos.");
            return new ArrayList<>(); //retorna lista vazia
        }
    }
}