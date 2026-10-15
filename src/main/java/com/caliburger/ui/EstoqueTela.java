package com.caliburger.ui;

import com.caliburger.modelo.Ingrediente;
import com.caliburger.servico.EstoqueServico;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

/** Aba de controle de estoque dos ingredientes. */
public final class EstoqueTela implements Tela {

    private static final PseudoClass ESTOQUE_BAIXO = PseudoClass.getPseudoClass("estoque-baixo");

    private final EstoqueServico servico;
    private final BorderPane raiz = new BorderPane();
    private final TableView<Ingrediente> tabela = new TableView<>();
    private final Label alerta = new Label();

    private final TextField campoNome = Componentes.campo("Ex.: Pão de hambúrguer");
    private final TextField campoQuantidade = Componentes.campo("0");
    private final TextField campoMinimo = Componentes.campo("0");
    private final Label rotuloFormulario = Componentes.titulo("Novo ingrediente");
    private final Button botaoSalvar = Componentes.botao("Cadastrar", "botao-primario");
    private final Button botaoCancelar = Componentes.botao("Cancelar edição");
    private final Button botaoExcluir = Componentes.botao("Excluir", "botao-perigo");
    private final Spinner<Integer> spinnerMovimento = new Spinner<>(1, 100_000, 1);
    private final Button botaoEntrada = Componentes.botao("+ Entrada");
    private final Button botaoBaixa = Componentes.botao("- Baixa");

    public EstoqueTela(EstoqueServico servico) {
        this.servico = servico;
        montar();
        atualizar();
    }

    @Override
    public String getTitulo() {
        return "Estoque";
    }

    @Override
    public Node getConteudo() {
        return raiz;
    }

    private void montar() {
        // Cadastro e ajuste
        GridPane campos = new GridPane();
        campos.setHgap(8);
        campos.setVgap(4);
        campos.add(new Label("Quantidade"), 0, 0);
        campos.add(new Label("Mínimo"), 1, 0);
        campos.add(campoQuantidade, 0, 1);
        campos.add(campoMinimo, 1, 1);

        // Movimentação rápida do item selecionado
        spinnerMovimento.setEditable(true);
        spinnerMovimento.setMaxWidth(Double.MAX_VALUE);
        HBox botoesMovimento = new HBox(8, botaoEntrada, botaoBaixa);
        HBox.setHgrow(botaoEntrada, Priority.ALWAYS);
        HBox.setHgrow(botaoBaixa, Priority.ALWAYS);

        VBox formulario = new VBox(8,
                rotuloFormulario,
                new Label("Nome"), campoNome,
                campos,
                botaoSalvar, botaoCancelar, botaoExcluir,
                new Separator(),
                Componentes.titulo("Movimentar estoque"),
                new Label("Quantidade a movimentar"), spinnerMovimento,
                botoesMovimento);
        formulario.getStyleClass().add("painel");
        formulario.setPrefWidth(260);

        // Tabela
        TableColumn<Ingrediente, String> colunaNome = Componentes.coluna("Ingrediente", Ingrediente::getNome);
        TableColumn<Ingrediente, String> colunaQtd = Componentes.coluna("Quantidade", i -> String.valueOf(i.getQuantidade()));
        TableColumn<Ingrediente, String> colunaMinimo = Componentes.coluna("Mínimo", i -> String.valueOf(i.getEstoqueMinimo()));
        TableColumn<Ingrediente, String> colunaSituacao = Componentes.coluna("Situação",
                i -> i.isEstoqueBaixo() ? "Repor" : "OK");
        for (TableColumn<Ingrediente, String> c : List.of(colunaQtd, colunaMinimo, colunaSituacao)) {
            c.setPrefWidth(100);
            c.getStyleClass().add("coluna-numero");
        }
        tabela.getColumns().add(colunaNome);
        tabela.getColumns().add(colunaQtd);
        tabela.getColumns().add(colunaMinimo);
        tabela.getColumns().add(colunaSituacao);
        tabela.getColumns().forEach(c -> c.setSortable(false));
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_FIRST_COLUMN);
        tabela.setPlaceholder(new Label("Nenhum ingrediente cadastrado."));
        tabela.setRowFactory(t -> new TableRow<>() {
            @Override
            protected void updateItem(Ingrediente item, boolean vazio) {
                super.updateItem(item, vazio);
                pseudoClassStateChanged(ESTOQUE_BAIXO, !vazio && item != null && item.isEstoqueBaixo());
            }
        });
        VBox.setVgrow(tabela, Priority.ALWAYS);

