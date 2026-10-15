package com.caliburger;

import com.caliburger.servico.Servicos;
import com.caliburger.ui.CardapiosTela;
import com.caliburger.ui.EstoqueTela;
import com.caliburger.ui.PedidosTela;
import com.caliburger.ui.ProdutosTela;
import com.caliburger.ui.Tela;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.Objects;

/** Janela principal do Cali Burger. */
public class App extends Application {

    private Servicos servicos;

    @Override
    public void init() {
        servicos = Servicos.abrir(Servicos.pastaPadrao());
    }

    @Override
    public void start(Stage janela) {
        Scene cena = new Scene(criarInterface(servicos), 1000, 640);
        aplicarEstilo(cena);
        janela.setScene(cena);
        janela.setTitle("Cali Burger");
        janela.setMinWidth(860);
        janela.setMinHeight(540);
        janela.show();
    }

    /** Monta a interface completa. Também usada pelo gerador de capturas de tela. */
    public static Parent criarInterface(Servicos servicos) {
        List<Tela> telas = List.of(
                new ProdutosTela(servicos.produtos()),
                new CardapiosTela(servicos.cardapios(), servicos.produtos()),
                new PedidosTela(servicos.pedidos(), servicos.produtos()),
                new EstoqueTela(servicos.estoque()));

        TabPane abas = new TabPane();
        abas.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        for (Tela tela : telas) {
            Tab aba = new Tab(tela.getTitulo(), tela.getConteudo());
            aba.setUserData(tela);
            abas.getTabs().add(aba);
        }
        // Ao trocar de aba, recarrega os dados (ex.: produtos novos aparecem nos cardápios).
        abas.getSelectionModel().selectedItemProperty().addListener((obs, anterior, atual) -> {
            if (atual != null) {
                ((Tela) atual.getUserData()).atualizar();
            }
        });

        Label titulo = new Label("Cali Burger");
        titulo.getStyleClass().add("titulo");
        Label subtitulo = new Label("Gestão da lanchonete");
        subtitulo.getStyleClass().add("subtitulo");
        HBox cabecalho = new HBox(new VBox(titulo, subtitulo));
        cabecalho.getStyleClass().add("cabecalho");

        BorderPane raiz = new BorderPane(abas);
        raiz.setTop(cabecalho);
        return raiz;
    }

    public static void aplicarEstilo(Scene cena) {
        String css = Objects.requireNonNull(App.class.getResource("ui/estilo.css"), "estilo.css").toExternalForm();
        cena.getStylesheets().add(css);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
