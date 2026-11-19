package com.example.netelsansmarthome;

import android.app.AlertDialog;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.VH> {


    public interface OnRoomAction{
        void onSelected(Room room);
        void onEdit(Room room, int position);
        void onDelete(Room room, int position);
    }

    private final List<Room> rooms;

    private final OnRoomAction listener;

    private int selectedPos = 0;

    public RoomAdapter(List<Room> rooms, OnRoomAction listener) {
        this.rooms = rooms;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_room, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Room room = rooms.get(pos);

        h.txtRoomName.setText(room.name);
        h.imgIcon.setImageResource(room.iconRes);


        if (pos == selectedPos) {
            h.card.setCardBackgroundColor(Color.parseColor("#D32F2F"));
            h.txtRoomName.setTextColor(Color.WHITE);
            h.imgIcon.setColorFilter(Color.WHITE);
        } else {
            h.card.setCardBackgroundColor(Color.parseColor("#F5F5F5"));
            h.txtRoomName.setTextColor(Color.parseColor("#757575"));
            h.imgIcon.setColorFilter(Color.parseColor("#757575"));
        }

        h.itemView.setOnClickListener(v -> {
            int p = h.getAdapterPosition();
            if (p == RecyclerView.NO_POSITION) return;

            selectedPos = p;
            notifyDataSetChanged();
            listener.onSelected(rooms.get(p));
        });
        //uzun basma
        h.itemView.setOnLongClickListener(v -> {

            int p = h.getAdapterPosition();
            if (p == RecyclerView.NO_POSITION) return true;

            new AlertDialog.Builder(v.getContext())
                    .setTitle(room.name)
                    .setItems(new String[]{"Odayı Düzenle", "Odayı Sil"}, (dialog, which) -> {

                        if (which == 0) {
                            listener.onEdit(room, p);
                        } else if (which == 1) {
                            listener.onDelete(room, p);
                        }

                    })
                    .show();

            return true;
        });
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView imgIcon;
        TextView txtRoomName;
        CardView card;

        VH(View v) {
            super(v);
            imgIcon = v.findViewById(R.id.imgRoomIcon);
            txtRoomName = v.findViewById(R.id.txtRoomName);
            card = v.findViewById(R.id.cardRoom);
        }
    }
}

