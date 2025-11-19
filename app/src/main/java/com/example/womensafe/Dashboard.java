package com.example.womensafe;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Dashboard extends AppCompatActivity {

    CardView c1, c2, c3, c4, c5;
    TextView okay_text, cancel_text, textView;
    DatabaseReference db;
    DatabaseReference db1;
    FirebaseAuth auth;
    String ussd = "";
    FirebaseAuth mAuth;
    String mob = "";
    ImageView imageView;
    Button button;
    private static final int PERMISSION_REQUEST_CODE = 123;

    private RecyclerView mRecyclerView;
    private ImageAdpter mAdapter;
    private List<people> mUploads;
    ImageView img1, img2, img3, img4;
    DatabaseReference databaseReference;
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0;

    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private final Set<String> processedNumbers = new HashSet<>();
    DatabaseReference databaseReferencemsg;
    String ssoss, restaurant_name;
    DatabaseReference ro;

    double latitude,longitude;
    String lat,log;

    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    private static final int REQUEST_CODE = 101;
    private static final int REQUEST_SMS_PERMISSION = 1;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        c1 = findViewById(R.id.card1);
        c2 = findViewById(R.id.card2);
        c3 = findViewById(R.id.card3);
        textView = findViewById(R.id.womenname);
        imageView = findViewById(R.id.imageview1);
        button = findViewById(R.id.logoutbtn);

        img4 = findViewById(R.id.menuimage);

        mRecyclerView = findViewById(R.id.rcc);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance().getReference("users");
        db1 = FirebaseDatabase.getInstance().getReference("status");
        ro = FirebaseDatabase.getInstance().getReference("currentlocation");



        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        ussd = currentFirebaseUser.getUid();
        //////////////////////////////////////////////component declaration end here///////////////////////////


        //////////////////////////////////////////////////request for permission device location access


        c1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Dashboard.this, addpeople.class);
                startActivity(intent);
            }
        });

        c2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertgenrator();
            }
        });
        c3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Dashboard.this, safetytips.class);
                startActivity(intent);
            }
        });


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                auth.signOut();
                Toast.makeText(Dashboard.this, "signOut Successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Dashboard.this, login.class);
                startActivity(intent);
                finish();
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Dashboard.this, profile.class);
                startActivity(intent);
            }
        });

        img4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Dashboard.this, menu.class);
                startActivity(intent);
            }
        });///////menu
        /////////////////////////////////////////////////////Option with listner

        db.child(ussd).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    restaurant_name = dataSnapshot.child("name").getValue(String.class);
                    mob = dataSnapshot.child("mobile").getValue(String.class);
                    String link = dataSnapshot.child("uri").getValue(String.class);
                    if (restaurant_name.isEmpty()) {
                        Toast.makeText(Dashboard.this, "empty", Toast.LENGTH_SHORT).show();
                    } else {
                        startService(new Intent(Dashboard.this, backgroundserv.class));
                    }
                    // Set retrieved data in TextViews
                    textView.setText(" " + restaurant_name);
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
        /////////////////////////////////////
        databaseReferencemsg = FirebaseDatabase.getInstance().getReference("customemsg");
        databaseReferencemsg.child(ussd).child("sos_msg").addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    ssoss = dataSnapshot.child("massage").getValue(String.class);
                } else {
                    Log.d("Realtime Database", "No such document");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("Realtime Database", "onCancelled: " + databaseError.getMessage());
            }

        });





        //////////////////////////////////////fetch user detail/////////////////////////

        mUploads = new ArrayList<>();
        DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference("emergency").child(ussd);

        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUploads.clear(); // Clear list before adding new data to prevent duplication

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    people person = postSnapshot.getValue(people.class);
                    if (person != null) { // Null check to avoid crashes
                        mUploads.add(person);
                    }
                }

                if (mAdapter == null) {  // Initialize adapter only once
                    mAdapter = new ImageAdpter(Dashboard.this, mUploads);
                    mRecyclerView.setAdapter(mAdapter);
                } else {
                    mAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Database Error: " + error.getMessage());
            }
        });
        //////////////////////////////////show trusted people in list/////////////////////////

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor sensorshake = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        SensorEventListener sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                if (sensorEvent != null) {
                    float x_acc = sensorEvent.values[0];
                    float y_acc = sensorEvent.values[1];
                    float z_acc = sensorEvent.values[2];

                    float flsum = Math.abs(x_acc) + Math.abs(y_acc) + Math.abs(z_acc);

                    if (flsum > 45) {
                        alertgenrator();
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
            }
        };
        sensorManager.registerListener(sensorEventListener, sensorshake, SensorManager.SENSOR_DELAY_NORMAL);

    }



    ////////////////////////////////////////////////////sensor access shaking device//////////////////////////
    private void alertgenrator() {
        Dialog dialog = new Dialog(Dashboard.this);
        dialog.setContentView(R.layout.alertdialog);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);
        dialog.getWindow().getAttributes().windowAnimations = R.style.animation;
        okay_text = dialog.findViewById(R.id.okay_text);
        new CountDownTimer(5000, 1000) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onTick(long millisUntilFinished) {
                okay_text.setText("" + millisUntilFinished / 1000);
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onFinish() {
               //requestLocationUpdates();
                dialog.dismiss();
                fetchLocation();

            }
        }.start();
        cancel_text = dialog.findViewById(R.id.cancel_text);
        cancel_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Toast.makeText(Dashboard.this, "Cancel clicked", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
    }

    ////////////////////////////////////////////alert Genrator

    private void fetchLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }

        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location == null) {
                    Toast.makeText(Dashboard.this, "Enable GPS", Toast.LENGTH_SHORT).show();
                    return;
                }

                currentLocation = location;
                double lat = currentLocation.getLatitude();
                double lng = currentLocation.getLongitude();

                String lat1 = Double.toString(lat);
                String log1 = Double.toString(lng);

                sendsms(lat1, log1);
            }
        });
    }

    // Prevent duplicate messages
    private void sendsms(String lat1, String log1) {
        if (ussd == null || ussd.isEmpty()) {
            Log.e(TAG, "USSD code is null or empty, cannot fetch emergency contacts.");
            return;
        }

        Log.d(TAG, "USSD Code: " + ussd); // Debugging log

        databaseReference = FirebaseDatabase.getInstance().getReference().child("emergency").child(ussd);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String phoneNumber = userSnapshot.child("person_mobile").getValue(String.class);

                    if (phoneNumber != null && !phoneNumber.isEmpty() && !processedNumbers.contains(phoneNumber)) {
                        processedNumbers.add(phoneNumber);

                        String name = (restaurant_name != null) ? restaurant_name : "Unknown";
                        String sos = (ssoss != null) ? ssoss : "emergency";

                        String sms = "Hey I am: \n" +
                                name + "\n" + sos +
                                "\n\nMy Current location is:\n" +
                                "https://www.google.com/maps?q=" + lat1 + "," + log1;

                        Log.d(TAG, "Processing phone number: " + phoneNumber);
                        Log.d("SMS_DEBUG", "sendsms() called with lat: " + lat1 + ", log: " + log1);

                        try {
                            sendSMS1(phoneNumber, sms);
                          //  Toast.makeText(Dashboard.this, "Location: " + lat1 + ", " + log1, Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Log.e(TAG, "Failed to send SMS to " + phoneNumber + ": " + e.getMessage());
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
                Toast.makeText(Dashboard.this, "Failed to fetch emergency contacts. Please check your internet connection.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void sendSMS1(String phoneNumber, String sms) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty() || sms == null || sms.trim().isEmpty()) {
            Toast.makeText(this, "Invalid phone number or message", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, sms, null, null);
            Toast.makeText(this, "SMS sent successfully", Toast.LENGTH_SHORT).show();

            processedNumbers.add(phoneNumber);
        } catch (Exception e) {
            Toast.makeText(this, "Failed to send SMS", Toast.LENGTH_SHORT).show();
            Log.e("SMS_ERROR", "Error sending SMS: " + e.getMessage(), e);
        }
    }


}







