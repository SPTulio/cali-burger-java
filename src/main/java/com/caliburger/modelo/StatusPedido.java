package com.caliburger.modelo;

/** Etapas pelas quais um pedido passa. */
public enum StatusPedido {
    EM_PREPARACAO("Em preparação"),
    PRONTO("Pronto"),
    ENTREGUE("Entregue"),
    CANCELADO("Cancelado");

    private final String descricao;

    StatusPedido(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    /** Pedidos entregues ou cancelados não mudam mais. */
    public boolean isFinalizado() {
        return this == ENTREGUE || this == CANCELADO;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
