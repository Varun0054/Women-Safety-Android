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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Objects;


public class addpeople extends AppCompatActivity implements AdapterView.OnItemSelectedListener {


    Spinner spinner1,spinner2,spinner3;
    EditText editText1, editText2,editText3,editText4,editText5;
    Button button;
    ImageView imageView;
    private static final int PROFILE_REQUEST_CODE = 1;
    String ussd="";
    Uri uri;
    DatabaseReference databaseReference;
    String[] people = { "SELECT","PARENTS", "BROTHER",
            "FRINDES", "NEABERS","POLICE",
    };

    private FirebaseFirestore db1;

    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseFirestore db;
    DatabaseReference databaseReference1;

    StorageReference storageReference;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addpeople);

        editText1 = findViewById(R.id.pname);
        editText2 = findViewById(R.id.pmobile);


        button = findViewById(R.id.button4);
        imageView = findViewById(R.id.profile_image12);
        db = FirebaseFirestore.getInstance();

        auth=FirebaseAuth.getInstance();
        ussd = auth.getUid();

       spinner1 = findViewById(R.id.selecttype);
        spinner1.setOnItemSelectedListener(addpeople.this);



        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("emergency");

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
                    Toast.makeText(addpeople.this, "Select a Profile Picture first", Toast.LENGTH_SHORT).show();
                }
            }

        });

        ArrayAdapter a1 = new ArrayAdapter(this, android.R.layout.simple_spinner_item,
                people);
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

        StorageReference pdfStorageReference = storageReference.child("eme_people_profile").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).child(System.currentTimeMillis() + ".jpeg");

        pdfStorageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                progressDialog.dismiss();
                Toast.makeText(addpeople.this, "profile uploaded successfully", Toast.LENGTH_SHORT).show();
                pdfStorageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Bundle bundle = getIntent().getExtras();

                        String member_type = spinner1.getSelectedItem().toString();
                        String person_name = editText1.getText().toString().trim();
                        String person_mobile = editText2.getText().toString().trim();


                        CollectionReference dbCourses = db.collection("trusted_people");

                        // Create a PDF object with name, description, and URL
                        people people = new people(person_name, person_mobile,uri.toString(),member_type);


                        databaseReference.child(ussd).child(person_name).setValue(people).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(addpeople.this, "Added Succesfully", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });
                    }
                });
            }
        });
    }
}