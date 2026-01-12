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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthResult;
import com.google.android.gms.tasks.Task;

public class login extends AppCompatActivity {

    private EditText emailTextView, passwordTextView;
    private Button Btn;
    TextView textView,textView1;

    private FirebaseAuth mAuth;
    public ProgressDialog loginprogress;

    private static final int REQUEST_ALL_PERMISSIONS = 1;
    private String[] permissions = {
            Manifest.permission.SEND_SMS,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.INTERNET,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
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
                ActivityCompat.requestPermissions(this, permissions, REQUEST_ALL_PERMISSIONS);
                return;
            }
        }
        onPermissionsGranted();
    }

    private void onPermissionsGranted() {
        // Permissions are granted
    }

    @Override
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
        email = emailTextView.getText().toString().trim();
        password = passwordTextView.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(), "Please enter email!!", Toast.LENGTH_LONG).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(), "Please enter password!!", Toast.LENGTH_LONG).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(), "Login successful!!", Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(login.this, Dashboard.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    String errorMessage = "Login failed.";
                                    if (task.getException() != null) {
                                        errorMessage = task.getException().getMessage();
                                    }
                                    Toast.makeText(getApplicationContext(), "Login Failed: " + errorMessage, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser()!=null){
            Intent intent = new Intent(this,Dashboard.class);
            startActivity(intent);
            finish();
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

        emailet.setHint("Email");
        emailet.setMinEms(16);
        emailet.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        linearLayout.addView(emailet);
        linearLayout.setPadding(10,10,10,10);
        builder.setView(linearLayout);

        builder.setPositiveButton("Recover", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String email=emailet.getText().toString().trim();
                if(email.isEmpty()){
                    Toast.makeText(login.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                    return;
                }
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
                if(task.isSuccessful()) {
                    Toast.makeText(login.this,"Recovery email sent",Toast.LENGTH_LONG).show();
                } else {
                    String errorMessage = "Error Occurred";
                    if(task.getException() != null) {
                        errorMessage = task.getException().getMessage();
                    }
                    Toast.makeText(login.this, errorMessage ,Toast.LENGTH_LONG).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loginprogress.dismiss();
                Toast.makeText(login.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
