package com.zaranzalabs.zcar.card;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.zaranzalabs.zcar.R;
import com.zaranzalabs.zcar.pojo.GPSLocation;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CardMap extends Fragment implements OnMapReadyCallback
{
    private static final String TAG = CardMap.class.getSimpleName();
    private static final String GPS_KEY = "gps";

    @BindView(R.id.txt_last_position) TextView txtLastPosition;

    private FirebaseFirestore firestore;
    private DocumentReference docGPSRef;

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

        firestore = FirebaseFirestore.getInstance();

        CollectionReference collectionReference = firestore.collection("devices");
        docGPSRef = collectionReference.document(GPS_KEY);
        docGPSRef.addSnapshotListener(documentSnapshotEventListener);

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        this.map = googleMap;
    }

    private EventListener<DocumentSnapshot> documentSnapshotEventListener
            = new EventListener<DocumentSnapshot>() {
        @Override
        public void onEvent(@Nullable DocumentSnapshot snapshot,
                            @Nullable FirebaseFirestoreException e) {

            if (e != null) {
                Log.w(TAG, "Listen failed.", e);
                return;
            }

            if (snapshot != null && snapshot.exists()) {

                GPSLocation location = snapshot.toObject(GPSLocation.class);

                if (location != null){
                    addMarker(location);
                    setInfo(location);
                }

            } else {
                Log.d(TAG, "Current data: null");
            }
        }
    };

    private void addMarker(GPSLocation location)
    {
        if (map == null) return;

        map.clear();

        long now = System.currentTimeMillis(); // See note below
        long then = location.time;

        long minutes = TimeUnit.MILLISECONDS.toMinutes(now - then);

        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        Marker marker = map.addMarker(new MarkerOptions()
                .position(new LatLng(location.lat, location.lon))
                .snippet("Há " + minutes + " minutos")
                .title("ZCar Project"));

        builder.include(marker.getPosition());

        marker.showInfoWindow();

        try {
            LatLngBounds bounds = builder.build();
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 300);
            map.animateCamera(cu);
        } catch (Exception e) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.lat, location.lon), 19));
        }
    }

    private void setInfo(GPSLocation location)
    {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(getContext(), Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(location.lat, location.lon, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            txtLastPosition.setText("Última posição: " + address);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
