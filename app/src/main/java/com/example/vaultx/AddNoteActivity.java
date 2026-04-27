package com.example.vaultx;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class AddNoteActivity extends AppCompatActivity {

    private EditText etTitle, etContent;
    private Button btnSave;
    private String selectedColorHex = "#2A2A35"; // default color

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_note);
        View mainLayout = findViewById(R.id.main);

        ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), systemBars.bottom);
            return insets;
        });

        etTitle = findViewById(R.id.et_note_title);
        etContent = findViewById(R.id.et_note_content);
        btnSave = findViewById(R.id.btn_save);

        setupColorSelection();

        btnSave.setOnClickListener(v -> saveNote());
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void setupColorSelection() {
        View c1 = findViewById(R.id.color_1);
        View c2 = findViewById(R.id.color_2);
        View c3 = findViewById(R.id.color_3);
        View c4 = findViewById(R.id.color_4);
        View c5 = findViewById(R.id.color_5);

        View.OnClickListener listener = v -> {
            // Reset stroke or visual selection here if desired
            int id = v.getId();
            if (id == R.id.color_1) selectedColorHex = "#2A2A35";
            else if (id == R.id.color_2) selectedColorHex = "#424242";
            else if (id == R.id.color_3) selectedColorHex = "#1565C0";
            else if (id == R.id.color_4) selectedColorHex = "#2E7D32";
            else if (id == R.id.color_5) selectedColorHex = "#C62828";
            
            Toast.makeText(this, "Color selected", Toast.LENGTH_SHORT).show();
        };

        c1.setOnClickListener(listener);
        c2.setOnClickListener(listener);
        c3.setOnClickListener(listener);
        c4.setOnClickListener(listener);
        c5.setOnClickListener(listener);
    }

    private void saveNote() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "Title and content cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null ? 
                     com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid() : "default";
        File dir = new File(getFilesDir(), uid + "_vault_notes");
        if (!dir.exists()) {
            dir.mkdir();
        }

        String dataToSave = selectedColorHex + "\n" + title + "\n" + content;
        File file = new File(dir, title + "_" + System.currentTimeMillis() + ".txt");
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(dataToSave.getBytes("UTF-8"));
            fos.close();
            
            // Insert into Room DB
            FileEntity entity = new FileEntity(title, file.getAbsolutePath(), "NOTE", false, false, System.currentTimeMillis());
            new Thread(() -> {
                VaultDatabase.getInstance(AddNoteActivity.this).fileDao().insert(entity);
            }).start();

            Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show();
            finish();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving note", Toast.LENGTH_SHORT).show();
        }
    }
}
