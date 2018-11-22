package com.zaranzalabs.zcar;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;

import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.OnNmeaMessageListener;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.things.contrib.driver.gps.NmeaGpsDriver;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.zaranzalabs.zcar.board.BoardDefaults;
import com.zaranzalabs.zcar.camera.ZCamera;
import com.zaranzalabs.zcar.pojo.Headlights;

import java.io.IOException;
import java.nio.ByteBuffer;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();
    public static final int UART_BAUD = 9600;
    public static final float ACCURACY = 2.5f;

    private FirebaseFirestore firestore;
    private FirebaseStorage mStorage;
    private ZCamera mCamera;

    private Handler mCameraHandler;
    private HandlerThread mCameraThread;

    private Handler mCloudHandler;
    private HandlerThread mCloudThread;

    private LocationManager mLocationManager;
    private NmeaGpsDriver mGpsDriver;

    private Location lastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (checkSelfPermission(android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "No permission");
            return;
        }

        firestore = FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance();

        configCamera();
        configGPS();

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

//        DatabaseReference dbReference = mDatabase.getReference("status_camera");
//        dbReference.setValue(false);
//
//        dbReference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//
//                boolean value = dataSnapshot.getValue(Boolean.class);
//
//                if (value == true) {
//                    mCamera.takePicture();
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError error) {
//                Log.w("LOG", "Failed to read value.", error.toException());
//            }
//        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCamera.shutDown();

        mCameraThread.quitSafely();
        mCloudThread.quitSafely();
    }

    private ImageReader.OnImageAvailableListener mOnImageAvailableListener =
            new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image image = reader.acquireLatestImage();
                    // get image bytes
                    ByteBuffer imageBuf = image.getPlanes()[0].getBuffer();
                    final byte[] imageBytes = new byte[imageBuf.remaining()];
                    imageBuf.get(imageBytes);
                    image.close();

                    onPictureTaken(imageBytes);
                }
            };

    private void onPictureTaken(final byte[] imageBytes) {
        if (imageBytes != null) {
//            DatabaseReference dbReference = mDatabase.getReference("status_camera");
//            dbReference.setValue(false);
//
//            final DatabaseReference log = mDatabase.getReference("logs").push();
//            final StorageReference imageRef = mStorage.getReference().child(log.getKey());

//            UploadTask task = imageRef.putBytes(imageBytes);
//            task.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
//                @Override
//                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
//                    if (!task.isSuccessful()) {
//                        throw task.getException();
//                    }
//                    return imageRef.getDownloadUrl();
//                }
//            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
//                @Override
//                public void onComplete(@NonNull Task<Uri> task) {
//                    if (task.isSuccessful()) {
//                        Uri downloadUri = task.getResult();
//
//                        Log.i(TAG, "Image upload successful");
//                        log.child("timestamp").setValue(ServerValue.TIMESTAMP);
//                        log.child("image").setValue(downloadUri.toString());
//                    }
//                }
//            });

//            task.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                @Override
//                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//
//                }
//            }).addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception e) {
//                    // clean up this entry
//                    Log.w(TAG, "Unable to upload image to Firebase");
//                    log.removeValue();
//                }
//            });
        }
    }

    private void configCamera()
    {
        mCamera = ZCamera.getInstance();
        mCamera.initializeCamera(this, mCameraHandler, mOnImageAvailableListener);
        mCameraThread = new HandlerThread("CameraBackground");
        mCameraThread.start();
        mCameraHandler = new Handler(mCameraThread.getLooper());

        mCloudThread = new HandlerThread("CloudThread");
        mCloudThread.start();
        mCloudHandler = new Handler(mCloudThread.getLooper());
    }

    private void configGPS()
    {
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        try {
            mGpsDriver = new NmeaGpsDriver(this, BoardDefaults.getUartName(), UART_BAUD, ACCURACY);
            mGpsDriver.register();
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0, mLocationListener);
            mLocationManager.registerGnssStatusCallback(mStatusCallback);
            mLocationManager.addNmeaListener(mMessageListener);
        } catch (IOException e) {
            Log.w(TAG, "Unable to open GPS UART", e);
        }
    }

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (lastLocation == null) {
                lastLocation = location;

                sendLocationToFirebase(location);

                return;
            }

            if (lastLocation.distanceTo(location) > 1.0) {
                Log.v(TAG, "Location update: " + lastLocation.distanceTo(location));

                lastLocation = location;

                sendLocationToFirebase(location);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) { }

        @Override
        public void onProviderEnabled(String provider) { }

        @Override
        public void onProviderDisabled(String provider) { }
    };

    private GnssStatus.Callback mStatusCallback = new GnssStatus.Callback() {
        @Override
        public void onStarted() { }

        @Override
        public void onStopped() { }

        @Override
        public void onFirstFix(int ttffMillis) { }

        @Override
        public void onSatelliteStatusChanged(GnssStatus status) {
            //Log.v(TAG, "GNSS Status: " + status.getSatelliteCount() + " satellites.");
        }
    };

    private OnNmeaMessageListener mMessageListener = new OnNmeaMessageListener() {
        @Override
        public void onNmeaMessage(String message, long timestamp) {
            //Log.v(TAG, "NMEA: " + message);
        }
    };

    private void sendLocationToFirebase(Location location)
    {
//        DatabaseReference dbReference = mDatabase.getReference("location");
//        dbReference.child("lat").setValue(location.getLatitude());
//        dbReference.child("lon").setValue(location.getLongitude());
//        dbReference.child("speed").setValue(location.getSpeed());
//        dbReference.child("altitude").setValue(location.getAltitude());
//        dbReference.child("time").setValue(location.getTime());
//        dbReference.child("accuracy").setValue(location.getAccuracy());
//        dbReference.child("bearing").setValue(location.getBearing());
//        dbReference.child("bearing_accuracy_degrees").setValue(location.getBearingAccuracyDegrees());
//        dbReference.child("vertical_accuracy_meters").setValue(location.getVerticalAccuracyMeters());
//        dbReference.child("speed_accuracy_meters_per_second").setValue(location.getSpeedAccuracyMetersPerSecond());
    }
}
