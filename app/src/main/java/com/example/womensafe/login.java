package com.example.womensafe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class login extends AppCompatActivity {

    private EditText emailTextView, passwordTextView;
    private Button Btn;
    TextView textView,textView1;

    private FirebaseAuth mAuth;
    public ProgressDialog loginprogress;

    FirebaseAuth auth;


    private static final int REQUEST_ALL_PERMISSIONS = 1;
    private String[] permissions = {
            Manifest.permission.SEND_SMS,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.INTERNET,
    };

    // Add other permissions here as needed


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        auth = FirebaseAuth.getInstance();
        textView = findViewById(R.id.registertext);
        textView1 = findViewById(R.id.fpassword);
        loginprogress=new ProgressDialog(this);

        emailTextView = findViewById(R.id.logemail);
        passwordTextView = findViewById(R.id.logpass);
        Btn = findViewById(R.id.loginbtn);

        requestPermissionsIfNecessary();



        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(login.this,register.class);
                startActivity(intent);
            }
        });

        Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                loginUserAccount();
            }
        });
    }

    private void requestPermissionsIfNecessary() {

        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                ActivityCompat.requestPermissions(this, permissions, REQUEST_ALL_PERMISSIONS);
                return;
            }
        }
        // All permissions are granted
        onPermissionsGranted();
    }

    private void onPermissionsGranted() {
        Toast.makeText(this, "All permissions granted!", Toast.LENGTH_SHORT).show();
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_ALL_PERMISSIONS) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            if (allPermissionsGranted) {
                onPermissionsGranted();
            } else {
                Toast.makeText(this, "Some permissions were not granted!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loginUserAccount()
    {
        String email, password;
        email = emailTextView.getText().toString();
        password = passwordTextView.getText().toString();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(),
                            "Please enter email!!",
                            Toast.LENGTH_LONG)
                    .show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(),
                            "Please enter password!!",
                            Toast.LENGTH_LONG)
                    .show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(
                                    @NonNull Task<AuthResult> task)
                            {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(),
                                                    "Login successful!!",
                                                    Toast.LENGTH_LONG)
                                            .show();
                                    Intent intent
                                            = new Intent(login.this,
                                            Dashboard.class);
                                    startActivity(intent);
                                }

                                else {

                                    Toast.makeText(getApplicationContext(),
                                                    "Login failed!!",
                                                    Toast.LENGTH_LONG)
                                            .show();
                                }
                            }
                        });
    }
    public void onStart() {
        super.onStart();
        if (auth.getCurrentUser()!=null){
            Toast.makeText(this, "You Are Alrady Login", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this,Dashboard.class);
            startActivity(intent);
            finish();
        }
        else{
            Toast.makeText(this, "Login Know", Toast.LENGTH_SHORT).show();
        }
        textView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRecoverPasswordDialog();
            }
        });
    }
    private void showRecoverPasswordDialog() {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Recover Password");
        LinearLayout linearLayout=new LinearLayout(this);
        final EditText emailet= new EditText(this);

        // write the email using which you registered
        emailet.setText("Email");
        emailet.setMinEms(16);
        emailet.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        linearLayout.addView(emailet);
        linearLayout.setPadding(10,10,10,10);
        builder.setView(linearLayout);

        // Click on Recover and a email will be sent to your registered email id
        builder.setPositiveButton("Recover", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String email=emailet.getText().toString().trim();
                beginRecovery(email);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void beginRecovery(String email) {
        loginprogress=new ProgressDialog(this);
        loginprogress.setMessage("Sending Email....");
        loginprogress.setCanceledOnTouchOutside(false);
        loginprogress.show();
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                loginprogress.dismiss();
                if(task.isSuccessful())
                {
                    Toast.makeText(login.this,"Done sent",Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(login.this,"Error Occurred",Toast.LENGTH_LONG).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loginprogress.dismiss();
                Toast.makeText(login.this,"Error Failed",Toast.LENGTH_LONG).show();
            }
        });
    }
}