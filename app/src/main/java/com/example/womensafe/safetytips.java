package com.example.womensafe;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import java.util.ArrayList;

public class safetytips extends AppCompatActivity {

    RecyclerView tipsRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safetytips);

        tipsRecyclerView = findViewById(R.id.tips_recycler_view);
        tipsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Create a list of safety tips
        ArrayList<String> safetyTips = new ArrayList<>();
        safetyTips.add("Be aware of your surroundings at all times.");
        safetyTips.add("Trust your instincts. If a situation feels wrong, it probably is.");
        safetyTips.add("Avoid walking alone at night. Stick to well-lit, busy streets.");
        safetyTips.add("Let someone know your plans, where you are going, and when you expect to return.");
        safetyTips.add("Keep your phone fully charged and easily accessible.");
        safetyTips.add("Avoid displaying expensive items like jewelry or electronics in public.");
        safetyTips.add("When using a ride-sharing service, always verify the car model, license plate, and driver's photo before getting in.");
        safetyTips.add("Learn basic self-defense. Knowing how to protect yourself can be a powerful deterrent.");
        safetyTips.add("If you suspect you are being followed, cross the street and head for a busy, public place like a store or restaurant.");
        safetyTips.add("Be cautious about what you share on social media. Avoid posting your location in real-time.");
        safetyTips.add("Ensure all doors and windows in your home are securely locked, especially at night.");
        safetyTips.add("When going out with friends, use a buddy system and look out for each other.");
        safetyTips.add("Never leave your drink unattended in a public place.");
        safetyTips.add("Walk with purpose and confidence. Projecting a strong demeanor can make you a less likely target.");
        safetyTips.add("If a stranger offers you a ride, politely but firmly refuse.");

        // Create an adapter to display the tips
        SafetyTipsAdapter adapter = new SafetyTipsAdapter(safetyTips);

        // Set the adapter to the RecyclerView
        tipsRecyclerView.setAdapter(adapter);
    }
}
