package com.example.candycrushandreia;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.*;

public class FirebaseHelper {
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static void criarNivel1() { criarNivel("nivel1", 10, Arrays.asList(1, 2), Arrays.asList(5, 8)); }
    public static void criarNivel2() { criarNivel("nivel2", 15, Arrays.asList(3, 4), Arrays.asList(8, 12)); }
    public static void criarNivel3() { criarNivel("nivel3", 20, Arrays.asList(5, 1), Arrays.asList(11, 16)); }
    public static void criarNivel4() { criarNivel("nivel4", 25, Arrays.asList(2, 3), Arrays.asList(14, 20)); }
    public static void criarNivel5() { criarNivel("nivel5", 30, Arrays.asList(4, 5), Arrays.asList(17, 24)); }

    private static void criarNivel(String nivelId, int jogadas, List<Integer> frutas, List<Integer> quantidade) {
        Map<String, Object> nivel = new HashMap<>();
        nivel.put("Jogadas", jogadas);
        nivel.put("Frutas", frutas);
        nivel.put("Quantidade", quantidade);

        // Gera um tabuleiro aleatório sem combinações de 3+
        int[][] gridValido = gerarTabuleiroSemCombinacoes();
        Map<String, List<Integer>> tabuleiro = new HashMap<>();
        for (int i = 0; i < 6; i++) {
            List<Integer> linha = new ArrayList<>();
            for (int j = 0; j < 6; j++) {
                linha.add(gridValido[i][j]);
            }
            tabuleiro.put("linha" + i, linha);
        }
        nivel.put("Tabuleiro", tabuleiro);

        db.collection("niveis").document(nivelId).set(nivel)
                .addOnSuccessListener(aVoid -> System.out.println("Nível " + nivelId + " criado com sucesso!"))
                .addOnFailureListener(e -> System.err.println("Erro ao criar nível " + nivelId + ": " + e.getMessage()));
    }

    // Gera um tabuleiro sem formar combinações de 3 ou mais elementos iguais
    private static int[][] gerarTabuleiroSemCombinacoes() {
        Random random = new Random();
        int[][] grid;
        do {
            grid = new int[6][6];
            for (int i = 0; i < 6; i++) {
                for (int j = 0; j < 6; j++) {
                    int valor;
                    do {
                        valor = random.nextInt(5) + 1;
                        grid[i][j] = valor;
                    } while (causaCombinacao(grid, i, j));
                }
            }
        } while (temCombinacoes(grid));
        return grid;
    }

    // Verifica se a posição (i, j) causa uma combinação de 3+ elementos iguais
    private static boolean causaCombinacao(int[][] grid, int i, int j) {
        int val = grid[i][j];
        if (j >= 2 && grid[i][j - 1] == val && grid[i][j - 2] == val) return true;
        if (i >= 2 && grid[i - 1][j] == val && grid[i - 2][j] == val) return true;
        return false;
    }

    // Verifica se há alguma combinação de 3 ou mais elementos iguais no tabuleiro
    private static boolean temCombinacoes(int[][] grid) {
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                int val = grid[i][j];
                if (j <= 3 && val == grid[i][j + 1] && val == grid[i][j + 2]) return true;
                if (i <= 3 && val == grid[i + 1][j] && val == grid[i + 2][j]) return true;
            }
        }
        return false;
    }

    /**
     * Carrega os dados do nível (jogadas, frutas, quantidades e tabuleiro)
     * e retorna esses dados através do callback.
     */
    public static void carregarDadosFirestore(String nivelId, DataCallback callback) {
        db.collection("niveis").document(nivelId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                DocumentSnapshot document = task.getResult();
                int jogadas = document.getLong("Jogadas").intValue();
                List<Long> frutas = (List<Long>) document.get("Frutas");
                List<Long> quantidade = (List<Long>) document.get("Quantidade");

                // Reconstrói o tabuleiro a partir do Map armazenado
                Map<String, List<Long>> tabuleiroLista = (Map<String, List<Long>>) document.get("Tabuleiro");
                int[][] grid = new int[6][6];
                for (int i = 0; i < 6; i++) {
                    List<Long> linha = tabuleiroLista.get("linha" + i);
                    for (int j = 0; j < 6; j++) {
                        grid[i][j] = linha.get(j).intValue();
                    }
                }
                callback.onSuccess(jogadas, frutas, quantidade, grid);
            }
        });
    }

    /**
     * Retorna o recurso drawable correspondente ao id da fruta.
     */
    public static int getFrutaDrawable(long frutaId) {
        switch ((int) frutaId) {
            case 1: return R.drawable.objeto1;
            case 2: return R.drawable.objeto2;
            case 3: return R.drawable.objeto3;
            case 4: return R.drawable.objeto4;
            case 5: return R.drawable.objeto5;
            case 6: return R.drawable.objeto6;
            default: return R.drawable.objeto1;
        }
    }

    /**
     * Retorna o recurso drawable correspondente ao nome do booster.
     */
    public static int getBoosterDrawable(String nome) {
        switch (nome) {
            case "boost1": return R.drawable.booster1;
            case "boost2": return R.drawable.booster2;
            case "boost3": return R.drawable.booster3;
            case "boost4": return R.drawable.booster4;
            default: return R.drawable.booster1; // Ícone genérico, se necessário.
        }
    }
}
