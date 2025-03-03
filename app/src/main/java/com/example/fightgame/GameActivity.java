package com.example.fightgame;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;

import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.fightgame.models.Player;

public class GameActivity extends AppCompatActivity {

    private Player player1, player2;
    private ProgressBar progressHP1, progressMP1, progressHP2, progressMP2;
    private ImageView wizard1, wizard2;
    // Позиции магов: 1,2,3 соответствуют зонам
    private int position1 = 1, position2 = 1;
    private final Handler manaHandler = new Handler(Looper.getMainLooper());
    private SoundPool soundPool;
    private int soundAttack, soundMove, soundDamage;
    private android.media.MediaPlayer backgroundMusic;
    private ConstraintLayout rootLayout;
    private boolean gameEnded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Инициализация игроков
        player1 = new Player(100, 50);
        player2 = new Player(100, 50);

        // Привязка UI-элементов
        wizard1 = findViewById(R.id.wizard1);
        wizard2 = findViewById(R.id.wizard2);
        progressHP1 = findViewById(R.id.progressHP1);
        progressMP1 = findViewById(R.id.progressMP1);
        progressHP2 = findViewById(R.id.progressHP2);
        progressMP2 = findViewById(R.id.progressMP2);
        rootLayout = findViewById(R.id.rootLayout);

        // Инициализация SoundPool и загрузка звуковых эффектов
        soundPool = new SoundPool.Builder().setMaxStreams(3).build();
        soundAttack = soundPool.load(this, R.raw.attack_sound, 1);
        soundMove = soundPool.load(this, R.raw.move_sound, 1);
        soundDamage = soundPool.load(this, R.raw.damage_sound, 1);

        // Фоновая музыка
        backgroundMusic = android.media.MediaPlayer.create(this, R.raw.background_music);
        if (backgroundMusic != null) {
            backgroundMusic.setLooping(true);
            backgroundMusic.start();
        }

        // Привязка кнопок управления
        Button btnUp = findViewById(R.id.btnUp);
        Button btnDown = findViewById(R.id.btnDown);
        Button btnAttack = findViewById(R.id.btnAttack);
        Button btnUp2 = findViewById(R.id.btnUp2);
        Button btnDown2 = findViewById(R.id.btnDown2);
        Button btnAttack2 = findViewById(R.id.btnAttack2);
        Button backButton = findViewById(R.id.backButton);

        updateUI();
        // Обновляем позиции после того, как размеры wizard получены
        wizard1.post(this::updateWizardPositions);

