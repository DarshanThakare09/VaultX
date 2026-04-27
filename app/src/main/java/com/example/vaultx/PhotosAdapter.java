package com.example.vaultx;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

public class PhotosAdapter extends RecyclerView.Adapter<PhotosAdapter.PhotoViewHolder> {

    private List<File> photosList;
    private Context context;

    public PhotosAdapter(List<File> photosList, Context context) {
        this.photosList = photosList;
        this.context = context;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        File photoFile = photosList.get(position);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath(), options);
        holder.ivThumbnail.setImageBitmap(bitmap);

        // Tap to view full-screen
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PhotoViewerActivity.class);
            intent.putExtra("imagePath", photoFile.getAbsolutePath());
            context.startActivity(intent);
        });

        // Share button
        holder.btnShare.setOnClickListener(v -> {
            try {
                Uri uri = FileProvider.getUriForFile(context,
                        context.getPackageName() + ".provider", photoFile);
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("image/jpeg");
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.startActivity(Intent.createChooser(shareIntent, "Share Photo"));
            } catch (Exception e) {
                Toast.makeText(context, "Share failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return photosList.size();
    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail;
        ImageButton btnShare;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumbnail = itemView.findViewById(R.id.iv_thumbnail);
            btnShare    = itemView.findViewById(R.id.btn_share_photo);
        }
    }
}
