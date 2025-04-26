package com.example.candycrushandreia;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class PreJogoActivity extends AppCompatActivity {

    private static final int GRID_SIZE = 6;
    private static final int NUM_FRUTAS = 6;
    private int[][] grid = new int[GRID_SIZE][GRID_SIZE];
    private ImageView[][] imageViews = new ImageView[GRID_SIZE][GRID_SIZE];

    // Controle de toque
    private int selectedRow = -1, selectedCol = -1;

    // Objetivos – frutas alvo e acumuladores
    private int frutaAlvo1, frutaAlvo2;
    private int qtdFruta1 = 0, qtdFruta2 = 0;
    private int targetFruta1 = 8, targetFruta2 = 8;

    // Views do layout (informações dos objetivos e jogadas)
    private TextView tvQtdFruta1, tvQtdFruta2, tvJogadasRestantes;
    private ImageView imgFruta1, imgFruta2;
    private int jogadasRestantes = 10;

    // ImageViews das estrelas na tela do jogo
    private ImageView ivStarGame1, ivStarGame2, ivStarGame3;

    // Variáveis para identificar o nível e controle do fim do jogo
    private String nivelId;
    private boolean gameOver = false;

    // Botões dos boosters e suas quantidades
// Depois - declare como RelativeLayout (ou FrameLayout, conforme o que você usa no XML)
    private FrameLayout btnBooster1, btnBooster2, btnBooster3, btnBooster4;
    private int booster1Count = 0, booster2Count = 0, booster3Count = 0, booster4Count = 0;

    // Variável para identificar qual booster está ativo (0 = nenhum; 1-4 conforme cada booster)
    private int activeBoosterType = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_jogo);

        // Recupera o nível passado via Intent
        nivelId = getIntent().getStringExtra("nivelId");

        // Referências das views principais
        tvQtdFruta1 = findViewById(R.id.tv_qtd_fruta1);
        tvQtdFruta2 = findViewById(R.id.tv_qtd_fruta2);
        tvJogadasRestantes = findViewById(R.id.tv_jogadas_restantes);
        imgFruta1 = findViewById(R.id.img_fruta1);
        imgFruta2 = findViewById(R.id.img_fruta2);
        tvJogadasRestantes.setText("\n" + jogadasRestantes);

        // Referências das ImageViews das estrelas exibidas na tela
        ivStarGame1 = findViewById(R.id.iv_star_game1);
        ivStarGame2 = findViewById(R.id.iv_star_game2);
        ivStarGame3 = findViewById(R.id.iv_star_game3);

        // Outras inicializações já existentes, por exemplo, grid, texto de jogadas, frutas, etc.

        // Recupera o container dos boosters
        LinearLayout layoutBoosters = findViewById(R.id.layout_boosters);

        // Cria listas para armazenar os boosters (se precisar atualizar as imagens ou as quantidades)
        List<ImageView> imgBoosters = new ArrayList<>();
        List<TextView> txtBoosters = new ArrayList<>();

        // Percorre cada item (inclusão) do layout de boosters
        for (int i = 0; i < layoutBoosters.getChildCount(); i++) {
            View boosterItem = layoutBoosters.getChildAt(i);
            // Obtenha os elementos internos do booster
            ImageView ivBoost = boosterItem.findViewById(R.id.img_boost);
            TextView tvBoost = boosterItem.findViewById(R.id.tv_boost);

            if (ivBoost != null && tvBoost != null) {
                imgBoosters.add(ivBoost);
                txtBoosters.add(tvBoost);
            }

            // Configura o clique para que cada booster seja tratado como um botão
            int boosterType = i + 1; // Supondo: 1 para boost1, 2 para boost2, etc.
            boosterItem.setOnClickListener(v -> {
                // Aqui chama a lógica para selecionar ou usar o booster
                // Por exemplo: se já estiver ativo, desativa; se não, ativa.
                toggleBooster(boosterType);
            });
        }

        // Exemplo de carregamento dos boosters do Firebase –
        // veja o código anterior para atualizar as quantidades.
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("users").document(uid)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists() && document.contains("Boosters")) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> boosters = (Map<String, Object>) document.get("Boosters");

                        if (boosters.containsKey("boost1") && imgBoosters.size() > 0 && txtBoosters.size() > 0) {
                            int quant = ((Number) boosters.get("boost1")).intValue();
                            imgBoosters.get(0).setImageResource(FirebaseHelper.getBoosterDrawable("boost1"));
                            txtBoosters.get(0).setText("x" + quant);
                        }
                        if (boosters.containsKey("boost2") && imgBoosters.size() > 1 && txtBoosters.size() > 1) {
                            int quant = ((Number) boosters.get("boost2")).intValue();
                            imgBoosters.get(1).setImageResource(FirebaseHelper.getBoosterDrawable("boost2"));
                            txtBoosters.get(1).setText("x" + quant);
                        }
                        if (boosters.containsKey("boost3") && imgBoosters.size() > 2 && txtBoosters.size() > 2) {
                            int quant = ((Number) boosters.get("boost3")).intValue();
                            imgBoosters.get(2).setImageResource(FirebaseHelper.getBoosterDrawable("boost3"));
                            txtBoosters.get(2).setText("x" + quant);
                        }
                        if (boosters.containsKey("boost4") && imgBoosters.size() > 3 && txtBoosters.size() > 3) {
                            int quant = ((Number) boosters.get("boost4")).intValue();
                            imgBoosters.get(3).setImageResource(FirebaseHelper.getBoosterDrawable("boost4"));
                            txtBoosters.get(3).setText("x" + quant);
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Log.e("PreJogoActivity", "Erro ao carregar boosters: " + e.getMessage())
                );
        // Inicializando os botões dos boosters
        btnBooster1 = findViewById(R.id.btn_booster1);
        btnBooster2 = findViewById(R.id.btn_booster2);
        btnBooster3 = findViewById(R.id.btn_booster3);
        btnBooster4 = findViewById(R.id.btn_booster4);

        // Carrega as quantidades de boosters da conta do usuário
        carregarBoosters();
        setupBoosterButtons();

        // Configura o GridLayout (tabuleiro)
        GridLayout gridLayout = findViewById(R.id.gridLayout);
        gridLayout.removeAllViews();
        gridLayout.setRowCount(GRID_SIZE);
        gridLayout.setColumnCount(GRID_SIZE);
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                ImageView img = new ImageView(this);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = 128;
                params.height = 128;
                params.setMargins(4, 4, 4, 4);
                img.setLayoutParams(params);
                img.setAdjustViewBounds(true);
                img.setBackgroundResource(R.drawable.quadradinho);
                imageViews[i][j] = img;
                configurarToque(img, i, j);
                gridLayout.addView(img);
            }
        }

        // Se há dados do nível na BD (Firebase)
        if (nivelId != null) {
            FirebaseHelper.carregarDadosFirestore(nivelId, new DataCallback() {
                @Override
                public void onSuccess(int jogadas, List<Long> frutas, List<Long> quantidade, int[][] tabuleiro) {
                    frutaAlvo1 = frutas.get(0).intValue();
                    frutaAlvo2 = frutas.get(1).intValue();

                    imgFruta1.setImageResource(getFrutaResource(frutaAlvo1));
                    imgFruta2.setImageResource(getFrutaResource(frutaAlvo2));

                    targetFruta1 = quantidade.get(0).intValue();
                    targetFruta2 = quantidade.get(1).intValue();

                    tvQtdFruta1.setText("0 / " + targetFruta1);
                    tvQtdFruta2.setText("0 / " + targetFruta2);

                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("niveis").document(nivelId)
                            .get()
                            .addOnSuccessListener(nivelDoc -> {
                                if (nivelDoc.exists() && nivelDoc.get("Jogadas") != null) {
                                    jogadasRestantes = nivelDoc.getLong("Jogadas").intValue();
                                } else {
                                    jogadasRestantes = 20; // fallback
                                }
                                tvJogadasRestantes.setText("\n" + jogadasRestantes);
                            })
                            .addOnFailureListener(e -> {
                                Log.e("Firestore", "Erro ao buscar jogadas: " + e.getMessage());
                                jogadasRestantes = 20;
                                tvJogadasRestantes.setText("\n" + jogadasRestantes);
                            });

                    grid = tabuleiro;
                    atualizarGrid();
                }
            });
        } else {
            // Valores padrão
            frutaAlvo1 = 1;
            frutaAlvo2 = 2;
        }

        gerarTabuleiroInicial();
        atualizarGrid();
        verificarCombinacoes();
        if (!existeMovimentoValido()) {
            embaralharTabuleiro();
        }
        atualizarEstrelasNoJogo();
    }

    // ========= Métodos dos Boosters =========

    // Carrega as quantidades de boosters do usuário a partir do Firestore
    private void carregarBoosters() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference userRef = FirebaseFirestore.getInstance().collection("users").document(uid);
        userRef.get().addOnSuccessListener(document -> {
            if (document.exists()){
                if(document.contains("Boosters")) {
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> boosters = (java.util.Map<String, Object>) document.get("Boosters");
                    booster1Count = boosters.containsKey("boost1") ? ((Number) boosters.get("boost1")).intValue() : 0;
                    booster2Count = boosters.containsKey("boost2") ? ((Number) boosters.get("boost2")).intValue() : 0;
                    booster3Count = boosters.containsKey("boost3") ? ((Number) boosters.get("boost3")).intValue() : 0;
                    booster4Count = boosters.containsKey("boost4") ? ((Number) boosters.get("boost4")).intValue() : 0;
                } else {
                    booster1Count = booster2Count = booster3Count = booster4Count = 0;
                }
            }
        }).addOnFailureListener(e -> {
            Log.e("Booster", "Erro ao carregar boosters: " + e.getMessage());
            booster1Count = booster2Count = booster3Count = booster4Count = 0;
        });
    }

    // Configura os botões dos boosters para selecionar ou cancelar a ação
    private void setupBoosterButtons() {
        btnBooster1.setOnClickListener(v -> toggleBooster(1));
        btnBooster2.setOnClickListener(v -> toggleBooster(2));
        btnBooster3.setOnClickListener(v -> toggleBooster(3));
        btnBooster4.setOnClickListener(v -> toggleBooster(4));
    }

    // Alterna a seleção de um booster (se clicado duas vezes, cancela a seleção sem perder a jogada)
    private void toggleBooster(int boosterType) {
        // Debug: Verificar o clique
        Log.d("Booster", "toggleBooster chamado para booster " + boosterType);
        Toast.makeText(this, "Booster " + boosterType + " clicado", Toast.LENGTH_SHORT).show();

        if (activeBoosterType == boosterType) {
            activeBoosterType = 0;
            resetBoosterButtonImages();
            return;
        }
        if ((boosterType == 1 && booster1Count <= 0) ||
                (boosterType == 2 && booster2Count <= 0) ||
                (boosterType == 3 && booster3Count <= 0) ||
                (boosterType == 4 && booster4Count <= 0)) {
            Toast.makeText(this, "Booster " + boosterType + " não disponível", Toast.LENGTH_SHORT).show();
            return;
        }
        activeBoosterType = boosterType;
        resetBoosterButtonImages();
        switch (boosterType) {
            case 1:
                ((ImageView) btnBooster1.findViewById(R.id.img_boost))
                        .setImageResource(R.drawable.booster1_verde);
                break;
            case 2:
                ((ImageView) btnBooster2.findViewById(R.id.img_boost))
                        .setImageResource(R.drawable.booster2_verde);
                break;
            case 3:
                ((ImageView) btnBooster3.findViewById(R.id.img_boost))
                        .setImageResource(R.drawable.booster3_verde);
                break;
            case 4:
                ((ImageView) btnBooster4.findViewById(R.id.img_boost))
                        .setImageResource(R.drawable.booster4_verde);
                break;
        }
    }


    // Remove o filtro de cores dos botões dos boosters
    private void resetBoosterButtonImages() {
        // Restaura a imagem padrão (não ativa) em cada booster.
        ((ImageView) btnBooster1.findViewById(R.id.img_boost))
                .setImageResource(R.drawable.booster1);
        ((ImageView) btnBooster2.findViewById(R.id.img_boost))
                .setImageResource(R.drawable.booster2);
        ((ImageView) btnBooster3.findViewById(R.id.img_boost))
                .setImageResource(R.drawable.booster3);
        ((ImageView) btnBooster4.findViewById(R.id.img_boost))
                .setImageResource(R.drawable.booster4);
    }


    // ========= Fim dos métodos dos boosters =========


    // Modifica o listener de toque das células para priorizar a ação do booster se ativado
    private void configurarToque(ImageView img, int row, int col) {
        img.setOnClickListener(v -> {
            if (gameOver)
                return;
            // Se houver um booster ativo, executa sua lógica e cancela a seleção
            if (activeBoosterType != 0) {
                executarBooster(activeBoosterType, row, col);
                return;
            }
            // Lógica padrão: seleção e troca de peças
            if (selectedRow == -1) {
                selectedRow = row;
                selectedCol = col;
                img.setAlpha(0.5f);
            } else {
                if (saoVizinhos(selectedRow, selectedCol, row, col)
                        && movimentoValido(selectedRow, selectedCol, row, col)) {
                    trocarComAnimacao(selectedRow, selectedCol, row, col);
                    jogadasRestantes--;
                    tvJogadasRestantes.setText("\n" + jogadasRestantes);
                    new Handler().postDelayed(() -> {
                        verificarCombinacoes();
                        atualizarEstrelasNoJogo();
                        checarFimDeJogo();
                    }, 500);
                }
                limparSelecao();
            }
        });
    }

    // Remove a seleção atual
    private void limparSelecao() {
        if (selectedRow != -1 && selectedCol != -1)
            imageViews[selectedRow][selectedCol].setAlpha(1f);
        selectedRow = -1;
        selectedCol = -1;
    }

    private boolean saoVizinhos(int r1, int c1, int r2, int c2) {
        return (Math.abs(r1 - r2) == 1 && c1 == c2)
                || (Math.abs(c1 - c2) == 1 && r1 == r2);
    }

    // Troca as peças com animação
    private void trocarComAnimacao(final int r1, final int c1, final int r2, final int c2) {
        trocar(r1, c1, r2, c2);
        final ImageView v1 = imageViews[r1][c1];
        final ImageView v2 = imageViews[r2][c2];
        final float deltaX = v2.getX() - v1.getX();
        final float deltaY = v2.getY() - v1.getY();
        v1.animate().translationX(deltaX).translationY(deltaY)
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        v1.setTranslationX(0);
                        v1.setTranslationY(0);
                        atualizarGrid();
                    }
                }).start();
        v2.animate().translationX(-deltaX).translationY(-deltaY)
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        v2.setTranslationX(0);
                        v2.setTranslationY(0);
                    }
                }).start();
    }

    // Swap simples no array
    private void trocar(int r1, int c1, int r2, int c2) {
        int temp = grid[r1][c1];
        grid[r1][c1] = grid[r2][c2];
        grid[r2][c2] = temp;
    }

    // Método que testa o movimento (faz o swap temporariamente para verificar se forma combinação)
    private boolean movimentoValido(int r1, int c1, int r2, int c2) {
        trocar(r1, c1, r2, c2);
        boolean valido = temCombinacao();
        trocar(r1, c1, r2, c2);
        return valido;
    }

    private boolean temCombinacao() {
        // Verifica horizontalmente
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE - 2; j++) {
                if (grid[i][j] != 0 &&
                        grid[i][j] == grid[i][j + 1] &&
                        grid[i][j] == grid[i][j + 2])
                    return true;
            }
        }
        // Verifica verticalmente
        for (int j = 0; j < GRID_SIZE; j++) {
            for (int i = 0; i < GRID_SIZE - 2; i++) {
                if (grid[i][j] != 0 &&
                        grid[i][j] == grid[i + 1][j] &&
                        grid[i][j] == grid[i + 2][j])
                    return true;
            }
        }
        return false;
    }

    private boolean existeMovimentoValido() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (j < GRID_SIZE - 1) {
                    trocar(i, j, i, j + 1);
                    if (temCombinacao()){
                        trocar(i, j, i, j+1);
                        return true;
                    }
                    trocar(i, j, i, j+1);
                }
                if (i < GRID_SIZE - 1) {
                    trocar(i, j, i+1, j);
                    if (temCombinacao()){
                        trocar(i, j, i+1, j);
                        return true;
                    }
                    trocar(i, j, i+1, j);
                }
            }
        }
        return false;
    }

    private void embaralharTabuleiro() {
        List<Integer> todasFrutas = new ArrayList<>();
        for (int i = 0; i < GRID_SIZE; i++){
            for (int j = 0; j < GRID_SIZE; j++){
                todasFrutas.add(grid[i][j]);
            }
        }
        Collections.shuffle(todasFrutas);
        int index = 0;
        for (int i = 0; i < GRID_SIZE; i++){
            for (int j = 0; j < GRID_SIZE; j++){
                grid[i][j] = todasFrutas.get(index++);
            }
        }
        atualizarGrid();
    }

    // Atualiza a grade: para cada célula, se o valor for menor ou igual a 0, repovemos imediatamente.
    private void atualizarGrid() {
        for (int i = 0; i < GRID_SIZE; i++){
            for (int j = 0; j < GRID_SIZE; j++){
                if (grid[i][j] <= 0) {
                    grid[i][j] = getRandomFruta();
                }
                int frutaId = grid[i][j];
                int resId = getResources().getIdentifier("objeto" + frutaId, "drawable", getPackageName());
                imageViews[i][j].setImageResource(resId);
                imageViews[i][j].setAlpha(1f);
            }
        }
    }

    // Aplica gravidade às peças – novas peças caem de cima com animação
    private void cairFrutas() {
        for (int j = 0; j < GRID_SIZE; j++) {
            int emptyRow = GRID_SIZE - 1;
            for (int i = GRID_SIZE - 1; i >= 0; i--) {
                if (grid[i][j] != 0) {
                    grid[emptyRow][j] = grid[i][j];
                    if (emptyRow != i) {
                        grid[i][j] = 0;
                    }
                    emptyRow--;
                }
            }
            while (emptyRow >= 0) {
                grid[emptyRow][j] = getRandomFruta();
                final ImageView cell = imageViews[emptyRow][j];
                cell.setTranslationY(-150f);
                cell.animate().translationY(0f).setDuration(300).start();
                emptyRow--;
            }
        }
        atualizarGrid();
        new Handler().postDelayed(() -> {
            verificarCombinacoes();
            checarFimDeJogo();
        }, 500);
    }

    private void verificarCombinacoes() {
        boolean[][] paraRemover = new boolean[GRID_SIZE][GRID_SIZE];
        // Combinações horizontais
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE - 2; j++) {
                int fruta = grid[i][j];
                if (fruta != 0 &&
                        fruta == grid[i][j + 1] &&
                        fruta == grid[i][j + 2]) {
                    paraRemover[i][j] = paraRemover[i][j + 1] = paraRemover[i][j + 2] = true;
                }
            }
        }
        // Combinações verticais
        for (int j = 0; j < GRID_SIZE; j++) {
            for (int i = 0; i < GRID_SIZE - 2; i++) {
                int fruta = grid[i][j];
                if (fruta != 0 &&
                        fruta == grid[i + 1][j] &&
                        fruta == grid[i + 2][j]) {
                    paraRemover[i][j] = paraRemover[i + 1][j] = paraRemover[i + 2][j] = true;
                }
            }
        }
        int pontuacao1 = 0, pontuacao2 = 0;
        boolean houveComb = false;
        for (int i = 0; i < GRID_SIZE; i++){
            for (int j = 0; j < GRID_SIZE; j++){
                if (paraRemover[i][j]) {
                    int fruta = grid[i][j];
                    if (fruta == frutaAlvo1)
                        pontuacao1++;
                    if (fruta == frutaAlvo2)
                        pontuacao2++;
                    grid[i][j] = 0;  // Marca para remoção
                    houveComb = true;
                }
            }
        }
        if (houveComb) {
            qtdFruta1 += pontuacao1;
            qtdFruta2 += pontuacao2;
            tvQtdFruta1.setText(qtdFruta1 + " / " + targetFruta1);
            tvQtdFruta2.setText(qtdFruta2 + " / " + targetFruta2);
            if (qtdFruta1 >= targetFruta1)
                tvQtdFruta1.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            else
                tvQtdFruta1.setTextColor(getResources().getColor(android.R.color.black));
            if (qtdFruta2 >= targetFruta2)
                tvQtdFruta2.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            else
                tvQtdFruta2.setTextColor(getResources().getColor(android.R.color.black));
            cairFrutas();
        }
        if (!existeMovimentoValido()){
            embaralharTabuleiro();
        }
    }

    private void checarFimDeJogo() {
        if (gameOver)
            return;
        if (qtdFruta1 >= targetFruta1 && qtdFruta2 >= targetFruta2) {
            gameOver = true;
            desbloquearProximoNivel();
            finalizarPartida();
        } else if (jogadasRestantes <= 0) {
            gameOver = true;
            finalizarPartida();
        }
    }

    private void desbloquearProximoNivel() {
        int nivel = 1;
        if(nivelId != null) {
            try {
                nivel = Integer.parseInt(nivelId.replace("nivel", ""));
            } catch (NumberFormatException e) {
                nivel = 1;
            }
        }
        final int proximoNivel = nivel + 1;
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference userRef = FirebaseFirestore.getInstance().collection("users").document(uid);
        userRef.get().addOnSuccessListener(document -> {
            if(document.exists() && document.get("NivelAtual") != null){
                long nivelAtual = document.getLong("NivelAtual");
                if(proximoNivel > nivelAtual) {
                    userRef.update("NivelAtual", proximoNivel)
                            .addOnSuccessListener(aVoid -> Log.d("Nivel", "NivelAtual atualizado para " + proximoNivel))
                            .addOnFailureListener(e -> Log.e("Nivel", "Erro ao atualizar NivelAtual: " + e.getMessage()));
                }
            } else {
                userRef.update("NivelAtual", proximoNivel)
                        .addOnSuccessListener(aVoid -> Log.d("Nivel", "NivelAtual criado com valor " + proximoNivel))
                        .addOnFailureListener(e -> Log.e("Nivel", "Erro ao criar NivelAtual: " + e.getMessage()));
            }
        }).addOnFailureListener(e -> Log.e("Nivel", "Erro ao obter dados do usuário: " + e.getMessage()));
    }

    private void finalizarPartida() {
        int nivel = 1;
        if (nivelId != null) {
            try {
                nivel = Integer.parseInt(nivelId.replace("nivel", ""));
            } catch (NumberFormatException e) {
                nivel = 1;
            }
        }
        double totalObjetivo = targetFruta1 + targetFruta2;
        double totalObtido = qtdFruta1 + qtdFruta2;
        double basePercent = (totalObtido / totalObjetivo) * 100.0;
        double bonusPercent = jogadasRestantes * (11 - nivel);
        double totalPercentage = basePercent + bonusPercent;

        int estrelas;
        if (totalPercentage >= 150)
            estrelas = 3;
        else if (totalPercentage >= 125)
            estrelas = 2;
        else if (totalPercentage >= 100)
            estrelas = 1;
        else
            estrelas = 0;
        exibirResultado(totalPercentage, estrelas);
    }

    private void exibirResultado(double percentageTotal, int estrelas) {
        Dialog resultDialog = new Dialog(this);
        resultDialog.setContentView(R.layout.item_fimjogo);
        resultDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        resultDialog.setCancelable(false);
        resultDialog.setCanceledOnTouchOutside(false);

        TextView tvResultTitle = resultDialog.findViewById(R.id.tv_result_title);
        TextView tvPercentage = resultDialog.findViewById(R.id.tv_percentage);
        TextView tvMoedasGanhas = resultDialog.findViewById(R.id.tv_moedas_ganhas);
        FrameLayout btnContinuar = resultDialog.findViewById(R.id.btn_continuar);
        View layoutResultado = resultDialog.findViewById(R.id.layout_resultado);

        // Elementos para os objetivos
        ImageView ivObjetivo1Fim = resultDialog.findViewById(R.id.iv_objetivo1_fim);
        ImageView ivObjetivo2Fim = resultDialog.findViewById(R.id.iv_objetivo2_fim);
        TextView tvObjetivo1 = resultDialog.findViewById(R.id.tv_objetivo1);
        TextView tvObjetivo2 = resultDialog.findViewById(R.id.tv_objetivo2);

        if (qtdFruta1 >= targetFruta1 && qtdFruta2 >= targetFruta2) {
            tvResultTitle.setText("Vitória!");
        } else {
            tvResultTitle.setText("Derrota!");
        }
        tvPercentage.setText(String.format("Alcançou: %.1f%%", percentageTotal));

        ivObjetivo1Fim.setImageResource(getFrutaResource(frutaAlvo1));
        ivObjetivo2Fim.setImageResource(getFrutaResource(frutaAlvo2));
        tvObjetivo1.setText(qtdFruta1 + " / " + targetFruta1);
        tvObjetivo2.setText(qtdFruta2 + " / " + targetFruta2);
        if (qtdFruta1 >= targetFruta1)
            tvObjetivo1.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        if (qtdFruta2 >= targetFruta2)
            tvObjetivo2.setTextColor(getResources().getColor(android.R.color.holo_green_dark));

        // Atualiza o fundo conforme as estrelas
        switch (estrelas) {
            case 0:
                layoutResultado.setBackgroundResource(R.drawable.fim0estrelas);
                break;
            case 1:
                layoutResultado.setBackgroundResource(R.drawable.fim1estrelas);
                break;
            case 2:
                layoutResultado.setBackgroundResource(R.drawable.fim2estrelas);
                break;
            case 3:
                layoutResultado.setBackgroundResource(R.drawable.fim3estrelas);
                break;
            default:
                layoutResultado.setBackgroundResource(R.drawable.fim0estrelas);
                break;
        }
        // Atualiza as estrelas para o usuário
        atualizarEstrelasParaNivel(nivelId, estrelas);

        // --- Cálculo das moedas ganhas ---
        long moedasGanhas = 0;
        if (tvResultTitle.getText().toString().equals("Vitória!")) {
            String nivelNumerico = nivelId.replaceAll("\\D+", ""); // Remove tudo que não é número
            int nivel = Integer.parseInt(nivelNumerico);
            double multiplicador = Double.parseDouble("1." + nivel); // ex: 1.6 para nível 6

            moedasGanhas = (long) (100 * estrelas * multiplicador); // Faz o cast para long
            tvMoedasGanhas.setText("Moedas: " + moedasGanhas);

            // Cria uma cópia final para uso na lambda
            final long moedasGanhasFinal = moedasGanhas;

            // Atualiza o BD: obtém as moedas atuais e soma as ganhas
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DocumentReference userRef = FirebaseFirestore.getInstance().collection("users").document(uid);
            userRef.get().addOnSuccessListener(document -> {
                if (document.exists()) {
                    long moedasAtuais = 0;
                    if (document.contains("Moedas"))
                        moedasAtuais = document.getLong("Moedas");
                    long novoValor = moedasAtuais + moedasGanhasFinal;
                    userRef.update("Moedas", novoValor)
                            .addOnSuccessListener(aVoid -> Log.d("Moedas", "Moedas atualizadas para: " + novoValor))
                            .addOnFailureListener(e -> Log.e("Moedas", "Erro ao atualizar moedas: " + e.getMessage()));
                }
            });
        } else {
            moedasGanhas = 0;
            tvMoedasGanhas.setText("Moedas: 0");
        }

        btnContinuar.setOnClickListener(v -> {
            resultDialog.dismiss();
            startActivity(new Intent(PreJogoActivity.this, GameHomeActivity.class));
            finish();
        });

        resultDialog.show();
    }

    private void atualizarEstrelasParaNivel(String nivelId, int novasEstrelas) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference userRef = FirebaseFirestore.getInstance().collection("users").document(uid);
        userRef.get().addOnSuccessListener(document -> {
            if (document.exists()) {
                java.util.Map<String, Long> estrelasPorNivel;
                if (document.contains("EstrelasPorNivel") && document.get("EstrelasPorNivel") != null)
                    estrelasPorNivel = (java.util.Map<String, Long>) document.get("EstrelasPorNivel");
                else
                    estrelasPorNivel = new java.util.HashMap<>();
                long recordAtual = estrelasPorNivel.containsKey(nivelId) ? estrelasPorNivel.get(nivelId) : 0;
                if (novasEstrelas > recordAtual) {
                    estrelasPorNivel.put(nivelId, (long) novasEstrelas);
                    userRef.update("EstrelasPorNivel", estrelasPorNivel)
                            .addOnSuccessListener(aVoid ->
                                    Log.d("Estrelas", "Record atualizado para " + nivelId + ": " + novasEstrelas))
                            .addOnFailureListener(e ->
                                    Log.e("Estrelas", "Erro ao atualizar o record: " + e.getMessage()));
                }
            }
        }).addOnFailureListener(e -> Log.e("Estrelas", "Erro ao obter dados do usuário: " + e.getMessage()));
    }

    private int getFrutaResource(int frutaCodigo) {
        switch (frutaCodigo) {
            case 1: return R.drawable.objeto1;
            case 2: return R.drawable.objeto2;
            case 3: return R.drawable.objeto3;
            case 4: return R.drawable.objeto4;
            case 5: return R.drawable.objeto5;
            case 6: return R.drawable.objeto6;
            default: return R.drawable.objeto1;
        }
    }

    private void gerarTabuleiroInicial() {
        Random random = new Random();
        do {
            for (int i = 0; i < GRID_SIZE; i++){
                for (int j = 0; j < GRID_SIZE; j++){
                    grid[i][j] = getRandomFruta();
                }
            }
        } while (existeCombinaçãoInicial());
    }

    private boolean existeCombinaçãoHorizontal() {
        for (int i = 0; i < GRID_SIZE; i++){
            for (int j = 0; j < GRID_SIZE - 2; j++){
                if (grid[i][j] == grid[i][j+1] && grid[i][j] == grid[i][j+2])
                    return true;
            }
        }
        return false;
    }

    private boolean existeCombinaçãoVertical() {
        for (int j = 0; j < GRID_SIZE; j++){
            for (int i = 0; i < GRID_SIZE - 2; i++){
                if (grid[i][j] == grid[i+1][j] && grid[i][j] == grid[i+2][j])
                    return true;
            }
        }
        return false;
    }

    private boolean existeCombinaçãoInicial() {
        return existeCombinaçãoHorizontal() || existeCombinaçãoVertical();
    }

    private int getRandomFruta(){
        return 1 + new Random().nextInt(NUM_FRUTAS);
    }

    // Atualiza as estrelas na tela do jogo usando somente o percentual base
    private void atualizarEstrelasNoJogo() {
        double totalObjetivo = targetFruta1 + targetFruta2;
        double totalObtido = qtdFruta1 + qtdFruta2;
        double percent = (totalObtido / totalObjetivo) * 100.0;
        int estrelas;
        if (percent >= 150)
            estrelas = 3;
        else if (percent >= 125)
            estrelas = 2;
        else if (percent >= 100)
            estrelas = 1;
        else
            estrelas = 0;
        ivStarGame1.setImageResource(estrelas >= 1 ? R.drawable.comestrela : R.drawable.semestrela);
        ivStarGame2.setImageResource(estrelas >= 2 ? R.drawable.comestrela : R.drawable.semestrela);
        ivStarGame3.setImageResource(estrelas >= 3 ? R.drawable.comestrela : R.drawable.semestrela);
    }

    // ========= Lógica dos Boosters =========

    // Executa o efeito do booster na célula clicada
