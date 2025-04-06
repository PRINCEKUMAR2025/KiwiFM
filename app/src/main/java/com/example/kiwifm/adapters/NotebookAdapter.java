package com.example.kiwifm.adapters;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;


import com.example.kiwifm.NotebookDetailActivity;
import com.example.kiwifm.R;
import com.example.kiwifm.models.Notebook;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotebookAdapter extends RecyclerView.Adapter<NotebookAdapter.NotebookViewHolder> {

    private Context context;
    private List<Notebook> notebookList;

    public NotebookAdapter(Context context, List<Notebook> notebookList) {
        this.context = context;
        this.notebookList = notebookList;
    }

    @NonNull
    @Override
    public NotebookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notebook, parent, false);
        return new NotebookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotebookViewHolder holder, int position) {
        Notebook notebook = notebookList.get(position);

        holder.titleTextView.setText(notebook.getTitle());
        holder.creatorTextView.setText("By: " + notebook.getCreatorEmail());

        // Format date
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        String formattedDate = sdf.format(new Date(notebook.getCreationTime()));
        holder.dateTextView.setText("Created: " + formattedDate);

        // Set number of contributions
        int contributionCount = notebook.getContributions() != null ? notebook.getContributions().size() : 0;
        holder.contributionsTextView.setText(contributionCount + " contributions");

        // Set click listener
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, NotebookDetailActivity.class);
                intent.putExtra("NOTEBOOK_ID", notebook.getId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notebookList.size();
    }

    public static class NotebookViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView titleTextView, creatorTextView, dateTextView, contributionsTextView;

        public NotebookViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            creatorTextView = itemView.findViewById(R.id.creatorTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            contributionsTextView = itemView.findViewById(R.id.contributionsTextView);
        }
    }
}
