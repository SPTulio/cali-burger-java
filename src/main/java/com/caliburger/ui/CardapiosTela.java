package com.caliburger.ui;

import com.caliburger.modelo.Cardapio;
import com.caliburger.modelo.Produto;
import com.caliburger.servico.CardapioServico;
import com.caliburger.servico.ProdutoServico;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** Aba de montagem dos cardápios a partir dos produtos cadastrados. */
public final class CardapiosTela implements Tela {

    private final CardapioServico servico;
    private final ProdutoServico produtos;
    private final BorderPane raiz = new BorderPane();

    private final ComboBox<Cardapio> comboCardapios = new ComboBox<>();
    private final ListView<Produto> listaDisponiveis = new ListView<>();
    private final ListView<Produto> listaDoCardapio = new ListView<>();
    private final Label tituloCardapio = Componentes.titulo("Produtos do cardápio");
    private final Button botaoRenomear = Componentes.botao("Renomear");
    private final Button botaoExcluir = Componentes.botao("Excluir", "botao-perigo");
    private final Button botaoAdicionar = Componentes.botao("Adicionar  >");
    private final Button botaoRemover = Componentes.botao("<  Remover");

    public CardapiosTela(CardapioServico servico, ProdutoServico produtos) {
        this.servico = servico;
        this.produtos = produtos;
        montar();
        atualizar();
    }

    @Override
    public String getTitulo() {
        return "Cardápios";
    }

    @Override
    public Node getConteudo() {
        return raiz;
    }

    private void montar() {
        comboCardapios.setPromptText("Selecione um cardápio");
        comboCardapios.setPrefWidth(260);
        Button botaoNovo = Componentes.botao("Novo cardápio", "botao-primario");
        for (Button b : new Button[] {botaoNovo, botaoRenomear, botaoExcluir}) {
            b.setMaxWidth(Region.USE_PREF_SIZE);
        }
        HBox barra = new HBox(8, new Label("Cardápio:"), comboCardapios, botaoNovo, botaoRenomear, botaoExcluir);
        barra.setAlignment(Pos.CENTER_LEFT);
        barra.getStyleClass().add("painel");

        listaDisponiveis.setPlaceholder(new Label("Cadastre produtos na aba Produtos."));
        listaDoCardapio.setPlaceholder(new Label("Nenhum produto neste cardápio."));
        VBox colunaDisponiveis = coluna(Componentes.titulo("Produtos cadastrados"), listaDisponiveis);
        VBox colunaCardapio = coluna(tituloCardapio, listaDoCardapio);

        VBox botoesMeio = new VBox(8, botaoAdicionar, botaoRemover);
        botoesMeio.setAlignment(Pos.CENTER);
        botoesMeio.setPrefWidth(130);
        botoesMeio.setMinWidth(130);

        HBox listas = new HBox(12, colunaDisponiveis, botoesMeio, colunaCardapio);
        VBox.setVgrow(listas, Priority.ALWAYS);
        Label dica = new Label("Dica: dê um duplo clique em um produto para movê-lo entre as listas.");
        dica.getStyleClass().add("status");

        VBox conteudo = new VBox(12, barra, listas, dica);
        conteudo.getStyleClass().add("conteudo");
        raiz.setCenter(conteudo);

        comboCardapios.valueProperty().addListener((obs, antigo, novo) -> atualizarListas());
        botaoNovo.setOnAction(e -> criar());
        botaoRenomear.setOnAction(e -> renomear());
        botaoExcluir.setOnAction(e -> excluir());
        botaoAdicionar.setOnAction(e -> adicionarSelecionado());
        botaoRemover.setOnAction(e -> removerSelecionado());
        listaDisponiveis.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                adicionarSelecionado();
            }
        });
        listaDoCardapio.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                removerSelecionado();
            }
        });
    }

    private static VBox coluna(Label titulo, ListView<Produto> lista) {
        VBox.setVgrow(lista, Priority.ALWAYS);
        VBox coluna = new VBox(8, titulo, lista);
        coluna.getStyleClass().add("painel");
        HBox.setHgrow(coluna, Priority.ALWAYS);
        coluna.setMaxWidth(Double.MAX_VALUE);
        return coluna;
    }

    @Override
    public void atualizar() {
        Cardapio selecionado = comboCardapios.getValue();
        comboCardapios.getItems().setAll(servico.listar());
        if (selecionado != null && comboCardapios.getItems().contains(selecionado)) {
            comboCardapios.setValue(selecionado);
        } else if (!comboCardapios.getItems().isEmpty()) {
            comboCardapios.setValue(comboCardapios.getItems().get(0));
        } else {
            comboCardapios.setValue(null);
        }
        atualizarListas();
    }

    private void atualizarListas() {
        Cardapio cardapio = comboCardapios.getValue();
        boolean temCardapio = cardapio != null;
        botaoRenomear.setDisable(!temCardapio);
        botaoExcluir.setDisable(!temCardapio);
        botaoAdicionar.setDisable(!temCardapio);
        botaoRemover.setDisable(!temCardapio);

        if (temCardapio) {
            listaDoCardapio.getItems().setAll(servico.produtosDo(cardapio));
            tituloCardapio.setText("Produtos em \"" + cardapio.getNome() + "\"");
            listaDisponiveis.getItems().setAll(produtos.pesquisar("", ProdutoServico.Ordenacao.NOME).stream()
                    .filter(p -> !cardapio.contem(p.getId()))
                    .toList());
        } else {
            listaDoCardapio.getItems().clear();
            tituloCardapio.setText("Produtos do cardápio");
            listaDisponiveis.getItems().setAll(produtos.pesquisar("", ProdutoServico.Ordenacao.NOME));
        }
    }

    private void criar() {
        Componentes.pedirTexto(raiz, "Novo cardápio", "Nome:", "").ifPresent(nome -> {
            Cardapio[] criado = new Cardapio[1];
            if (Componentes.executar(raiz, () -> criado[0] = servico.criar(nome))) {
                atualizar();
                comboCardapios.setValue(criado[0]);
            }
        });
    }

    private void renomear() {
        Cardapio cardapio = comboCardapios.getValue();
        if (cardapio == null) {
            return;
        }
        Componentes.pedirTexto(raiz, "Renomear cardápio", "Novo nome:", cardapio.getNome()).ifPresent(nome -> {
            if (Componentes.executar(raiz, () -> servico.renomear(cardapio, nome))) {
                atualizar();
            }
        });
    }

    private void excluir() {
        Cardapio cardapio = comboCardapios.getValue();
        if (cardapio != null
                && Componentes.confirmar(raiz, "Excluir cardápio",
                        "Excluir o cardápio \"" + cardapio.getNome() + "\"? Os produtos continuam cadastrados.")
                && Componentes.executar(raiz, () -> servico.excluir(cardapio))) {
            atualizar();
        }
    }

    private void adicionarSelecionado() {
        Cardapio cardapio = comboCardapios.getValue();
        Produto produto = listaDisponiveis.getSelectionModel().getSelectedItem();
        if (cardapio != null && produto != null
                && Componentes.executar(raiz, () -> servico.adicionarProduto(cardapio, produto))) {
            atualizarListas();
        }
    }

    private void removerSelecionado() {
        Cardapio cardapio = comboCardapios.getValue();
        Produto produto = listaDoCardapio.getSelectionModel().getSelectedItem();
        if (cardapio != null && produto != null
                && Componentes.executar(raiz, () -> servico.removerProduto(cardapio, produto))) {
            atualizarListas();
        }
    }
}
