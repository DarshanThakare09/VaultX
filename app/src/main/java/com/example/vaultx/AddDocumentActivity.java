package com.example.vaultx;

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

public class AddDocumentActivity extends AppCompatActivity {

    private EditText etDocTitle, etDocContent;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_document);
        View mainLayout = findViewById(R.id.main);

        ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), systemBars.bottom);
            return insets;
        });

        etDocTitle = findViewById(R.id.et_doc_title);
        etDocContent = findViewById(R.id.et_doc_content);
        btnSave = findViewById(R.id.btn_save);

        btnSave.setOnClickListener(v -> saveDocument());
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void saveDocument() {
        String title = etDocTitle.getText().toString().trim();
        String content = etDocContent.getText().toString().trim();

        if (title.isEmpty()) {
            etDocTitle.setError("Title cannot be empty");
            return;
        }

        String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null ? 
                     com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid() : "default";
        File dir = new File(getFilesDir(), uid + "_vault_docs");
        if (!dir.exists()) {
            dir.mkdir();
        }

        File file = new File(dir, title + ".txt");
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(content.getBytes("UTF-8"));
            fos.close();
            
            // Insert into Room DB
            FileEntity entity = new FileEntity(title, file.getAbsolutePath(), "DOCUMENT", false, false, System.currentTimeMillis());
            new Thread(() -> {
                VaultDatabase.getInstance(AddDocumentActivity.this).fileDao().insert(entity);
            }).start();

            Toast.makeText(this, "Document saved", Toast.LENGTH_SHORT).show();
            finish();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving document", Toast.LENGTH_SHORT).show();
        }
    }
}
