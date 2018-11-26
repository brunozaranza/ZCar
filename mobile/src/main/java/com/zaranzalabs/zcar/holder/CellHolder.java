package com.zaranzalabs.zcar.holder;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.zaranzalabs.zcar.R;

public class CellHolder extends RecyclerView.ViewHolder
{
    public CardView cardCamera;
    public CardView cardCommands;
    public CardView cardMap;

    public CellHolder(View itemView)
    {
        super(itemView);
        cardCamera = itemView.findViewById(R.id.cardview_camera);
        cardCommands = itemView.findViewById(R.id.cardview_commands);
        cardMap = itemView.findViewById(R.id.cardview_map);
    }
}