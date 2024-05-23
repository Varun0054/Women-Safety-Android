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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Objects;

public class completeprofile extends AppCompatActivity implements AdapterView.OnItemSelectedListener {


    Spinner spinner1,spinner2,spinner3;
    EditText editText1, editText2,editText3,editText4,editText5;
    Button button;
    ImageView imageView;
    private static final int PROFILE_REQUEST_CODE = 1;
    String ussd="";
    Uri uri;
    DatabaseReference databaseReference;
    String[] gender = { "SELECT","MALE", "FEMALE",
            "TRANS", "No to mention",
           };

        DatabaseReference db1;
    FirebaseAuth auth;
    FirebaseUser user;
    DatabaseReference db;

    StorageReference storageReference;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_completeprofile);

        editText1 = findViewById(R.id.wname);
        editText2 = findViewById(R.id.wmob);
        editText3 = findViewById(R.id.waddress);
        editText4 = findViewById(R.id.wdob);
        editText5 = findViewById(R.id.wadhar);

        button = findViewById(R.id.savebtn);
        imageView = findViewById(R.id.profile_image);

        auth=FirebaseAuth.getInstance();
        ussd = auth.getUid();

        spinner1 = findViewById(R.id.spinner);
        spinner1.setOnItemSelectedListener(completeprofile.this);



        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users");

        db1 = FirebaseDatabase.getInstance().getReference().
                child("usersnumber");

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, PROFILE_REQUEST_CODE);
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (uri != null) {
                    uplode();
                } else {
                    Toast.makeText(completeprofile.this, "Select a Profile Picture first", Toast.LENGTH_SHORT).show();
                }
            }

        });

        ArrayAdapter a1 = new ArrayAdapter(this, android.R.layout.simple_spinner_item,
                gender);

        a1.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(a1);



    }/////////////////////////////////////////////



    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

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
    private void uplode() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Creating Profile...");
        progressDialog.show();

        StorageReference pdfStorageReference = storageReference.child("Profile").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).child(System.currentTimeMillis() + ".jpeg");

        pdfStorageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                progressDialog.dismiss();
                Toast.makeText(completeprofile.this, "profile uploaded successfully", Toast.LENGTH_SHORT).show();
                pdfStorageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Bundle bundle = getIntent().getExtras();

                        String name = editText1.getText().toString().trim();
                        String mobile = editText2.getText().toString().trim();
                        String address = editText3.getText().toString().trim();
                        String dob= editText4.getText().toString().trim();
                        String adhar= editText5.getText().toString().trim();
                        String gender=spinner1.getSelectedItem().toString();
                        String uid=ussd.toString();

                        // Create a PDF object with name, description, and URL
                        Img img = new Img
                                (name, mobile,uri.toString(),address,dob,adhar,gender,uid);

                        databaseReference.child(ussd).setValue(img).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Intent intent = new Intent(completeprofile.this,login.class);
                                    startActivity(intent);

                                    db1.child(mobile).setValue(img).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            finish();

                                        }
                                    });


                                }
                                else {
                                    Toast.makeText(completeprofile.this, "failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    }
                });
            }
        });
    }
}
