package com.example.vaultx;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {

    private List<Note> notesList;
    private Context context;

    public NotesAdapter(List<Note> notesList, Context context) {
        this.notesList = notesList;
        this.context = context;
    }

    // Backward-compat constructor
    public NotesAdapter(List<Note> notesList) {
        this.notesList = notesList;
        this.context = null;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = notesList.get(position);
        holder.tvTitle.setText(note.getTitle());
        holder.tvContent.setText(note.getContent());

        try {
            holder.cardNote.setCardBackgroundColor(Color.parseColor(note.getColorHex()));
        } catch (Exception e) {
            holder.cardNote.setCardBackgroundColor(Color.parseColor("#2A2A35"));
        }

        // Share note as text
        holder.btnShare.setOnClickListener(v -> {
            Context ctx = context != null ? context : v.getContext();
            String shareText = note.getTitle() + "\n\n" + note.getContent();
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, shareText);
            ctx.startActivity(Intent.createChooser(intent, "Share Note"));
        });
    }

    @Override
    public int getItemCount() {
        return notesList.size();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent;
        MaterialCardView cardNote;
        ImageButton btnShare;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle  = itemView.findViewById(R.id.tv_item_title);
            tvContent = itemView.findViewById(R.id.tv_item_content);
            cardNote = itemView.findViewById(R.id.card_note);
            btnShare = itemView.findViewById(R.id.btn_share_note);
        }
    }
}
