package com.example.vaultx;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class DocumentListActivity extends AppCompatActivity {

    private RecyclerView recyclerDocuments;
    private TextView tvEmpty;
    private DocumentsAdapter adapter;
    private List<Document> documentList;

    // Launcher: Pick ANY file (PDF, DOCX, TXT, etc.)
    private final ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri fileUri = result.getData().getData();
                    if (fileUri != null) importFileToVault(fileUri);
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
        setContentView(R.layout.activity_document_list);
        View mainLayout = findViewById(R.id.main);

        ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), systemBars.bottom);
            return insets;
        });



        recyclerDocuments = findViewById(R.id.recycler_documents);
        tvEmpty = findViewById(R.id.tv_empty);

        documentList = new ArrayList<>();
        adapter = new DocumentsAdapter(documentList, this);
        recyclerDocuments.setLayoutManager(new LinearLayoutManager(this));
        recyclerDocuments.setAdapter(adapter);

        FloatingActionButton fabAdd = findViewById(R.id.fab_add);
        fabAdd.startAnimation(android.view.animation.AnimationUtils.loadAnimation(this, R.anim.scale_pop));
        fabAdd.setOnClickListener(v -> showAddOptions());
    }

    private void showAddOptions() {
        com.google.android.material.bottomsheet.BottomSheetDialog sheet =
                new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        View sv = getLayoutInflater().inflate(R.layout.layout_bottom_sheet_doc_add, null);
        sheet.setContentView(sv);

        // Write a text note
        sv.findViewById(R.id.btn_doc_write).setOnClickListener(b -> {
            sheet.dismiss();
            startActivity(new Intent(this, AddDocumentActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        // Import any file (PDF, DOCX, etc.)
        sv.findViewById(R.id.btn_doc_import).setOnClickListener(b -> {
            sheet.dismiss();
            pickAnyFile();
        });

        sheet.show();
    }

    private void pickAnyFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        String[] mimeTypes = {
                "application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "application/vnd.ms-powerpoint",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                "text/plain",
                "text/csv",
                "application/zip",
                "image/jpeg",
                "image/png"
        };
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        filePickerLauncher.launch(Intent.createChooser(intent, "Import File to Vault"));
    }

    private void importFileToVault(Uri uri) {
        new Thread(() -> {
            try {
                // Get filename and mime type
                String fileName = getFileNameFromUri(uri);
                String mimeType = getContentResolver().getType(uri);
                if (mimeType == null) mimeType = "application/octet-stream";

                String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                        ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "default";
                File dir = new File(getFilesDir(), uid + "_vault_docs");
                if (!dir.exists()) dir.mkdir();

                // Avoid overwriting: append timestamp if file exists
                File outFile = new File(dir, fileName);
                if (outFile.exists()) {
                    String base = fileName.contains(".")
                            ? fileName.substring(0, fileName.lastIndexOf("."))
                            : fileName;
                    String ext = fileName.contains(".")
                            ? fileName.substring(fileName.lastIndexOf("."))
                            : "";
                    outFile = new File(dir, base + "_" + System.currentTimeMillis() + ext);
                }
                File finalOutFile = outFile;

                // Copy bytes
                InputStream in = getContentResolver().openInputStream(uri);
                FileOutputStream fos = new FileOutputStream(outFile);
                byte[] buf = new byte[4096];
                int len;
                while ((len = in.read(buf)) > 0) fos.write(buf, 0, len);
                fos.close();
                in.close();

                // Save to Room DB
                String finalMime = mimeType;
                FileEntity entity = new FileEntity(
                        fileName, finalOutFile.getAbsolutePath(), "DOCUMENT",
                        false, false, System.currentTimeMillis());
                VaultDatabase.getInstance(this).fileDao().insert(entity);

                runOnUiThread(() -> {
                    Toast.makeText(this, "✅ \"" + fileName + "\" saved to vault", Toast.LENGTH_SHORT).show();
                    loadDocuments();
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Import failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private String getFileNameFromUri(Uri uri) {
        String name = "file_" + System.currentTimeMillis();
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            if (idx >= 0) name = cursor.getString(idx);
            cursor.close();
        }
        return name;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDocuments();
    }

    private void loadDocuments() {
        documentList.clear();
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "default";
        File dir = new File(getFilesDir(), uid + "_vault_docs");
        if (dir.exists()) {
            File[] files = dir.listFiles(f -> !f.getName().endsWith(".tmp"));
            if (files != null) {
                for (File file : files) {
                    String name = file.getName();
                    String mime = guessMimeFromExtension(name);

                    if (mime.equals("text/plain")) {
                        // Text doc: read content for preview
                        try {
                            FileInputStream fis = new FileInputStream(file);
                            byte[] data = new byte[(int) Math.min(file.length(), 500)];
                            fis.read(data);
                            fis.close();
                            String preview = new String(data, "UTF-8");
                            String title = name.contains(".") ? name.substring(0, name.lastIndexOf(".")) : name;
                            documentList.add(new Document(title, preview, name, file.getAbsolutePath(), mime));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        // Binary file: no content preview
                        String title = name.contains(".") ? name.substring(0, name.lastIndexOf(".")) : name;
                        documentList.add(new Document(title, "", name, file.getAbsolutePath(), mime));
                    }
                }
            }
        }

        adapter.notifyDataSetChanged();
        tvEmpty.setVisibility(documentList.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerDocuments.setVisibility(documentList.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private String guessMimeFromExtension(String name) {
        if (name.endsWith(".pdf"))  return "application/pdf";
        if (name.endsWith(".doc"))  return "application/msword";
        if (name.endsWith(".docx")) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        if (name.endsWith(".xls"))  return "application/vnd.ms-excel";
        if (name.endsWith(".xlsx")) return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        if (name.endsWith(".ppt"))  return "application/vnd.ms-powerpoint";
        if (name.endsWith(".pptx")) return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return "image/jpeg";
        if (name.endsWith(".png"))  return "image/png";
        if (name.endsWith(".zip"))  return "application/zip";
        return "text/plain";
    }
}
