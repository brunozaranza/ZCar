package com.zaranzalabs.zcar.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.zaranzalabs.zcar.R;

public class CellMainHolder extends RecyclerView.ViewHolder
{
    public TextView title;

    public CellMainHolder(View itemView)
    {
        super(itemView);
        title = itemView.findViewById(R.id.main_line_title);

    }
}