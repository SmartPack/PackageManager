package com.smartpack.packagemanager.adapters;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;

import java.util.List;

import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.utils.SerializableItems.MenuItems;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 25, 2025
 */
public class BottomMenuAdapter extends RecyclerView.Adapter<BottomMenuAdapter.ViewHolder> {

    private final boolean isMenu;
    private final List<MenuItems> data;
    private final OnItemClickListener listener;
    private final String currentStatus;

    public BottomMenuAdapter(List<MenuItems> items, String currentStatus, OnItemClickListener listener, boolean isMenu) {
        this.data = items;
        this.currentStatus = currentStatus;
        this.listener = listener;
        this.isMenu = isMenu;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_bottom_menu, parent, false);
        return new ViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MenuItems item = this.data.get(position);
        holder.title.setText(item.getTile());

        if (this.data.get(position).getDrawable() != Integer.MIN_VALUE) {
            holder.icon.setImageDrawable(sCommonUtils.getDrawable(this.data.get(position).getDrawable(), holder.icon.getContext()));
            holder.icon.setVisibility(VISIBLE);
        } else {
            holder.icon.setVisibility(GONE);
        }

        if (this.data.get(position).getDescription() != null) {
            holder.description.setText(item.getDescription());
            holder.description.setVisibility(VISIBLE);
        } else {
            holder.description.setVisibility(GONE);
        }

        if (!isMenu) {
            if (currentStatus.contains(data.get(position).getTile().toLowerCase())) {
                holder.itemView.setAlpha(1);
            } else {
                holder.itemView.setAlpha((float) 0.5);
            }
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final AppCompatImageButton icon;
        private final MaterialTextView title, description;
        ViewHolder(@NonNull View view) {
            super(view);
            icon = view.findViewById(R.id.icon);
            title = view.findViewById(R.id.title);
            description = view.findViewById(R.id.description);

            view.setOnClickListener(v -> listener.onItemClick(data.get(getBindingAdapterPosition()).getID()));
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int id);
    }

}