package com.example.womensafe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class editmsg extends AppCompatActivity {

    TextView t1,t2,t3,t4;
    EditText editText;
    Button button;
    DatabaseReference db;
    String ussd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editmsg);

        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentFirebaseUser == null) {
            Toast.makeText(editmsg.this, "You need to be logged in to edit messages.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        ussd = currentFirebaseUser.getUid();

        t1=findViewById(R.id.textView28);
        t2=findViewById(R.id.textView29);
        t3=findViewById(R.id.textView30);
        t4=findViewById(R.id.textView31);
        editText = findViewById(R.id.editTextTextMultiLine);
        button = findViewById(R.id.button);
        db= FirebaseDatabase.getInstance().getReference("customemsg");

        t1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editText.setText(t1.getText());
            }
        });
        t2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editText.setText(t2.getText());
            }
        });
        t3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editText.setText(t3.getText());
            }
        });
        t4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editText.setText(t4.getText());
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = editText.getText().toString().trim();

                if (msg.isEmpty()){
                    Toast.makeText(editmsg.this, "Please insert a message.", Toast.LENGTH_SHORT).show();
                }
                else {
                    savemsg(msg);
                }
            }
        });
    }

    private void savemsg(String msg) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyyHH:mm:ss");
        String currentDateAndTime = sdf.format(new Date());

        Map<String,String > map = new HashMap<>();
        map.put("currenttime",currentDateAndTime);
        map.put("massage",msg);

        db.child(ussd).child("sos_msg").setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    Toast.makeText(editmsg.this, "Saved Successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String error = "Failed to save message.";
                    if(task.getException() != null) {
                        error = task.getException().getMessage();
                    }
                    Toast.makeText(editmsg.this, "Error: " + error, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
