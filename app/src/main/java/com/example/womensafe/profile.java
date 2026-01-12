package com.example.womensafe;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class profile extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    private static final int IMAGE_PICK_CODE = 1000;

    private CircleImageView profileImageView;
    private TextView changeProfileText;
    private EditText nameEditText, mobileEditText, addressEditText, dobEditText, aadharEditText, genderEditText;
    private Button updateProfileButton;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private StorageReference mStorage;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // Not logged in, redirect to login
            startActivity(new Intent(this, login.class));
            finish();
            return;
        }

        mDatabase = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
        mStorage = FirebaseStorage.getInstance().getReference("profile_images").child(currentUser.getUid());

        profileImageView = findViewById(R.id.editimg);
        changeProfileText = findViewById(R.id.change_profile_text);
        nameEditText = findViewById(R.id.vname);
        mobileEditText = findViewById(R.id.vmob);
        addressEditText = findViewById(R.id.vprn);
        dobEditText = findViewById(R.id.vdop);
        aadharEditText = findViewById(R.id.adhar);
        genderEditText = findViewById(R.id.gender);
        updateProfileButton = findViewById(R.id.update_profile_button);

        loadUserProfile();

        changeProfileText.setOnClickListener(v -> openFileChooser());
        profileImageView.setOnClickListener(v -> openFileChooser());
        updateProfileButton.setOnClickListener(v -> updateUserProfile());
    }

    private void loadUserProfile() {
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("name").getValue(String.class);
                    String mobile = dataSnapshot.child("mobile").getValue(String.class);
                    String address = dataSnapshot.child("address").getValue(String.class);
                    String dob = dataSnapshot.child("dob").getValue(String.class);
                    String aadhar = dataSnapshot.child("adhar").getValue(String.class);
                    String gender = dataSnapshot.child("gender").getValue(String.class);
                    String profileImageUri = dataSnapshot.child("uri").getValue(String.class);

                    nameEditText.setText(name);
                    mobileEditText.setText(mobile);
                    addressEditText.setText(address);
                    dobEditText.setText(dob);
                    aadharEditText.setText(aadhar);
                    genderEditText.setText(gender);

                    if (profileImageUri != null && !profileImageUri.isEmpty()) {
                        Picasso.get().load(profileImageUri).into(profileImageView);
                    }
                } else {
                    Toast.makeText(profile.this, "Profile not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Failed to load profile.", databaseError.toException());
                Toast.makeText(profile.this, "Failed to load profile.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            profileImageView.setImageURI(imageUri);
            uploadImageToStorage();
        }
    }

    private void uploadImageToStorage() {
        if (imageUri != null) {
            mStorage.putFile(imageUri).addOnSuccessListener(taskSnapshot -> mStorage.getDownloadUrl().addOnSuccessListener(uri -> {
                String downloadUrl = uri.toString();
                mDatabase.child("uri").setValue(downloadUrl);
                Toast.makeText(profile.this, "Profile image updated", Toast.LENGTH_SHORT).show();
            })).addOnFailureListener(e -> {
                Log.e(TAG, "Image upload failed.", e);
                Toast.makeText(profile.this, "Image upload failed.", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void updateUserProfile() {
        String name = nameEditText.getText().toString().trim();
        String mobile = mobileEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();
        String dob = dobEditText.getText().toString().trim();
        String aadhar = aadharEditText.getText().toString().trim();
        String gender = genderEditText.getText().toString().trim();

        Map<String, Object> profileMap = new HashMap<>();
        profileMap.put("name", name);
        profileMap.put("mobile", mobile);
        profileMap.put("address", address);
        profileMap.put("dob", dob);
        profileMap.put("adhar", aadhar);
        profileMap.put("gender", gender);

        mDatabase.updateChildren(profileMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(profile.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                Log.e(TAG, "Profile update failed.", task.getException());
                Toast.makeText(profile.this, "Profile update failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
