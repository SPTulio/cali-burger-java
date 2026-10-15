package com.example.demo;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ArquivoIngrediente {
    private static final String NOME_ARQUIVO = "ingredientes.dat";

    public static void salvar(Map<String, Ingrediente> ingredientes) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(NOME_ARQUIVO))) {
            oos.writeObject(ingredientes);
            System.out.println("Ingredientes salvos.");
        } catch (IOException e) {
            System.out.println("Erro ao salvar: " + e.getMessage());
        }
    }

    public static void carregar(Map<String, Ingrediente> ingredientes) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(NOME_ARQUIVO))) {
            Map<String, Ingrediente> ingredientesLidos = (Map<String, Ingrediente>) ois.readObject();
            ingredientes.clear();
            ingredientes.putAll(ingredientesLidos);
            System.out.println("Ingredientes carregados do binário.");
        } catch (FileNotFoundException e) {
            System.out.println("Arquivo não encontrado. Criando lista nova.");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Erro ao carregar: " + e.getMessage());
        }
    }
}
