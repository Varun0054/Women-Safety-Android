package com.example.womensafe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class register extends AppCompatActivity {
    Button button;
    EditText ed1,ed2,ed3;
    FirebaseAuth auth;

    TextView oky;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        button = findViewById(R.id.completeprofilebtn);

        ed1 = findViewById(R.id.remail);
        ed2 = findViewById(R.id.rpass);
        ed3 = findViewById(R.id.rcpass);
        auth = FirebaseAuth.getInstance();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email1 = ed1.getText().toString();
                String pass1 = ed2.getText().toString();
                String cpass1 = ed3.getText().toString();

                if (pass1.equals(cpass1)){
                    if (email1.isEmpty()||pass1.isEmpty()||cpass1.isEmpty()){
                        Toast.makeText(register.this, "Invalid data", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        registerdd(email1,pass1);
                    }
                }else {
                    Toast.makeText(register.this, "Password Not match", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void registerdd(String email1, String pass1) {

        auth.createUserWithEmailAndPassword(email1, pass1).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        genratesuccess();
                    }
                    else {
                        Toast.makeText(register.this, "Failed , Try after Some time", Toast.LENGTH_SHORT).show();
                    }
            }
        });

    }

    private void genratesuccess() {
        Dialog dialog = new Dialog(register.this);
        dialog.setContentView(R.layout.success);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);
        dialog.getWindow().getAttributes().windowAnimations = R.style.animation;
        oky = dialog.findViewById(R.id.okay_text);
        new CountDownTimer(6000, 1000) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onTick(long millisUntilFinished) {
                oky.setText("" + millisUntilFinished / 1000);
            }
            @SuppressLint("SetTextI18n")
            @Override
            public void onFinish() {
                dialog.dismiss();
                Intent intent = new Intent(register.this,completeprofile.class);
                startActivity(intent);
                finish();
            }
        }.start();
        dialog.show();
    }
}