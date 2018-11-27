package com.zaranzalabs.zcar.card;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.zaranzalabs.zcar.R;
import com.zaranzalabs.zcar.activity.MainActivity;
import com.zaranzalabs.zcar.pojo.Headlights;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CardCommand extends Fragment
{
    private static final String TAG = CardCommand.class.getSimpleName();

    @BindView(R.id.switch1) Switch switch1;
    @BindView(R.id.switch2) Switch switch2;
    @BindView(R.id.switch3) Switch switch3;

    private FirebaseFirestore firestore;

    public CardCommand() {
    }

    public static CardCommand newInstance()
    {
        CardCommand fragment = new CardCommand();
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
        View view = inflater.inflate(R.layout.card_command, container, false);
        ButterKnife.bind(this, view);

        firestore = FirebaseFirestore.getInstance();

        final DocumentReference df = firestore.collection("headlights").document("status");
        df.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    Log.d(TAG, "Current data: " + snapshot.getData());
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });

        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                df.set(isChecked);
            }
        });

        return view;
    }

}
