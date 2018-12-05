package com.zaranzalabs.zcar.card;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.zaranzalabs.zcar.R;
import com.zaranzalabs.zcar.activity.MainActivity;
import com.zaranzalabs.zcar.pojo.Headlights;
import com.zaranzalabs.zcar.pojo.Locks;
import com.zaranzalabs.zcar.pojo.Windows;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CardCommand extends Fragment
{
    private static final String TAG = CardCommand.class.getSimpleName();

    private static final String HEADLIGHTS_KEY = "headlights";
    private static final String LOCKS_KEY = "locks";
    private static final String WINDOWS_KEY = "windows";

    @BindView(R.id.switch_headlights) Switch switchHeadlights;
    @BindView(R.id.switch_locks) Switch switchLocks;
    @BindView(R.id.switch_windows) Switch switchWindows;

    private FirebaseFirestore firestore;

    DocumentReference docHeadlightsRef;
    DocumentReference docLocksRef;
    DocumentReference docWindowsRef;

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

        CollectionReference collectionReference = firestore.collection("status");

        docHeadlightsRef = collectionReference.document(HEADLIGHTS_KEY);
        docLocksRef = collectionReference.document(LOCKS_KEY);
        docWindowsRef = collectionReference.document(WINDOWS_KEY);

        docHeadlightsRef.get().addOnCompleteListener(documentSnapshotOnCompleteListener);
        docLocksRef.get().addOnCompleteListener(documentSnapshotOnCompleteListener);
        docWindowsRef.get().addOnCompleteListener(documentSnapshotOnCompleteListener);

        docHeadlightsRef.addSnapshotListener(documentSnapshotEventListener);
        docLocksRef.addSnapshotListener(documentSnapshotEventListener);
        docWindowsRef.addSnapshotListener(documentSnapshotEventListener);

        switchHeadlights.setOnCheckedChangeListener(switchOnCheckedChangeListener);
        switchLocks.setOnCheckedChangeListener(switchOnCheckedChangeListener);
        switchWindows.setOnCheckedChangeListener(switchOnCheckedChangeListener);

        return view;
    }

    private OnCompleteListener<DocumentSnapshot> documentSnapshotOnCompleteListener
            = new OnCompleteListener<DocumentSnapshot>() {
        @Override
        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();

                DocumentReference reference = document.getReference();

                if (!document.exists()) {
                    if (reference.getId().equals(HEADLIGHTS_KEY)) {
                        document.getReference().set(new Headlights(false));
                    } else if (reference.getId().equals(LOCKS_KEY)) {
                        document.getReference().set(new Locks(false));
                    } else if (reference.getId().equals(WINDOWS_KEY)) {
                        document.getReference().set(new Windows(false));
                    }
                }
            }
        }
    };

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

                DocumentReference reference = snapshot.getReference();

                Boolean status = (Boolean) snapshot.get("status");

                if (reference.getId().equals(HEADLIGHTS_KEY)) {
                    switchHeadlights.setChecked(status.booleanValue());
                } else if (reference.getId().equals(LOCKS_KEY)) {
                    switchLocks.setChecked(status.booleanValue());
                } else if (reference.getId().equals(WINDOWS_KEY)) {
                    switchWindows.setChecked(status.booleanValue());
                }
            } else {
                Log.d(TAG, "Current data: null");
            }
        }
    };

    private CompoundButton.OnCheckedChangeListener switchOnCheckedChangeListener
            = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (switchHeadlights.equals(buttonView)) {
                docHeadlightsRef.set(new Headlights(isChecked));
            } else if (switchLocks.equals(buttonView)){
                docLocksRef.set(new Locks(isChecked));
            } else if (switchWindows.equals(buttonView)) {
                docWindowsRef.set(new Windows(isChecked));
            }
        }
    };

}
