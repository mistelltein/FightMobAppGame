package com.example.fightgame;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import com.example.fightgame.models.Player;

public class GameActivity extends AppCompatActivity {

    private Player player1;
    private Player player2;
    private ProgressBar progressHP1, progressMP1, progressHP2, progressMP2;
    private ImageView wizard1, wizard2, projectile;
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
        projectile = findViewById(R.id.projectile);
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

        btnLeft.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_press));
            if (position1 > 0 && position1 - 1 != position2) {
                position1--;
                updateWizardPositions();
            }
        });

        btnRight.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_press));
            if (position1 < 4 && position1 + 1 != position2) {
                position1++;
                updateWizardPositions();
            }
        });

        btnAttack.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_press));
            if (player1.getCurrentMP() >= 10) {
                player1.useMana(10);
                launchProjectile(player1, position1, position2, wizard1, wizard2);
                updateUI();
            }
        });

        btnLeft2.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_press));
            if (position2 > 0 && position2 - 1 != position1) {
                position2--;
                updateWizardPositions();
            }
        });

        btnRight2.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_press));
            if (position2 < 4 && position2 + 1 != position1) {
                position2++;
                updateWizardPositions();
            }
        });

        btnAttack2.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_press));
            if (player2.getCurrentMP() >= 10) {
                player2.useMana(10);
                launchProjectile(player2, position2, position1, wizard2, wizard1);
                updateUI();
            }
        });

        backButton.setOnClickListener(v -> finish());

        startManaRegeneration();
    }

    private void updateWizardPositions() {
        float[] positionsX = {50f, 130f, 210f, 290f, 370f};
        wizard1.animate().x(positionsX[position1]).setDuration(300).start();
        wizard2.animate().x(positionsX[position2]).setDuration(300).start();
    }

    private void launchProjectile(Player attacker, int fromPos, int toPos, ImageView fromWizard, ImageView toWizard) {
        float[] positionsX = {50f, 130f, 210f, 290f, 370f};
        float startX = positionsX[fromPos];
        float endX = positionsX[toPos];
        projectile.setX(startX);
        projectile.setY(fromWizard.getY());
        projectile.setVisibility(ImageView.VISIBLE);

        projectile.animate()
                .x(endX)
                .setDuration(1000)
                .withEndAction(() -> {
                    if (toPos == (attacker == player1 ? position2 : position1)) {
                        Player target = (attacker == player1 ? player2 : player1);
                        target.takeDamage(20);
                        flashWizard(toWizard); // Animation of taking damage
                    }
                    projectile.setVisibility(ImageView.INVISIBLE);
                    updateUI();
                })
                .start();
    }

    private void flashWizard(ImageView wizard) {
        wizard.animate()
                .alpha(0f)
                .setDuration(100)
                .withEndAction(() -> wizard.animate().alpha(1f).setDuration(100).start())
                .start();
    }

    private void updateUI() {
        progressHP1.setProgress(player1.getCurrentHP());
        progressMP1.setProgress(player1.getCurrentMP());
        progressHP2.setProgress(player2.getCurrentHP());
        progressMP2.setProgress(player2.getCurrentMP());
    }

    private void startManaRegeneration() {
        Runnable manaRunnable = new Runnable() {
            @Override
            public void run() {
                player1.setCurrentMP(Math.min(player1.getCurrentMP() + 5, player1.getMaxMP()));
                player2.setCurrentMP(Math.min(player2.getCurrentMP() + 5, player2.getMaxMP()));
                updateUI();
                manaHandler.postDelayed(this, 5000);
            }
        };
        manaHandler.postDelayed(manaRunnable, 5000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        manaHandler.removeCallbacksAndMessages(null);
    }
}