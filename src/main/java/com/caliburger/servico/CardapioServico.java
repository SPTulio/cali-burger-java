package com.caliburger.servico;

import com.caliburger.modelo.Cardapio;
import com.caliburger.modelo.Produto;
import com.caliburger.repositorio.RepositorioArquivo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Regras de negócio dos cardápios. */
public final class CardapioServico {

    private final RepositorioArquivo<Cardapio> repositorio;
    private final ProdutoServico produtos;
    private final List<Cardapio> cardapios;

    public CardapioServico(RepositorioArquivo<Cardapio> repositorio, ProdutoServico produtos) {
        this.repositorio = repositorio;
        this.produtos = produtos;
        this.cardapios = repositorio.carregar();
        produtos.conectarCardapios(this);
        removerReferenciasInvalidas();
    }

    public List<Cardapio> listar() {
        return List.copyOf(cardapios);
    }

    public Cardapio criar(String nome) {
        Cardapio novo = new Cardapio(nome);
        garantirNomeUnico(novo.getNome(), null);
        cardapios.add(novo);
        salvar();
        return novo;
    }

    public void renomear(Cardapio cardapio, String novoNome) {
        Cardapio existente = exigirExistente(cardapio);
        String nomeValidado = new Cardapio(novoNome).getNome();
        garantirNomeUnico(nomeValidado, existente);
        existente.setNome(nomeValidado);
        salvar();
    }

    public void excluir(Cardapio cardapio) {
        cardapios.remove(exigirExistente(cardapio));
        salvar();
    }

    /** Retorna os produtos do cardápio na versão atual (nome e preço atualizados). */
    public List<Produto> produtosDo(Cardapio cardapio) {
        return exigirExistente(cardapio).getIdsProdutos().stream()
                .map(produtos::buscarPorId)
                .flatMap(Optional::stream)
                .toList();
    }

    /** Retorna false se o produto já estava no cardápio. */
    public boolean adicionarProduto(Cardapio cardapio, Produto produto) {
        Cardapio existente = exigirExistente(cardapio);
        if (produtos.buscarPorId(produto.getId()).isEmpty()) {
            throw new IllegalArgumentException("Produto não encontrado.");
        }
        boolean adicionou = existente.adicionarProduto(produto.getId());
        if (adicionou) {
            salvar();
        }
        return adicionou;
    }

    public void removerProduto(Cardapio cardapio, Produto produto) {
        if (exigirExistente(cardapio).removerProduto(produto.getId())) {
            salvar();
        }
    }

    void removerProdutoDeTodos(UUID idProduto) {
        boolean alterou = false;
        for (Cardapio cardapio : cardapios) {
            alterou |= cardapio.removerProduto(idProduto);
        }
        if (alterou) {
            salvar();
        }
    }

    /** Limpa ids de produtos que não existem mais (ex.: dados antigos). */
    private void removerReferenciasInvalidas() {
        boolean alterou = false;
        for (Cardapio cardapio : cardapios) {
            for (UUID id : List.copyOf(cardapio.getIdsProdutos())) {
                if (produtos.buscarPorId(id).isEmpty()) {
                    alterou |= cardapio.removerProduto(id);
                }
            }
        }
        if (alterou) {
            salvar();
        }
    }

    private Cardapio exigirExistente(Cardapio cardapio) {
        return cardapios.stream()
                .filter(c -> c.equals(cardapio))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Cardápio não encontrado."));
    }

    private void garantirNomeUnico(String nome, Cardapio ignorar) {
        boolean repetido = cardapios.stream()
                .anyMatch(c -> !c.equals(ignorar) && ProdutoServico.COMPARADOR_TEXTO.compare(c.getNome(), nome) == 0);
        if (repetido) {
            throw new IllegalArgumentException("Já existe um cardápio chamado \"" + nome + "\".");
        }
    }

    private void salvar() {
        repositorio.salvar(cardapios);
    }
}
