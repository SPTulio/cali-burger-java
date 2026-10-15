package com.example.demo;

import javafx.application.Application;
import javafx.collections.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.text.Font;
import java.util.List;


public class Main extends Application {

    private final ObservableList<Produto> produtosDisponiveis = FXCollections.observableArrayList();
    private final ObservableList<Cardapio> cardapios = FXCollections.observableArrayList();
    private final ComboBox<Cardapio> comboCardapios = new ComboBox<>();
    private final ListView<Produto> listViewCardapio = new ListView<>();
    private final ArquivoProduto repositorio = new ArquivoProduto();
    private final TextField pesquisaField = new TextField();
    private final ListView<Produto> listView = new ListView<>();
    private TableView<Pedido> tabela;
    private final ComboBox<Produto> comboProdutos = new ComboBox<>();
    private int numeroPedidoAtual = 1;
    private final Map<String, Ingrediente> ingredientes = new HashMap<>();
    private final TextArea resultadoArea = new TextArea();


    @Override
    public void start(Stage stage) {
        TabPane tabPane = new TabPane();

        Tab tabProdutos = new Tab("Produtos", criarAbaProdutos());
        Tab tabCardapio = new Tab("Cardápio", criarAbaCardapio());
        Tab tabPedidos = new Tab("Pedidos", criarAbaPedidos());
        Tab tabIngrediente = new Tab("Estoque", criarAbaIngrediente());

        tabPane.getTabs().addAll(tabProdutos, tabCardapio, tabPedidos, tabIngrediente);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Scene scene = new Scene(tabPane, 700, 450);
        stage.setScene(scene);
        stage.setTitle("Cali Burger");
        stage.show();
    }

    private VBox criarAbaProdutos() {
        TextField nomeField = new TextField();
        nomeField.setPromptText("Nome do Produto");

        TextField valorField = new TextField();
        valorField.setPromptText("Valor");

        Button btnAdicionar = new Button("Adicionar");
        Button btnEditar = new Button("Editar");
        Button btnExcluir = new Button("Excluir");
        Button btnOrdenarNome = new Button("Ordenar por Nome");
        Button btnOrdenarValor = new Button("Ordenar por Valor");
        Button btnLimpar = new Button("Limpar Todos");

        atualizarLista();

        btnAdicionar.setOnAction(e -> {
            try {
                String nome = nomeField.getText();
                double valor = Double.parseDouble(valorField.getText());
                repositorio.adicionarProduto(new Produto(nome, valor));
                atualizarLista();
                nomeField.clear();
                valorField.clear();
            } catch (NumberFormatException ex) {
                mostrarAlerta("Erro, Digite um valor válido.");
            }
        });

        btnEditar.setOnAction(e -> {
            Produto selecionado = listView.getSelectionModel().getSelectedItem();
            if (selecionado != null) {
                try {
                    String novoNome = nomeField.getText();
                    double novoValor = Double.parseDouble(valorField.getText());
                    repositorio.editarProduto(selecionado.getNome(), novoNome, novoValor);
                    atualizarLista();
                    nomeField.clear();
                    valorField.clear();
                } catch (NumberFormatException ex) {
                    mostrarAlerta("Erro, Digite um valor válido.");
                }
            } else {
                mostrarAlerta("Aviso, Selecione um produto para editar.");
            }
        });

        btnExcluir.setOnAction(e -> {
            Produto selecionado = listView.getSelectionModel().getSelectedItem();
            if (selecionado != null) {
                boolean confirmado = mostrarConfirmacao("Confirmar Exclusão", "Deseja realmente excluir este produto?");
                if (confirmado) {
                    repositorio.excluirProduto(selecionado.getNome());
                    atualizarLista();
                }
            } else {
                mostrarAlerta("Aviso, Selecione um produto para excluir.");
            }
        });

        btnOrdenarNome.setOnAction(e -> {
            repositorio.ordenarPorNome();
            atualizarLista();
        });

        btnOrdenarValor.setOnAction(e -> {
            repositorio.ordenarPorValor();
            atualizarLista();
        });

        btnLimpar.setOnAction(e -> {
            boolean confirmado = mostrarConfirmacao("Confirmar Limpeza", "Deseja realmente limpar todos os produtos?");
            if (confirmado) {
                repositorio.limparProdutos();
                atualizarLista();
            }
        });

        pesquisaField.setPromptText("Pesquisar produto...");
        pesquisaField.textProperty().addListener((obs, oldVal, newVal) -> atualizarLista());

        VBox inputs = new VBox(10,
                nomeField,
                valorField,
                btnAdicionar,
                btnEditar,
                btnExcluir,
                btnOrdenarNome,
                btnOrdenarValor,
                btnLimpar,
                pesquisaField);
        inputs.setPrefWidth(250);
        inputs.setStyle("-fx-padding: 15; -fx-background-color: #f9f9f9; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-radius: 5;");

        HBox layout = new HBox(15, inputs, listView);
        layout.setStyle("-fx-padding: 15;");

        HBox.setHgrow(listView, Priority.ALWAYS);
        listView.setMaxWidth(Double.MAX_VALUE);
        listView.setMaxHeight(Double.MAX_VALUE);

        VBox root = new VBox(layout);
        VBox.setVgrow(layout, Priority.ALWAYS);

        return root;
    }

