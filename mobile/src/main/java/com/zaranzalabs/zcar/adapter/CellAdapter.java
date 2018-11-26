package com.zaranzalabs.zcar.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zaranzalabs.zcar.R;
import com.zaranzalabs.zcar.holder.CellHolder;

import java.util.ArrayList;
import java.util.List;

public class CellAdapter extends RecyclerView.Adapter<CellHolder> {

    private final List<String> values;

    public CellAdapter(ArrayList _values) {
        values = _values;
    }

    @Override
    public CellHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CellHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_cell, parent, false));
    }

    @Override
    public void onBindViewHolder(CellHolder holder, int position) {

        switch (position) {
            case 0:
                holder.cardCamera.setVisibility(View.VISIBLE);
                holder.cardCommands.setVisibility(View.GONE);
                holder.cardMap.setVisibility(View.GONE);

                break;
            case 1:
                holder.cardCamera.setVisibility(View.GONE);
                holder.cardCommands.setVisibility(View.VISIBLE);
                holder.cardMap.setVisibility(View.GONE);
                break;
            case 2:
                holder.cardCamera.setVisibility(View.GONE);
                holder.cardCommands.setVisibility(View.GONE);
                holder.cardMap.setVisibility(View.VISIBLE);

                break;
            default:
                break;
        }

//        holder.moreButton.setOnClickListener(view -> updateItem(position));
//        holder.deleteButton.setOnClickListener(view -> removerItem(position));
    }

    @Override
    public int getItemCount() {
        return values != null ? values.size() : 0;
    }

}