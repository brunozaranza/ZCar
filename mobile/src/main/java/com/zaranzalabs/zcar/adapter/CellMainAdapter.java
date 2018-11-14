package com.zaranzalabs.zcar.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.zaranzalabs.zcar.R;
import com.zaranzalabs.zcar.holder.CellMainHolder;

import java.util.ArrayList;
import java.util.List;

public class CellMainAdapter extends RecyclerView.Adapter<CellMainHolder> {

    private final List<String> values;

    public CellMainAdapter(ArrayList _values) {
        values = _values;
    }

    @Override
    public CellMainHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CellMainHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_view_cell, parent, false));
    }

    @Override
    public void onBindViewHolder(CellMainHolder holder, int position) {
        holder.title.setText("Test " + position);

//        holder.moreButton.setOnClickListener(view -> updateItem(position));
//        holder.deleteButton.setOnClickListener(view -> removerItem(position));
    }

    @Override
    public int getItemCount() {
        return values != null ? values.size() : 0;
    }

}