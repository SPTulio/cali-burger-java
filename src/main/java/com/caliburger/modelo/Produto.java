package com.caliburger.modelo;

import com.caliburger.util.Moeda;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Item vendido pela lanchonete (ex.: "X-Burger", R$ 25,90).
 * Cada produto tem um identificador único, então pode ser renomeado sem
 * perder as referências nos cardápios.
 */
public final class Produto implements Serializable {

    @Serial
    private static final long serialVersionUID = 2L;

    private final UUID id;
    private String nome;
    private BigDecimal preco;

    public Produto(String nome, BigDecimal preco) {
        this(UUID.randomUUID(), nome, preco);
    }

    public Produto(UUID id, String nome, BigDecimal preco) {
        this.id = Objects.requireNonNull(id, "id");
        setNome(nome);
        setPreco(preco);
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = Validacao.textoObrigatorio(nome, "O nome do produto");
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = Validacao.valorPositivo(preco, "O preço");
    }

    /** Produtos são iguais quando têm o mesmo id, mesmo que o nome mude. */
    @Override
    public boolean equals(Object outro) {
        return this == outro || (outro instanceof Produto p && id.equals(p.id));
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return nome + " — " + Moeda.formatar(preco);
    }
}
