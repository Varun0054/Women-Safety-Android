package com.example.womensafe;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class profile extends AppCompatActivity {

    Button button;
    FirebaseAuth auth;
    FirebaseUser user;
    DatabaseReference db;
    FirebaseDatabase database;
    StorageReference storageReference;
    DatabaseReference databaseReference;
    private static final int PROFILE_REQUEST_CODE = 1;
    String ussd="";
    Uri uri;
    String link;
    EditText t1,t2,t3,t4,t5,t6,t7;
    TextView textView;
    ImageView c1,c2,c3,c4,c5,c6;
    Spinner sp1;
    Spinner sp2;
    Spinner spinner3;
    ImageView imageView1,imageView,imageView2;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        t1 = findViewById(R.id.vname);
        t2 = findViewById(R.id.vmob);
        t3 = findViewById(R.id.vprn);
        t4 = findViewById(R.id.vdop);
        t5 = findViewById(R.id.adhar);
        t6 = findViewById(R.id.gender);
        textView = findViewById(R.id.textView28);


        imageView = findViewById(R.id.editimg);
        imageView1 = findViewById(R.id.imageView11);
        imageView2 = findViewById(R.id.imageView10);



        auth=FirebaseAuth.getInstance();
        db=FirebaseDatabase.getInstance().getReference("users");
        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
        assert currentFirebaseUser != null;
        ussd = currentFirebaseUser.getUid();


        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users");

//        String value = t1.getText().toString().trim();
//        String bra = t3.getText().toString().trim();








        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, PROFILE_REQUEST_CODE);
            }
        });

        imageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(profile.this,Dashboard.class);
                startActivity(intent);
                finish();
            }
        });


        imageView1.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                if (uri != null) {
                    withprofileimg();
                } else {
                    withoutprofileimg();
                }
            }
        });






        db.child(ussd).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String restaurant_name = dataSnapshot.child("name").getValue(String.class);
                    String restaurant_address = dataSnapshot.child("mobile").getValue(String.class);
                    String restaurant_mobile = dataSnapshot.child("address").getValue(String.class);
                    String owner_name = dataSnapshot.child("dob").getValue(String.class);
                    String owner_name1 = dataSnapshot.child("adhar").getValue(String.class);
                    String owner_name2 = dataSnapshot.child("gender").getValue(String.class);
                    link = dataSnapshot.child("uri").getValue(String.class);
                    String dpart = dataSnapshot.child("Departmetn").getValue(String.class);

                    // Set retrieved data in TextViews
                    t1.setText(" " + restaurant_name);
                    t2.setText(" " + restaurant_address);
                    t3.setText(" " + restaurant_mobile);
                    t4.setText(" " + owner_name);
                    t5.setText(" " + owner_name1);
                    t6.setText(" " + owner_name2);



                    Picasso.get().load(link).into(imageView);




                } else {
                    Log.d("Realtime Database", "No such document");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("Realtime Database", "onCancelled: " + databaseError.getMessage());
            }

        });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PROFILE_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uri = data.getData();
            Toast.makeText(this, "Profile selected", Toast.LENGTH_SHORT).show();
            imageView.setImageURI(uri);
        }
    }

    private void withprofileimg() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Updating Profile...");
        progressDialog.show();

        StorageReference pdfStorageReference = storageReference.child("Profile").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).child(System.currentTimeMillis() + ".jpeg");

        pdfStorageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                progressDialog.dismiss();
                Toast.makeText(profile.this, "profile Updated successfully", Toast.LENGTH_SHORT).show();
                pdfStorageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Bundle bundle = getIntent().getExtras();
                        auth=FirebaseAuth.getInstance();
                        String uidd = auth.getUid();

                        String name = t1.getText().toString().trim();
                        String mobile= t2.getText().toString().trim();
                        String address = t3.getText().toString().trim();
                        String dob = t4.getText().toString().trim();
                        String adhar = t5.getText().toString().trim();
                        String gender = t6.getText().toString().trim();
                        String uid = uidd.toString();


                        // Create a PDF object with name, description, and URL
                        Img img = new Img(name, mobile,uri.toString(),address,dob,adhar,gender,uid);

                        databaseReference.child(ussd).setValue(img).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Intent intent = new Intent(profile.this,Dashboard.class);
                                    startActivity(intent);
                                    finish();
                                }
                                else {
                                    Toast.makeText(profile.this, "failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    }
                });
            }
        });
    }

    private void withoutprofileimg() {
        Map<String, String> map = new HashMap<>();
        map.put("name",t1.getText().toString());
        map.put("mobile",t2.getText().toString());
        map.put("address",t3.getText().toString());
        map.put("dob",t4.getText().toString());
        map.put("adhar",t5.getText().toString());
        map.put("gender",t6.getText().toString());


        map.put("uri",link.toString().trim());


        db.child(ussd).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(profile.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(profile.this,Dashboard.class);
                    startActivity(intent);
                    finish();

                }else
                {
                    Toast.makeText(profile.this, "Faild to Update", Toast.LENGTH_SHORT).show();
                }

            }
        });


    }


}