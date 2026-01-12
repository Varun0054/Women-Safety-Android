package com.example.womensafe;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatbotActivity extends AppCompatActivity {

    private static final String TAG = "ChatbotActivity";
    // IMPORTANT: Replace with your actual API key
    private final String OPENROUTER_API_KEY = "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";

    private RecyclerView chatRecyclerView;
    private EditText messageEditText;
    private FloatingActionButton sendButton;
    private ChatAdapter chatAdapter;
    private final List<ChatMessage> chatMessages = new ArrayList<>();
    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        chatRecyclerView = findViewById(R.id.chat_recycler_view);
        messageEditText = findViewById(R.id.message_edit_text);
        sendButton = findViewById(R.id.send_button);

        chatAdapter = new ChatAdapter(chatMessages);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        // Welcome message
        addMessageToChat("Hi, I’m Vaishnavi. I’m here to support you. How can I help you today?", false);

        sendButton.setOnClickListener(v -> {
            String message = messageEditText.getText().toString().trim();
            if (!message.isEmpty()) {
                addMessageToChat(message, true);
                sendMessageToBot(message);
                messageEditText.setText("");
            }
        });
    }

    private void addMessageToChat(String message, boolean isUser) {
        runOnUiThread(() -> {
            chatMessages.add(new ChatMessage(message, isUser));
            chatAdapter.notifyItemInserted(chatMessages.size() - 1);
            chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
        });
    }

    private void sendMessageToBot(String userMessage) {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("model", "xiaomi/mimo-v2-flash:free");

            JSONArray messages = new JSONArray();

            // ✅ Vaishnavi Persona (ONLY ADDITION)
            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put(
                    "content",
                    "You are Vaishnavi, a calm, caring, and supportive assistant in a women's safety application. " +
                            "You speak gently and respectfully. " +
                            "Your purpose is to emotionally support the user and provide general safety guidance. " +
                            "Do not give legal, medical, or police advice. " +
                            "Do not encourage confrontation or risky behavior. " +
                            "If the user feels unsafe, gently remind them that the SOS feature and trusted contacts are available. " +
                            "Keep responses short, reassuring, and non-judgmental."
            );
            messages.put(systemMessage);
            // User message (unchanged)
            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", userMessage);
            messages.put(message);

            jsonBody.put("messages", messages);

        } catch (JSONException e) {
            Log.e(TAG, "JSON error", e);
            return;
        }

        RequestBody body = RequestBody.create(
                jsonBody.toString(),
                MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url("https://openrouter.ai/api/v1/chat/completions")
                .header("Authorization", "Bearer " + OPENROUTER_API_KEY)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "API call failed", e);
                addMessageToChat("I’m here with you, but I’m having trouble connecting right now.", false);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        String botReply = jsonResponse
                                .getJSONArray("choices")
                                .getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content");
                        addMessageToChat(botReply, false);
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parsing error", e);
                        addMessageToChat("I’m here with you. Can you please say that again?", false);
                    }
                } else {
                    Log.e(TAG, "API call unsuccessful: " + response.code());
                    addMessageToChat("Something went wrong. You can still use the SOS button anytime.", false);
                }
            }
        });
    }
}
