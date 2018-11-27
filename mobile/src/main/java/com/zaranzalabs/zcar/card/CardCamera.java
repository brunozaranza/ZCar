package com.zaranzalabs.zcar.card;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.zaranzalabs.zcar.R;

import butterknife.ButterKnife;

public class CardCamera extends Fragment
{
    public CardCamera() {
    }

    public static CardCamera newInstance()
    {
        CardCamera fragment = new CardCamera();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.card_camera, container, false);
        ButterKnife.bind(this, view);

        return view;
    }
}
