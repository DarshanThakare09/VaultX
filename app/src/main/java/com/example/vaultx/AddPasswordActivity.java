package com.example.vaultx;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.SecureRandom;

public class AddPasswordActivity extends AppCompatActivity {

    private EditText etTitle, etWebsite, etUser, etPass;
    private Button btnSave, btnGenerate;
    private ImageButton btnToggle;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_password);
        View mainLayout = findViewById(R.id.main);

        ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), systemBars.bottom);
            return insets;
        });

        etTitle = findViewById(R.id.et_pass_title);
        etWebsite = findViewById(R.id.et_pass_website);
        etUser = findViewById(R.id.et_pass_user);
        etPass = findViewById(R.id.et_pass_code);
        btnSave = findViewById(R.id.btn_save);
        btnGenerate = findViewById(R.id.btn_generate);
        btnToggle = findViewById(R.id.btn_toggle_visibility);

        btnSave.setOnClickListener(v -> savePassword());

        btnGenerate.setOnClickListener(v -> {
            etPass.setText(generateRandomPassword(16));
        });

        btnToggle.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            if (isPasswordVisible) {
                etPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                btnToggle.setImageResource(android.R.drawable.ic_menu_close_clear_cancel); // placeholder for hidden
            } else {
                etPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                btnToggle.setImageResource(android.R.drawable.ic_menu_view);
            }
            etPass.setSelection(etPass.getText().length());
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private String generateRandomPassword(int length) {
        final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(chars.length());
            sb.append(chars.charAt(randomIndex));
        }
        return sb.toString();
    }

    private void savePassword() {
        String title = etTitle.getText().toString().trim();
        String website = etWebsite.getText().toString().trim();
        String user = etUser.getText().toString().trim();
        String pass = etPass.getText().toString().trim();

        if (title.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Title and Password are required", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null ? 
                     com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid() : "default";
        File dir = new File(getFilesDir(), uid + "_vault_passwords");
        if (!dir.exists()) {
            dir.mkdir();
        }

        String content = title + "\n" + user + "\n" + pass + "\n" + website;
        File file = new File(dir, title + "_" + System.currentTimeMillis() + ".txt");
        
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(content.getBytes("UTF-8"));
            fos.close();
            
            // Insert into Room DB
            FileEntity entity = new FileEntity(title, file.getAbsolutePath(), "PASSWORD", false, false, System.currentTimeMillis());
            new Thread(() -> {
                VaultDatabase.getInstance(AddPasswordActivity.this).fileDao().insert(entity);
            }).start();

            Toast.makeText(this, "Password saved", Toast.LENGTH_SHORT).show();
            finish();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving password", Toast.LENGTH_SHORT).show();
        }
    }
}
