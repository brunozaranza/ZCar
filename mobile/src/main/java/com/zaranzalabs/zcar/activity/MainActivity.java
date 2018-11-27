package com.zaranzalabs.zcar.activity;

import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.zaranzalabs.zcar.R;
import com.zaranzalabs.zcar.card.CardCamera;
import com.zaranzalabs.zcar.card.CardCommand;
import com.zaranzalabs.zcar.card.CardMap;
import com.zaranzalabs.zcar.pojo.Headlights;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity
{
    private static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.pull_to_refresh) SwipeRefreshLayout refresh;

    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        firestore = FirebaseFirestore.getInstance();

        firestore.collection("headlights")
                .add(new Headlights(true))
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });

        setupRefresh();

        getSupportFragmentManager().beginTransaction().add(R.id.table_layout, CardCamera.newInstance()).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.table_layout, CardCommand.newInstance()).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.table_layout, CardMap.newInstance()).commit();

    }

    private void setupRefresh()
    {
        refresh.hasNestedScrollingParent();
        refresh.setOnRefreshListener(() ->
            refresh.setRefreshing(false)
        );
    }


}