// Executa o efeito do booster na célula clicada
    private void executarBooster(int boosterType, int row, int col) {
        List<int[]> positionsToRemove = new ArrayList<>();
        switch (boosterType) {
            case 1:
                // Booster 1: Remove o bloco clicado + adjacentes (acima, abaixo, esquerda e direita)
                positionsToRemove.add(new int[]{row, col});
                if (row > 0) positionsToRemove.add(new int[]{row - 1, col});
                if (row < GRID_SIZE - 1) positionsToRemove.add(new int[]{row + 1, col});
                if (col > 0) positionsToRemove.add(new int[]{row, col - 1});
                if (col < GRID_SIZE - 1) positionsToRemove.add(new int[]{row, col + 1});
                break;
            case 2:
                // Booster 2: Remove APENAS o bloco clicado.
                positionsToRemove.add(new int[]{row, col});
                break;
            case 3:
                // Booster 3: Remove TODA a coluna (vertical) em que o bloco foi clicado.
                for (int i = 0; i < GRID_SIZE; i++) {
                    positionsToRemove.add(new int[]{i, col});
                }
                break;
            case 4:
                // Booster 4: Remove todos os blocos iguais ao clicado (mesma fruta).
                int targetFruit = grid[row][col];
                for (int i = 0; i < GRID_SIZE; i++) {
                    for (int j = 0; j < GRID_SIZE; j++) {
                        if (grid[i][j] == targetFruit) {
                            positionsToRemove.add(new int[]{i, j});
                        }
                    }
                }
                break;
            default:
                return;
        }

        // Processa as remoções e atualiza objetivos
        for (int[] pos : positionsToRemove) {
            int r = pos[0], c = pos[1];
            if (grid[r][c] != 0) {
                int fruit = grid[r][c];
                if (fruit == frutaAlvo1)
                    qtdFruta1++;
                if (fruit == frutaAlvo2)
                    qtdFruta2++;
                grid[r][c] = 0;
            }
        }

        tvQtdFruta1.setText(qtdFruta1 + " / " + targetFruta1);
        tvQtdFruta2.setText(qtdFruta2 + " / " + targetFruta2);
        // Atualiza as cores dos textos conforme a condição
        tvQtdFruta1.setTextColor(qtdFruta1 >= targetFruta1 ?
                getResources().getColor(android.R.color.holo_green_dark) :
                getResources().getColor(android.R.color.black));
        tvQtdFruta2.setTextColor(qtdFruta2 >= targetFruta2 ?
                getResources().getColor(android.R.color.holo_green_dark) :
                getResources().getColor(android.R.color.black));

        // Consome uma jogada
        jogadasRestantes--;
        tvJogadasRestantes.setText("\n" + jogadasRestantes);

        // Atualiza a contagem do booster utilizado e persiste no Firestore.
        // Aqui, também atualizamos o TextView do booster para refletir a nova quantidade.
        switch (boosterType) {
            case 1:
                booster1Count--;
                atualizarBoosterFirestore(1, booster1Count);
                ((TextView) btnBooster1.findViewById(R.id.tv_boost)).setText("x" + booster1Count);
                break;
            case 2:
                booster2Count--;
                atualizarBoosterFirestore(2, booster2Count);
                ((TextView) btnBooster2.findViewById(R.id.tv_boost)).setText("x" + booster2Count);
                break;
            case 3:
                booster3Count--;
                atualizarBoosterFirestore(3, booster3Count);
                ((TextView) btnBooster3.findViewById(R.id.tv_boost)).setText("x" + booster3Count);
                break;
            case 4:
                booster4Count--;
                atualizarBoosterFirestore(4, booster4Count);
                ((TextView) btnBooster4.findViewById(R.id.tv_boost)).setText("x" + booster4Count);
                break;
        }

        activeBoosterType = 0;
        resetBoosterButtonImages();

        new Handler().postDelayed(() -> {
            cairFrutas();
            atualizarEstrelasNoJogo();
            checarFimDeJogo();
        }, 300);
    }

    // Atualiza a quantidade do booster no Firestore para o usuário
    private void atualizarBoosterFirestore(int boosterType, int newCount) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference userRef = FirebaseFirestore.getInstance().collection("users").document(uid);
        userRef.get().addOnSuccessListener(document -> {
            if (document.exists()){
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> boosters = document.contains("Boosters")
                        ? (java.util.Map<String, Object>) document.get("Boosters")
                        : new java.util.HashMap<>();
                boosters.put("boost" + boosterType, newCount);
                userRef.update("Boosters", boosters)
                        .addOnSuccessListener(aVoid ->
                                Log.d("Booster", "Booster " + boosterType + " atualizado para " + newCount))
                        .addOnFailureListener(e ->
                                Log.e("Booster", "Erro ao atualizar booster " + boosterType + ": " + e.getMessage()));
            }
        }).addOnFailureListener(e ->
                Log.e("Booster", "Erro ao obter dados do usuário: " + e.getMessage()));
    }
}
