package com.caliburger.servico;

import com.caliburger.modelo.Ingrediente;
import com.caliburger.repositorio.RepositorioArquivo;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** Regras de negócio do estoque de ingredientes. */
public final class EstoqueServico {

    private final RepositorioArquivo<Ingrediente> repositorio;
    private final List<Ingrediente> ingredientes;

    public EstoqueServico(RepositorioArquivo<Ingrediente> repositorio) {
        this.repositorio = repositorio;
        this.ingredientes = repositorio.carregar();
    }

    /** Ingredientes em ordem alfabética. */
    public List<Ingrediente> listar() {
        return ingredientes.stream()
                .sorted(Comparator.comparing(Ingrediente::getNome, ProdutoServico.COMPARADOR_TEXTO))
                .toList();
    }

    public List<Ingrediente> listarEstoqueBaixo() {
        return listar().stream().filter(Ingrediente::isEstoqueBaixo).toList();
    }

    /** Busca pelo nome, sem diferenciar maiúsculas e acentos. */
    public Optional<Ingrediente> buscar(String nome) {
        return ingredientes.stream()
                .filter(i -> ProdutoServico.COMPARADOR_TEXTO.compare(i.getNome(), nome == null ? "" : nome.trim()) == 0)
                .findFirst();
    }

    public Ingrediente cadastrar(String nome, int quantidade, int estoqueMinimo) {
        Ingrediente novo = new Ingrediente(nome, quantidade, estoqueMinimo);
        if (buscar(novo.getNome()).isPresent()) {
            throw new IllegalArgumentException("O ingrediente \"" + novo.getNome() + "\" já está cadastrado.");
        }
        ingredientes.add(novo);
        salvar();
        return novo;
    }

    public void darEntrada(Ingrediente ingrediente, int quantidade) {
        exigirExistente(ingrediente).darEntrada(quantidade);
        salvar();
    }

    public void darBaixa(Ingrediente ingrediente, int quantidade) {
        exigirExistente(ingrediente).darBaixa(quantidade);
        salvar();
    }

    /** Corrige a quantidade e o estoque mínimo (ex.: após uma contagem do estoque). */
    public void ajustar(Ingrediente ingrediente, int novaQuantidade, int novoMinimo) {
        Ingrediente existente = exigirExistente(ingrediente);
        new Ingrediente(existente.getNome(), novaQuantidade, novoMinimo); // valida antes de alterar
        existente.definirQuantidade(novaQuantidade);
        existente.definirEstoqueMinimo(novoMinimo);
        salvar();
    }

    public void excluir(Ingrediente ingrediente) {
        ingredientes.remove(exigirExistente(ingrediente));
        salvar();
    }

    private Ingrediente exigirExistente(Ingrediente ingrediente) {
        return ingredientes.stream()
                .filter(i -> i == ingrediente || i.getNome().equals(ingrediente.getNome()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Ingrediente não encontrado."));
    }

    private void salvar() {
        repositorio.salvar(ingredientes);
    }
}
