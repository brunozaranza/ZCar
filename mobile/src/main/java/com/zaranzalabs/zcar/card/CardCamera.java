package com.zaranzalabs.zcar.card;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.zaranzalabs.zcar.R;
import com.zaranzalabs.zcar.pojo.CameraList;
import com.zaranzalabs.zcar.pojo.CameraTakeStatus;
import com.zaranzalabs.zcar.pojo.GPSLocation;
import com.zaranzalabs.zcar.pojo.Headlights;
import com.zaranzalabs.zcar.pojo.Locks;
import com.zaranzalabs.zcar.pojo.Windows;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CardCamera extends Fragment
{
    private static final String TAG = CardCamera.class.getSimpleName();
    private static final String CAMERA_KEY = "camera";
    private static final String CAMERA_TAKE_PICTURE_KEY = "camera_take_picture";

    @BindView(R.id.imgCamera) ImageView imgCamera;
    @BindView(R.id.txtCamera) TextView txtCamera;

    private FirebaseFirestore firestore;
    private DocumentReference docCameraRef;
    private DocumentReference docCameraTakePicureRef;

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

        firestore = FirebaseFirestore.getInstance();

        CollectionReference devicesCollectionReference = firestore.collection("devices");
        CollectionReference commandsCollectionReference = firestore.collection("commands");

        docCameraRef = devicesCollectionReference.document(CAMERA_KEY);
        docCameraRef.get().addOnCompleteListener(documentSnapshotOnCompleteListener);
        docCameraRef.addSnapshotListener(documentSnapshotEventListener);

        docCameraTakePicureRef = commandsCollectionReference.document(CAMERA_TAKE_PICTURE_KEY);
        docCameraTakePicureRef.get().addOnCompleteListener(documentSnapshotOnCompleteListener);
        docCameraTakePicureRef.addSnapshotListener(documentSnapshotEventListener);

        return view;
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

                DocumentReference reference = snapshot.getReference();

                if (reference.getId().equals(CAMERA_KEY)) {
                    CameraList list = snapshot.toObject(CameraList.class);

                    if (list != null) {
                        if (list.urlImgs == null) {
                            setNoUserImage();
                            return;
                        }

                        if (list.urlImgs.isEmpty()) {
                            setNoUserImage();
                            return;
                        }
                    }

                } else if (reference.getId().equals(CAMERA_TAKE_PICTURE_KEY)){
                    CameraTakeStatus status = snapshot.toObject(CameraTakeStatus.class);

                    if (status != null) {

                    }
                }
            } else {
                Log.d(TAG, "Current data: null");
            }
        }
    };

    private OnCompleteListener<DocumentSnapshot> documentSnapshotOnCompleteListener
            = new OnCompleteListener<DocumentSnapshot>() {
        @Override
        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();

                DocumentReference reference = document.getReference();

                if (!document.exists()) {
                    if (reference.getId().equals(CAMERA_KEY)) {
                        setNoUserImage();
                        document.getReference().set(new CameraList());
                    } else if (reference.getId().equals(CAMERA_TAKE_PICTURE_KEY)) {
                        document.getReference().set(new CameraTakeStatus(false));
                    }


                }
            }
        }
    };

    private void setNoUserImage()
    {
        imgCamera.setImageResource(R.drawable.user_no_image);
        imgCamera.setAlpha((float) 0.5);
    }
}
