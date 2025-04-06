package com.example.kiwifm;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kiwifm.models.Contribution;
import com.example.kiwifm.models.Notebook;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ContributeActivity extends AppCompatActivity {

    private TextView notebookTitleTextView;
    private EditText contentEditText;
    private Button contributeButton;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String notebookId;
    private String notebookTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contribute);

        // Get notebook ID and title from intent
        notebookId = getIntent().getStringExtra("NOTEBOOK_ID");
        notebookTitle = getIntent().getStringExtra("NOTEBOOK_TITLE");

        if (notebookId == null) {
            finish();
            return;
        }

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize Firebase Database
        mDatabase = FirebaseDatabase.getInstance().getReference("notebooks").child(notebookId);

        // Initialize UI elements
        notebookTitleTextView = findViewById(R.id.notebookTitleTextView);
        contentEditText = findViewById(R.id.contentEditText);
        contributeButton = findViewById(R.id.contributeButton);
        progressBar = findViewById(R.id.progressBar);

        // Set notebook title
        if (notebookTitle != null) {
            notebookTitleTextView.setText(notebookTitle);
        }

        // Set click listener for contribute button
        contributeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addContribution();
            }
        });
    }

    private void addContribution() {
        String content = contentEditText.getText().toString().trim();

        // Validate input
        if (TextUtils.isEmpty(content)) {
            contentEditText.setError("Content is required");
            contentEditText.requestFocus();
            return;
        }

        // Show progress bar
        progressBar.setVisibility(View.VISIBLE);

        // Get current user
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "You must be logged in to contribute", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Get current notebook data
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Notebook notebook = dataSnapshot.getValue(Notebook.class);

                if (notebook != null) {
                    // Create new contribution for history
                    Contribution contribution = new Contribution();
                    contribution.setEmail(currentUser.getEmail());
                    contribution.setContent(content);
                    contribution.setTimestamp(new Date().getTime());

                    // Add contribution to history
                    List<Contribution> contributions = notebook.getContributions();
                    if (contributions == null) {
                        contributions = new ArrayList<>();
                    }
                    contributions.add(contribution);
                    notebook.setContributions(contributions);

                    // Update full content by appending the new contribution
                    notebook.appendContent(content, currentUser.getEmail());

                    // Update last update time
                    notebook.setLastUpdateTime(new Date().getTime());

                    // Save updated notebook
                    mDatabase.setValue(notebook)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    progressBar.setVisibility(View.GONE);

                                    if (task.isSuccessful()) {
                                        Toast.makeText(ContributeActivity.this,
                                                "Contribution added successfully!", Toast.LENGTH_SHORT).show();
                                        finish();
                                    } else {
                                        Toast.makeText(ContributeActivity.this,
                                                "Failed to add contribution: " + task.getException().getMessage(),
                                                Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ContributeActivity.this,
                            "Notebook not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ContributeActivity.this,
                        "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
