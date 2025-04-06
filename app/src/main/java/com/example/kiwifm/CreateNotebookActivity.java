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
import android.widget.Toast;

import com.example.kiwifm.models.Contribution;
import com.example.kiwifm.models.Notebook;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CreateNotebookActivity extends AppCompatActivity {

    private EditText titleEditText, contentEditText;
    private Button createButton;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_notebook);
        // Safely set back button if action bar exists
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize Firebase Database
        mDatabase = FirebaseDatabase.getInstance().getReference("notebooks");

        // Initialize UI elements
        titleEditText = findViewById(R.id.titleEditText);
        contentEditText = findViewById(R.id.contentEditText);
        createButton = findViewById(R.id.createButton);
        progressBar = findViewById(R.id.progressBar);

        // Set click listener for create button
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNotebook();
            }
        });
    }

    private void createNotebook() {
        String title = titleEditText.getText().toString().trim();
        String content = contentEditText.getText().toString().trim();

        // Validate input
        if (TextUtils.isEmpty(title)) {
            titleEditText.setError("Title is required");
            titleEditText.requestFocus();
            return;
        }

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
            Toast.makeText(this, "You must be logged in to create a notebook", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Create contribution for initial content
        Contribution initialContribution = new Contribution();
        initialContribution.setEmail(currentUser.getEmail());
        initialContribution.setContent(content);
        initialContribution.setTimestamp(new Date().getTime());

        List<Contribution> contributions = new ArrayList<>();
        contributions.add(initialContribution);

        // Create notebook object
        Notebook notebook = new Notebook();
        notebook.setTitle(title);
        notebook.setCreatorEmail(currentUser.getEmail());
        notebook.setCreationTime(new Date().getTime());
        notebook.setLastUpdateTime(new Date().getTime());
        notebook.setContributions(contributions);

        // Set initial content with creator info
        notebook.setFullContent(content + "\n-- Contributed by: " + currentUser.getEmail());

        // Generate a new key and save notebook
        String notebookId = mDatabase.push().getKey();

        mDatabase.child(notebookId).setValue(notebook)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressBar.setVisibility(View.GONE);

                        if (task.isSuccessful()) {
                            Toast.makeText(CreateNotebookActivity.this,
                                    "Notebook created successfully!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(CreateNotebookActivity.this,
                                    "Failed to create notebook: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
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
