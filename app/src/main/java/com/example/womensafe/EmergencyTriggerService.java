package com.example.womensafe;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class EmergencyTriggerService extends Service implements SensorEventListener {

    private static final String TAG = "EmergencyTriggerService";
    private static final String CHANNEL_ID = "EmergencyTriggerServiceChannel";
    private static final String SOS_CHANNEL_ID = "SOS_CHANNEL";
    private static final int FOREGROUND_NOTIFICATION_ID = 1;
    private static final int SOS_NOTIFICATION_ID = 101;
    private static final float SHAKE_THRESHOLD_GRAVITY = 3.5F;
    private static final int SHAKE_SLOP_TIME_MS = 500;
    private static final int SHAKE_COUNT_RESET_TIME_MS = 3000;

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private long mShakeTimestamp;
    private int mShakeCount;

    private FusedLocationProviderClient fusedLocationClient;
    private DatabaseReference db;
    private FirebaseAuth auth;
    private String ussd = "";
    private String restaurant_name, ssoss;
    private final Set<String> processedNumbers = new HashSet<>();

    String SENT = "SMS_SENT";
    String DELIVERED = "SMS_DELIVERED";
    PendingIntent sentPI, deliveredPI;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        createSOSNotificationChannel(); // Create channel for SOS notifications

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (accelerometer != null) {
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance().getReference();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            ussd = currentUser.getUid();
            fetchUserData();
        }

        sentPI = PendingIntent.getBroadcast(this, 1, new Intent(SENT), PendingIntent.FLAG_IMMUTABLE);
        deliveredPI = PendingIntent.getBroadcast(this, 2, new Intent(DELIVERED), PendingIntent.FLAG_IMMUTABLE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && SosDialogActivity.ACTION_SEND_SOS.equals(intent.getAction())) {
            fetchLocation();
            return START_STICKY;
        }

        Intent notificationIntent = new Intent(this, Dashboard.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("WomenSafe Protection Enabled")
                .setContentText("Emergency shake detection is active.")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(FOREGROUND_NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
        } else {
            startForeground(FOREGROUND_NOTIFICATION_ID, notification);
        }
        return START_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            handleShake(event);
        }
    }

    private void handleShake(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        float gX = x / SensorManager.GRAVITY_EARTH;
        float gY = y / SensorManager.GRAVITY_EARTH;
        float gZ = z / SensorManager.GRAVITY_EARTH;

        float gForce = (float) Math.sqrt(gX * gX + gY * gY + gZ * gZ);

        if (gForce > SHAKE_THRESHOLD_GRAVITY) {
            final long now = System.currentTimeMillis();
            if (mShakeTimestamp + SHAKE_SLOP_TIME_MS > now) {
                return;
            }

            if (mShakeTimestamp + SHAKE_COUNT_RESET_TIME_MS < now) {
                mShakeCount = 0;
            }

            mShakeTimestamp = now;
            mShakeCount++;

            if (mShakeCount >= 3) {
                triggerSOS("Heavy Shake");
                mShakeCount = 0;
            }
        }
    }

    private void triggerSOS(String source) {
        Log.i(TAG, "SOS triggered by: " + source);

        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            v.vibrate(500);
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (notificationManager.areNotificationsEnabled()) {
            Intent intent = new Intent(this, SosDialogActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

            PendingIntent pi = PendingIntent.getActivity(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            Notification notification =
                    new NotificationCompat.Builder(this, SOS_CHANNEL_ID)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle("Emergency SOS")
                            .setContentText("Shake detected. SOS active.")
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setCategory(NotificationCompat.CATEGORY_ALARM)
                            .setOngoing(true)
                            .setContentIntent(pi)
                            .setFullScreenIntent(pi, true)
                            .build();

            startForeground(SOS_NOTIFICATION_ID, notification);
        } else {
            Log.e(TAG, "Notifications are disabled by the system. Cannot show SOS screen.");
            new Handler(Looper.getMainLooper()).post(() -> {
                String message = "SOS feature cannot be displayed because notifications are disabled for this app. " +
                               "Please enable notifications from your phone's settings. " +
                               "On some devices, you may also need to check a 'Security' or 'Power Manager' app.";
                Toast.makeText(
                    getApplicationContext(),
                    message,
                    Toast.LENGTH_LONG
                ).show();
            });
        }
    }

    private void fetchLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Location permissions not granted.");
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                sendsms(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));
            } else {
                Log.e(TAG, "Failed to get location.");
            }
        });
    }

    private void sendsms(String lat1, String log1) {
        if (ussd == null || ussd.isEmpty()) {
            Log.e(TAG, "User ID is null or empty, cannot fetch emergency contacts.");
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Send SMS permission not granted");
            return;
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
            Log.e("SMS_ERROR", "Error sending SMS: " + e.getMessage(), e);
        }
    }

    private void fetchUserData() {
        db.child("users").child(ussd).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    restaurant_name = dataSnapshot.child("name").getValue(String.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        db.child("customemsg").child(ussd).child("sos_msg").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    ssoss = dataSnapshot.child("massage").getValue(String.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Emergency Trigger Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    private void createSOSNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "SOS Notifications";
            String description = "Channel for SOS alerts";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(SOS_CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
