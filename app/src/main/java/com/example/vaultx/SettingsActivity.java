package com.example.vaultx;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREF_NAME = "vault_settings";
    private static final String KEY_APP_LOCK = "app_lock_enabled";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(
                android.view.WindowManager.LayoutParams.FLAG_SECURE,
                android.view.WindowManager.LayoutParams.FLAG_SECURE
        );
        setContentView(R.layout.activity_settings);

        ImageButton btnBack = findViewById(R.id.btn_settings_back);
        btnBack.setOnClickListener(v -> finish());

        // App Lock Toggle
        Switch switchAppLock = findViewById(R.id.switch_app_lock);
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        switchAppLock.setChecked(prefs.getBoolean(KEY_APP_LOCK, true));
        switchAppLock.setOnCheckedChangeListener((btn, checked) -> {
            prefs.edit().putBoolean(KEY_APP_LOCK, checked).apply();
            Toast.makeText(this, checked ? "App Lock Enabled" : "App Lock Disabled", Toast.LENGTH_SHORT).show();
        });

        // Change PIN
        LinearLayout btnChangePin = findViewById(R.id.btn_change_pin);
        btnChangePin.setOnClickListener(v -> showChangePinDialog());

        // Logout
        LinearLayout btnLogout = findViewById(R.id.btn_logout_settings);
        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to log out? Your data will remain encrypted and safe.")
                .setPositiveButton("Logout", (dialog, which) -> {
                    PinManager.clearPin(this);
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                })
                .setNegativeButton("Cancel", null)
                .show();
        });

        // Display current user email
        TextView tvUserEmail = findViewById(R.id.tv_settings_email);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            tvUserEmail.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void showChangePinDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_pin, null);
        TextInputEditText etOldPin = dialogView.findViewById(R.id.et_old_pin);
        TextInputEditText etNewPin = dialogView.findViewById(R.id.et_new_pin);
        TextInputEditText etConfirmPin = dialogView.findViewById(R.id.et_confirm_pin);

        new AlertDialog.Builder(this)
            .setTitle("Change PIN")
            .setView(dialogView)
            .setPositiveButton("Save", (dialog, which) -> {
                String oldPin = etOldPin.getText() != null ? etOldPin.getText().toString().trim() : "";
                String newPin = etNewPin.getText() != null ? etNewPin.getText().toString().trim() : "";
                String confirmPin = etConfirmPin.getText() != null ? etConfirmPin.getText().toString().trim() : "";

                String savedPin = PinManager.getPin(this);
                if (!oldPin.equals(savedPin)) {
                    Toast.makeText(this, "Incorrect current PIN", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (newPin.length() < 4) {
                    Toast.makeText(this, "PIN must be at least 4 digits", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!newPin.equals(confirmPin)) {
                    Toast.makeText(this, "New PINs do not match", Toast.LENGTH_SHORT).show();
                    return;
                }
                PinManager.savePin(this, newPin);
                Toast.makeText(this, "PIN changed successfully!", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    public static boolean isAppLockEnabled(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_APP_LOCK, true);
    }
}
