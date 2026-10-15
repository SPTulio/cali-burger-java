package com.caliburger.servico;

import com.caliburger.modelo.Cardapio;
import com.caliburger.modelo.Ingrediente;
import com.caliburger.modelo.Pedido;
import com.caliburger.modelo.Produto;
import com.caliburger.repositorio.RepositorioArquivo;

import java.nio.file.Path;

/** Cria e reúne todos os serviços do sistema, com os dados em uma única pasta. */
public record Servicos(
        ProdutoServico produtos,
        CardapioServico cardapios,
        PedidoServico pedidos,
        EstoqueServico estoque,
        Path pastaDados) {

    /**
     * Pasta padrão dos dados: a definida em -Dcaliburger.dados, ou "dados"
     * dentro da pasta em que o programa foi aberto.
     */
    public static Path pastaPadrao() {
        return Path.of(System.getProperty("caliburger.dados", "dados"));
    }

    public static Servicos abrir(Path pastaDados) {
        ProdutoServico produtos = new ProdutoServico(
                new RepositorioArquivo<>(pastaDados.resolve("produtos.dat"), Produto.class));
        CardapioServico cardapios = new CardapioServico(
                new RepositorioArquivo<>(pastaDados.resolve("cardapios.dat"), Cardapio.class), produtos);
        PedidoServico pedidos = new PedidoServico(
                new RepositorioArquivo<>(pastaDados.resolve("pedidos.dat"), Pedido.class));
        EstoqueServico estoque = new EstoqueServico(
                new RepositorioArquivo<>(pastaDados.resolve("ingredientes.dat"), Ingrediente.class));
        return new Servicos(produtos, cardapios, pedidos, estoque, pastaDados);
    }
}
