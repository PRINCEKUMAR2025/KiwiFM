package com.example.kiwifm;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private static final int SPLASH_TIMEOUT = 2000; // Splash screen timeout in milliseconds
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Find views for animation
        ImageView logoImageView = findViewById(R.id.logoImageView);
        TextView titleTextView = findViewById(R.id.titleTextView);

        // Load animations
        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        fadeIn.setDuration(1000);

        // Start animations
        logoImageView.startAnimation(fadeIn);
        titleTextView.startAnimation(fadeIn);

        // Delayed transition to next activity
        new Handler().postDelayed(() -> {
            FirebaseUser currentUser = mAuth.getCurrentUser();

            Intent intent;
            if (currentUser != null) {
                // User is already logged in, go to notebook list
                intent = new Intent(MainActivity.this, NotebookListActivity.class);
            } else {
                // User needs to log in
                intent = new Intent(MainActivity.this, LoginActivity.class);
            }

            startActivity(intent);
            finish();
        }, SPLASH_TIMEOUT);
    }
}
