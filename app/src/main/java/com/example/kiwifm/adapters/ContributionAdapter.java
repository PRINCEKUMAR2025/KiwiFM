package com.example.kiwifm.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.kiwifm.R;
import com.example.kiwifm.models.Contribution;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ContributionAdapter extends RecyclerView.Adapter<ContributionAdapter.ContributionViewHolder> {

    private Context context;
    private List<Contribution> contributionList;

    public ContributionAdapter(Context context, List<Contribution> contributionList) {
        this.context = context;
        this.contributionList = contributionList;
    }

    @NonNull
    @Override
    public ContributionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_contribution, parent, false);
        return new ContributionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContributionViewHolder holder, int position) {
        Contribution contribution = contributionList.get(position);

        holder.emailTextView.setText(contribution.getEmail());
        holder.contentTextView.setText(contribution.getContent());

        // Format date
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        String formattedDate = sdf.format(new Date(contribution.getTimestamp()));
        holder.timestampTextView.setText(formattedDate);
    }

    @Override
    public int getItemCount() {
        return contributionList.size();
    }

    public static class ContributionViewHolder extends RecyclerView.ViewHolder {
        TextView emailTextView, contentTextView, timestampTextView;

        public ContributionViewHolder(@NonNull View itemView) {
            super(itemView);
            emailTextView = itemView.findViewById(R.id.emailTextView);
            contentTextView = itemView.findViewById(R.id.contentTextView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);
        }
    }
}

