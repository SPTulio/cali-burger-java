package com.example.demo;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.*;
import java.util.ArrayList;

public class ArquivoCardapio {
    private static final String CAMINHO_ARQUIVO = "cardapios.dat";

    public static void salvarLista(ArrayList<Cardapio> cardapios) {
        try {
            File arq = new File(CAMINHO_ARQUIVO);
            if (!arq.exists()) {
                arq.createNewFile();
            }

            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(arq));
            oos.writeObject(cardapios);
            oos.close();
            System.out.println("Lista de cardápios salva com sucesso.");
        } catch (IOException e) {
            System.err.println("Erro ao salvar lista: " + e.getMessage());
        }
    }

    public static ArrayList<Cardapio> lerLista() {
        ArrayList<Cardapio> lista = new ArrayList<>();

        try {
            File arq = new File(CAMINHO_ARQUIVO);
            if (arq.exists()) {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(arq));
                lista = (ArrayList<Cardapio>) ois.readObject();
                ois.close();
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erro ao ler lista: " + e.getMessage());
        }

        return lista;
    }


    public static void adicionarCardapio(Cardapio novo) {
        ArrayList<Cardapio> cardapios = lerLista();
        cardapios.add(novo);
        salvarLista(cardapios);
    }
}
