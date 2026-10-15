package com.caliburger.ui;

import com.caliburger.App;
import com.caliburger.modelo.Cardapio;
import com.caliburger.modelo.ItemPedido;
import com.caliburger.modelo.Pedido;
import com.caliburger.modelo.Produto;
import com.caliburger.modelo.StatusPedido;
import com.caliburger.repositorio.RepositorioArquivo;
import com.caliburger.servico.PedidoServico;
import com.caliburger.servico.ProdutoServico;
import com.caliburger.servico.Servicos;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * Abre a interface com dados de exemplo e salva uma captura de cada aba.
 * Usado no GitHub Actions (com tela virtual) para gerar as imagens do README
 * e conferir que todas as telas são montadas sem erros.
 *
 * <p>Uso: {@code GeradorCapturas <pasta-de-saida>}</p>
 */
public final class GeradorCapturas {

    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

    private GeradorCapturas() {
    }

    public static void main(String[] args) throws Exception {
        Path saida = Path.of(args.length > 0 ? args[0] : "target/capturas");
        Files.createDirectories(saida);
        Path dados = Files.createTempDirectory("cali-burger-capturas");
        popular(dados);
        Servicos servicos = Servicos.abrir(dados);

        iniciarJavaFx();
        Stage janela = naTelaJavaFx(() -> {
            Stage stage = new Stage();
            Scene cena = new Scene(App.criarInterface(servicos), 1000, 640);
            App.aplicarEstilo(cena);
            stage.setScene(cena);
            stage.setTitle("Cali Burger");
            stage.show();
            return stage;
        });
        TabPane abas = (TabPane) ((BorderPane) janela.getScene().getRoot()).getCenter();

        String[] nomes = {"produtos", "cardapios", "pedidos", "estoque"};
        for (int i = 0; i < nomes.length; i++) {
            int indice = i;
            naTelaJavaFx(() -> {
                abas.getSelectionModel().select(indice);
                return null;
            });
            Thread.sleep(700);
            salvar(naTelaJavaFx(() -> janela.getScene().snapshot(null)), saida.resolve(nomes[i] + ".png"));
        }

        // Janela de edição de pedido
        Pedido pedido = servicos.pedidos().listar().get(1);
        PedidoDialogo dialogo = naTelaJavaFx(() -> {
            PedidoDialogo d = new PedidoDialogo(
                    servicos.produtos().pesquisar("", ProdutoServico.Ordenacao.NOME), pedido, pedido.getNumero());
            d.initOwner(janela);
            App.aplicarEstilo(d.getDialogPane().getScene());
            d.show();
            return d;
        });
        Thread.sleep(700);
        salvar(naTelaJavaFx(() -> dialogo.getDialogPane().getScene().snapshot(null)), saida.resolve("pedido-dialogo.png"));

        naTelaJavaFx(() -> {
            dialogo.close();
            janela.close();
            return null;
        });
        Platform.exit();
        System.out.println("Capturas salvas em " + saida.toAbsolutePath());
    }

