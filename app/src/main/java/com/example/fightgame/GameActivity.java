package com.example.fightgame;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.fightgame.models.Player;

public class GameActivity extends AppCompatActivity {

    private Player player1;
    private Player player2;

    private ProgressBar progressHP1;
    private ProgressBar progressMP1;
    private ProgressBar progressHP2;
    private ProgressBar progressMP2;

    private ImageView wizard1;
    private ImageView wizard2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        player1 = new Player(100, 50);
        player2 = new Player(100, 50);

        wizard1 = findViewById(R.id.wizard1);
        wizard2 = findViewById(R.id.wizard2);

        progressHP1 = findViewById(R.id.progressHP1);
        progressMP1 = findViewById(R.id.progressMP1);
        progressHP2 = findViewById(R.id.progressHP2);
        progressMP2 = findViewById(R.id.progressMP2);

        Button btnLeft = findViewById(R.id.btnLeft);
        Button btnRight = findViewById(R.id.btnRight);
        Button btnJump = findViewById(R.id.btnJump);
        Button btnAttack = findViewById(R.id.btnAttack);
        Button backButton = findViewById(R.id.backButton);

        updateUI();

        btnLeft.setOnClickListener(v -> {
            // TODO: Логика передвижения влево
        });

        btnRight.setOnClickListener(v -> {
            // TODO: Логика передвижения вправо
        });

        btnJump.setOnClickListener(v -> {
            // TODO: Логика прыжка
        });

        btnAttack.setOnClickListener(v -> {
            // TODO: Логика атаки: тратим ману, создаём снаряд и т.д.
            updateUI();
        });

        backButton.setOnClickListener(v -> finish());
    }

    private void updateUI() {
        progressHP1.setProgress(player1.getCurrentHP());
        progressMP1.setProgress(player1.getCurrentMP());
        progressHP2.setProgress(player2.getCurrentHP());
        progressMP2.setProgress(player2.getCurrentMP());
    }
}
