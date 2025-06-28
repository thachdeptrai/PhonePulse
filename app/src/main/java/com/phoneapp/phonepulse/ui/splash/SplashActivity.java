package com.phoneapp.phonepulse.ui.splash;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.ViewPropertyAnimatorCompat;

import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.ui.auth.LoginActivity;

import dagger.hilt.android.AndroidEntryPoint;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 3000; // 3 seconds
    private static final int ANIMATION_DURATION = 1500; // 1.5 seconds

    private View logoCircle;
    private ImageView phoneIcon;
    private TextView appName;
    private TextView tagline;
    private ProgressBar progressBar;
    private TextView loadingText;

    private Handler handler;
    private Runnable navigateRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Hide status bar for full screen experience
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        initViews();
        startAnimations();
        startProgressAnimation();
        scheduleNavigation();
    }

    private void initViews() {
        logoCircle = findViewById(R.id.logo_circle);
        phoneIcon = findViewById(R.id.phone_icon);
        appName = findViewById(R.id.app_name);
        tagline = findViewById(R.id.tagline);
        progressBar = findViewById(R.id.progress_bar);
        loadingText = findViewById(R.id.loading_text);

        // Set initial state for animations
        logoCircle.setScaleX(0f);
        logoCircle.setScaleY(0f);
        phoneIcon.setScaleX(0f);
        phoneIcon.setScaleY(0f);
        appName.setAlpha(0f);
        appName.setTranslationY(50f);
        tagline.setAlpha(0f);
        tagline.setTranslationY(30f);
        progressBar.setAlpha(0f);
        loadingText.setAlpha(0f);
    }

    private void startAnimations() {
        // Logo circle animation with bounce effect
        ObjectAnimator logoScaleX = ObjectAnimator.ofFloat(logoCircle, "scaleX", 0f, 1.2f, 1f);
        ObjectAnimator logoScaleY = ObjectAnimator.ofFloat(logoCircle, "scaleY", 0f, 1.2f, 1f);
        logoScaleX.setDuration(800);
        logoScaleY.setDuration(800);
        logoScaleX.setInterpolator(new BounceInterpolator());
        logoScaleY.setInterpolator(new BounceInterpolator());

        // Phone icon animation
        ObjectAnimator phoneScaleX = ObjectAnimator.ofFloat(phoneIcon, "scaleX", 0f, 1f);
        ObjectAnimator phoneScaleY = ObjectAnimator.ofFloat(phoneIcon, "scaleY", 0f, 1f);
        ObjectAnimator phoneRotation = ObjectAnimator.ofFloat(phoneIcon, "rotation", 0f, 360f);
        phoneScaleX.setDuration(600);
        phoneScaleY.setDuration(600);
        phoneRotation.setDuration(800);
        phoneScaleX.setStartDelay(400);
        phoneScaleY.setStartDelay(400);
        phoneRotation.setStartDelay(400);
        phoneScaleX.setInterpolator(new AccelerateDecelerateInterpolator());
        phoneScaleY.setInterpolator(new AccelerateDecelerateInterpolator());

        // App name animation
        ViewPropertyAnimatorCompat appNameAnimator = ViewCompat.animate(appName)
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .setStartDelay(800)
                .setInterpolator(new AccelerateDecelerateInterpolator());

        // Tagline animation
        ViewPropertyAnimatorCompat taglineAnimator = ViewCompat.animate(tagline)
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .setStartDelay(1000)
                .setInterpolator(new AccelerateDecelerateInterpolator());

        // Progress bar and loading text animation
        ViewPropertyAnimatorCompat progressAnimator = ViewCompat.animate(progressBar)
                .alpha(1f)
                .setDuration(400)
                .setStartDelay(1200);

        ViewPropertyAnimatorCompat loadingTextAnimator = ViewCompat.animate(loadingText)
                .alpha(1f)
                .setDuration(400)
                .setStartDelay(1300);

        // Start all animations
        AnimatorSet logoSet = new AnimatorSet();
        logoSet.play(logoScaleX).with(logoScaleY);
        logoSet.start();

        AnimatorSet phoneSet = new AnimatorSet();
        phoneSet.play(phoneScaleX).with(phoneScaleY).with(phoneRotation);
        phoneSet.start();

        appNameAnimator.start();
        taglineAnimator.start();
        progressAnimator.start();
        loadingTextAnimator.start();
    }

    private void startProgressAnimation() {
        // Animate progress bar from 0 to 100
        ValueAnimator progressAnimator = ValueAnimator.ofInt(0, 100);
        progressAnimator.setDuration(SPLASH_DURATION - 500);
        progressAnimator.setStartDelay(1500);
        progressAnimator.addUpdateListener(animation -> {
            int progress = (int) animation.getAnimatedValue();
            progressBar.setProgress(progress);

            // Update loading text based on progress
            if (progress < 30) {
                loadingText.setText("Initializing...");
            } else if (progress < 60) {
                loadingText.setText("Loading resources...");
            } else if (progress < 90) {
                loadingText.setText("Almost ready...");
            } else {
                loadingText.setText("Welcome!");
            }
        });
        progressAnimator.start();
    }

    private void scheduleNavigation() {
        handler = new Handler();
        navigateRunnable = () -> {
            // Start fade out animation before navigation
            fadeOutAndNavigate();
        };
        handler.postDelayed(navigateRunnable, SPLASH_DURATION);
    }

    private void fadeOutAndNavigate() {
        // Fade out all views
        ViewCompat.animate(logoCircle).alpha(0f).setDuration(500);
        ViewCompat.animate(phoneIcon).alpha(0f).setDuration(500);
        ViewCompat.animate(appName).alpha(0f).setDuration(500);
        ViewCompat.animate(tagline).alpha(0f).setDuration(500);
        ViewCompat.animate(progressBar).alpha(0f).setDuration(500);
        ViewCompat.animate(loadingText).alpha(0f).setDuration(500)
                .withEndAction(() -> {
                    // Navigate to LoginActivity
                    Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && navigateRunnable != null) {
            handler.removeCallbacks(navigateRunnable);
        }
    }

}