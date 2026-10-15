package com.caliburger.servico;

import com.caliburger.modelo.ItemPedido;
import com.caliburger.modelo.Pedido;
import com.caliburger.modelo.Produto;
import com.caliburger.modelo.StatusPedido;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PedidoServicoTest {

    @TempDir
    Path pasta;

    private ProdutoServico produtos;
    private Produto burger;
    private Produto suco;

    @BeforeEach
    void preparar() {
        produtos = Servicos.abrir(pasta).produtos();
        burger = produtos.adicionar("X-Burger", new BigDecimal("25.90"));
        suco = produtos.adicionar("Suco", new BigDecimal("8.00"));
    }

    private PedidoServico servicoNoHorario(String dataHora) {
        Instant instante = LocalDateTime.parse(dataHora).toInstant(ZoneOffset.ofHours(-3));
        return new PedidoServico(
                new com.caliburger.repositorio.RepositorioArquivo<>(pasta.resolve("pedidos.dat"), Pedido.class),
                Clock.fixed(instante, ZoneId.of("America/Sao_Paulo")));
    }

    @Test
    @DisplayName("calcula o total com as quantidades")
    void total() {
        Pedido pedido = servicoNoHorario("2026-10-15T19:00:00")
                .criar(List.of(ItemPedido.de(burger, 2), ItemPedido.de(suco, 1)));
        assertEquals(new BigDecimal("59.80"), pedido.getValorTotal());
        assertEquals(3, pedido.getQuantidadeDeItens());
        assertEquals("2x X-Burger, 1x Suco", pedido.getResumoItens());
    }

    @Test
    @DisplayName("junta itens repetidos do mesmo produto")
    void agrupa() {
        Pedido pedido = servicoNoHorario("2026-10-15T19:00:00")
                .criar(List.of(ItemPedido.de(burger, 1), ItemPedido.de(suco, 1), ItemPedido.de(burger, 2)));
        assertEquals(List.of("3x X-Burger", "1x Suco"),
                pedido.getItens().stream().map(i -> i.quantidade() + "x " + i.nomeProduto()).toList());
    }

    @Test
    @DisplayName("numera em sequência, continuando de onde parou ao reabrir o sistema")
    void numeracao() {
        PedidoServico servico = servicoNoHorario("2026-10-15T19:00:00");
        assertEquals(1, servico.criar(List.of(ItemPedido.de(suco, 1))).getNumero());
        Pedido segundo = servico.criar(List.of(ItemPedido.de(suco, 1)));
        servico.criar(List.of(ItemPedido.de(suco, 1)));
        servico.excluir(segundo);

        PedidoServico reaberto = servicoNoHorario("2026-10-16T10:00:00");
        assertEquals(4, reaberto.criar(List.of(ItemPedido.de(burger, 1))).getNumero());
        assertEquals(List.of(4, 3, 1), reaberto.listar().stream().map(Pedido::getNumero).toList());
    }

    @Test
    @DisplayName("não aceita pedido sem itens ou com quantidade inválida")
    void validacoes() {
        PedidoServico servico = servicoNoHorario("2026-10-15T19:00:00");
        assertThrows(IllegalArgumentException.class, () -> servico.criar(List.of()));
        assertThrows(IllegalArgumentException.class, () -> ItemPedido.de(burger, 0));
    }

    @Test
    @DisplayName("mudar o preço do produto não altera pedidos já feitos")
    void precoHistorico() {
        PedidoServico servico = servicoNoHorario("2026-10-15T19:00:00");
        Pedido pedido = servico.criar(List.of(ItemPedido.de(burger, 1)));

        produtos.editar(burger, "X-Burger", new BigDecimal("30.00"));

        assertEquals(new BigDecimal("25.90"), pedido.getValorTotal());
        Pedido reaberto = servicoNoHorario("2026-10-15T20:00:00").listar().get(0);
        assertEquals(new BigDecimal("25.90"), reaberto.getValorTotal());
    }

    @Test
    @DisplayName("editar mantém o horário em que o pedido foi feito e registra a alteração")
    void edicaoMantemHorario() {
        Pedido pedido = servicoNoHorario("2026-10-15T19:00:00").criar(List.of(ItemPedido.de(burger, 1)));

        PedidoServico depois = servicoNoHorario("2026-10-15T19:25:00");
        Pedido carregado = depois.listar().get(0);
        depois.atualizar(carregado, List.of(ItemPedido.de(burger, 1), ItemPedido.de(suco, 2)), StatusPedido.PRONTO);

        assertEquals(LocalDateTime.parse("2026-10-15T19:00:00"), carregado.getCriadoEm());
        assertEquals(LocalDateTime.parse("2026-10-15T19:25:00"), carregado.getAtualizadoEm());
        assertEquals(StatusPedido.PRONTO, carregado.getStatus());
        assertEquals(pedido.getNumero(), carregado.getNumero());
        assertEquals(new BigDecimal("41.90"), carregado.getValorTotal());
    }
}
