package com.caliburger.modelo;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Agrupamento de produtos (ex.: "Lanches", "Bebidas").
 * Guarda apenas os ids dos produtos: se um produto for renomeado ou tiver o
 * preço alterado, o cardápio mostra automaticamente a versão atual.
 */
public final class Cardapio implements Serializable {

    @Serial
    private static final long serialVersionUID = 2L;

    private final UUID id;
    private String nome;
    private final ArrayList<UUID> idsProdutos = new ArrayList<>();

    public Cardapio(String nome) {
        this.id = UUID.randomUUID();
        setNome(nome);
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = Validacao.textoObrigatorio(nome, "O nome do cardápio");
    }

    public List<UUID> getIdsProdutos() {
        return Collections.unmodifiableList(idsProdutos);
    }

    public boolean contem(UUID idProduto) {
        return idsProdutos.contains(idProduto);
    }

    /** Adiciona o produto. Retorna false se ele já estava no cardápio. */
    public boolean adicionarProduto(UUID idProduto) {
        Objects.requireNonNull(idProduto, "idProduto");
        if (contem(idProduto)) {
            return false;
        }
        return idsProdutos.add(idProduto);
    }

    public boolean removerProduto(UUID idProduto) {
        return idsProdutos.remove(idProduto);
    }

    @Override
    public boolean equals(Object outro) {
        return this == outro || (outro instanceof Cardapio c && id.equals(c.id));
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return nome;
    }
}
