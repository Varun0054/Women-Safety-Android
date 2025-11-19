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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Objects;

public class addpeople extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    Spinner spinner1;
    EditText editText1, editText2;
    Button button;
    ImageView imageView;
    private static final int PROFILE_REQUEST_CODE = 1;
    Uri uri;
    DatabaseReference databaseReference;
    String ussd = "";

    private FirebaseFirestore db;
    FirebaseAuth auth;
    StorageReference storageReference;

    String[] people = {"SELECT", "PARENTS", "BROTHER", "FRIENDS", "NEIGHBORS", "POLICE"};

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addpeople);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("emergency");

        // Initialize UI components
        editText1 = findViewById(R.id.pname);
        editText2 = findViewById(R.id.pmobile);
        button = findViewById(R.id.button4);
        imageView = findViewById(R.id.profile_image12);
        spinner1 = findViewById(R.id.selecttype);
        spinner1.setOnItemSelectedListener(addpeople.this);

        // Check if user is logged in
        if (auth.getCurrentUser() != null) {
            ussd = auth.getUid();
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Spinner setup
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, people);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(adapter);

        // Image selection
        imageView.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, PROFILE_REQUEST_CODE);
        });

        // Upload button click
        button.setOnClickListener(v -> {
            if (uri != null) {
                upload();
            } else {
                Toast.makeText(addpeople.this, "Select a Profile Picture first", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // Do nothing for now
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Do nothing for now
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

    private void upload() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Creating Profile...");
        progressDialog.show();

        // Input validation
        if (editText1 == null || editText1.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter a valid name", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
            return;
        }

        if (editText2 == null || editText2.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter a valid mobile number", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
            return;
        }

        if (spinner1 == null || spinner1.getSelectedItem() == null) {
            Toast.makeText(this, "Please select a valid member type", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
            return;
        }

        String person_name = editText1.getText().toString().trim();
        String person_mobile = editText2.getText().toString().trim();
        String member_type = spinner1.getSelectedItem().toString();

        if (member_type.equals("SELECT")) {
            Toast.makeText(this, "Please select a valid member type", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
            return;
        }

        // Upload image to Firebase Storage
        StorageReference profileRef = storageReference.child("eme_people_profile")
                .child(Objects.requireNonNull(auth.getCurrentUser()).getUid())
                .child(System.currentTimeMillis() + ".jpeg");

        profileRef.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> profileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    // Store data in Firebase Realtime Database
                    people person = new people(person_name, person_mobile, uri.toString(), member_type);

                    databaseReference.child(ussd).child(person_name).setValue(person)
                            .addOnCompleteListener(task -> {
                                progressDialog.dismiss();
                                if (task.isSuccessful()) {
                                    Toast.makeText(addpeople.this, "Added Successfully", Toast.LENGTH_SHORT).show();
                                    finish();
                                } else {
                                    Toast.makeText(addpeople.this, "Failed to add. Try again!", Toast.LENGTH_SHORT).show();
                                }
                            });
                }))
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(addpeople.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
