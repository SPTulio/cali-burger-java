package com.caliburger.ui;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Window;

import java.io.UncheckedIOException;
import java.util.Optional;
import java.util.function.Function;

/** Funções auxiliares de interface usadas por todas as telas. */
final class Componentes {

    /** Botões com texto fixo em português, independente do idioma do sistema. */
    static final ButtonType OK = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
    static final ButtonType CONFIRMAR = new ButtonType("Confirmar", ButtonBar.ButtonData.OK_DONE);
    static final ButtonType CANCELAR = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);

    private Componentes() {
    }

    static Button botao(String texto, String... classes) {
        Button botao = new Button(texto);
        botao.getStyleClass().addAll(classes);
        botao.setMaxWidth(Double.MAX_VALUE);
        return botao;
    }

    static Label titulo(String texto) {
        Label label = new Label(texto);
        label.getStyleClass().add("secao");
        return label;
    }

    static TextField campo(String dica) {
        TextField campo = new TextField();
        campo.setPromptText(dica);
        return campo;
    }

    static <T> TableColumn<T, String> coluna(String titulo, Function<T, String> valor) {
        TableColumn<T, String> coluna = new TableColumn<>(titulo);
        coluna.setCellValueFactory(dados -> new ReadOnlyStringWrapper(valor.apply(dados.getValue())));
        return coluna;
    }

    static <T, V extends Comparable<V>> TableColumn<T, V> colunaOrdenavel(String titulo, Function<T, V> valor) {
        TableColumn<T, V> coluna = new TableColumn<>(titulo);
        coluna.setCellValueFactory(dados -> new ReadOnlyObjectWrapper<>(valor.apply(dados.getValue())));
        return coluna;
    }

    /**
     * Executa uma ação que pode falhar por dados inválidos e mostra o erro ao usuário.
     *
     * @return true se a ação foi concluída
     */
    static boolean executar(Node origem, Runnable acao) {
        try {
            acao.run();
            return true;
        } catch (IllegalArgumentException e) {
            erro(origem, e.getMessage());
        } catch (UncheckedIOException e) {
            erro(origem, "Não foi possível salvar os dados no disco: " + e.getCause().getMessage());
        }
        return false;
    }

    static void erro(Node origem, String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.WARNING, mensagem, OK);
        alerta.setHeaderText(null);
        alerta.setTitle("Atenção");
        alerta.initOwner(janela(origem));
        alerta.showAndWait();
    }

    static boolean confirmar(Node origem, String titulo, String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION, mensagem, CONFIRMAR, CANCELAR);
        alerta.setHeaderText(null);
        alerta.setTitle(titulo);
        alerta.initOwner(janela(origem));
        return alerta.showAndWait().filter(CONFIRMAR::equals).isPresent();
    }

    static Optional<String> pedirTexto(Node origem, String titulo, String rotulo, String valorInicial) {
        TextInputDialog dialogo = new TextInputDialog(valorInicial);
        dialogo.setTitle(titulo);
        dialogo.setHeaderText(null);
        dialogo.setGraphic(null);
        dialogo.setContentText(rotulo);
        dialogo.getDialogPane().getButtonTypes().setAll(OK, CANCELAR);
        dialogo.initOwner(janela(origem));
        return dialogo.showAndWait();
    }

    static int lerInteiro(TextField campo, String nomeCampo) {
        String texto = campo.getText() == null ? "" : campo.getText().trim();
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(nomeCampo + " deve ser um número inteiro.");
        }
    }

    /** Lê o valor digitado no Spinner mesmo que o usuário não tenha apertado Enter. */
    static int lerSpinner(Spinner<Integer> spinner, String nomeCampo) {
        String texto = spinner.getEditor().getText().trim();
        try {
            int valor = Integer.parseInt(texto);
            spinner.getValueFactory().setValue(valor);
            return spinner.getValue();
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(nomeCampo + " deve ser um número inteiro.");
        }
    }

    static Window janela(Node node) {
        return node != null && node.getScene() != null ? node.getScene().getWindow() : null;
    }
}
