package com.caliburger.servico;

import com.caliburger.modelo.ItemPedido;
import com.caliburger.modelo.Pedido;
import com.caliburger.modelo.StatusPedido;
import com.caliburger.repositorio.RepositorioArquivo;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Regras de negócio dos pedidos: criação, numeração, alteração e exclusão. */
public final class PedidoServico {

    private final RepositorioArquivo<Pedido> repositorio;
    private final List<Pedido> pedidos;
    private final Clock relogio;

    public PedidoServico(RepositorioArquivo<Pedido> repositorio) {
        this(repositorio, Clock.systemDefaultZone());
    }

    /** Permite informar o relógio, para os testes controlarem o horário. */
    public PedidoServico(RepositorioArquivo<Pedido> repositorio, Clock relogio) {
        this.repositorio = repositorio;
        this.relogio = relogio;
        this.pedidos = repositorio.carregar();
    }

    /** Pedidos do mais recente para o mais antigo. */
    public List<Pedido> listar() {
        return pedidos.stream()
                .sorted(Comparator.comparingInt(Pedido::getNumero).reversed())
                .toList();
    }

    public int proximoNumero() {
        return pedidos.stream().mapToInt(Pedido::getNumero).max().orElse(0) + 1;
    }

    public Pedido criar(List<ItemPedido> itens) {
        Pedido novo = new Pedido(proximoNumero(), agruparItens(itens), agora());
        pedidos.add(novo);
        salvar();
        return novo;
    }

    public void atualizar(Pedido pedido, List<ItemPedido> itens, StatusPedido status) {
        Pedido existente = exigirExistente(pedido);
        existente.atualizar(agruparItens(itens), status, agora());
        salvar();
    }

    public void excluir(Pedido pedido) {
        pedidos.remove(exigirExistente(pedido));
        salvar();
    }

    /** Junta itens repetidos do mesmo produto e preço: 1x + 2x X-Burger viram 3x X-Burger. */
    static List<ItemPedido> agruparItens(List<ItemPedido> itens) {
        if (itens == null) {
            return List.of();
        }
        Map<String, ItemPedido> agrupados = new LinkedHashMap<>();
        for (ItemPedido item : itens) {
            String chave = item.idProduto() + "|" + item.precoUnitario();
            agrupados.merge(chave, item,
                    (atual, novo) -> atual.comQuantidade(Math.addExact(atual.quantidade(), novo.quantidade())));
        }
        return new ArrayList<>(agrupados.values());
    }

    private Pedido exigirExistente(Pedido pedido) {
        return pedidos.stream()
                .filter(p -> p.equals(pedido))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado."));
    }

    private LocalDateTime agora() {
        return LocalDateTime.now(relogio).withNano(0);
    }

    private void salvar() {
        repositorio.salvar(pedidos);
    }
}
