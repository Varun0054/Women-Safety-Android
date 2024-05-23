package com.example.womensafe;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.text.method.SingleLineTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.auth.User;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class trustedpeopledetailed extends AppCompatActivity {

    EditText ed1,ed2;
    Button button,button1;
    ImageView imageView;
    DatabaseReference databaseReference;
    DatabaseReference db;
    FirebaseAuth mAuth;
    String ussd;
    String n1,n2,n3,n4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trustedpeopledetailed);


        ed1 = findViewById(R.id.tpername);
        ed2=findViewById(R.id.tpermob);
        button1 = findViewById(R.id.tperdelete);///delete
        imageView = findViewById(R.id.imageView5);
        databaseReference = FirebaseDatabase.getInstance().getReference("emergency");
        db = FirebaseDatabase.getInstance().getReference("emergency");

        Intent intent = getIntent();
         n1 = intent.getStringExtra("m1");//////name
         n2 = intent.getStringExtra("m2");////mobile
         n3 = intent.getStringExtra("m3");////uri
        n4 = intent.getStringExtra("m4");///type

        ed1.setText(n1);
        ed2.setText(n2);
        Picasso.get().load(n3).placeholder(R.mipmap.ic_launcher)
                .fit()
                .centerCrop()
                .into(imageView);


        //////////////////////////////////////////////////////////////////

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                deletepeople();
            }
        });
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
         ussd = mAuth.getUid().toString();

    }

    private void deletepeople() {


        String dataKey = ed1.getText().toString();


        DatabaseReference dataNodeReference = databaseReference.child(ussd).child(dataKey);
        dataNodeReference.removeValue()
                .addOnSuccessListener(aVoid -> {
                    // Data deleted successfully
                    // You can add any additional logic here
                    Toast.makeText(this, "Deleted Succesfully", Toast.LENGTH_SHORT).show();
                   // Intent intent = new Intent(trustedpeopledetailed.this,Dashboard.class);
                    //startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Failed to delete data
                    // Handle the error here
                    Toast.makeText(this, "error ", Toast.LENGTH_SHORT).show();

                });
    }


}



