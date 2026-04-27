package com.example.vaultx;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class PhotoListActivity extends AppCompatActivity {

    private RecyclerView recyclerPhotos;
    private TextView tvEmpty;
    private PhotosAdapter adapter;
    private List<File> photosList;

    private final ActivityResultLauncher<Intent> photoPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) saveImageToInternalStorage(imageUri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(
                android.view.WindowManager.LayoutParams.FLAG_SECURE,
                android.view.WindowManager.LayoutParams.FLAG_SECURE
        );
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_photo_list);
        View mainLayout = findViewById(R.id.main);

        ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), systemBars.bottom);
            return insets;
        });

        recyclerPhotos = findViewById(R.id.recycler_photos);
        tvEmpty = findViewById(R.id.tv_empty);
        FloatingActionButton fabAdd = findViewById(R.id.fab_add);
        fabAdd.startAnimation(android.view.animation.AnimationUtils.loadAnimation(this, R.anim.scale_pop));

        photosList = new ArrayList<>();
        adapter = new PhotosAdapter(photosList, this);
        recyclerPhotos.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerPhotos.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            photoPickerLauncher.launch(intent);
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPhotos();
    }

    private void saveImageToInternalStorage(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                        ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "default";
                File dir = new File(getFilesDir(), uid + "_vault_photos");
                if (!dir.exists()) dir.mkdir();

                // Create .nomedia to hide from gallery
                File noMedia = new File(dir, ".nomedia");
                if (!noMedia.exists()) noMedia.createNewFile();

                long timestamp = System.currentTimeMillis();
                File outFile = new File(dir, "IMG_" + timestamp + ".jpg");
                FileOutputStream fos = new FileOutputStream(outFile);

                byte[] buffer = new byte[4096];
                int len;
                while ((len = inputStream.read(buffer)) > 0) fos.write(buffer, 0, len);
                fos.close();
                inputStream.close();

                // Insert into Room DB
                FileEntity entity = new FileEntity("Photo_" + timestamp, outFile.getAbsolutePath(), "PHOTO", false, false, timestamp);
                new Thread(() -> {
                    VaultDatabase.getInstance(this).fileDao().insert(entity);
                    runOnUiThread(this::loadPhotos);
                }).start();

                Toast.makeText(this, "Photo secured in vault 🔒", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to secure photo", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadPhotos() {
        photosList.clear();
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "default";
        File dir = new File(getFilesDir(), uid + "_vault_photos");
        if (dir.exists()) {
            File[] files = dir.listFiles(f -> !f.getName().equals(".nomedia") && !f.getName().endsWith(".tmp"));
            if (files != null) {
                for (File file : files) photosList.add(file);
            }
        }

        adapter.notifyDataSetChanged();
        tvEmpty.setVisibility(photosList.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerPhotos.setVisibility(photosList.isEmpty() ? View.GONE : View.VISIBLE);
    }
}
