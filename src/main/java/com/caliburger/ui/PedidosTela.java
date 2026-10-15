package com.caliburger.ui;

import com.caliburger.modelo.Pedido;
import com.caliburger.modelo.StatusPedido;
import com.caliburger.servico.PedidoServico;
import com.caliburger.servico.ProdutoServico;
import com.caliburger.util.Moeda;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

/** Aba de pedidos: lista, criação, edição e exclusão. */
public final class PedidosTela implements Tela {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final PseudoClass CANCELADO = PseudoClass.getPseudoClass("cancelado");
    private static final PseudoClass ENTREGUE = PseudoClass.getPseudoClass("entregue");

    private final PedidoServico servico;
    private final ProdutoServico produtos;
    private final BorderPane raiz = new BorderPane();
    private final TableView<Pedido> tabela = new TableView<>();
    private final Label resumo = new Label();

    public PedidosTela(PedidoServico servico, ProdutoServico produtos) {
        this.servico = servico;
        this.produtos = produtos;
        montar();
        atualizar();
    }

    @Override
    public String getTitulo() {
        return "Pedidos";
    }

    @Override
    public Node getConteudo() {
        return raiz;
    }

    private void montar() {
        Button botaoNovo = Componentes.botao("Novo pedido", "botao-primario");
        Button botaoEditar = Componentes.botao("Editar");
        Button botaoExcluir = Componentes.botao("Excluir", "botao-perigo");
        for (Button b : new Button[] {botaoNovo, botaoEditar, botaoExcluir}) {
            b.setMaxWidth(Region.USE_PREF_SIZE);
        }
        botaoEditar.disableProperty().bind(tabela.getSelectionModel().selectedItemProperty().isNull());
        botaoExcluir.disableProperty().bind(tabela.getSelectionModel().selectedItemProperty().isNull());
        Region espaco = new Region();
        HBox.setHgrow(espaco, Priority.ALWAYS);
        resumo.getStyleClass().add("status");
        HBox barra = new HBox(8, botaoNovo, botaoEditar, botaoExcluir, espaco, resumo);
        barra.setAlignment(Pos.CENTER_LEFT);

        TableColumn<Pedido, Integer> colunaNumero = Componentes.colunaOrdenavel("Nº", Pedido::getNumero);
        TableColumn<Pedido, String> colunaItens = Componentes.coluna("Itens", Pedido::getResumoItens);
        TableColumn<Pedido, String> colunaStatus = Componentes.coluna("Status", p -> p.getStatus().getDescricao());
        TableColumn<Pedido, String> colunaData = Componentes.coluna("Feito em", p -> p.getCriadoEm().format(FORMATO_DATA));
        TableColumn<Pedido, BigDecimal> colunaTotal = Componentes.colunaOrdenavel("Total", Pedido::getValorTotal);
        colunaTotal.setCellFactory(coluna -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(BigDecimal valor, boolean vazio) {
                super.updateItem(valor, vazio);
                setText(vazio || valor == null ? null : Moeda.formatar(valor));
            }
        });
        colunaNumero.setPrefWidth(60);
        colunaNumero.setMaxWidth(80);
        colunaStatus.setPrefWidth(120);
        colunaData.setPrefWidth(140);
        colunaTotal.setPrefWidth(110);
        colunaNumero.getStyleClass().add("coluna-numero");
        colunaTotal.getStyleClass().add("coluna-numero");
        tabela.getColumns().add(colunaNumero);
        tabela.getColumns().add(colunaItens);
        tabela.getColumns().add(colunaStatus);
        tabela.getColumns().add(colunaData);
        tabela.getColumns().add(colunaTotal);
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        colunaItens.setPrefWidth(360);
        tabela.setPlaceholder(new Label("Nenhum pedido ainda. Clique em \"Novo pedido\"."));
        tabela.setRowFactory(t -> new TableRow<>() {
            @Override
            protected void updateItem(Pedido pedido, boolean vazio) {
                super.updateItem(pedido, vazio);
                StatusPedido status = vazio || pedido == null ? null : pedido.getStatus();
                pseudoClassStateChanged(CANCELADO, status == StatusPedido.CANCELADO);
                pseudoClassStateChanged(ENTREGUE, status == StatusPedido.ENTREGUE);
            }
        });
        VBox.setVgrow(tabela, Priority.ALWAYS);

        VBox painel = new VBox(10, barra, tabela);
        painel.getStyleClass().add("painel");
        VBox conteudo = new VBox(painel);
        VBox.setVgrow(painel, Priority.ALWAYS);
        conteudo.getStyleClass().add("conteudo");
        raiz.setCenter(conteudo);

        botaoNovo.setOnAction(e -> novoPedido());
        botaoEditar.setOnAction(e -> editarSelecionado());
        botaoExcluir.setOnAction(e -> excluirSelecionado());
        tabela.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                editarSelecionado();
            }
        });
    }

    @Override
    public void atualizar() {
        Pedido selecionado = tabela.getSelectionModel().getSelectedItem();
        tabela.getItems().setAll(servico.listar());
        if (selecionado != null && tabela.getItems().contains(selecionado)) {
            tabela.getSelectionModel().select(selecionado);
        }
        long emPreparo = tabela.getItems().stream().filter(p -> p.getStatus() == StatusPedido.EM_PREPARACAO).count();
        BigDecimal faturamento = tabela.getItems().stream()
                .filter(p -> p.getStatus() != StatusPedido.CANCELADO)
                .map(Pedido::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        resumo.setText(emPreparo + " em preparação  ·  Faturamento: " + Moeda.formatar(faturamento));
    }

    private void novoPedido() {
        PedidoDialogo dialogo = new PedidoDialogo(
                produtos.pesquisar("", ProdutoServico.Ordenacao.NOME), null, servico.proximoNumero());
        dialogo.initOwner(Componentes.janela(raiz));
        dialogo.showAndWait().ifPresent(resultado -> {
            if (Componentes.executar(raiz, () -> servico.criar(resultado.itens()))) {
                atualizar();
                tabela.getSelectionModel().select(0);
            }
        });
    }

    private void editarSelecionado() {
        Pedido pedido = tabela.getSelectionModel().getSelectedItem();
        if (pedido == null) {
            return;
        }
        PedidoDialogo dialogo = new PedidoDialogo(
                produtos.pesquisar("", ProdutoServico.Ordenacao.NOME), pedido, pedido.getNumero());
        dialogo.initOwner(Componentes.janela(raiz));
        dialogo.showAndWait().ifPresent(resultado -> {
            if (Componentes.executar(raiz, () -> servico.atualizar(pedido, resultado.itens(), resultado.status()))) {
                atualizar();
            }
        });
    }

    private void excluirSelecionado() {
        Pedido pedido = tabela.getSelectionModel().getSelectedItem();
        if (pedido != null
                && Componentes.confirmar(raiz, "Excluir pedido",
                        "Excluir o pedido nº " + pedido.getNumero() + "?\n"
                                + "Para registrar que o cliente desistiu, prefira mudar o status para Cancelado.")
                && Componentes.executar(raiz, () -> servico.excluir(pedido))) {
            atualizar();
        }
    }
}
