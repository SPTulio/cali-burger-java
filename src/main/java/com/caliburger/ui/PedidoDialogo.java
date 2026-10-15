package com.caliburger.ui;

import com.caliburger.modelo.ItemPedido;
import com.caliburger.modelo.Pedido;
import com.caliburger.modelo.Produto;
import com.caliburger.modelo.StatusPedido;
import com.caliburger.util.Moeda;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Janela para montar um pedido novo ou alterar um existente. */
public final class PedidoDialogo extends Dialog<PedidoDialogo.Resultado> {

    /** O que o usuário confirmou na janela. */
    public record Resultado(List<ItemPedido> itens, StatusPedido status) {
    }

    private static final int QUANTIDADE_MAXIMA = 99;

    private final ComboBox<Produto> comboProduto = new ComboBox<>();
    private final Spinner<Integer> spinnerQuantidade = new Spinner<>(1, QUANTIDADE_MAXIMA, 1);
    private final TableView<ItemPedido> tabelaItens = new TableView<>();
    private final ComboBox<StatusPedido> comboStatus = new ComboBox<>();
    private final Label rotuloTotal = new Label();

    /**
     * @param produtos produtos disponíveis para venda
     * @param pedido   pedido a alterar, ou null para um pedido novo
     * @param numero   número que o pedido terá (ou já tem)
     */
    public PedidoDialogo(List<Produto> produtos, Pedido pedido, int numero) {
        boolean novo = pedido == null;
        setTitle(novo ? "Novo pedido" : "Editar pedido nº " + numero);
        setHeaderText(novo ? "Pedido nº " + numero : null);
        setResizable(true);

        ButtonType confirmar = new ButtonType(novo ? "Finalizar pedido" : "Salvar alterações",
                ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(confirmar, Componentes.CANCELAR);

        // Seleção de produto e quantidade
        comboProduto.getItems().setAll(produtos);
        comboProduto.setPromptText(produtos.isEmpty() ? "Nenhum produto cadastrado" : "Selecione um produto");
        comboProduto.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(comboProduto, Priority.ALWAYS);
        spinnerQuantidade.setEditable(true);
        spinnerQuantidade.setPrefWidth(80);
        Button botaoAdicionar = new Button("Adicionar");
        botaoAdicionar.getStyleClass().add("botao-primario");
        botaoAdicionar.disableProperty().bind(comboProduto.valueProperty().isNull());
        HBox linhaProduto = new HBox(8, comboProduto, spinnerQuantidade, botaoAdicionar);
        linhaProduto.setAlignment(Pos.CENTER_LEFT);

        // Itens do pedido
        TableColumn<ItemPedido, String> colunaQtd = Componentes.coluna("Qtd.", i -> String.valueOf(i.quantidade()));
        TableColumn<ItemPedido, String> colunaProduto = Componentes.coluna("Produto", ItemPedido::nomeProduto);
        TableColumn<ItemPedido, String> colunaUnitario = Componentes.coluna("Unitário", i -> Moeda.formatar(i.precoUnitario()));
        TableColumn<ItemPedido, String> colunaSubtotal = Componentes.coluna("Subtotal", i -> Moeda.formatar(i.getSubtotal()));
        colunaQtd.setPrefWidth(50);
        colunaQtd.setMaxWidth(60);
        colunaUnitario.setPrefWidth(95);
        colunaSubtotal.setPrefWidth(95);
        colunaQtd.getStyleClass().add("coluna-numero");
        colunaUnitario.getStyleClass().add("coluna-numero");
        colunaSubtotal.getStyleClass().add("coluna-numero");
        tabelaItens.getColumns().add(colunaQtd);
        tabelaItens.getColumns().add(colunaProduto);
        tabelaItens.getColumns().add(colunaUnitario);
        tabelaItens.getColumns().add(colunaSubtotal);
        tabelaItens.getColumns().forEach(c -> c.setSortable(false));
        tabelaItens.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabelaItens.setPlaceholder(new Label("Adicione produtos ao pedido."));
        tabelaItens.setPrefHeight(220);
        VBox.setVgrow(tabelaItens, Priority.ALWAYS);

        Button botaoRemover = new Button("Remover item");
        botaoRemover.disableProperty().bind(tabelaItens.getSelectionModel().selectedItemProperty().isNull());
        Region espaco = new Region();
        HBox.setHgrow(espaco, Priority.ALWAYS);
        rotuloTotal.getStyleClass().add("total");
        HBox linhaTotal = new HBox(8, botaoRemover, espaco, rotuloTotal);
        linhaTotal.setAlignment(Pos.CENTER_LEFT);

        VBox conteudo = new VBox(10, Componentes.titulo("Adicionar produto"), linhaProduto,
                Componentes.titulo("Itens do pedido"), tabelaItens, linhaTotal);

        // Status (somente ao editar)
        if (!novo) {
            comboStatus.getItems().setAll(StatusPedido.values());
            comboStatus.setValue(pedido.getStatus());
            tabelaItens.getItems().setAll(pedido.getItens());
            GridPane linhaStatus = new GridPane();
            linhaStatus.setHgap(8);
            linhaStatus.addRow(0, new Label("Status:"), comboStatus);
            conteudo.getChildren().add(0, linhaStatus);
        }

        conteudo.setPrefWidth(520);
        getDialogPane().setContent(conteudo);
        atualizarTotal();

        botaoAdicionar.setOnAction(e -> adicionarItem());
        botaoRemover.setOnAction(e -> {
            tabelaItens.getItems().remove(tabelaItens.getSelectionModel().getSelectedItem());
            atualizarTotal();
        });

        Button botaoConfirmar = (Button) getDialogPane().lookupButton(confirmar);
        botaoConfirmar.disableProperty().bind(javafx.beans.binding.Bindings.isEmpty(tabelaItens.getItems()));

        setResultConverter(botao -> botao == confirmar
                ? new Resultado(new ArrayList<>(tabelaItens.getItems()), novo ? StatusPedido.EM_PREPARACAO : comboStatus.getValue())
                : null);
    }

    /** Adiciona o produto escolhido; se ele já estiver na lista, soma a quantidade. */
    private void adicionarItem() {
        Produto produto = comboProduto.getValue();
        if (produto == null) {
            return;
        }
        Componentes.executar(tabelaItens, () -> {
            int quantidade = Componentes.lerSpinner(spinnerQuantidade, "A quantidade");
            List<ItemPedido> itens = tabelaItens.getItems();
            for (int i = 0; i < itens.size(); i++) {
                ItemPedido item = itens.get(i);
                if (item.idProduto().equals(produto.getId()) && item.precoUnitario().equals(produto.getPreco())) {
                    itens.set(i, item.comQuantidade(Math.min(QUANTIDADE_MAXIMA, item.quantidade() + quantidade)));
                    return;
                }
            }
            itens.add(ItemPedido.de(produto, quantidade));
        });
        comboProduto.setValue(null);
        spinnerQuantidade.getValueFactory().setValue(1);
        atualizarTotal();
    }

    private void atualizarTotal() {
        BigDecimal total = tabelaItens.getItems().stream()
                .map(ItemPedido::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        rotuloTotal.setText("Total: " + Moeda.formatar(total));
    }
}
