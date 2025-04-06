package com.example.kiwifm;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class WalletActivity extends AppCompatActivity {

    Toolbar toolbar;
    FirebaseFirestore firestore;
    FirebaseAuth auth;
    TextView name, email, coin_view;
    private int totalcoins = 0;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);
        toolbar = findViewById(R.id.profile_toolbar);
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        coin_view = findViewById(R.id.tv_coin);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        email.setText(auth.getCurrentUser().getEmail());

        fetchCurrentCoins();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void fetchCurrentCoins() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            firestore.collection("CurrentUser").document(userId)
                    .collection("Coins").get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                                    if (doc.exists() && doc.get("amount") != null) {
                                        totalcoins = doc.getLong("amount").intValue();
                                        coin_view.setText(String.valueOf(totalcoins));
                                        Toast.makeText(WalletActivity.this, "Coins: " + totalcoins, Toast.LENGTH_SHORT).show();
                                    } else {
                                        Log.d("Firestore", "No such document or amount field missing");
                                    }
                                }
                            } else {
                                Log.d("Firestore", "get failed with ", task.getException());
                            }
                        }
                    });
        } else {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }


}