package com.example.womensafe;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Objects;

public class completeprofile extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Spinner spinner1;
    private EditText editText1, editText2, editText3, editText4, editText5;
    private Button button;
    private ImageView imageView;
    private static final int PROFILE_REQUEST_CODE = 1;
    private Uri uri;
    private String ussd = "";

    private DatabaseReference databaseReference, db1;
    private FirebaseAuth auth;
    private StorageReference storageReference;

    private final String[] genderOptions = {"SELECT", "MALE", "FEMALE", "TRANS", "Prefer not to say"};

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_completeprofile);

        // Initialize UI elements
        editText1 = findViewById(R.id.wname);
        editText2 = findViewById(R.id.wmob);
        editText3 = findViewById(R.id.waddress);
        editText4 = findViewById(R.id.wdob);
        editText5 = findViewById(R.id.wadhar);
        button = findViewById(R.id.savebtn);
        imageView = findViewById(R.id.profile_image);
        spinner1 = findViewById(R.id.spinner);

        spinner1.setOnItemSelectedListener(this);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        db1 = FirebaseDatabase.getInstance().getReference().child("usersnumber");

        // Get user ID
        if (auth.getCurrentUser() != null) {
            ussd = auth.getUid();
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Setup gender spinner
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, genderOptions);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(genderAdapter);

        // Select profile picture
        imageView.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, PROFILE_REQUEST_CODE);
        });

        // Upload button
        button.setOnClickListener(v -> {
            if (uri != null) {
                uploadProfile();
            } else {
                Toast.makeText(completeprofile.this, "Select a Profile Picture first", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // No action needed for now
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // No action needed for now
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PROFILE_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uri = data.getData();
            imageView.setImageURI(uri);
            Toast.makeText(this, "Profile selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadProfile() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Creating Profile...");
        progressDialog.show();

        // Input validation
        String name = editText1.getText().toString().trim();
        String mobile = editText2.getText().toString().trim();
        String address = editText3.getText().toString().trim();
        String dob = editText4.getText().toString().trim();
        String aadhar = editText5.getText().toString().trim();
        String gender = spinner1.getSelectedItem().toString();

        if (name.isEmpty() || mobile.isEmpty() || address.isEmpty() || dob.isEmpty() || aadhar.isEmpty()) {
            progressDialog.dismiss();
            Toast.makeText(this, "All fields must be filled!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (gender.equals("SELECT")) {
            progressDialog.dismiss();
            Toast.makeText(this, "Please select a valid gender", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mobile.length() != 10) {
            progressDialog.dismiss();
            Toast.makeText(this, "Enter a valid 10-digit mobile number", Toast.LENGTH_SHORT).show();
            return;
        }

        // Upload image to Firebase Storage
        StorageReference profileRef = storageReference.child("Profile")
                .child(Objects.requireNonNull(auth.getCurrentUser()).getUid())
                .child(System.currentTimeMillis() + ".jpeg");

        profileRef.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> profileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    // Store data in Firebase Realtime Database
                    Img userProfile = new Img(name, mobile, uri.toString(), address, dob, aadhar, gender, ussd);

                    databaseReference.child(ussd).setValue(userProfile)
                            .addOnCompleteListener(task -> {
                                progressDialog.dismiss();
                                if (task.isSuccessful()) {
                                    // Store number separately
                                    db1.child(mobile).setValue(userProfile)
                                            .addOnCompleteListener(task1 -> finish());

                                    Toast.makeText(completeprofile.this, "Profile Created Successfully", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(completeprofile.this, login.class));
                                } else {
                                    Toast.makeText(completeprofile.this, "Failed to save profile", Toast.LENGTH_SHORT).show();
                                }
                            });
                }))
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(completeprofile.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
