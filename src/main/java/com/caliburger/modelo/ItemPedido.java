package com.caliburger.modelo;

import com.caliburger.util.Moeda;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Linha de um pedido. Guarda nome e preço do produto no momento da venda,
 * para que alterar o cardápio depois não mude o valor de pedidos antigos.
 */
public record ItemPedido(UUID idProduto, String nomeProduto, BigDecimal precoUnitario, int quantidade)
        implements Serializable {

    public ItemPedido {
        Objects.requireNonNull(idProduto, "idProduto");
        nomeProduto = Validacao.textoObrigatorio(nomeProduto, "O nome do produto");
        precoUnitario = Validacao.valorPositivo(precoUnitario, "O preço");
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }
    }

    public static ItemPedido de(Produto produto, int quantidade) {
        return new ItemPedido(produto.getId(), produto.getNome(), produto.getPreco(), quantidade);
    }

    public BigDecimal getSubtotal() {
        return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }

    public ItemPedido comQuantidade(int novaQuantidade) {
        return new ItemPedido(idProduto, nomeProduto, precoUnitario, novaQuantidade);
    }

    @Override
    public String toString() {
        return quantidade + "x " + nomeProduto + " (" + Moeda.formatar(precoUnitario) + ")";
    }
}
