package com.example.kiwifm;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
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

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;

public class NotebookDetailActivity extends AppCompatActivity {
    private TextView titleTextView, creatorTextView, dateTextView, fullContentTextView;
    private RecyclerView contributionsRecyclerView;
    private FloatingActionButton contributeButton,generate_AudioBook;
    private ProgressBar progressBar;

    ImageView AI_enhance;

    private ContributionAdapter contributionAdapter;
    private List<Contribution> contributionList;

    private DatabaseReference mDatabase;
    private String notebookId;
    private Notebook currentNotebook;
    String content;
    TextToSpeech TT;

    private final String GEMINI_API_KEY = "AIzaSyCPKj6qYqxs7V-jdANPqM_L3rDgs4vAdzU"; // Replace with your key


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notebook_detail);

        TT = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i !=TextToSpeech.ERROR){
                    TT.setLanguage(Locale.ENGLISH);
                }
            }
        });


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
        AI_enhance=findViewById(R.id.enhance_ai);
        fullContentTextView = findViewById(R.id.fullContentTextView);
        generate_AudioBook=findViewById(R.id.publishAudioBook);
        contributionsRecyclerView = findViewById(R.id.contributionsRecyclerView);
        contributeButton = findViewById(R.id.contributeButton);
        progressBar = findViewById(R.id.progressBar);

        loadNotebookDetails();

        AI_enhance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (content != null && !content.isEmpty()) {
                    generateEnhancedStory(content);
                }
            }
        });

        generate_AudioBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text=content.toString();
                TT.speak(text,TextToSpeech.QUEUE_FLUSH,null);
            }
        });

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
                    content = currentNotebook.getFullContent();
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

    private void generateEnhancedStory(String originalContent) {
        progressBar.setVisibility(View.VISIBLE);

        try {
            JSONObject contentJson = new JSONObject();
            JSONArray contentsArray = new JSONArray();

            JSONObject textPart = new JSONObject();
            textPart.put("text", "Take this story and enhance it by adding subtle elements of thriller, suspense, romance, and unexpected twists. Do not write a full story or ending. Only add slight enhancements and leave the story open-ended. Make minimal edits to keep the original tone intact.\n\n Directly Give Title and then story." +
                     originalContent);

            JSONObject partObj = new JSONObject();
            partObj.put("parts", new JSONArray().put(textPart));
            contentsArray.put(partObj);

            contentJson.put("contents", contentsArray);

            OkHttpClient client = new OkHttpClient();

            RequestBody body = RequestBody.create(contentJson.toString(), MediaType.parse("application/json"));
            Request request = new Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + GEMINI_API_KEY)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Log.e("GeminiAPI", "API call failed", e);
                    });
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    runOnUiThread(() -> progressBar.setVisibility(View.GONE));

                    if (response.isSuccessful()) {
                        String responseData = response.body().string();
                        try {
                            JSONObject jsonResponse = new JSONObject(responseData);
                            JSONArray candidates = jsonResponse.getJSONArray("candidates");
                            JSONObject firstCandidate = candidates.getJSONObject(0);
                            JSONArray partsArray = firstCandidate.getJSONObject("content").getJSONArray("parts");
                            String updatedStory = partsArray.getJSONObject(0).getString("text");

                            // Update content in Firebase
                            mDatabase.child("fullContent").setValue(updatedStory);

                            // Update UI
                            runOnUiThread(() -> {
                                fullContentTextView.setText(updatedStory);
                                content = updatedStory;
                            });

                        } catch (Exception e) {
                            Log.e("GeminiAPI", "JSON parsing error", e);
                        }
                    } else {
                        Log.e("GeminiAPI", "API error: " + response.code());
                    }
                }
            });

        } catch (Exception e) {
            Log.e("GeminiAPI", "Request build error", e);
            progressBar.setVisibility(View.GONE);
        }
    }

}
