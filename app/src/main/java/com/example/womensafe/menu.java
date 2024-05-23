package com.example.womensafe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.squareup.picasso.Picasso;

public class menu extends AppCompatActivity {

    CardView cd1,cd2,cd3,cd4,cd5;
        DatabaseReference databaseReference;
        String ussd;

        TextView tt1,tt2,tt3,tt4;
        ImageView imageView,img2;
    Bitmap bitmap;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        cd1 = findViewById(R.id.c1);////add people
        cd2 = findViewById(R.id.c2);////////current location
        cd3 = findViewById(R.id.c3);////tips
        cd4 = findViewById(R.id.c4);////create own msg
        cd5 = findViewById(R.id.c5);//////call trusted person


        tt1=findViewById(R.id.textView33);
        imageView = findViewById(R.id.fsfsf);
        img2 = findViewById(R.id.imageView9);


        cd1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =  new Intent(menu.this,addpeople.class);
                startActivity(intent);
            }
        });
        cd2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =  new Intent(menu.this,locationn.class);
                startActivity(intent);
            }
        });
        cd3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =  new Intent(menu.this,safetytips.class);
                startActivity(intent);
            }
        });
        cd4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =  new Intent(menu.this,editmsg.class);
                startActivity(intent);
            }
        });
        cd5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =  new Intent(menu.this,aboutus.class);
                startActivity(intent);
            }
        });





        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        ussd = currentFirebaseUser.getUid();

        databaseReference= FirebaseDatabase.getInstance().getReference("users");

        databaseReference.child(ussd).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                     String restaurant_name = dataSnapshot.child("name").getValue(String.class);
                     String mob = dataSnapshot.child("mobile").getValue(String.class);
                    String address = dataSnapshot.child("address").getValue(String.class);
                    String adhar = dataSnapshot.child("adhar").getValue(String.class);

                    String link = dataSnapshot.child("uri").getValue(String.class);


                    // Set retrieved data in TextViews
                    tt1.setText("Name: " + restaurant_name);
                    genrateqrcode(ussd);


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

    private void genrateqrcode(String ussd) {
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(ussd, BarcodeFormat.QR_CODE, 400, 400);
            img2.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }


    }
}