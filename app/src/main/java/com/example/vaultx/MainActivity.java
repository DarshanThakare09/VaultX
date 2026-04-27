package com.example.vaultx;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerRecent;
    private TextView tvEmptyRecent;
    private RecentFilesAdapter recentAdapter;
    private TextView tvStatDocs, tvStatPhotos, tvStatPasswords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // BLOCK SCREENSHOTS AND SCREEN RECORDING
        getWindow().setFlags(
                android.view.WindowManager.LayoutParams.FLAG_SECURE,
                android.view.WindowManager.LayoutParams.FLAG_SECURE
        );

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        View mainLayout = findViewById(R.id.main);

        ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), systemBars.bottom);
            return insets;
        });

        // Greet user
        TextView tvGreeting = findViewById(R.id.tv_greeting);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            String name = (email != null && email.contains("@")) ? email.split("@")[0] : "User";
            tvGreeting.setText("Hello, " + capitalize(name) + " 👋");
        }

        // Stats TextViews
        tvStatDocs = findViewById(R.id.tv_stat_docs);
        tvStatPhotos = findViewById(R.id.tv_stat_photos);
        tvStatPasswords = findViewById(R.id.tv_stat_passwords);

        // Settings button
        ImageButton btnSettings = findViewById(R.id.btn_settings);
        btnSettings.setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        // Recent Files RecyclerView
        recyclerRecent = findViewById(R.id.recycler_recent_files);
        tvEmptyRecent = findViewById(R.id.tv_empty_recent);
        recyclerRecent.setLayoutManager(new LinearLayoutManager(this));
        recentAdapter = new RecentFilesAdapter(new ArrayList<>());
        recyclerRecent.setAdapter(recentAdapter);

        // Live Search
        EditText etSearch = findViewById(R.id.et_search);
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (recentAdapter != null) recentAdapter.filterFiles(s.toString());
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        // Category Cards with scale animation
        View.OnTouchListener touchAnim = (v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN)
                v.animate().scaleX(0.94f).scaleY(0.94f).setDuration(100).start();
            else if (event.getAction() == android.view.MotionEvent.ACTION_UP ||
                     event.getAction() == android.view.MotionEvent.ACTION_CANCEL)
                v.animate().scaleX(1f).scaleY(1f).setDuration(150).start();
            return false;
        };

        View cardDocuments = findViewById(R.id.card_documents);
        View cardPhotos    = findViewById(R.id.card_photos);
        View cardPasswords = findViewById(R.id.card_passwords);
        View cardNotes     = findViewById(R.id.card_notes);

        cardDocuments.setOnTouchListener(touchAnim);
        cardPhotos.setOnTouchListener(touchAnim);
        cardPasswords.setOnTouchListener(touchAnim);
        cardNotes.setOnTouchListener(touchAnim);

        cardDocuments.setOnClickListener(v -> launch(DocumentListActivity.class));
        cardPhotos.setOnClickListener(v -> launch(PhotoListActivity.class));
        cardPasswords.setOnClickListener(v -> launch(PasswordListActivity.class));
        cardNotes.setOnClickListener(v -> launch(NoteListActivity.class));

        // FAB — Add Menu Bottom Sheet
        FloatingActionButton fabAdd = findViewById(R.id.fab_main_add);
        fabAdd.setOnClickListener(v -> showAddBottomSheet());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRecentFiles();
        loadStats();
    }

    private void launch(Class<?> cls) {
        startActivity(new Intent(this, cls));
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void loadRecentFiles() {
        new Thread(() -> {
            List<FileEntity> files = VaultDatabase.getInstance(this).fileDao().getAllFiles();
            runOnUiThread(() -> {
                recentAdapter.setFiles(files);
                boolean empty = files.isEmpty();
                tvEmptyRecent.setVisibility(empty ? View.VISIBLE : View.GONE);
                recyclerRecent.setVisibility(empty ? View.GONE : View.VISIBLE);
            });
        }).start();
    }

    private void loadStats() {
        new Thread(() -> {
            List<FileEntity> docs  = VaultDatabase.getInstance(this).fileDao().getFilesByType("DOCUMENT");
            List<FileEntity> photos = VaultDatabase.getInstance(this).fileDao().getFilesByType("PHOTO");
            List<FileEntity> passes = VaultDatabase.getInstance(this).fileDao().getFilesByType("PASSWORD");
            runOnUiThread(() -> {
                tvStatDocs.setText(String.valueOf(docs.size()));
                tvStatPhotos.setText(String.valueOf(photos.size()));
                tvStatPasswords.setText(String.valueOf(passes.size()));
            });
        }).start();
    }

    private void showAddBottomSheet() {
        com.google.android.material.bottomsheet.BottomSheetDialog sheet =
                new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_bottom_sheet_add, null);
        sheet.setContentView(view);

        view.findViewById(R.id.btn_add_document).setOnClickListener(b -> { sheet.dismiss(); launch(AddDocumentActivity.class); });
        view.findViewById(R.id.btn_add_photo).setOnClickListener(b -> { sheet.dismiss(); launch(PhotoListActivity.class); });
        view.findViewById(R.id.btn_add_password).setOnClickListener(b -> { sheet.dismiss(); launch(AddPasswordActivity.class); });
        view.findViewById(R.id.btn_add_note).setOnClickListener(b -> { sheet.dismiss(); launch(AddNoteActivity.class); });
        view.findViewById(R.id.btn_scan_document).setOnClickListener(b -> {
            sheet.dismiss();
            android.widget.Toast.makeText(this, "Document scanner launching camera…", android.widget.Toast.LENGTH_SHORT).show();
            Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivity(takePicture);
        });

        sheet.show();
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}