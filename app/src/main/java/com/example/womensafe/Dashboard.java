package com.example.womensafe;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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

    CardView c1, c2, c3, chatbotCard, currentLocationCard;
    DatabaseReference db;
    FirebaseAuth auth;
    String ussd = "";
    String mob = "";
    ImageView imageView;

    private RecyclerView mRecyclerView;
    private ImageAdpter mAdapter;
    private List<people> mUploads;
    ImageView img4;

    private FusedLocationProviderClient fusedLocationClient;
    private final Set<String> processedNumbers = new HashSet<>();
    DatabaseReference databaseReferencemsg;
    String ssoss, restaurant_name;

    private static final int REQUEST_CODE = 101;
    private static final int REQUEST_SEND_SMS_PERMISSION = 2;

    // Shake detection
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private SensorEventListener sensorEventListener;
    private Dialog alert;
    private CountDownTimer countDownTimer;
    private String lastLatForSms;
    private String lastLonForSms;

    String SENT = "SMS_SENT";
    String DELIVERED = "SMS_DELIVERED";
    PendingIntent sentPI, deliveredPI;
    BroadcastReceiver smsSentReceiver, smsDeliveredReceiver;

    private final ActivityResultLauncher<Intent> overlayPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (!Settings.canDrawOverlays(this)) {
                    Toast.makeText(this, "Overlay permission is required for emergency alerts.", Toast.LENGTH_SHORT).show();
                }
            });


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        c1 = findViewById(R.id.card1);
        c2 = findViewById(R.id.card2);
        c3 = findViewById(R.id.card3);
        chatbotCard = findViewById(R.id.chatbot_card);
        currentLocationCard = findViewById(R.id.current_location_card);

        TextView textView = findViewById(R.id.womenname);
        imageView = findViewById(R.id.imageview1);

        img4 = findViewById(R.id.menuimage);

        mRecyclerView = findViewById(R.id.rcc);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance().getReference("users");

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Start the EmergencyTriggerService
        Intent serviceIntent = new Intent(this, EmergencyTriggerService.class);
        startService(serviceIntent);

        checkOverlayPermission();

        sentPI = PendingIntent.getBroadcast(this, 1, new Intent(SENT), PendingIntent.FLAG_IMMUTABLE);
        deliveredPI = PendingIntent.getBroadcast(this, 2, new Intent(DELIVERED), PendingIntent.FLAG_IMMUTABLE);

        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentFirebaseUser != null) {
            ussd = currentFirebaseUser.getUid();
        }

        c1.setOnClickListener(view -> {
            Intent intent = new Intent(Dashboard.this, addpeople.class);
            startActivity(intent);
        });

        c2.setOnClickListener(view -> alertgenrator());
        c3.setOnClickListener(view -> {
            Intent intent = new Intent(Dashboard.this, safetytips.class);
            startActivity(intent);
        });
        chatbotCard.setOnClickListener(view -> {
            Intent intent = new Intent(Dashboard.this, com.example.womensafe.ChatbotActivity.class);
            startActivity(intent);
        });

        currentLocationCard.setOnClickListener(view -> {
            Intent intent = new Intent(Dashboard.this, locationn.class);
            startActivity(intent);
        });

        imageView.setOnClickListener(v -> {
            Intent intent = new Intent(Dashboard.this, profile.class);
            startActivity(intent);
        });

        img4.setOnClickListener(view -> {
            Intent intent = new Intent(Dashboard.this, menu.class);
            startActivity(intent);
        });

        if (!ussd.isEmpty()) {
            db.child(ussd).addListenerForSingleValueEvent(new ValueEventListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        restaurant_name = dataSnapshot.child("name").getValue(String.class);
                        mob = dataSnapshot.child("mobile").getValue(String.class);
                        String link = dataSnapshot.child("uri").getValue(String.class);
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

            mUploads = new ArrayList<>();
            DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference("emergency").child(ussd);
            mDatabaseRef.addValueEventListener(new ValueEventListener() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    mUploads.clear();
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        people person = postSnapshot.getValue(people.class);
                        if (person != null) {
                            mUploads.add(person);
                        }
                    }
                    if (mAdapter == null) {
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
        }

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorEventListener = new SensorEventListener() {
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
            public void onAccuracyChanged(Sensor sensor, int i) {}
        };
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        // Set up broadcast receivers for SMS status
        smsSentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(context, "SMS sent", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(context, "SMS failed: Generic failure", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(context, "SMS failed: No service", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(context, "SMS failed: Null PDU", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(context, "SMS failed: Radio off", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };

        smsDeliveredReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(context, "SMS delivered", Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(context, "SMS not delivered", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(smsSentReceiver, new IntentFilter(SENT), Context.RECEIVER_NOT_EXPORTED);
            registerReceiver(smsDeliveredReceiver, new IntentFilter(DELIVERED), Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(smsSentReceiver, new IntentFilter(SENT));
            registerReceiver(smsDeliveredReceiver, new IntentFilter(DELIVERED));
        }

        // Fetch latest SOS message every time the activity resumes
        if (ussd != null && !ussd.isEmpty()) {
            databaseReferencemsg = FirebaseDatabase.getInstance().getReference("customemsg");
            databaseReferencemsg.child(ussd).child("sos_msg").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        ssoss = dataSnapshot.child("massage").getValue(String.class);
                        Log.i(TAG, "Fetched latest SOS message: " + ssoss);
                    } else {
                        Log.w(TAG, "No SOS message document found for user.");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "SOS message fetch cancelled: " + databaseError.getMessage());
                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(sensorEventListener);
        unregisterReceiver(smsSentReceiver);
        unregisterReceiver(smsDeliveredReceiver);
    }

    public void alertgenrator() {
        if (alert != null && alert.isShowing()) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(Dashboard.this);
        View view = getLayoutInflater().inflate(R.layout.alertdialog, null);
        builder.setView(view);

        alert = builder.create();
        alert.setCancelable(false);

        final TextView okay_text = view.findViewById(R.id.okay_text);
        TextView cancel_text = view.findViewById(R.id.cancel_text);

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(5000, 1000) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onTick(long millisUntilFinished) {
                okay_text.setText("" + millisUntilFinished / 1000);
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onFinish() {
                if (alert.isShowing()) {
                    alert.dismiss();
                }
                fetchLocation();
            }
        }.start();

        cancel_text.setOnClickListener(v -> {
            countDownTimer.cancel();
            alert.dismiss();
            Toast.makeText(Dashboard.this, "Operation Cancelled", Toast.LENGTH_SHORT).show();
        });

        alert.show();
    }

    private void fetchLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location == null) {
                Toast.makeText(Dashboard.this, "Enable GPS to send location", Toast.LENGTH_SHORT).show();
                return;
            }
            sendsms(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to get location: " + e.getMessage());
            Toast.makeText(Dashboard.this, "Failed to get location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE: // Location
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    fetchLocation();
                } else {
                    Toast.makeText(this, "Location permission denied. Cannot send location.", Toast.LENGTH_LONG).show();
                }
                break;
            case REQUEST_SEND_SMS_PERMISSION: // SMS
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (lastLatForSms != null && lastLonForSms != null) {
                        sendsms(lastLatForSms, lastLonForSms);
                    }
                } else {
                    Toast.makeText(this, "SMS permission denied. SOS message not sent.", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private void sendsms(String lat1, String log1) {
        if (ussd == null || ussd.isEmpty()) {
            Log.e(TAG, "User ID is null or empty, cannot fetch emergency contacts.");
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            lastLatForSms = lat1;
            lastLonForSms = log1;
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, REQUEST_SEND_SMS_PERMISSION);
            return; // Wait for permission result
        }

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("emergency").child(ussd);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                processedNumbers.clear();
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String phoneNumber = userSnapshot.child("person_mobile").getValue(String.class);

                    if (phoneNumber != null && !phoneNumber.isEmpty() && !processedNumbers.contains(phoneNumber)) {
                        String name = (restaurant_name != null) ? restaurant_name : "Unknown";
                        String sos = (ssoss != null) ? ssoss : "I am in an emergency situation.";
                        String sms = "Emergency! I am " + name + ".\n" + sos + "\nMy current location is: https://www.google.com/maps?q=" + lat1 + "," + log1;

                        Log.i(TAG, "Preparing to send SMS to: " + phoneNumber + " with message: " + sms);
                        sendSMS1(phoneNumber, sms);
                        processedNumbers.add(phoneNumber);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
                Toast.makeText(Dashboard.this, "Failed to fetch emergency contacts.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendSMS1(String phoneNumber, String sms) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            ArrayList<String> parts = smsManager.divideMessage(sms);

            ArrayList<PendingIntent> sentIntents = new ArrayList<>();
            ArrayList<PendingIntent> deliveredIntents = new ArrayList<>();

            for (int i = 0; i < parts.size(); i++) {
                sentIntents.add(sentPI);
                deliveredIntents.add(deliveredPI);
            }

            smsManager.sendMultipartTextMessage(phoneNumber, null, parts, sentIntents, deliveredIntents);

        } catch (Exception e) {
            Toast.makeText(this, "Failed to send SMS to " + phoneNumber, Toast.LENGTH_SHORT).show();
            Log.e("SMS_ERROR", "Error sending SMS: " + e.getMessage(), e);
        }
    }

    private void checkOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            overlayPermissionLauncher.launch(intent);
        }
    }
}
