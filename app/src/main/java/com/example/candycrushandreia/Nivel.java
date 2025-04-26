package com.example.candycrushandreia;

import java.util.List;

public class Nivel {
    private int jogadas;
    private List<Integer> frutas;
    private List<Integer> quantidade;
    private int[][] tabuleiro;

    public Nivel(int jogadas, List<Integer> frutas, List<Integer> quantidade, int[][] tabuleiro) {
        this.jogadas = jogadas;
        this.frutas = frutas;
        this.quantidade = quantidade;
        this.tabuleiro = tabuleiro;
    }

    public int getJogadas() {
        return jogadas;
    }

    public List<Integer> getFrutas() {
        return frutas;
    }

    public List<Integer> getQuantidade() {
        return quantidade;
    }

    public int[][] getTabuleiro() {
        return tabuleiro;
    }
}
