package com.example.kiwifm;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.kiwifm.adapters.ContributionAdapter;
import com.example.kiwifm.models.Contribution;
import com.example.kiwifm.models.Notebook;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
public class NotebookDetailActivity extends AppCompatActivity {
    private TextView titleTextView, creatorTextView, dateTextView, fullContentTextView;
    private RecyclerView contributionsRecyclerView;
    private FloatingActionButton contributeButton;
    private ProgressBar progressBar;

    private ContributionAdapter contributionAdapter;
    private List<Contribution> contributionList;

    private DatabaseReference mDatabase;
    private String notebookId;
    private Notebook currentNotebook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notebook_detail);


        // Get notebook ID from intent
        notebookId = getIntent().getStringExtra("NOTEBOOK_ID");
        if (notebookId == null) {
            finish();
            return;
        }

        // Initialize Firebase Database
        mDatabase = FirebaseDatabase.getInstance().getReference("notebooks").child(notebookId);

        // Initialize UI elements
        titleTextView = findViewById(R.id.titleTextView);
        creatorTextView = findViewById(R.id.creatorTextView);
        dateTextView = findViewById(R.id.dateTextView);
        fullContentTextView = findViewById(R.id.fullContentTextView);
        contributionsRecyclerView = findViewById(R.id.contributionsRecyclerView);
        contributeButton = findViewById(R.id.contributeButton);
        progressBar = findViewById(R.id.progressBar);

        // Setup RecyclerView
        contributionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        contributionList = new ArrayList<>();
        contributionAdapter = new ContributionAdapter(this, contributionList);
        contributionsRecyclerView.setAdapter(contributionAdapter);

        // Set up contribute button click listener
        contributeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentNotebook != null) {
                    Intent intent = new Intent(NotebookDetailActivity.this, ContributeActivity.class);
                    intent.putExtra("NOTEBOOK_ID", notebookId);
                    intent.putExtra("NOTEBOOK_TITLE", currentNotebook.getTitle());
                    startActivity(intent);
                }
            }
        });

        // Load notebook details
        loadNotebookDetails();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload notebook details when returning to this activity
        if (notebookId != null) {
            loadNotebookDetails();
        }
    }

    private void loadNotebookDetails() {
        progressBar.setVisibility(View.VISIBLE);

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentNotebook = dataSnapshot.getValue(Notebook.class);

                if (currentNotebook != null) {
                    // Set notebook details
                    titleTextView.setText(currentNotebook.getTitle());
                    creatorTextView.setText("Created by: " + currentNotebook.getCreatorEmail());

                    // Format date
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
                    String formattedDate = sdf.format(new Date(currentNotebook.getCreationTime()));
                    dateTextView.setText("Created on: " + formattedDate);

                    // Set full content
                    String content = currentNotebook.getFullContent();
                    if (content != null && !content.isEmpty()) {
                        fullContentTextView.setText(content);
                    } else {
                        fullContentTextView.setText("No content yet. Be the first to contribute!");
                    }

                    // Update the list of contributions (history)
                    contributionList.clear();
                    if (currentNotebook.getContributions() != null) {
                        contributionList.addAll(currentNotebook.getContributions());
                    }
                    contributionAdapter.notifyDataSetChanged();
                }

                progressBar.setVisibility(View.GONE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
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
