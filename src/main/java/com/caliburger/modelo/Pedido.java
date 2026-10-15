package com.caliburger.modelo;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/** Pedido feito por um cliente, com seus itens, status e horários. */
public final class Pedido implements Serializable {

    @Serial
    private static final long serialVersionUID = 2L;

    private final int numero;
    private final LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
    private StatusPedido status = StatusPedido.EM_PREPARACAO;
    private final ArrayList<ItemPedido> itens = new ArrayList<>();

    public Pedido(int numero, List<ItemPedido> itens, LocalDateTime criadoEm) {
        if (numero <= 0) {
            throw new IllegalArgumentException("O número do pedido deve ser positivo.");
        }
        this.numero = numero;
        this.criadoEm = Objects.requireNonNull(criadoEm, "criadoEm");
        this.atualizadoEm = criadoEm;
        definirItens(itens);
    }

    public int getNumero() {
        return numero;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public List<ItemPedido> getItens() {
        return Collections.unmodifiableList(itens);
    }

    public BigDecimal getValorTotal() {
        return itens.stream()
                .map(ItemPedido::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getQuantidadeDeItens() {
        return itens.stream().mapToInt(ItemPedido::quantidade).sum();
    }

    /** Ex.: "2x X-Burger, 1x Refrigerante". */
    public String getResumoItens() {
        return itens.stream()
                .map(item -> item.quantidade() + "x " + item.nomeProduto())
                .collect(Collectors.joining(", "));
    }

    /** Substitui itens e status, registrando o horário da alteração (o horário de criação é mantido). */
    public void atualizar(List<ItemPedido> novosItens, StatusPedido novoStatus, LocalDateTime quando) {
        definirItens(novosItens);
        this.status = Objects.requireNonNull(novoStatus, "status");
        this.atualizadoEm = Objects.requireNonNull(quando, "quando");
    }

    private void definirItens(List<ItemPedido> novosItens) {
        if (novosItens == null || novosItens.isEmpty()) {
            throw new IllegalArgumentException("O pedido deve ter pelo menos um produto.");
        }
        itens.clear();
        itens.addAll(novosItens);
    }

    @Override
    public boolean equals(Object outro) {
        return this == outro || (outro instanceof Pedido p && numero == p.numero);
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(numero);
    }

    @Override
    public String toString() {
        return "Pedido nº " + numero;
    }
}
