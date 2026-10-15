package com.caliburger.servico;

import com.caliburger.modelo.Produto;
import com.caliburger.repositorio.RepositorioArquivo;

import java.math.BigDecimal;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/** Regras de negócio dos produtos: cadastro, edição, exclusão, busca e ordenação. */
public final class ProdutoServico {

    /** Compara textos ignorando maiúsculas e acentos ("Água" fica junto de "agua"). */
    static final Collator COMPARADOR_TEXTO = Collator.getInstance(Locale.of("pt", "BR"));

    static {
        COMPARADOR_TEXTO.setStrength(Collator.PRIMARY);
    }

    public enum Ordenacao {
        NOME("Nome (A-Z)", Comparator.comparing(Produto::getNome, COMPARADOR_TEXTO)),
        MENOR_PRECO("Menor preço", Comparator.comparing(Produto::getPreco)),
        MAIOR_PRECO("Maior preço", Comparator.comparing(Produto::getPreco).reversed());

        private final String descricao;
        private final Comparator<Produto> comparador;

        Ordenacao(String descricao, Comparator<Produto> comparador) {
            this.descricao = descricao;
            this.comparador = comparador.thenComparing(Produto::getNome, COMPARADOR_TEXTO);
        }

        @Override
        public String toString() {
            return descricao;
        }
    }

    private final RepositorioArquivo<Produto> repositorio;
    private final List<Produto> produtos;
    private CardapioServico cardapios;

    public ProdutoServico(RepositorioArquivo<Produto> repositorio) {
        this.repositorio = repositorio;
        this.produtos = repositorio.carregar();
    }

    /** Liga os serviços para que excluir um produto também o retire dos cardápios. */
    void conectarCardapios(CardapioServico cardapioServico) {
        this.cardapios = cardapioServico;
    }

    public List<Produto> listar() {
        return List.copyOf(produtos);
    }

    /** Lista filtrando pelo nome (sem diferenciar maiúsculas e acentos) e na ordem escolhida. */
    public List<Produto> pesquisar(String filtro, Ordenacao ordenacao) {
        String termo = normalizar(filtro);
        List<Produto> resultado = new ArrayList<>();
        for (Produto produto : produtos) {
            if (termo.isEmpty() || normalizar(produto.getNome()).contains(termo)) {
                resultado.add(produto);
            }
        }
        resultado.sort(ordenacao.comparador);
        return resultado;
    }

    public Optional<Produto> buscarPorId(UUID id) {
        return produtos.stream().filter(p -> p.getId().equals(id)).findFirst();
    }

    public Produto adicionar(String nome, BigDecimal preco) {
        Produto novo = new Produto(nome, preco);
        garantirNomeUnico(novo.getNome(), null);
        produtos.add(novo);
        salvar();
        return novo;
    }

    public void editar(Produto produto, String novoNome, BigDecimal novoPreco) {
        Produto existente = exigirExistente(produto);
        // Valida tudo antes de alterar, para não deixar o produto pela metade.
        Produto validado = new Produto(existente.getId(), novoNome, novoPreco);
        garantirNomeUnico(validado.getNome(), existente);
        existente.setNome(validado.getNome());
        existente.setPreco(validado.getPreco());
        salvar();
    }

    public void excluir(Produto produto) {
        Produto existente = exigirExistente(produto);
        produtos.remove(existente);
        salvar();
        if (cardapios != null) {
            cardapios.removerProdutoDeTodos(existente.getId());
        }
    }

    public void excluirTodos() {
        List<Produto> removidos = List.copyOf(produtos);
        produtos.clear();
        salvar();
        if (cardapios != null) {
            removidos.forEach(p -> cardapios.removerProdutoDeTodos(p.getId()));
        }
    }

    private Produto exigirExistente(Produto produto) {
        return buscarPorId(produto.getId())
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado."));
    }

    private void garantirNomeUnico(String nome, Produto ignorar) {
        boolean repetido = produtos.stream()
                .anyMatch(p -> !p.equals(ignorar) && COMPARADOR_TEXTO.compare(p.getNome(), nome) == 0);
        if (repetido) {
            throw new IllegalArgumentException("Já existe um produto chamado \"" + nome + "\".");
        }
    }

    private void salvar() {
        repositorio.salvar(produtos);
    }

    static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        String semAcentos = java.text.Normalizer.normalize(texto, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return semAcentos.toLowerCase(Locale.ROOT).trim();
    }
}
