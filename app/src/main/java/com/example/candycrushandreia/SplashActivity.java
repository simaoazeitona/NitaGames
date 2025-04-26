package com.example.candycrushandreia;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Encontrar o ImageView onde o GIF será carregado
        ImageView splashGif = findViewById(R.id.splashGif);

        // Carregar o GIF usando Glide
        Glide.with(this)
                .load(R.drawable.croissant)  // Substitua "splash_gif" pelo nome do seu GIF
                .into(splashGif);

        // Atraso de 4 segundos antes de ir para a próxima Activity
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Após 4 segundos, abrir a próxima Activity (exemplo MainActivity)
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();  // Finaliza a SplashActivity para que ela não volte quando o usuário pressionar voltar
            }
        }, 5000); // 4000ms = 4 segundos
    }
}
