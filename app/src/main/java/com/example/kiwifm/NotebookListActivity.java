package com.example.kiwifm;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.kiwifm.adapters.NotebookAdapter;
import com.example.kiwifm.models.Notebook;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.List;

public class NotebookListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotebookAdapter notebookAdapter;
    private List<Notebook> notebookList;
    private FloatingActionButton addNotebookFab;
    private FloatingActionButton checkWallet;
    private ProgressBar progressBar;
    private TextView emptyTextView;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notebook_list);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize Firebase Database
        mDatabase = FirebaseDatabase.getInstance().getReference("notebooks");

        // Initialize UI elements
        recyclerView = findViewById(R.id.notebooksRecyclerView);
        addNotebookFab = findViewById(R.id.addNotebookFab);
        checkWallet=findViewById(R.id.check_wallet);
        progressBar = findViewById(R.id.progressBar);
        emptyTextView = findViewById(R.id.emptyTextView);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        notebookList = new ArrayList<>();
        notebookAdapter = new NotebookAdapter(this, notebookList);
        recyclerView.setAdapter(notebookAdapter);

        // Set up FAB click listener
        addNotebookFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(NotebookListActivity.this, CreateNotebookActivity.class));
            }
        });

        checkWallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(NotebookListActivity.this, WalletActivity.class));
            }
        });

        // Load notebooks from Firebase
        loadNotebooks();
    }

    private void loadNotebooks() {
        progressBar.setVisibility(View.VISIBLE);

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                notebookList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Notebook notebook = snapshot.getValue(Notebook.class);
                    if (notebook != null) {
                        notebook.setId(snapshot.getKey());
                        notebookList.add(notebook);
                    }
                }

                notebookAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);

                // Show empty view if no notebooks
                if (notebookList.isEmpty()) {
                    emptyTextView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    emptyTextView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                emptyTextView.setText("Error loading notebooks: " + databaseError.getMessage());
                emptyTextView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        });
    }
}
