package com.example.fightgame;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import com.example.fightgame.models.Player;

public class GameActivity extends AppCompatActivity {

    private Player player1;
    private Player player2;
    private ProgressBar progressHP1, progressMP1, progressHP2, progressMP2;
    private ImageView wizard1, wizard2;
    private int position1 = 0;
    private int position2 = 4;
    private Handler manaHandler = new Handler(Looper.getMainLooper());

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
        Button btnAttack = findViewById(R.id.btnAttack);
        Button btnLeft2 = findViewById(R.id.btnLeft2);
        Button btnRight2 = findViewById(R.id.btnRight2);
        Button btnAttack2 = findViewById(R.id.btnAttack2);
        Button backButton = findViewById(R.id.backButton);

        updateUI();

        // Button handlers for the first player
        btnLeft.setOnClickListener(v -> {
            if (position1 > 0 && position1 - 1 != position2) {
                position1--;
                updateWizardPositions();
            }
        });

        btnRight.setOnClickListener(v -> {
            if (position1 < 4 && position1 + 1 != position2) {
                position1++;
                updateWizardPositions();
            }
        });

        btnAttack.setOnClickListener(v -> {
            // The logic of the attack will be added later.
        });

        // Button handlers for the second player
        btnLeft2.setOnClickListener(v -> {
            if (position2 > 0 && position2 - 1 != position1) {
                position2--;
                updateWizardPositions();
            }
        });

        btnRight2.setOnClickListener(v -> {
            if (position2 < 4 && position2 + 1 != position1) {
                position2++;
                updateWizardPositions();
            }
        });

        btnAttack2.setOnClickListener(v -> {
            // The logic of the attack will be added later.
        });

        backButton.setOnClickListener(v -> finish());

        // Starting Mana recovery
        startManaRegeneration();
    }

    private void updateWizardPositions() {
        float[] positionsX = {50f, 130f, 210f, 290f, 370f};
        wizard1.animate().x(positionsX[position1]).setDuration(300).start();
        wizard2.animate().x(positionsX[position2]).setDuration(300).start();
    }

    private void updateUI() {
        progressHP1.setProgress(player1.getCurrentHP());
        progressMP1.setProgress(player1.getCurrentMP());
        progressHP2.setProgress(player2.getCurrentHP());
        progressMP2.setProgress(player2.getCurrentMP());
    }

    private void startManaRegeneration() {
        // There's a stub for now, implement it later.
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        manaHandler.removeCallbacksAndMessages(null);
    }
}