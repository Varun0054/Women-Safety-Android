package com.example.womensafe;

import android.graphics.drawable.AnimationDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SafetyTipsAdapter extends RecyclerView.Adapter<SafetyTipsAdapter.ViewHolder> {

    private final ArrayList<String> safetyTips;

    public SafetyTipsAdapter(ArrayList<String> safetyTips) {
        this.safetyTips = safetyTips;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.safety_tip_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tipTextView.setText(safetyTips.get(position));

        // Start the gradient animation
        AnimationDrawable animationDrawable = (AnimationDrawable) holder.tipContainer.getBackground();
        animationDrawable.setEnterFadeDuration(10);
        animationDrawable.setExitFadeDuration(4000);
        animationDrawable.start();
    }

    @Override
    public int getItemCount() {
        return safetyTips.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView tipTextView;
        public final LinearLayout tipContainer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tipTextView = itemView.findViewById(R.id.tip_text_view);
            tipContainer = itemView.findViewById(R.id.tip_container);
        }
    }
}