    /** Cadastra produtos, cardápios, pedidos e estoque de exemplo. */
    private static void popular(Path dados) {
        Servicos servicos = Servicos.abrir(dados);
        ProdutoServico produtos = servicos.produtos();
        Map<String, String> precos = new java.util.LinkedHashMap<>();
        precos.put("Cali Burger Clássico", "27.90");
        precos.put("Cali Burger Duplo", "36.90");
        precos.put("X-Bacon", "31.50");
        precos.put("X-Salada", "26.00");
        precos.put("Veggie Burger", "29.90");
        precos.put("Batata Frita", "14.00");
        precos.put("Onion Rings", "16.50");
        precos.put("Refrigerante Lata", "6.50");
        precos.put("Suco Natural", "9.00");
        precos.put("Milkshake de Chocolate", "18.00");
        precos.put("Água Mineral", "4.00");
        precos.forEach((nome, preco) -> produtos.adicionar(nome, new BigDecimal(preco)));

        Map<String, List<String>> cardapios = Map.of(
                "Lanches", List.of("Cali Burger Clássico", "Cali Burger Duplo", "X-Bacon", "X-Salada", "Veggie Burger"),
                "Acompanhamentos", List.of("Batata Frita", "Onion Rings"),
                "Bebidas", List.of("Refrigerante Lata", "Suco Natural", "Milkshake de Chocolate", "Água Mineral"));
        for (String nome : List.of("Lanches", "Acompanhamentos", "Bebidas")) {
            Cardapio cardapio = servicos.cardapios().criar(nome);
            cardapios.get(nome).forEach(p -> servicos.cardapios().adicionarProduto(cardapio, produto(produtos, p)));
        }

        Object[][] pedidos = {
                {"2026-10-15T18:42", StatusPedido.ENTREGUE, new Object[] {"Cali Burger Duplo", 2, "Batata Frita", 1, "Refrigerante Lata", 2}},
                {"2026-10-15T18:55", StatusPedido.ENTREGUE, new Object[] {"X-Bacon", 1, "Milkshake de Chocolate", 1}},
                {"2026-10-15T19:03", StatusPedido.CANCELADO, new Object[] {"X-Salada", 1}},
                {"2026-10-15T19:10", StatusPedido.PRONTO, new Object[] {"Veggie Burger", 1, "Suco Natural", 1, "Onion Rings", 1}},
                {"2026-10-15T19:18", StatusPedido.EM_PREPARACAO, new Object[] {"Cali Burger Clássico", 3, "Batata Frita", 2, "Água Mineral", 3}},
                {"2026-10-15T19:21", StatusPedido.EM_PREPARACAO, new Object[] {"X-Bacon", 2, "Refrigerante Lata", 2}},
        };
        Path arquivoPedidos = dados.resolve("pedidos.dat");
        for (Object[] dadosPedido : pedidos) {
            LocalDateTime quando = LocalDateTime.parse((String) dadosPedido[0]);
            Clock relogio = Clock.fixed(quando.atZone(FUSO).toInstant(), FUSO);
            PedidoServico servico = new PedidoServico(new RepositorioArquivo<>(arquivoPedidos, Pedido.class), relogio);
            Object[] itens = (Object[]) dadosPedido[2];
            List<ItemPedido> lista = new java.util.ArrayList<>();
            for (int i = 0; i < itens.length; i += 2) {
                lista.add(ItemPedido.de(produto(produtos, (String) itens[i]), (Integer) itens[i + 1]));
            }
            Pedido pedido = servico.criar(lista);
            if (dadosPedido[1] != StatusPedido.EM_PREPARACAO) {
                servico.atualizar(pedido, pedido.getItens(), (StatusPedido) dadosPedido[1]);
            }
        }

        Object[][] estoque = {
                {"Pão de hambúrguer", 120, 40}, {"Carne 150 g", 32, 40}, {"Queijo cheddar (fatias)", 85, 30},
                {"Bacon (porções)", 14, 20}, {"Alface (unidades)", 9, 5}, {"Tomate (kg)", 6, 3},
                {"Batata congelada (kg)", 18, 10}, {"Refrigerante lata", 48, 24}};
        for (Object[] item : estoque) {
            servicos.estoque().cadastrar((String) item[0], (Integer) item[1], (Integer) item[2]);
        }
    }

    private static Produto produto(ProdutoServico produtos, String nome) {
        return produtos.listar().stream().filter(p -> p.getNome().equals(nome)).findFirst().orElseThrow();
    }

    private static void iniciarJavaFx() throws InterruptedException {
        CountDownLatch pronto = new CountDownLatch(1);
        Platform.startup(pronto::countDown);
        if (!pronto.await(30, TimeUnit.SECONDS)) {
            throw new IllegalStateException("JavaFX não iniciou");
        }
    }

    /** Executa na thread do JavaFX e espera o resultado. */
    private static <T> T naTelaJavaFx(Supplier<T> acao) throws Exception {
        AtomicReference<T> resultado = new AtomicReference<>();
        AtomicReference<Throwable> erro = new AtomicReference<>();
        CountDownLatch fim = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                resultado.set(acao.get());
            } catch (Throwable t) {
                erro.set(t);
            } finally {
                fim.countDown();
            }
        });
        if (!fim.await(30, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Tempo esgotado esperando a interface");
        }
        if (erro.get() != null) {
            throw new IllegalStateException("Erro na interface", erro.get());
        }
        return resultado.get();
    }

    private static void salvar(WritableImage imagem, Path arquivo) throws IOException {
        int largura = (int) imagem.getWidth();
        int altura = (int) imagem.getHeight();
        int[] pixels = new int[largura * altura];
        PixelReader leitor = imagem.getPixelReader();
        leitor.getPixels(0, 0, largura, altura, javafx.scene.image.PixelFormat.getIntArgbInstance(), pixels, 0, largura);
        BufferedImage saida = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_ARGB);
        saida.setRGB(0, 0, largura, altura, pixels, 0, largura);
        ImageIO.write(saida, "png", arquivo.toFile());
        System.out.println("  " + arquivo.getFileName() + " (" + largura + "x" + altura + ")");
    }
}
