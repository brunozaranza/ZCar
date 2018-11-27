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

public class CardMap extends Fragment implements OnMapReadyCallback
{

    public GoogleMap map;

    public CardMap() {
    }

    public static CardMap newInstance()
    {
        CardMap fragment = new CardMap();
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
        View view = inflater.inflate(R.layout.card_map, container, false);
        ButterKnife.bind(this, view);

        ((SupportMapFragment)getChildFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        this.map = googleMap;
    }
}
