package com.example.netelsansmarthome;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.VH> {

    private final List<LogItem> list;

    public LogAdapter(List<LogItem> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_log, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        LogItem item = list.get(position);
        holder.title.setText(item.title);
        holder.msg.setText(item.message);
        holder.time.setText(item.time);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }




    static class VH extends RecyclerView.ViewHolder {
        TextView title, msg, time;
        VH(View v) {
            super(v);
            title = v.findViewById(R.id.txtLogTitle);
            msg = v.findViewById(R.id.txtLogMessage);
            time = v.findViewById(R.id.txtLogTime);
        }
    }
}