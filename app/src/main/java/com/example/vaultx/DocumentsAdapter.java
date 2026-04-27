package com.example.vaultx;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

public class DocumentsAdapter extends RecyclerView.Adapter<DocumentsAdapter.DocumentViewHolder> {

    private List<Document> documentsList;
    private Context context;

    public DocumentsAdapter(List<Document> documentsList, Context context) {
        this.documentsList = documentsList;
        this.context = context;
    }

    @NonNull
    @Override
    public DocumentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_document, parent, false);
        return new DocumentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DocumentViewHolder holder, int position) {
        Document doc = documentsList.get(position);
        holder.tvTitle.setText(doc.getTitle());

        // Show mime type or content preview
        String mime = doc.getMimeType();
        if (mime != null && !mime.equals("text/plain")) {
            holder.tvContent.setText(getMimeLabel(mime));
        } else {
            String preview = doc.getContent();
            holder.tvContent.setText(preview.isEmpty() ? "Tap to open" : preview);
        }

        // Set icon by type
        holder.ivTypeIcon.setImageResource(getIconForMime(mime));

        // Tap item → open with system app
        holder.itemView.setOnClickListener(v -> openDocument(doc));

        // Share button
        holder.btnShare.setOnClickListener(v -> shareDocument(doc));
    }

    private void openDocument(Document doc) {
        try {
            File f = new File(doc.getFilePath().isEmpty() ? "" : doc.getFilePath());
            if (!f.exists()) {
                Toast.makeText(context, "File not found on disk.", Toast.LENGTH_SHORT).show();
                return;
            }
            Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", f);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, doc.getMimeType());
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(Intent.createChooser(intent, "Open with"));
        } catch (Exception e) {
            Toast.makeText(context, "No app found to open this file.", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareDocument(Document doc) {
        try {
            File f = new File(doc.getFilePath().isEmpty() ? "" : doc.getFilePath());
            if (!f.exists()) {
                // For text-only docs, share content as text
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, doc.getTitle() + "\n\n" + doc.getContent());
                context.startActivity(Intent.createChooser(intent, "Share " + doc.getTitle()));
                return;
            }
            Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", f);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType(doc.getMimeType());
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(Intent.createChooser(intent, "Share " + doc.getTitle()));
        } catch (Exception e) {
            Toast.makeText(context, "Share failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String getMimeLabel(String mime) {
        if (mime == null) return "Document";
        if (mime.contains("pdf"))  return "PDF Document";
        if (mime.contains("word") || mime.contains("docx")) return "Word Document";
        if (mime.contains("image")) return "Image";
        if (mime.contains("zip"))  return "Archive";
        return "File";
    }

    private int getIconForMime(String mime) {
        if (mime == null) return android.R.drawable.ic_menu_edit;
        if (mime.contains("pdf"))   return android.R.drawable.ic_menu_agenda;
        if (mime.contains("image")) return android.R.drawable.ic_menu_gallery;
        return android.R.drawable.ic_menu_edit;
    }

    @Override
    public int getItemCount() {
        return documentsList.size();
    }

    static class DocumentViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent;
        ImageView ivTypeIcon;
        ImageButton btnShare;

        public DocumentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle   = itemView.findViewById(R.id.tv_item_title);
            tvContent = itemView.findViewById(R.id.tv_item_content);
            ivTypeIcon = itemView.findViewById(R.id.iv_doc_type_icon);
            btnShare  = itemView.findViewById(R.id.btn_share_doc);
        }
    }
}
