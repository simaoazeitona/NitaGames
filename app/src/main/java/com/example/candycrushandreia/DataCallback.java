package com.example.candycrushandreia;

import java.util.List;

public interface DataCallback {
    void onSuccess(int jogadas, List<Long> frutas, List<Long> quantidade, int[][] tabuleiro);

}
