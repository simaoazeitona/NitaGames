package com.example.candycrushandreia;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private SignInClient oneTapClient;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        oneTapClient = Identity.getSignInClient(this);

        // **Verifica se já há um usuário logado**
        if (firebaseAuth.getCurrentUser() != null) {
            Log.d("MainActivity", "Usuário autenticado! Carregando dados...");
            verificarOuCriarUsuario(); // Carregar dados antes de abrir a tela principal
        }

        // Configuração do login com Google
        BeginSignInRequest signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(
                        BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                                .setSupported(true)
                                .setServerClientId("")
                                .setFilterByAuthorizedAccounts(false)
                                .build())
                .build();

        ActivityResultLauncher<IntentSenderRequest> signInLauncher =
                registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        try {
                            SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(result.getData());
                            String idToken = credential.getGoogleIdToken();
                            if (idToken != null) {
                                firebaseAuth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null))
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                Log.d("Auth", "Login bem-sucedido!");
                                                verificarOuCriarUsuario();
                                            } else {
                                                Log.e("Auth", "Falha no login!", task.getException());
                                            }
                                        });
                            }
                        } catch (ApiException e) {
                            Log.e("Auth", "Erro ao obter credenciais", e);
                        }
                    }
                });

        findViewById(R.id.login_button).setOnClickListener(v -> {
            oneTapClient.beginSignIn(signInRequest)
                    .addOnSuccessListener(this, result -> {
                        try {
                            IntentSenderRequest intentSenderRequest = new IntentSenderRequest.Builder(result.getPendingIntent().getIntentSender()).build();
                            signInLauncher.launch(intentSenderRequest);
                        } catch (Exception e) {
                            Log.e("Auth", "Erro ao iniciar login", e);
                        }
                    })
                    .addOnFailureListener(this, e -> Log.e("Auth", "Erro ao iniciar login", e));
        });
    }

    private void verificarOuCriarUsuario() {
        String uid = firebaseAuth.getCurrentUser().getUid();
        DocumentReference userRef = db.collection("users").document(uid);

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Log.d("Firestore", "Usuário encontrado: " + document.getData());

                    userRef.update("UltimoLogin", Timestamp.now())
                            .addOnSuccessListener(aVoid -> iniciarGameHomeActivity())
                            .addOnFailureListener(e -> Log.e("Firestore", "Erro ao atualizar último login: " + e.getMessage()));
                } else {
                    Map<String, Object> novoUsuario = new HashMap<>();
                    novoUsuario.put("UID", uid);
                    novoUsuario.put("Email", firebaseAuth.getCurrentUser().getEmail());
                    novoUsuario.put("Avatar", "avatar1");
                    novoUsuario.put("Moedas", 1000);
                    novoUsuario.put("NivelAtual", 1);
                    novoUsuario.put("Ranking", 9999);
                    novoUsuario.put("TotalEstrelas", 0);
                    novoUsuario.put("TotalPontos", 0);
                    novoUsuario.put("DataCriacao", Timestamp.now());
                    novoUsuario.put("UltimoLogin", Timestamp.now());

                    // **Boosters com valores ajustados**
                    Map<String, Integer> boosters = new HashMap<>();
                    boosters.put("boost1", 0);
                    boosters.put("boost2", 0);
                    boosters.put("boost3", 0);
                    boosters.put("boost4", 0);
                    novoUsuario.put("Boosters", boosters);

                    // **Estrelas por nível**
                    Map<String, Integer> estrelasPorNivel = new HashMap<>();
                    estrelasPorNivel.put("nivel1", 0); // Ajustado para 0 estrelas como pedido
                    novoUsuario.put("EstrelasPorNivel", estrelasPorNivel);
                    userRef.set(novoUsuario)
                            .addOnSuccessListener(aVoid -> iniciarGameHomeActivity())
                            .addOnFailureListener(e -> Log.e("Firestore", "Erro ao criar usuário: " + e.getMessage()));
                }
            } else {
                Log.e("Firestore", "Erro ao verificar usuário: " + task.getException().getMessage());
            }
        });
    }

    private void iniciarGameHomeActivity() {
        Intent intent = new Intent(MainActivity.this, GameHomeActivity.class);
        startActivity(intent);
        finish();
    }
}
