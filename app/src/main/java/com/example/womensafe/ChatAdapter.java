package com.example.womensafe;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_BOT = 2;

    private final List<ChatMessage> chatMessages;

    public ChatAdapter(List<ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
    }

    @Override
    public int getItemViewType(int position) {
        if (chatMessages.get(position).isUser()) {
            return VIEW_TYPE_USER;
        } else {
            return VIEW_TYPE_BOT;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_USER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_message_item, parent, false);
            return new UserViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bot_message_item, parent, false);
            return new BotViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage chatMessage = chatMessages.get(position);
        if (holder.getItemViewType() == VIEW_TYPE_USER) {
            ((UserViewHolder) holder).userMessageText.setText(chatMessage.getMessage());
        } else {
            // Use Html.fromHtml to render bold tags
            ((BotViewHolder) holder).botMessageText.setText(Html.fromHtml(chatMessage.getMessage(), Html.FROM_HTML_MODE_COMPACT));
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView userMessageText;
        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userMessageText = itemView.findViewById(R.id.user_message_text);
        }
    }

    static class BotViewHolder extends RecyclerView.ViewHolder {
        TextView botMessageText;
        BotViewHolder(@NonNull View itemView) {
            super(itemView);
            botMessageText = itemView.findViewById(R.id.bot_message_text);
        }
    }
}
