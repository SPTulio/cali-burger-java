package com.caliburger.repositorio;

import com.caliburger.modelo.Produto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RepositorioArquivoTest {

    @TempDir
    Path pasta;

    @Test
    @DisplayName("sem arquivo, começa com a lista vazia")
    void semArquivo() {
        var repositorio = new RepositorioArquivo<>(pasta.resolve("produtos.dat"), Produto.class);
        assertTrue(repositorio.carregar().isEmpty());
    }

    @Test
    @DisplayName("salva e carrega os mesmos dados, criando a pasta se preciso")
    void idaEVolta() {
        var arquivo = pasta.resolve("subpasta").resolve("produtos.dat");
        var repositorio = new RepositorioArquivo<>(arquivo, Produto.class);
        Produto burger = new Produto("X-Burger", new BigDecimal("25.90"));

        repositorio.salvar(List.of(burger));
        List<Produto> lidos = new RepositorioArquivo<>(arquivo, Produto.class).carregar();

        assertEquals(1, lidos.size());
        assertEquals(burger.getId(), lidos.get(0).getId());
        assertEquals("X-Burger", lidos.get(0).getNome());
        assertEquals(new BigDecimal("25.90"), lidos.get(0).getPreco());
    }

    @Test
    @DisplayName("não deixa arquivos temporários para trás")
    void semTemporarios() throws IOException {
        var repositorio = new RepositorioArquivo<>(pasta.resolve("produtos.dat"), Produto.class);
        repositorio.salvar(List.of(new Produto("Suco", BigDecimal.TEN)));
        repositorio.salvar(List.of());
        try (Stream<Path> arquivos = Files.list(pasta)) {
            assertEquals(List.of(pasta.resolve("produtos.dat")), arquivos.toList());
        }
    }

    @Test
    @DisplayName("arquivo corrompido é preservado com outro nome e o sistema segue funcionando")
    void arquivoCorrompido() throws IOException {
        Path arquivo = pasta.resolve("produtos.dat");
        Files.writeString(arquivo, "isto não é um arquivo válido");
        var repositorio = new RepositorioArquivo<>(arquivo, Produto.class);

        assertTrue(repositorio.carregar().isEmpty());
        assertFalse(Files.exists(arquivo));
        try (Stream<Path> arquivos = Files.list(pasta)) {
            assertTrue(arquivos.anyMatch(p -> p.getFileName().toString().startsWith("produtos.dat.corrompido-")));
        }
    }
}
