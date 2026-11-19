package com.example.netelsansmarthome;

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

public class ModuleAdapter extends RecyclerView.Adapter<ModuleAdapter.VH> {

    public interface OnModuleSelected {
        void onSelected(Module module);
    }

    private final List<Module> modules;
    private final OnModuleSelected listener;
    private int selectedPos = 0;

    public ModuleAdapter(List<Module> modules, OnModuleSelected listener) {
        this.modules = modules;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_module, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Module m = modules.get(pos);

        h.txt.setText(m.title);
        h.icon.setImageResource(m.iconRes);

        if (pos == selectedPos) {
            h.card.setCardBackgroundColor(Color.parseColor("#D32F2F"));
            h.txt.setTextColor(Color.WHITE);
            h.icon.setColorFilter(Color.WHITE);
        } else {
            h.card.setCardBackgroundColor(Color.parseColor("#F5F5F5"));
            h.txt.setTextColor(Color.parseColor("#757575"));
            h.icon.setColorFilter(Color.parseColor("#757575"));
        }

        h.itemView.setOnClickListener(v -> {
            int p = h.getAdapterPosition();
            if (p == RecyclerView.NO_POSITION) return;

            selectedPos = p;
            notifyDataSetChanged();

            if (listener != null) listener.onSelected(m);
        });
    }

    @Override
    public int getItemCount() {
        return modules.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        CardView card;
        ImageView icon;
        TextView txt;

        VH(View v) {
            super(v);
            card = v.findViewById(R.id.cardModule);
            icon = v.findViewById(R.id.imgModuleIcon);
            txt = v.findViewById(R.id.txtModuleName);
        }
    }
}
