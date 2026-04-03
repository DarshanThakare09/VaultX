package com.example.vaultx;
import android.content.Context;

public class PinManager {

    private static final String PREF = "vault_pref";
    private static final String KEY_PIN = "user_pin";

    public static void savePin(Context context, String pin) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_PIN, pin)
                .apply();
    }

    public static String getPin(Context context) {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .getString(KEY_PIN, null);
    }

    public static void clearPin(Context context) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .edit()
                .remove(KEY_PIN)
                .apply();
    }
}