        alerta.getStyleClass().add("status");
        VBox lista = new VBox(8, Componentes.titulo("Ingredientes"), tabela, alerta);
        lista.getStyleClass().add("painel");
        HBox.setHgrow(lista, Priority.ALWAYS);

        HBox conteudo = new HBox(12, formulario, lista);
        conteudo.getStyleClass().add("conteudo");
        raiz.setCenter(conteudo);

        tabela.getSelectionModel().selectedItemProperty().addListener((obs, antigo, item) -> preencher(item));
        botaoSalvar.setOnAction(e -> salvar());
        botaoCancelar.setOnAction(e -> tabela.getSelectionModel().clearSelection());
        botaoExcluir.setOnAction(e -> excluirSelecionado());
        botaoEntrada.setOnAction(e -> movimentar(true));
        botaoBaixa.setOnAction(e -> movimentar(false));
        preencher(null);
    }

    @Override
    public void atualizar() {
        Ingrediente selecionado = tabela.getSelectionModel().getSelectedItem();
        tabela.getItems().setAll(servico.listar());
        if (selecionado != null && tabela.getItems().contains(selecionado)) {
            tabela.getSelectionModel().select(selecionado);
            preencher(selecionado);
        }
        tabela.refresh();
        List<Ingrediente> baixos = servico.listarEstoqueBaixo();
        alerta.setText(baixos.isEmpty()
                ? "Nenhum ingrediente abaixo do estoque mínimo."
                : baixos.size() + " ingrediente(s) precisando de reposição.");
    }

    private void preencher(Ingrediente item) {
        boolean editando = item != null;
        rotuloFormulario.setText(editando ? "Ajustar ingrediente" : "Novo ingrediente");
        botaoSalvar.setText(editando ? "Salvar ajuste" : "Cadastrar");
        campoNome.setText(editando ? item.getNome() : "");
        campoNome.setDisable(editando);
        campoQuantidade.setText(editando ? String.valueOf(item.getQuantidade()) : "");
        campoMinimo.setText(editando ? String.valueOf(item.getEstoqueMinimo()) : "");
        botaoCancelar.setVisible(editando);
        botaoCancelar.setManaged(editando);
        botaoExcluir.setDisable(!editando);
        botaoEntrada.setDisable(!editando);
        botaoBaixa.setDisable(!editando);
        spinnerMovimento.setDisable(!editando);
    }

    private void salvar() {
        Ingrediente selecionado = tabela.getSelectionModel().getSelectedItem();
        boolean ok = Componentes.executar(raiz, () -> {
            int quantidade = campoQuantidade.getText().isBlank() ? 0 : Componentes.lerInteiro(campoQuantidade, "A quantidade");
            int minimo = campoMinimo.getText().isBlank() ? 0 : Componentes.lerInteiro(campoMinimo, "O estoque mínimo");
            if (selecionado == null) {
                servico.cadastrar(campoNome.getText(), quantidade, minimo);
            } else {
                servico.ajustar(selecionado, quantidade, minimo);
            }
        });
        if (ok) {
            tabela.getSelectionModel().clearSelection();
            atualizar();
            campoNome.requestFocus();
        }
    }

    private void movimentar(boolean entrada) {
        Ingrediente selecionado = tabela.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            return;
        }
        if (Componentes.executar(raiz, () -> {
            int quantidade = Componentes.lerSpinner(spinnerMovimento, "A quantidade");
            if (entrada) {
                servico.darEntrada(selecionado, quantidade);
            } else {
                servico.darBaixa(selecionado, quantidade);
            }
        })) {
            atualizar();
        }
    }

    private void excluirSelecionado() {
        Ingrediente selecionado = tabela.getSelectionModel().getSelectedItem();
        if (selecionado != null
                && Componentes.confirmar(raiz, "Excluir ingrediente", "Excluir \"" + selecionado.getNome() + "\" do estoque?")
                && Componentes.executar(raiz, () -> servico.excluir(selecionado))) {
            tabela.getSelectionModel().clearSelection();
            atualizar();
        }
    }
}