        // Кнопки первого игрока
        btnUp.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_press));
            if (gameEnded) return;
            if (position1 > 1) {
                position1--;
                updateWizardPositions();
                playMoveSound();
            }
        });

        btnDown.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_press));
            if (gameEnded) return;
            if (position1 < 3) {
                position1++;
                updateWizardPositions();
                playMoveSound();
            }
        });

        btnAttack.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_press));
            if (gameEnded || player1.getCurrentHP() <= 0 || player2.getCurrentHP() <= 0 || player1.getCurrentMP() < 10)
                return;
            player1.useMana(10);
            attack(player1, player2, position1, position2, wizard1, wizard2);
            playAttackSound();
            updateUI();
        });

        // Кнопки второго игрока
        btnUp2.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_press));
            if (gameEnded) return;
            if (position2 > 1) {
                position2--;
                updateWizardPositions();
                playMoveSound();
            }
        });

        btnDown2.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_press));
            if (gameEnded) return;
            if (position2 < 3) {
                position2++;
                updateWizardPositions();
                playMoveSound();
            }
        });

        btnAttack2.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_press));
            if (gameEnded || player2.getCurrentHP() <= 0 || player1.getCurrentHP() <= 0 || player2.getCurrentMP() < 10)
                return;
            player2.useMana(10);
            attack(player2, player1, position2, position1, wizard2, wizard1);
            playAttackSound();
            updateUI();
        });

        backButton.setOnClickListener(v -> finish());

        startManaRegeneration();
    }

    /**
     * Обновляет вертикальные позиции персонажей на основе гайдлайнов с id: guideline_zone1, guideline_zone2, guideline_zone3.
     */
    private void updateWizardPositions() {
        Guideline guideline1 = findViewById(R.id.guideline_zone1);
        Guideline guideline2 = findViewById(R.id.guideline_zone2);
        Guideline guideline3 = findViewById(R.id.guideline_zone3);
        Guideline[] guidelines = { guideline1, guideline2, guideline3 };

        float targetY1 = guidelines[position1 - 1].getY() - wizard1.getHeight() / 2f;
        float targetY2 = guidelines[position2 - 1].getY() - wizard2.getHeight() / 2f;

        wizard1.animate().y(targetY1).setDuration(300).start();
        wizard2.animate().y(targetY2).setDuration(300).start();
    }

    /**
     * Метод атаки: создаёт снаряд (ImageView), анимирует его движение по горизонтали.
     * Если атакующий и цель находятся в одной зоне, наносится урон.
     */
    private void attack(Player attacker, Player target, int fromPos, int toPos,
                        ImageView fromWizard, ImageView toWizard) {
        ImageView projectile = new ImageView(this);
        projectile.setImageResource(R.drawable.ic_projectile);
        projectile.setLayoutParams(new ConstraintLayout.LayoutParams(20, 20));
        rootLayout.addView(projectile);

        // Получаем гайдлайны для зон
        Guideline guideline1 = findViewById(R.id.guideline_zone1);
        Guideline guideline2 = findViewById(R.id.guideline_zone2);
        Guideline guideline3 = findViewById(R.id.guideline_zone3);
        Guideline[] guidelines = { guideline1, guideline2, guideline3 };

        float startY = guidelines[fromPos - 1].getY() - projectile.getHeight() / 2f;
        float startX = (attacker == player1) ? fromWizard.getX() + fromWizard.getWidth() : fromWizard.getX();
        float endX = (attacker == player1) ? getResources().getDisplayMetrics().widthPixels : 0;

        projectile.setX(startX);
        projectile.setY(startY);
        projectile.setVisibility(View.VISIBLE);

        projectile.animate()
                .x(endX)
                .setDuration(1000)
                .withEndAction(() -> {
                    if (fromPos == toPos && target.getCurrentHP() > 0) {
                        target.takeDamage(20);
                        flashWizard(toWizard);
                        playDamageSound();
                    }
                    rootLayout.removeView(projectile);
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
        manaHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!gameEnded) {
                    player1.setCurrentMP(Math.min(player1.getCurrentMP() + 5, player1.getMaxMP()));
                    player2.setCurrentMP(Math.min(player2.getCurrentMP() + 5, player2.getMaxMP()));
                    updateUI();
                    manaHandler.postDelayed(this, 5000);
                }
            }
        }, 5000);
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

    /**
     * Проверка на наличие победителя.
     */
    private void checkForWinner() {
        if (gameEnded) return;
        String winner = null;
        if (player1.getCurrentHP() <= 0 && player2.getCurrentHP() > 0) {
            winner = "Player 2";
        } else if (player2.getCurrentHP() <= 0 && player1.getCurrentHP() > 0) {
            winner = "Player 1";
        } else if (player1.getCurrentHP() <= 0 && player2.getCurrentHP() <= 0) {
            winner = "Draw";
        }
        if (winner != null) {
            gameEnded = true;
            showWinnerDialog(winner);
        }
    }

    /**
     * Показ диалогового окна с результатом игры. Пользователь может выйти в меню или переиграть.
     */
    private void showWinnerDialog(String winner) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_winner, null);
        builder.setView(dialogView);

        TextView winnerText = dialogView.findViewById(R.id.winner_text);
        Button btnMenu = dialogView.findViewById(R.id.btn_menu);
        Button btnReplay = dialogView.findViewById(R.id.btn_replay);

        winnerText.setText(winner + " won!");

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);

        btnMenu.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });

        btnReplay.setOnClickListener(v -> {
            dialog.dismiss();
            resetGame();
        });

        dialog.show();
    }

    private void resetGame() {
        player1 = new Player(100, 50);
        player2 = new Player(100, 50);
        position1 = 1;
        position2 = 1;
        gameEnded = false;
        updateUI();
        updateWizardPositions();
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
