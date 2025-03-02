package com.example.fightgame;

import androidx.appcompat.app.AppCompatActivity;
import android.media.MediaPlayer;
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
    private int position1 = 1; // Начальная позиция первого мага (1, 2, 3)
    private int position2 = 1; // Начальная позиция второго мага (1, 2, 3)
    private Handler manaHandler = new Handler(Looper.getMainLooper());
    private MediaPlayer attackSound, moveSound, damageSound, backgroundMusic;

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

        attackSound = MediaPlayer.create(this, R.raw.attack_sound);
        moveSound = MediaPlayer.create(this, R.raw.move_sound);
        damageSound = MediaPlayer.create(this, R.raw.damage_sound);
        backgroundMusic = MediaPlayer.create(this, R.raw.background_music);
        if (backgroundMusic != null) {
            backgroundMusic.setLooping(true);
            backgroundMusic.start();
        }

        Button btnUp = findViewById(R.id.btnUp);
        Button btnDown = findViewById(R.id.btnDown);
        Button btnAttack = findViewById(R.id.btnAttack);
        Button btnUp2 = findViewById(R.id.btnUp2);
        Button btnDown2 = findViewById(R.id.btnDown2);
        Button btnAttack2 = findViewById(R.id.btnAttack2);
        Button backButton = findViewById(R.id.backButton);

        updateUI();
        updateWizardPositions(); // Установить начальные позиции

        btnUp.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_press));
            if (position1 > 1) {
                position1--;
                updateWizardPositions();
                playMoveSound();
            }
        });

        btnDown.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_press));
            if (position1 < 3) {
                position1++;
                updateWizardPositions();
                playMoveSound();
            }
        });

        btnAttack.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_press));
            if (player1.getCurrentMP() >= 10) {
                player1.useMana(10);
                launchProjectile(player1, position1, position2, wizard1, wizard2);
                playAttackSound();
                updateUI();
            }
        });

        btnUp2.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_press));
            if (position2 > 1) {
                position2--;
                updateWizardPositions();
                playMoveSound();
            }
        });

        btnDown2.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_press));
            if (position2 < 3) {
                position2++;
                updateWizardPositions();
                playMoveSound();
            }
        });

        btnAttack2.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_press));
            if (player2.getCurrentMP() >= 10) {
                player2.useMana(10);
                launchProjectile(player2, position2, position1, wizard2, wizard1);
                playAttackSound();
                updateUI();
            }
        });

        backButton.setOnClickListener(v -> finish());

        startManaRegeneration();
    }

    private void updateWizardPositions() {
        // Определяем Y-координаты для позиций 1, 2, 3
        float[] positionsY = {
                getResources().getDisplayMetrics().heightPixels * 0.25f, // Позиция 1
                getResources().getDisplayMetrics().heightPixels * 0.45f, // Позиция 2
                getResources().getDisplayMetrics().heightPixels * 0.65f  // Позиция 3
        };
        wizard1.animate().y(positionsY[position1 - 1] - wizard1.getHeight() / 2).setDuration(300).start();
        wizard2.animate().y(positionsY[position2 - 1] - wizard2.getHeight() / 2).setDuration(300).start();
    }

    private void launchProjectile(Player attacker, int fromPos, int toPos, ImageView fromWizard, ImageView toWizard) {
        float[] positionsY = {
                getResources().getDisplayMetrics().heightPixels * 0.25f,
                getResources().getDisplayMetrics().heightPixels * 0.45f,
                getResources().getDisplayMetrics().heightPixels * 0.65f
        };
        float startY = positionsY[fromPos - 1] - projectile.getHeight() / 2;
        float startX = (attacker == player1) ? fromWizard.getX() + fromWizard.getWidth() : fromWizard.getX();
        float endX = (attacker == player1) ? getResources().getDisplayMetrics().widthPixels : 0;

        projectile.setY(startY);
        projectile.setX(startX);
        projectile.setVisibility(ImageView.VISIBLE);

        projectile.animate()
                .x(endX)
                .setDuration(1000)
                .withEndAction(() -> {
                    if (fromPos == toPos) {
                        Player target = (attacker == player1 ? player2 : player1);
                        target.takeDamage(20);
                        flashWizard(toWizard);
                        playDamageSound();
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

    private void playAttackSound() {
        if (attackSound != null) attackSound.start();
    }

    private void playMoveSound() {
        if (moveSound != null) moveSound.start();
    }

    private void playDamageSound() {
        if (damageSound != null) damageSound.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        manaHandler.removeCallbacksAndMessages(null);
        if (attackSound != null) attackSound.release();
        if (moveSound != null) moveSound.release();
        if (damageSound != null) damageSound.release();
        if (backgroundMusic != null) {
            backgroundMusic.stop();
            backgroundMusic.release();
        }
    }
}