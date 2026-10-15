package com.caliburger.repositorio;

import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Guarda uma lista de objetos em um arquivo binário (serialização Java).
 * Uma única classe genérica atende produtos, cardápios, pedidos e estoque.
 *
 * <p>A gravação é feita em um arquivo temporário que depois substitui o
 * original, então uma queda de energia no meio da gravação não corrompe os
 * dados. Se o arquivo existente estiver corrompido, ele é renomeado (para não
 * ser sobrescrito) e o sistema começa com uma lista vazia.</p>
 *
 * @param <T> tipo dos objetos guardados
 */
public class RepositorioArquivo<T extends Serializable> {

    private static final Logger LOG = Logger.getLogger(RepositorioArquivo.class.getName());

    private final Path arquivo;
    private final Class<T> tipo;

    public RepositorioArquivo(Path arquivo, Class<T> tipo) {
        this.arquivo = arquivo;
        this.tipo = tipo;
    }

    public Path getArquivo() {
        return arquivo;
    }

    public List<T> carregar() {
        if (!Files.exists(arquivo)) {
            return new ArrayList<>();
        }
        try (InputStream entrada = Files.newInputStream(arquivo);
             ObjectInputStream objetos = new ObjectInputStream(entrada)) {
            Object lido = objetos.readObject();
            if (!(lido instanceof List<?> lista)) {
                throw new InvalidClassException("Conteúdo inesperado em " + arquivo);
            }
            List<T> resultado = new ArrayList<>(lista.size());
            for (Object item : lista) {
                resultado.add(tipo.cast(item));
            }
            return resultado;
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            preservarArquivoCorrompido(e);
            return new ArrayList<>();
        }
    }

    public void salvar(List<T> itens) {
        try {
            Path pasta = arquivo.toAbsolutePath().getParent();
            Files.createDirectories(pasta);
            Path temporario = Files.createTempFile(pasta, arquivo.getFileName().toString(), ".tmp");
            try (OutputStream saida = Files.newOutputStream(temporario);
                 ObjectOutputStream objetos = new ObjectOutputStream(saida)) {
                objetos.writeObject(new ArrayList<>(itens));
            }
            substituir(temporario, arquivo);
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível salvar " + arquivo.getFileName(), e);
        }
    }

    private static void substituir(Path origem, Path destino) throws IOException {
        try {
            Files.move(origem, destino, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(origem, destino, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void preservarArquivoCorrompido(Exception causa) {
        String sufixo = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        Path copia = arquivo.resolveSibling(arquivo.getFileName() + ".corrompido-" + sufixo);
        try {
            Files.move(arquivo, copia, StandardCopyOption.REPLACE_EXISTING);
            LOG.log(Level.WARNING, "Arquivo " + arquivo + " ilegível; guardado como " + copia, causa);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Arquivo " + arquivo + " ilegível e não pôde ser renomeado", e);
        }
    }
}
