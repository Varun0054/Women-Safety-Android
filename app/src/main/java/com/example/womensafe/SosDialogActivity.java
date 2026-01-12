package com.example.womensafe;

import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SosDialogActivity extends AppCompatActivity {

    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos_dialog);

        // Make the activity show over the lock screen and wake up the device
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            );
        }

        // Make the activity a floating dialog
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        }

        final TextView countdownText = findViewById(R.id.countdown_text);
        View cancelButton = findViewById(R.id.cancel_button);

        countDownTimer = new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                countdownText.setText(String.valueOf(millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                // Countdown finished, proceed with SOS
                if (Dashboard.instance != null) {
                    Dashboard.instance.alertgenrator();
                }
                finish();
            }
        }.start();

        cancelButton.setOnClickListener(v -> {
            countDownTimer.cancel();
            Toast.makeText(this, "SOS Cancelled", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
