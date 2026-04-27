package com.example.vaultx;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PasswordsAdapter extends RecyclerView.Adapter<PasswordsAdapter.PasswordViewHolder> {

    private List<Password> passwordsList;
    private Context context;

    public PasswordsAdapter(List<Password> passwordsList, Context context) {
        this.passwordsList = passwordsList;
        this.context = context;
    }

    @NonNull
    @Override
    public PasswordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_password, parent, false);
        return new PasswordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PasswordViewHolder holder, int position) {
        Password pass = passwordsList.get(position);
        holder.tvTitle.setText(pass.getTitle());
        holder.tvUser.setText(pass.getUsername().isEmpty() ? pass.getWebsite() : pass.getUsername());

        // Copy password to clipboard
        holder.btnCopy.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Password", pass.getPassword());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context, "Password copied to clipboard", Toast.LENGTH_SHORT).show();
        });

        // Share account details (excluding password - user decides)
        holder.btnShare.setOnClickListener(v -> {
            String shareText = "Account: " + pass.getTitle()
                    + "\nUsername: " + pass.getUsername()
                    + (pass.getWebsite().isEmpty() ? "" : "\nWebsite: " + pass.getWebsite());
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, shareText);
            context.startActivity(Intent.createChooser(intent, "Share Account Info"));
        });
    }

    @Override
    public int getItemCount() {
        return passwordsList.size();
    }

    static class PasswordViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvUser;
        ImageButton btnCopy, btnShare;

        public PasswordViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle  = itemView.findViewById(R.id.tv_item_title);
            tvUser   = itemView.findViewById(R.id.tv_item_user);
            btnCopy  = itemView.findViewById(R.id.btn_copy);
            btnShare = itemView.findViewById(R.id.btn_share_password);
        }
    }
}
