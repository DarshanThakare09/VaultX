package com.example.vaultx;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecentFilesAdapter extends RecyclerView.Adapter<RecentFilesAdapter.ViewHolder> {

    private List<FileEntity> files;
    private List<FileEntity> filesFull;

    public RecentFilesAdapter(List<FileEntity> files) {
        this.files = new ArrayList<>(files);
        this.filesFull = new ArrayList<>(files);
    }

    public void setFiles(List<FileEntity> files) {
        this.files = new ArrayList<>(files);
        this.filesFull = new ArrayList<>(files);
        notifyDataSetChanged();
    }

    public void filterFiles(String query) {
        files.clear();
        if (query.isEmpty()) {
            files.addAll(filesFull);
        } else {
            String q = query.toLowerCase(Locale.getDefault());
            for (FileEntity f : filesFull) {
                if (f.name.toLowerCase(Locale.getDefault()).contains(q) ||
                    f.type.toLowerCase(Locale.getDefault()).contains(q)) {
                    files.add(f);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recent_file, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FileEntity file = files.get(position);
        holder.tvName.setText(file.name);

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault());
        holder.tvDate.setText(sdf.format(new Date(file.timestamp)));

        // Lock icon
        holder.ivLock.setVisibility(file.isEncrypted ? View.VISIBLE : View.GONE);

        // Type icon and color tint
        switch (file.type) {
            case "PHOTO":    holder.ivIcon.setImageResource(android.R.drawable.ic_menu_gallery); break;
            case "PASSWORD": holder.ivIcon.setImageResource(android.R.drawable.ic_lock_lock); break;
            case "NOTE":     holder.ivIcon.setImageResource(android.R.drawable.ic_menu_agenda); break;
            default:         holder.ivIcon.setImageResource(android.R.drawable.ic_menu_edit); break;
        }

        // Tap to open
        holder.itemView.setOnClickListener(v -> {
            if (file.isEncrypted) {
                Toast.makeText(v.getContext(), "🔒 File is locked. Use options to unlock.", Toast.LENGTH_SHORT).show();
            } else {
                openFile(v.getContext(), file);
            }
        });

        // Options button → bottom sheet
        holder.ivOptions.setOnClickListener(v -> showFileOptionsSheet(v.getContext(), file, position));
    }

    private void openFile(Context ctx, FileEntity file) {
        Toast.makeText(ctx, "Opening: " + file.name, Toast.LENGTH_SHORT).show();
    }

    private void showFileOptionsSheet(Context ctx, FileEntity file, int position) {
        BottomSheetDialog sheet = new BottomSheetDialog(ctx);
        View sv = LayoutInflater.from(ctx).inflate(R.layout.layout_bottom_sheet_file_options, null);
        sheet.setContentView(sv);

        TextView tvName  = sv.findViewById(R.id.tv_bs_file_name);
        TextView tvLock  = sv.findViewById(R.id.tv_bs_lock_text);
        ImageView ivLockIcon = sv.findViewById(R.id.iv_bs_lock_icon);

        tvName.setText(file.name);
        tvLock.setText(file.isEncrypted ? "Unlock File" : "Lock File");
        ivLockIcon.setImageResource(file.isEncrypted ? android.R.drawable.ic_lock_idle_lock : android.R.drawable.ic_lock_lock);

        // Open
        sv.findViewById(R.id.btn_bs_open).setOnClickListener(b -> {
            sheet.dismiss();
            if (file.isEncrypted) {
                Toast.makeText(ctx, "Unlock the file first.", Toast.LENGTH_SHORT).show();
            } else {
                openFile(ctx, file);
            }
        });

        // Lock / Unlock
        sv.findViewById(R.id.btn_bs_lock).setOnClickListener(b -> {
            sheet.dismiss();
            new Thread(() -> {
                File physicalFile = new File(file.path);
                File tempFile = new File(file.path + ".tmp");
                boolean success;
                if (file.isEncrypted) {
                    success = EncryptionManager.decryptFile(physicalFile, tempFile);
                    if (success) {
                        physicalFile.delete();
                        tempFile.renameTo(physicalFile);
                        file.isEncrypted = false;
                    }
                } else {
                    success = EncryptionManager.encryptFile(physicalFile, tempFile);
                    if (success) {
                        physicalFile.delete();
                        tempFile.renameTo(physicalFile);
                        file.isEncrypted = true;
                    }
                }
                boolean finalSuccess = success;
                boolean finalIsEncrypted = file.isEncrypted;
                VaultDatabase.getInstance(ctx).fileDao().update(file);
                ((android.app.Activity) ctx).runOnUiThread(() -> {
                    if (finalSuccess) {
                        notifyItemChanged(position);
                        Toast.makeText(ctx, finalIsEncrypted ? "🔒 File locked" : "🔓 File unlocked", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ctx, "Operation failed.", Toast.LENGTH_SHORT).show();
                    }
                });
            }).start();
        });

        // Share
        sv.findViewById(R.id.btn_bs_share).setOnClickListener(b -> {
            sheet.dismiss();
            if (file.isEncrypted) {
                Toast.makeText(ctx, "Cannot share a locked file. Unlock it first.", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                File f = new File(file.path);
                android.net.Uri uri = FileProvider.getUriForFile(ctx,
                        ctx.getPackageName() + ".provider", f);
                android.content.Intent shareIntent = new android.content.Intent(android.content.Intent.ACTION_SEND);
                shareIntent.setType("*/*");
                shareIntent.putExtra(android.content.Intent.EXTRA_STREAM, uri);
                shareIntent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
                ctx.startActivity(android.content.Intent.createChooser(shareIntent, "Share " + file.name));
            } catch (Exception e) {
                Toast.makeText(ctx, "Share failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Delete
        sv.findViewById(R.id.btn_bs_delete).setOnClickListener(b -> {
            sheet.dismiss();
            new androidx.appcompat.app.AlertDialog.Builder(ctx)
                .setTitle("Delete File")
                .setMessage("Are you sure you want to permanently delete \"" + file.name + "\"?")
                .setPositiveButton("Delete", (d, w) -> new Thread(() -> {
                    new File(file.path).delete();
                    VaultDatabase.getInstance(ctx).fileDao().delete(file);
                    int idx = files.indexOf(file);
                    filesFull.remove(file);
                    files.remove(file);
                    ((android.app.Activity) ctx).runOnUiThread(() -> {
                        if (idx >= 0) notifyItemRemoved(idx);
                        Toast.makeText(ctx, "Deleted", Toast.LENGTH_SHORT).show();
                    });
                }).start())
                .setNegativeButton("Cancel", null)
                .show();
        });

        sheet.show();
    }

    @Override
    public int getItemCount() {
        return files != null ? files.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDate;
        ImageView ivIcon, ivLock, ivOptions;

        public ViewHolder(@NonNull View v) {
            super(v);
            tvName  = v.findViewById(R.id.tv_file_name);
            tvDate  = v.findViewById(R.id.tv_file_date);
            ivIcon  = v.findViewById(R.id.iv_file_icon);
            ivLock  = v.findViewById(R.id.iv_lock_status);
            ivOptions = v.findViewById(R.id.iv_options);
        }
    }
}
