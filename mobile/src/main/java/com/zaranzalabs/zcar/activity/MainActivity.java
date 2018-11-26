package com.zaranzalabs.zcar.activity;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.zaranzalabs.zcar.R;
import com.zaranzalabs.zcar.adapter.CellAdapter;
import com.zaranzalabs.zcar.card.CardMap;
import com.zaranzalabs.zcar.pojo.Headlights;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
{
    private static final String TAG = MainActivity.class.getSimpleName();

    private FirebaseFirestore firestore;

    private RecyclerView recyclerView;

    private CellAdapter rvAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

//        recyclerView = findViewById(R.id.recycler_view);
//
//        setupRecycler();

        getFragmentTransaction().add(R.id.container, new CardMap(), "map").commit();

    }

    private FragmentTransaction getFragmentTransaction()
    {
        return getSupportFragmentManager().beginTransaction().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
    }


    private void setupRecycler()
    {

//        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
//        recyclerView.setLayoutManager(layoutManager);
//
//        ArrayList<String> arrayList = new ArrayList<>();
//
//        for (int i = 0; i < 3; i++) {
//            arrayList.add("Cell " + i);
//        }
//
//        rvAdapter = new CellAdapter(arrayList);
//        recyclerView.setAdapter(rvAdapter);

    }
}
