package com.caliburger.ui;

import javafx.scene.Node;

/** Uma aba da janela principal. */
public interface Tela {

    String getTitulo();

    Node getConteudo();

    /** Recarrega os dados exibidos a partir dos serviços. */
    void atualizar();
}
