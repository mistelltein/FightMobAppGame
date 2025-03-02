package com.example.fightgame;

import androidx.appcompat.app.AppCompatActivity;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.example.fightgame.models.Player;

public class GameActivity extends AppCompatActivity {

    private Player player1;
    private Player player2;
    private ProgressBar progressHP1, progressMP1, progressHP2, progressMP2;
    private ImageView wizard1, wizard2, projectile;
    private int position1 = 1;
    private int position2 = 1;
    private Handler manaHandler = new Handler(Looper.getMainLooper());
    private SoundPool soundPool;
    private int soundAttack, soundMove, soundDamage;
    private android.media.MediaPlayer backgroundMusic;

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

        soundPool = new SoundPool.Builder().setMaxStreams(3).build();
        soundAttack = soundPool.load(this, R.raw.attack_sound, 1);
        soundMove = soundPool.load(this, R.raw.move_sound, 1);
        soundDamage = soundPool.load(this, R.raw.damage_sound, 1);

        backgroundMusic = android.media.MediaPlayer.create(this, R.raw.background_music);
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
        updateWizardPositions();

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
        float[] positionsY = {
                getResources().getDisplayMetrics().heightPixels * 0.25f,
                getResources().getDisplayMetrics().heightPixels * 0.45f,
                getResources().getDisplayMetrics().heightPixels * 0.65f
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
                    checkForWinner();
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
        soundPool.play(soundAttack, 1.0f, 1.0f, 1, 0, 1.0f);
    }

    private void playMoveSound() {
        soundPool.play(soundMove, 1.0f, 1.0f, 1, 0, 1.0f);
    }

    private void playDamageSound() {
        soundPool.play(soundDamage, 1.0f, 1.0f, 1, 0, 1.0f);
    }

    private void checkForWinner() {
        if (player1.getCurrentHP() <= 0 && player2.getCurrentHP() > 0) {
            announceWinner("Player 2");
        } else if (player2.getCurrentHP() <= 0 && player1.getCurrentHP() > 0) {
            announceWinner("Player 1");
        } else if (player1.getCurrentHP() <= 0 && player2.getCurrentHP() <= 0) {
            announceWinner("Ничья");
        }
    }

    private void announceWinner(String winner) {
        Toast.makeText(this, winner + " победил!", Toast.LENGTH_LONG).show();
        disableButtons();
    }

    private void disableButtons() {
        findViewById(R.id.btnUp).setEnabled(false);
        findViewById(R.id.btnDown).setEnabled(false);
        findViewById(R.id.btnAttack).setEnabled(false);
        findViewById(R.id.btnUp2).setEnabled(false);
        findViewById(R.id.btnDown2).setEnabled(false);
        findViewById(R.id.btnAttack2).setEnabled(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (backgroundMusic != null && backgroundMusic.isPlaying()) {
            backgroundMusic.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (backgroundMusic != null && !backgroundMusic.isPlaying()) {
            backgroundMusic.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        manaHandler.removeCallbacksAndMessages(null);
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        if (backgroundMusic != null) {
            backgroundMusic.stop();
            backgroundMusic.release();
            backgroundMusic = null;
        }
    }
}