    private VBox criarAbaCardapio() {
        ListView<Produto> listViewProdutos = new ListView<>(produtosDisponiveis);

        comboCardapios.setItems(cardapios);
        comboCardapios.setPromptText("Selecione um cardápio");
        cardapios.addAll(ArquivoCardapio.lerLista());
        comboCardapios.setItems(cardapios);

        Button btnNovoCardapio = new Button("Novo Cardápio");
        Button btnRenomearCardapio = new Button("Renomear");
        Button btnExcluirCardapio = new Button("Excluir");


        btnNovoCardapio.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Novo Cardápio");
            dialog.setHeaderText("Criar novo cardápio");
            dialog.setContentText("Nome:");
            dialog.setGraphic(null);

            dialog.showAndWait().ifPresent(nome -> {
                if (!nome.trim().isEmpty()) {
                    Cardapio novo = new Cardapio(nome);
                    cardapios.add(novo);
                    comboCardapios.getSelectionModel().select(novo);
                    ArquivoCardapio.salvarLista(new ArrayList<>(cardapios));
                }
            });
        });

        btnRenomearCardapio.setOnAction(e -> {
            Cardapio selecionado = comboCardapios.getSelectionModel().getSelectedItem();
            if (selecionado != null) {
                TextInputDialog dialog = new TextInputDialog(selecionado.getNome());
                dialog.setTitle("Renomear Cardápio");
                dialog.setHeaderText("Renomear cardápio selecionado");
                dialog.setContentText("Novo nome:");
                dialog.setGraphic(null);

                dialog.showAndWait().ifPresent(novoNome -> {
                    if (!novoNome.trim().isEmpty()) {
                        selecionado.setNome(novoNome);
                        comboCardapios.setItems(null);
                        comboCardapios.setItems(cardapios);
                        comboCardapios.getSelectionModel().select(selecionado);
                        ArquivoCardapio.salvarLista(new ArrayList<>(cardapios));
                    }
                });
            } else {
                mostrarAlerta("Nenhum cardápio selecionado.");
            }
        });

        btnExcluirCardapio.setOnAction(e -> {
            Cardapio selecionado = comboCardapios.getSelectionModel().getSelectedItem();

            if (selecionado != null) {
                Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
                confirmacao.setTitle("Excluir Cardápio");
                confirmacao.setHeaderText("Tem certeza que deseja excluir o cardápio '" + selecionado.getNome() + "'?");
                confirmacao.setContentText("Essa ação não pode ser desfeita.");
                confirmacao.setGraphic(null);

                confirmacao.showAndWait().ifPresent(resposta -> {
                    if (resposta == ButtonType.OK) {
                        cardapios.remove(selecionado);
                        ArquivoCardapio.salvarLista(new ArrayList<>(cardapios));
                        comboCardapios.getSelectionModel().clearSelection();
                        listViewCardapio.setItems(FXCollections.emptyObservableList());
                    }
                });
            } else {
                mostrarAlerta("Nenhum cardápio selecionado.");
            }
        });

        comboCardapios.setOnAction(e -> {
            Cardapio selecionado = comboCardapios.getSelectionModel().getSelectedItem();
            if (selecionado != null) {
                listViewCardapio.setItems(selecionado.getItens());
            } else {
                listViewCardapio.setItems(FXCollections.emptyObservableList());
            }
        });

        Button btnAdicionarProduto = new Button(">> Adicionar >>");
        Button btnRemoverProduto = new Button("<< Remover <<");

        btnAdicionarProduto.setOnAction(e -> {
            Produto selecionado = listViewProdutos.getSelectionModel().getSelectedItem();
            Cardapio cardapioSelecionado = comboCardapios.getSelectionModel().getSelectedItem();

            if (selecionado != null && cardapioSelecionado != null) {
                cardapioSelecionado.getItens().add(selecionado);
                ArquivoCardapio.salvarLista(new ArrayList<>(cardapios)); // salvar aqui
            }
        });


        btnRemoverProduto.setOnAction(e -> {
            Produto selecionado = listViewCardapio.getSelectionModel().getSelectedItem();
            Cardapio cardapioSelecionado = comboCardapios.getSelectionModel().getSelectedItem();

            if (selecionado != null && cardapioSelecionado != null) {
                cardapioSelecionado.getItens().remove(selecionado);
                ArquivoCardapio.salvarLista(new ArrayList<>(cardapios)); // salvar aqui também
            }
        });


        HBox botoesTopo = new HBox(10, comboCardapios, btnNovoCardapio, btnRenomearCardapio, btnExcluirCardapio);
        HBox botoesMeio = new HBox(10, btnAdicionarProduto, btnRemoverProduto);
        HBox listas = new HBox(20, listViewProdutos, botoesMeio, listViewCardapio);

        VBox layout = new VBox(10, botoesTopo, listas);
        layout.setStyle("-fx-padding: 10");
        return layout;
    }

    private VBox criarAbaPedidos() {
        VBox boxTitulo = new VBox();
        boxTitulo.setAlignment(Pos.TOP_CENTER);

        Label titulo = new Label("Tela de Pedidos:");
        titulo.setFont(new Font("Arial", 24));
        boxTitulo.getChildren().add(titulo);

        tabela = new TableView<>();

        // coluna num pedido
        TableColumn<Pedido, String> colNumPedido = new TableColumn<>("Nº Pedido");
        colNumPedido.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue().getNumPedido())));

        // coluna produtos
        TableColumn<Pedido, String> colProdutos = new TableColumn<>("Produtos");
        colProdutos.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getProdutosComoTexto()));

        // coluna status
        TableColumn<Pedido, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus()));

        // coluna data e hora
        TableColumn<Pedido, String> colDataHora = new TableColumn<>("Data/Hora");
        colDataHora.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getDataHora()));

        // coluna valor total e formatar
        TableColumn<Pedido, String> colValor = new TableColumn<>("Valor Total");
        colValor.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty("R$ " + String.format("%.2f", data.getValue().getValorTotal()).replace('.', ',')));

        //colocar colunas na tabela
        tabela.getColumns().addAll(colNumPedido, colProdutos, colStatus, colDataHora, colValor);
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); //ocupar a tela toda

        // carregar pedidos salvos no .dat
        List<Pedido> pedidosSalvos = ArquivoPedido.carregarPedidos();
        tabela.getItems().addAll(pedidosSalvos);

        //calcular num pedido
        int maiorNumero = 0;
        for (Pedido p : pedidosSalvos) {
            if (p.getNumPedido() > maiorNumero) {
                maiorNumero = p.getNumPedido();
            }
        }
        numeroPedidoAtual = maiorNumero + 1;

        // mensagens feedback
        Label lbStatus = new Label("");
        lbStatus.setFont(new Font("Arial", 20));

        Button btFazerPedido = new Button("Fazer pedido");
        btFazerPedido.setOnAction(e -> abrirJanelaPedido(lbStatus));

        Button btEditar = new Button("Editar");
        btEditar.setOnAction(e -> {
            Pedido selecionado = tabela.getSelectionModel().getSelectedItem();
            if (selecionado != null) {
                abrirJanelaEditarPedido(selecionado, lbStatus);
            } else {
                lbStatus.setText("Selecione um pedido para editar.");
            }
        });

        Button btExcluir = new Button("Excluir Pedido");
        btExcluir.setOnAction(e -> {
            Pedido selecionado = tabela.getSelectionModel().getSelectedItem();
            if (selecionado != null) {
                tabela.getItems().remove(selecionado);
                ArquivoPedido.salvarPedidos(tabela.getItems());
                lbStatus.setText("Pedido excluído.");
            } else {
                lbStatus.setText("Selecione um pedido para excluir.");
            }
        });

        //box dos botoes
        HBox boxBotoes = new HBox(20, btFazerPedido, btEditar, btExcluir);
        boxBotoes.setAlignment(Pos.CENTER);

        // box principal
        VBox boxPrincipal = new VBox(10, boxTitulo, tabela, boxBotoes, lbStatus);
        boxPrincipal.setPadding(new Insets(20));
        boxPrincipal.setAlignment(Pos.CENTER);

        return boxPrincipal;
    }


    private void abrirJanelaPedido(Label lbStatus) {
        comboProdutos.setItems(produtosDisponiveis);

        Stage janela = new Stage();
        janela.setTitle("Novo Pedido");

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        root.setAlignment(Pos.CENTER);

        // ComboBox para selecionar produto
        ComboBox<Produto> comboProdutos = new ComboBox<>();
        comboProdutos.setItems(produtosDisponiveis);
        comboProdutos.setPromptText("Selecione um produto");

        ListView<String> listaProdutos = new ListView<>(); // produtos adicionados
        ArrayList<Produto> produtos = new ArrayList<>();

        Button btnAdicionar = new Button("Adicionar Produto");
        btnAdicionar.setOnAction(e -> {
            Produto selecionado = comboProdutos.getSelectionModel().getSelectedItem();

            // validação
            if (selecionado == null) {
                mostrarAlerta("Selecione um produto para adicionar.");
                return;
            }

            produtos.add(selecionado);
            listaProdutos.getItems().add(selecionado.getNome() + " (R$ " +
                    String.format("%.2f", selecionado.getValor()).replace('.', ',') + ")");
            comboProdutos.getSelectionModel().clearSelection(); // limpar seleção
        });

        Button btnFinalizar = new Button("Finalizar Pedido");
        btnFinalizar.setOnAction(e -> {
            if (produtos.isEmpty()) {
                mostrarAlerta("Adicione pelo menos um produto.");
                return;
            }

            double total = 0;
            for (Produto p : produtos) {
                total += p.getValor();
            }

            StringBuilder texto = new StringBuilder("Confirme seu pedido:\n\n");
            for (Produto p : produtos) {
                texto.append("- ").append(p.getNome())
                        .append(" (R$ ")
                        .append(String.format("%.2f", p.getValor()).replace('.', ','))
                        .append(")\n");
            }
            texto.append("\nValor total: R$ ")
                    .append(String.format("%.2f", total).replace('.', ','));

            // confirmação
            Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION, texto.toString(), ButtonType.OK, ButtonType.CANCEL);
            confirmacao.setTitle("Confirmar Pedido");
            confirmacao.setHeaderText("Deseja realmente finalizar este pedido?");
            confirmacao.showAndWait().ifPresent(resposta -> {
                if (resposta == ButtonType.OK) {
                    Pedido novoPedido = new Pedido();
                    produtos.forEach(novoPedido::pedirProdutos);
                    novoPedido.setNumPedido(numeroPedidoAtual++);
                    novoPedido.setStatus("Em preparação");
                    novoPedido.registrarDataHora();
                    tabela.getItems().add(novoPedido);
                    ArquivoPedido.salvarPedidos(tabela.getItems());
                    lbStatus.setText("Pedido feito com sucesso!");
                    janela.close();
                }
            });
        });

        // novo layout com ComboBox
        root.getChildren().addAll(
                new Label("Produto:"), comboProdutos,
                btnAdicionar,
                new Label("Produtos adicionados:"), listaProdutos,
                btnFinalizar
        );

        janela.setScene(new Scene(root, 350, 400));
        janela.showAndWait(); // bloqueia a outra tela
    }


    private void abrirJanelaEditarPedido(Pedido pedido, Label lbStatus) {
        comboProdutos.setItems(produtosDisponiveis);
        comboProdutos.setPromptText("Selecione um produto");

        Stage janela = new Stage();
        janela.setTitle("Editar Pedido nº " + pedido.getNumPedido());

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        root.setAlignment(Pos.CENTER);

        //select
        ComboBox<String> cbStatus = new ComboBox<>();
        cbStatus.getItems().addAll("Em preparação", "Pronto", "Entregue", "Cancelado");
        cbStatus.setValue(pedido.getStatus());

        ListView<String> lvProdutos = new ListView<>();
        //copia da lista de produtos pra nao alterar a real antes de confirmar
        ArrayList<Produto> produtosEditar = new ArrayList<>(pedido.getProdutos());
        atualizarListaProdutosView(lvProdutos, produtosEditar);

        TextField tfNomeNovo = new TextField();
        TextField tfValorNovo = new TextField();

        Button btnAdicionar = new Button("Adicionar Produto");
        btnAdicionar.setOnAction(e -> {
            Produto selecionado = comboProdutos.getSelectionModel().getSelectedItem();

            if (selecionado == null) {
                mostrarAlerta("Selecione um produto para adicionar.");
                return;
            }

            produtosEditar.add(selecionado);
            atualizarListaProdutosView(lvProdutos, produtosEditar);
            comboProdutos.getSelectionModel().clearSelection();
        });


        Button btnRemover = new Button("Remover Produto Selecionado");
        btnRemover.setOnAction(e -> {
            int selecionado = lvProdutos.getSelectionModel().getSelectedIndex();
            if (selecionado >= 0) {
                produtosEditar.remove(selecionado);
                atualizarListaProdutosView(lvProdutos, produtosEditar);
            } else {
                mostrarAlerta("Selecione um produto para remover.");
            }
        });

        Button btnSalvar = new Button("Salvar Alterações");
        btnSalvar.setOnAction(e -> {
            if (produtosEditar.isEmpty()) {
                mostrarAlerta( "O pedido deve ter pelo menos um produto.");
                return;
            }
            pedido.setStatus(cbStatus.getValue());
            pedido.setProdutos(produtosEditar);
            pedido.registrarDataHora();
            tabela.refresh();
            ArquivoPedido.salvarPedidos(tabela.getItems());
            lbStatus.setText("Pedido nº " + pedido.getNumPedido() + " atualizado com sucesso.");
            janela.close();
        });

        //coloca os itens no modal editar
        root.getChildren().addAll(
                new Label("Status do Pedido:"), cbStatus,
                new Label("Produtos do Pedido:"), lvProdutos,
                new Label("Novo Produto:"), comboProdutos,
                btnAdicionar, btnRemover, btnSalvar
        );

        janela.setScene(new Scene(root, 400, 500));
        janela.showAndWait();
    }

        private VBox criarAbaIngrediente(){
            ArquivoIngrediente.carregar(ingredientes);

            TextField nomeField = new TextField();
            nomeField.setPromptText("Nome do ingrediente");

            TextField qtdField = new TextField();
            qtdField.setPromptText("Quantidade");

            Button adicionarBtn = new Button("Adicionar");
            Button atualizarBtn = new Button("Atualizar");
            Button deletarBtn = new Button("Deletar");

            adicionarBtn.setOnAction(e -> {
                String nome = nomeField.getText().trim();
                if (nome.isEmpty()) {
                    resultadoArea.setText("O nome do ingrediente não pode estar vazio.");
                    return;
                }
                try {
                    int qtd = Integer.parseInt(qtdField.getText().trim());
                    ingredientes.putIfAbsent(nome, new Ingrediente(nome, 0));
                    ingredientes.get(nome).adicionar(qtd);
                    ArquivoIngrediente.salvar(ingredientes);
                    mostrarIngredientes();
                } catch (NumberFormatException ex) {
                    resultadoArea.setText("Quantidade inválida.");
                }
            });

            atualizarBtn.setOnAction(e -> {
                String nome = nomeField.getText().trim();
                if (nome.isEmpty()) {
                    resultadoArea.setText("O nome do ingrediente não pode estar vazio.");
                    return;
                }
                try {
                    int qtd = Integer.parseInt(qtdField.getText().trim());
                    ingredientes.put(nome, new Ingrediente(nome, qtd));
                    ArquivoIngrediente.salvar(ingredientes);
                    mostrarIngredientes();
                } catch (NumberFormatException ex) {
                    resultadoArea.setText("Quantidade inválida.");
                }
            });

            deletarBtn.setOnAction(e -> {
                String nome = nomeField.getText().trim();
                ingredientes.remove(nome);
                ArquivoIngrediente.salvar(ingredientes);
                mostrarIngredientes();
            });

            VBox leftPane = new VBox(10, nomeField, qtdField, adicionarBtn, atualizarBtn, deletarBtn);
            leftPane.setStyle("-fx-border-color: #ddd; -fx-border-width: 2px; -fx-padding: 10px;");

            resultadoArea.setEditable(false);
            resultadoArea.setPrefHeight(400);
            resultadoArea.setPrefWidth(450);

            HBox root = new HBox(20, leftPane, resultadoArea);
            root.setStyle("-fx-padding: 20");

            VBox layout = new VBox(root);

            mostrarIngredientes();

            return layout;

        }

    private void atualizarListaProdutosView(ListView<String> lv, ArrayList<Produto> produtos) {
        lv.getItems().clear();
        for (Produto p : produtos) {
            lv.getItems().add(p.getNome() + " (R$ " + String.format("%.2f", p.getValor()).replace('.', ',') + ")");
        }
    }

    private void atualizarLista() {
        String filtro = pesquisaField.getText().toLowerCase();
        var todos = repositorio.listarProdutos();

        var filtrados = filtro.isEmpty() ?
                todos :
                todos.stream()
                        .filter(p -> p.getNome().toLowerCase().contains(filtro))
                        .collect(Collectors.toList());

        listView.getItems().setAll(filtrados);

        // Aqui acontece a interligação com a aba Cardápio
        produtosDisponiveis.setAll(todos);
    }
    private void mostrarAlerta(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private boolean mostrarConfirmacao(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);

        ButtonType result = alert.showAndWait().orElse(ButtonType.CANCEL);
        return result == ButtonType.OK;
    }

    private void mostrarIngredientes() {
        if (ingredientes.isEmpty()) {
            resultadoArea.setText("Nenhum ingrediente cadastrado.");
            return;
        }

        StringBuilder sb = new StringBuilder("Ingredientes:\n");
        for (Ingrediente ing : ingredientes.values()) {
            sb.append("- ").append(ing).append("\n");
        }
        resultadoArea.setText(sb.toString());
    }

    public static void main(String[] args) {
        launch();
    }
}
