
package com.example.candycrushandreia;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GameHomeActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private TextView tvMoedas;
    private ImageButton btnNivel1, btnNivel2, btnNivel3, btnNivel4, btnNivel5;
    private ImageButton btnPerfil, btnPlay;
    private Long nivelAtual;
    private ImageView imgPerfil;

    private static final int MAX_NIVEIS_DISPONIVEIS = 5;  // atualiza se adicionares mais níveis

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_home);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvMoedas = findViewById(R.id.tv_moedas);
        btnNivel1 = findViewById(R.id.btn_nivel1);
        btnNivel2 = findViewById(R.id.btn_nivel2);
        btnNivel3 = findViewById(R.id.btn_nivel3);
        btnNivel4 = findViewById(R.id.btn_nivel4);
        btnNivel5 = findViewById(R.id.btn_nivel5);  
        btnPlay = findViewById(R.id.btn_play);
        btnPerfil = findViewById(R.id.btn_perfil);

        carregarDadosUsuario();
        atualizarTotalEstrelas();
        verificarEstrelasDosNiveis(); // Atualiza os botões e estrelas
        carregarEstrelasENiveis();

        btnNivel1.setOnClickListener(v -> mostrarItemJogoDialog("nivel1"));
        btnNivel2.setOnClickListener(v -> mostrarItemJogoDialog("nivel2"));
        btnNivel3.setOnClickListener(v -> mostrarItemJogoDialog("nivel3"));
        btnNivel4.setOnClickListener(v -> mostrarItemJogoDialog("nivel4"));
        btnNivel5.setOnClickListener(v -> mostrarItemJogoDialog("nivel5"));

        btnPlay.setOnClickListener(v -> {
            abrirPreJogoDialog();
            Log.d("TESTE", "Clicou no PLAY");
        });
        btnPerfil.setOnClickListener(v -> mostrarPerfilDialog());

    }
    private void carregarEstrelasENiveis() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference userRef = db.collection("users").document(uid);
        userRef.get().addOnSuccessListener(document -> {
            if (document.exists()) {
                Map<String, Object> estrelasPorNivel = (Map<String, Object>) document.get("EstrelasPorNivel");
                Long nivelAtual = document.getLong("NivelAtual");

                // Aqui vamos obter o nome do avatar do Firestore
                String avatarName = document.getString("Avatar"); // O nome do avatar armazenado no Firestore

                // Para pegar o recurso do avatar baseado no nome
                int avatarResId = getAvatarResource(avatarName);  // Usando o método getAvatarResource

                for (int i = 1; i <= 5; i++) {
                    int estrelas = 0;
                    if (estrelasPorNivel != null && estrelasPorNivel.get("nivel" + i) != null) {
                        estrelas = ((Long) estrelasPorNivel.get("nivel" + i)).intValue();
                    }

                    int estrelaViewId = getResources().getIdentifier("estrela_nivel" + i, "id", getPackageName());
                    ImageView estrelaView = findViewById(estrelaViewId);

                    // Mostrar estrelas, se houver
                    if (estrelas > 0) {
                        estrelaView.setVisibility(View.VISIBLE);
                        estrelaView.setImageResource(getStarDrawable(estrelas)); // Método que retorna a drawable de estrela
                    } else {
                        estrelaView.setVisibility(View.GONE); // Esconde se não houver estrelas
                    }

                    // Ativar botões até o nível atual
                    int botaoId = getResources().getIdentifier("btn_nivel" + i, "id", getPackageName());
                    ImageButton btn = findViewById(botaoId);
                    if (nivelAtual != null && i <= nivelAtual) {
                        btn.setEnabled(true);
                    }

                    // Mostrar avatar no nível mais alto
                    if (nivelAtual != null && i == nivelAtual.intValue()) {
                        int avatarViewId = getResources().getIdentifier("avatar_nivel" + i, "id", getPackageName());
                        ImageView avatarView = findViewById(avatarViewId);
                        avatarView.setVisibility(View.VISIBLE);

                        // Aqui, ao invés de buscar o avatar no Firebase Storage, usamos o recurso local
                        avatarView.setImageResource(avatarResId);  // Usando o recurso de avatar
                    }
                }
            }
        });
    }


    // Retorna o drawable correspondente ao número de estrelas
    private int getStarDrawable(int estrelas) {
        switch (estrelas) {
            case 1:
                return R.drawable.estrela1;
            case 2:
                return R.drawable.estrela2;
            case 3:
                return R.drawable.estrela3;
            default:
                return 0; // não necessário, mas mantemos por segurança
        }
    }


    private void carregarDadosUsuario() {
        String uid = auth.getCurrentUser().getUid();
        DocumentReference userRef = db.collection("users").document(uid);

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    int Moedas = document.getLong("Moedas").intValue();
                    tvMoedas.setText("  " + Moedas);

                    nivelAtual = document.getLong("NivelAtual");
                    liberarNiveis();

                    btnPerfil.setImageResource(getAvatarResource(document.getString("Avatar")));
                }
            }
        });
    }

    private void liberarNiveis() {
        btnNivel1.setEnabled(true);
        btnNivel2.setEnabled(nivelAtual >= 2);
        btnNivel3.setEnabled(nivelAtual >= 3);
        btnNivel4.setEnabled(nivelAtual >= 4);
        btnNivel5.setEnabled(nivelAtual >= 5);
    }

    private void abrirPreJogoDialog() {
        String uid = auth.getCurrentUser().getUid();

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(document -> {
                    String nivelId;
                    if (document.exists() && document.get("NivelAtual") != null) {
                        long nivel = document.getLong("NivelAtual");
                        nivelId = "nivel" + nivel;
                        if (nivel == 6) {
                            Toast.makeText(this, "Já acabou os níveis, se quiseres repete", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        nivelId = "nivel1";
                    }
                    mostrarItemJogoDialog(nivelId);
                })
                .addOnFailureListener(e -> {
                    mostrarItemJogoDialog("nivel1");
                });
    }

    private void atualizarTotalEstrelasNoPerfil() {
        // Obter o UID do usuário
        String uid = auth.getCurrentUser().getUid();

        // Referência ao documento do usuário no Firestore
        DocumentReference userRef = db.collection("users").document(uid);

        // Obter os dados do usuário
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Carregar as estrelas por nível
                Map<String, Object> estrelasPorNivel = (Map<String, Object>) documentSnapshot.get("EstrelasPorNivel");

                // Usando um array para garantir que o valor seja final (imutável)
                final int[] totalEstrelas = {0}; // array com valor inicial de 0

                // Verifique se o mapa de estrelas por nível não é nulo
                if (estrelasPorNivel != null) {
                    // Iterar sobre o mapa de EstrelasPorNivel e somar as estrelas de cada nível
                    for (Map.Entry<String, Object> entry : estrelasPorNivel.entrySet()) {
                        // Obter o número de estrelas para o nível
                        Long estrelas = (Long) entry.getValue();
                        if (estrelas != null) {
                            totalEstrelas[0] += estrelas.intValue(); // Atualiza o valor dentro do array
                        }
                    }
                }

                // Atualizar o campo TotalEstrelas com o valor calculado
                userRef.update("TotalEstrelas", totalEstrelas[0])
                        .addOnSuccessListener(aVoid -> {
                            // Log de sucesso ou qualquer ação após a atualização
                            Log.d("Atualização", "TotalEstrelas atualizado com sucesso: " + totalEstrelas[0]);
                        })
                        .addOnFailureListener(e -> {
                            // Log de erro ou qualquer ação caso haja falha na atualização
                            Log.e("Erro", "Erro ao atualizar TotalEstrelas", e);
                        });
            } else {
                // Caso o documento do usuário não exista, você pode criar um novo usuário ou apenas ignorar
                Log.e("Erro", "Usuário não encontrado no Firestore.");
            }
        }).addOnFailureListener(e -> {
            // Caso falhe ao buscar o documento do usuário
            Log.e("Erro", "Falha ao acessar dados do usuário", e);
        });
    }

    private void mostrarItemJogoDialog(String nivelId) {

        switch (nivelId) {
            case "nivel1":
                FirebaseHelper.criarNivel1();
                break;
            case "nivel2":
                FirebaseHelper.criarNivel2();
                break;
            case "nivel3":
                FirebaseHelper.criarNivel3();
                break;
            case "nivel4":
                FirebaseHelper.criarNivel4();
                break;
            case "nivel5":
                FirebaseHelper.criarNivel5();
                break;
        }
        // Inicializar o diálogo
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.item_jogo);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        atualizarTotalEstrelas(); // <-- atualiza o total antes de abrir o diálogo


        // Inicializar views antes de exibir o diálogo
        TextView tvNivel = dialog.findViewById(R.id.tv_nivel);
        TextView tvJogadas = dialog.findViewById(R.id.tv_jogadas);
        TextView tvQtd1 = dialog.findViewById(R.id.tv_qtd_objetivo1);
        TextView tvQtd2 = dialog.findViewById(R.id.tv_qtd_objetivo2);
        ImageView imgObj1 = dialog.findViewById(R.id.img_objetivo1);
        ImageView imgObj2 = dialog.findViewById(R.id.img_objetivo2);
        ImageButton btnComecar = dialog.findViewById(R.id.btn_comecar);
        LinearLayout layoutBoosters = dialog.findViewById(R.id.layout_boosters);
        List<ImageView> imgBoosters = new ArrayList<>();
        List<TextView> txtBoosters = new ArrayList<>();
        for (int i = 0; i < layoutBoosters.getChildCount(); i++) {
            View boosterItem = layoutBoosters.getChildAt(i);
            imgBoosters.add(boosterItem.findViewById(R.id.img_boost));
            txtBoosters.add(boosterItem.findViewById(R.id.tv_boost));
        }

        // Carregar os dados do Firestore primeiro, antes de mostrar o diálogo
        String uid = auth.getCurrentUser().getUid();
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists() && document.get("EstrelasPorNivel") != null) {
                        // Carregar as estrelas do nível
                        Map<String, Object> estrelasPorNivel = (Map<String, Object>) document.get("EstrelasPorNivel");
                        Long estrelasLong = (Long) estrelasPorNivel.get(nivelId); // Pega o valor como Long
                        int estrelas = (estrelasLong != null) ? estrelasLong.intValue() : 0; // Converte para int ou usa 0 se null

                        // Alterar o fundo do diálogo de acordo com as estrelas
                        int backgroundResId;
                        switch (estrelas) {
                            case 1:
                                backgroundResId = R.drawable.fim1estrelas; // Imagem de fundo para 1 estrela
                                break;
                            case 2:
                                backgroundResId = R.drawable.fim2estrelas; // Imagem de fundo para 2 estrelas
                                break;
                            case 3:
                                backgroundResId = R.drawable.fim3estrelas; // Imagem de fundo para 3 estrelas
                                break;
                            default:
                                backgroundResId = R.drawable.fim0estrelas; // Imagem de fundo para 0 estrelas
                                break;
                        }
                        dialog.findViewById(R.id.dialog_background).setBackgroundResource(backgroundResId); // Alterando o fundo do diálogo

                        // Agora que as estrelas foram definidas, podemos carregar os outros dados do nível
                        FirebaseHelper.carregarDadosFirestore(nivelId, new DataCallback() {
                            @Override
                            public void onSuccess(int jogadas, List<Long> frutas, List<Long> quantidades, int[][] tabuleiro) {
                                // Atualizar as views com os dados carregados
                                tvNivel.setText("Nível " + nivelId.replace("nivel", ""));
                                tvJogadas.setText("Jogadas: " + jogadas);
                                if (!frutas.isEmpty()) {
                                    imgObj1.setImageResource(FirebaseHelper.getFrutaDrawable(frutas.get(0)));
                                    tvQtd1.setText("x" + quantidades.get(0));
                                }
                                if (frutas.size() > 1) {
                                    imgObj2.setImageResource(FirebaseHelper.getFrutaDrawable(frutas.get(1)));
                                    tvQtd2.setText("x" + quantidades.get(1));
                                }

                                // Carregar boosters do Firebase
                                db.collection("users").document(uid)
                                        .get()
                                        .addOnSuccessListener(document -> {
                                            if (document.exists() && document.get("Boosters") != null) {
                                                Map<String, Object> boosters = (Map<String, Object>) document.get("Boosters");
                                                int i = 0;
                                                for (Map.Entry<String, Object> entry : boosters.entrySet()) {
                                                    if (i >= imgBoosters.size()) break;
                                                    imgBoosters.get(i).setImageResource(FirebaseHelper.getBoosterDrawable(entry.getKey()));
                                                    txtBoosters.get(i).setText("x" + entry.getValue());
                                                    i++;
                                                }
                                            }
                                        });

                                // Agora que os dados estão carregados e as views estão configuradas, mostramos o diálogo
                                dialog.show();
                            }
                        });
                    }
                });

        // Configurar o botão de começar
        btnComecar.setOnClickListener(v -> {
            dialog.dismiss();
            iniciarPreJogo(nivelId);
            finish();
        });
    }

    private void atualizarTotalEstrelas() {
        String uid = auth.getCurrentUser().getUid();
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.get("EstrelasPorNivel") != null) {
                        Map<String, Object> estrelasPorNivel = (Map<String, Object>) documentSnapshot.get("EstrelasPorNivel");
                        int totalEstrelas = 0;

                        for (Object value : estrelasPorNivel.values()) {
                            if (value instanceof Long) {
                                totalEstrelas += ((Long) value).intValue();
                            } else if (value instanceof Integer) {
                                totalEstrelas += (Integer) value;
                            }
                        }

                        db.collection("users").document(uid)
                                .update("TotalEstrelas", totalEstrelas);
                    }
                });
    }

    private void iniciarPreJogo(String nivelId) {
        Intent intent = new Intent(this, PreJogoActivity.class);
        intent.putExtra("nivelId", nivelId);
        startActivity(intent);
    }

    private void mostrarPerfilDialog() {
        atualizarTotalEstrelasNoPerfil();
        Dialog perfilDialog = new Dialog(this);
        perfilDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        perfilDialog.setContentView(R.layout.item_perfil);

        // Botão de sair do diálogo
        ImageButton btnSair = perfilDialog.findViewById(R.id.btnSair);
        btnSair.setOnClickListener(v -> perfilDialog.dismiss());

        // Referências das views básicas do perfil
        imgPerfil = perfilDialog.findViewById(R.id.img_perfil);
        TextView tvNivel = perfilDialog.findViewById(R.id.tv_nivel);
        TextView tvEstrelas = perfilDialog.findViewById(R.id.tv_estrelas);
        LinearLayout layoutAvatares = perfilDialog.findViewById(R.id.layout_avatares);
        ImageView avatar1 = perfilDialog.findViewById(R.id.avatar1);
        ImageView avatar2 = perfilDialog.findViewById(R.id.avatar2);
        ImageView avatar3 = perfilDialog.findViewById(R.id.avatar3);
        ImageView avatar4 = perfilDialog.findViewById(R.id.avatar4);
        ImageView avatar5 = perfilDialog.findViewById(R.id.avatar5);

        // Referências para exibição dos boosters (quantidades)
        TextView qtdBoost1 = perfilDialog.findViewById(R.id.tv_boost1);
        TextView qtdBoost2 = perfilDialog.findViewById(R.id.tv_boost2);
        TextView qtdBoost3 = perfilDialog.findViewById(R.id.tv_boost3);
        TextView qtdBoost4 = perfilDialog.findViewById(R.id.tv_boost4);

        // Obter também as referências dos botões de compra (o "+" sobreposto)
        ImageButton btnCompraBoost1 = perfilDialog.findViewById(R.id.btn_compra_boost1);
        ImageButton btnCompraBoost2 = perfilDialog.findViewById(R.id.btn_compra_boost2);
        ImageButton btnCompraBoost3 = perfilDialog.findViewById(R.id.btn_compra_boost3);
        ImageButton btnCompraBoost4 = perfilDialog.findViewById(R.id.btn_compra_boost4);

        // Configurar os listeners para os botões de compra para cada booster.
        btnCompraBoost1.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Compra de Booster 1")
                    .setMessage("Deseja comprar 1 unidades deste booster por 100 Moedas?")
                    .setPositiveButton("Sim", (dialogInterface, which) -> {
                        comprarBoosterNoPerfil(1, 1, 50, qtdBoost1);
                    })
                    .setNegativeButton("Não", null)
                    .show();
        });
        btnCompraBoost2.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Compra de Booster 2")
                    .setMessage("Deseja comprar 1 unidades deste booster por 50 Moedas?")
                    .setPositiveButton("Sim", (dialogInterface, which) -> {
                        comprarBoosterNoPerfil(2, 1, 50, qtdBoost2);
                    })
                    .setNegativeButton("Não", null)
                    .show();
        });
        btnCompraBoost3.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Compra de Booster 3")
                    .setMessage("Deseja comprar 1 unidades deste booster por 300 Moedas?")
                    .setPositiveButton("Sim", (dialogInterface, which) -> {
                        comprarBoosterNoPerfil(3, 1, 300, qtdBoost3);
                    })
                    .setNegativeButton("Não", null)
                    .show();
        });
        btnCompraBoost4.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Compra de Booster 4")
                    .setMessage("Deseja comprar 1 unidades deste booster por 450 Moedas?")
                    .setPositiveButton("Sim", (dialogInterface, which) -> {
                        comprarBoosterNoPerfil(4, 1, 450, qtdBoost4);
                    })
                    .setNegativeButton("Não", null)
                    .show();
        });

        // Carregar os dados do usuário do Firestore e atualizar as views do perfil
        String uid = auth.getCurrentUser().getUid();
        DocumentReference userRef = db.collection("users").document(uid);
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                DocumentSnapshot document = task.getResult();

                imgPerfil.setImageResource(getAvatarResource(document.getString("Avatar")));
                tvNivel.setText("Nível: " + document.getLong("NivelAtual"));
                tvEstrelas.setText("⭐ Total de Estrelas: " + document.getLong("TotalEstrelas"));


                // Atualiza as quantidades dos boosters
                qtdBoost1.setText(String.valueOf(document.getLong("Boosters.boost1")));
                qtdBoost2.setText(String.valueOf(document.getLong("Boosters.boost2")));
                qtdBoost3.setText(String.valueOf(document.getLong("Boosters.boost3")));
                qtdBoost4.setText(String.valueOf(document.getLong("Boosters.boost4")));
            }
        });

        // Configurar troca de avatar
        imgPerfil.setOnClickListener(v -> layoutAvatares.setVisibility(View.VISIBLE));
        avatar1.setOnClickListener(v -> atualizarAvatar("avatar1"));
        avatar2.setOnClickListener(v -> atualizarAvatar("avatar2"));
        avatar3.setOnClickListener(v -> atualizarAvatar("avatar3"));
        avatar4.setOnClickListener(v -> atualizarAvatar("avatar4"));
        avatar5.setOnClickListener(v -> atualizarAvatar("avatar5"));

        perfilDialog.show();
    }


    private void comprarBoosterNoPerfil(int boosterType, int unidadesCompradas, int custo, TextView qtdBoostView) {
        String uid = auth.getCurrentUser().getUid();
        DocumentReference userRef = db.collection("users").document(uid);

        userRef.get().addOnSuccessListener(document -> {
            if (document.exists()) {
                long MoedasAtuais = document.contains("Moedas") ? document.getLong("Moedas") : 0;
                if (MoedasAtuais < custo) {
                    Toast.makeText(this, "Moedas insuficientes!", Toast.LENGTH_SHORT).show();
                    return;
                }
                long novoSaldo = MoedasAtuais - custo;
                // Atualiza o saldo de Moedas
                userRef.update("Moedas", novoSaldo)
                        .addOnSuccessListener(aVoid -> {
                            // Obter o valor atual do booster
                            long valorAtual = 0;
                            if (document.contains("Boosters")) {
                                Map<String, Object> boosters = (Map<String, Object>) document.get("Boosters");
                                if (boosters.containsKey("boost" + boosterType)) {
                                    valorAtual = ((Number) boosters.get("boost" + boosterType)).longValue();
                                }
                            }
                            long novoValor = valorAtual + unidadesCompradas;
                            // Atualizar o valor do booster no banco
                            userRef.update("Boosters.boost" + boosterType, novoValor)
                                    .addOnSuccessListener(v -> {
                                        Toast.makeText(this, "Compra efetuada!", Toast.LENGTH_SHORT).show();
                                        qtdBoostView.setText(String.valueOf(novoValor));
                                        atualizarMoedas();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(this, "Erro ao atualizar boosters", Toast.LENGTH_SHORT).show());
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Erro ao atualizar Moedas", Toast.LENGTH_SHORT).show());
            }
        }).addOnFailureListener(e -> Toast.makeText(this, "Erro ao acessar o banco de dados", Toast.LENGTH_SHORT).show());
    }

    private void atualizarMoedas() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference userRef = FirebaseFirestore.getInstance().collection("users").document(uid);
        userRef.get().addOnSuccessListener(document -> {
            if (document.exists() && document.contains("Moedas")) {
                long Moedas = document.getLong("Moedas");
                TextView tvMoedas = findViewById(R.id.tv_moedas); // Certifique-se de ter esse TextView no layout
                tvMoedas.setText("  " + Moedas);
            }
        }).addOnFailureListener(e -> Log.e("GameHome", "Erro ao atualizar Moedas: " + e.getMessage()));
    }

    private void verificarEstrelasDosNiveis() {
        String uid = auth.getCurrentUser().getUid();
        DocumentReference userRef = db.collection("users").document(uid);

        userRef.get().addOnSuccessListener(document -> {
            if (document.exists()) {
                Map<String, Object> estrelasPorNivel = (Map<String, Object>) document.get("EstrelasPorNivel");
                Long nivelAtual = document.getLong("NivelAtual");

                // Se o usuário tem um valor para NivelAtual
                if (nivelAtual != null) {
                    // Itera somente pelos níveis até o nível atual
                    for (int i = 1; i <= Math.min(nivelAtual, MAX_NIVEIS_DISPONIVEIS); i++) {
                        String nivelId = "nivel" + i;

                        // Obter referências do botão e da imagem de estrela
                        int btnId = getResources().getIdentifier("btn_nivel" + i, "id", getPackageName());
                        int estrelaId = getResources().getIdentifier("estrela_nivel" + i, "id", getPackageName());

                        ImageButton btnNivel = findViewById(btnId);
                        ImageView estrelaView = findViewById(estrelaId);

                        // Pega o número de estrelas para o nível; se não existir, assume 0
                        int estrelas = 0;
                        if (estrelasPorNivel != null && estrelasPorNivel.get(nivelId) != null) {
                            estrelas = ((Long) estrelasPorNivel.get(nivelId)).intValue();
                        }

                        // Se o número de estrelas é maior que 0, mudar o fundo para a versão verde e exibir a estrela;
                        // Caso contrário, manter o fundo padrão e esconder a estrela.
                        if (estrelas > 0) {
                            btnNivel.setBackgroundResource(
                                    getResources().getIdentifier("btn" + i + "_verde", "drawable", getPackageName())
                            );
                            estrelaView.setVisibility(View.VISIBLE);
                            estrelaView.setImageResource(
                                    getResources().getIdentifier("estrelas" + estrelas, "drawable", getPackageName())
                            );
                        } else {
                            btnNivel.setBackgroundResource(
                                    getResources().getIdentifier("btn" + i, "drawable", getPackageName())
                            );
                            estrelaView.setVisibility(View.GONE);
                        }
                        // Habilitar o botão, pois o usuário já alcançou esse nível
                        btnNivel.setEnabled(true);
                    }
                }
            }
        }).addOnFailureListener(e -> {
            Log.e("Erro", "Erro ao recuperar dados do usuário.", e);
        });
    }

    private void atualizarAvatar(String novoAvatar) {
        String uid = auth.getCurrentUser().getUid();
        DocumentReference userRef = db.collection("users").document(uid);

        // Atualiza o avatar no Firestore
        userRef.update("Avatar", novoAvatar).addOnSuccessListener(aVoid -> {
            btnPerfil.setImageResource(getAvatarResource(novoAvatar));
            imgPerfil.setImageResource(getAvatarResource(novoAvatar));
            carregarEstrelasENiveis();

        });
    }

    private int getAvatarResource(String avatarName) {
        if (avatarName == null) return R.drawable.avatar1;
        switch (avatarName) {
            case "avatar2":
                return R.drawable.avatar2;
            case "avatar3":
                return R.drawable.avatar3;
            case "avatar4":
                return R.drawable.avatar4;
            case "avatar5":
                return R.drawable.avatar5;
            default:
                return R.drawable.avatar1;
        }
    }
}
