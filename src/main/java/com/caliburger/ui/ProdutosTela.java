package com.caliburger.ui;

import com.caliburger.modelo.Produto;
import com.caliburger.servico.ProdutoServico;
import com.caliburger.servico.ProdutoServico.Ordenacao;
import com.caliburger.util.Moeda;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;

/** Aba de cadastro de produtos. */
public final class ProdutosTela implements Tela {

    private final ProdutoServico servico;
    private final BorderPane raiz = new BorderPane();

    private final TextField campoNome = Componentes.campo("Ex.: X-Burger");
    private final TextField campoPreco = Componentes.campo("Ex.: 25,90");
    private final TextField campoPesquisa = Componentes.campo("Pesquisar produto...");
    private final ComboBox<Ordenacao> comboOrdenacao = new ComboBox<>();
    private final TableView<Produto> tabela = new TableView<>();
    private final Label rotuloFormulario = Componentes.titulo("Novo produto");
    private final Label status = new Label();
    private final Button botaoSalvar = Componentes.botao("Adicionar", "botao-primario");
    private final Button botaoExcluir = Componentes.botao("Excluir", "botao-perigo");
    private final Button botaoCancelar = Componentes.botao("Cancelar edição");

    public ProdutosTela(ProdutoServico servico) {
        this.servico = servico;
        montar();
        atualizar();
    }

    @Override
    public String getTitulo() {
        return "Produtos";
    }

    @Override
    public Node getConteudo() {
        return raiz;
    }

    private void montar() {
        // Formulário à esquerda
        Button botaoExcluirTodos = Componentes.botao("Excluir todos os produtos", "botao-discreto");
        VBox formulario = new VBox(8,
                rotuloFormulario,
                new Label("Nome"), campoNome,
                new Label("Preço (R$)"), campoPreco,
                botaoSalvar, botaoCancelar, botaoExcluir,
                new Separator(),
                botaoExcluirTodos);
        formulario.getStyleClass().add("painel");
        formulario.setPrefWidth(260);

        // Lista à direita
        comboOrdenacao.getItems().setAll(Ordenacao.values());
        comboOrdenacao.setValue(Ordenacao.NOME);
        HBox.setHgrow(campoPesquisa, Priority.ALWAYS);
        HBox filtros = new HBox(8, campoPesquisa, new Label("Ordenar:"), comboOrdenacao);
        filtros.getStyleClass().add("barra");

        TableColumn<Produto, String> colunaNome = Componentes.coluna("Produto", Produto::getNome);
        TableColumn<Produto, String> colunaPreco = Componentes.coluna("Preço", p -> Moeda.formatar(p.getPreco()));
        colunaPreco.getStyleClass().add("coluna-numero");
        colunaNome.setPrefWidth(400);
        colunaPreco.setPrefWidth(120);
        colunaPreco.setMaxWidth(160);
        colunaNome.setSortable(false);
        colunaPreco.setSortable(false);
        tabela.getColumns().add(colunaNome);
        tabela.getColumns().add(colunaPreco);
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        tabela.setPlaceholder(new Label("Nenhum produto cadastrado."));
        VBox.setVgrow(tabela, Priority.ALWAYS);

        VBox lista = new VBox(8, filtros, tabela, status);
        lista.getStyleClass().add("painel");
        HBox.setHgrow(lista, Priority.ALWAYS);

        HBox conteudo = new HBox(12, formulario, lista);
        conteudo.getStyleClass().add("conteudo");
        raiz.setCenter(conteudo);

        // Eventos
        tabela.getSelectionModel().selectedItemProperty().addListener((obs, antigo, produto) -> preencher(produto));
        campoPesquisa.textProperty().addListener((obs, antigo, novo) -> atualizar());
        comboOrdenacao.valueProperty().addListener((obs, antigo, novo) -> atualizar());
        botaoSalvar.setOnAction(e -> salvar());
        campoPreco.setOnAction(e -> salvar());
        botaoCancelar.setOnAction(e -> tabela.getSelectionModel().clearSelection());
        botaoExcluir.setOnAction(e -> excluirSelecionado());
        botaoExcluirTodos.setOnAction(e -> excluirTodos());
        preencher(null);
    }

    @Override
    public void atualizar() {
        Produto selecionado = tabela.getSelectionModel().getSelectedItem();
        tabela.getItems().setAll(servico.pesquisar(campoPesquisa.getText(), comboOrdenacao.getValue()));
        if (selecionado != null && tabela.getItems().contains(selecionado)) {
            tabela.getSelectionModel().select(selecionado);
        }
        int total = servico.listar().size();
        int exibidos = tabela.getItems().size();
        status.setText(exibidos == total
                ? total + " produto(s) cadastrado(s)"
                : exibidos + " de " + total + " produto(s)");
    }

    /** Selecionar um produto na tabela coloca o formulário em modo de edição. */
    private void preencher(Produto produto) {
        boolean editando = produto != null;
        rotuloFormulario.setText(editando ? "Editar produto" : "Novo produto");
        botaoSalvar.setText(editando ? "Salvar alterações" : "Adicionar");
        botaoExcluir.setDisable(!editando);
        botaoCancelar.setVisible(editando);
        botaoCancelar.setManaged(editando);
        campoNome.setText(editando ? produto.getNome() : "");
        campoPreco.setText(editando ? Moeda.formatar(produto.getPreco()).replace("R$ ", "") : "");
    }

    private void salvar() {
        Produto selecionado = tabela.getSelectionModel().getSelectedItem();
        boolean ok = Componentes.executar(raiz, () -> {
            BigDecimal preco = Moeda.converter(campoPreco.getText());
            if (selecionado == null) {
                servico.adicionar(campoNome.getText(), preco);
            } else {
                servico.editar(selecionado, campoNome.getText(), preco);
            }
        });
        if (ok) {
            tabela.getSelectionModel().clearSelection();
            atualizar();
            preencher(null);
            campoNome.requestFocus();
        }
    }

    private void excluirSelecionado() {
        Produto selecionado = tabela.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            return;
        }
        String mensagem = "Excluir \"" + selecionado.getNome() + "\"?\n"
                + "Ele também será retirado dos cardápios. Pedidos já feitos não são alterados.";
        if (Componentes.confirmar(raiz, "Excluir produto", mensagem)
                && Componentes.executar(raiz, () -> servico.excluir(selecionado))) {
            atualizar();
        }
    }

    private void excluirTodos() {
        if (servico.listar().isEmpty()) {
            return;
        }
        if (Componentes.confirmar(raiz, "Excluir todos os produtos",
                "Excluir todos os " + servico.listar().size() + " produtos? Esta ação não pode ser desfeita.")
                && Componentes.executar(raiz, servico::excluirTodos)) {
            atualizar();
        }
    }
}
