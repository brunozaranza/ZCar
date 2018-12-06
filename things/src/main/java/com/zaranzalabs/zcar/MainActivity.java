package com.zaranzalabs.zcar;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.OnNmeaMessageListener;
import android.media.Image;
import android.media.ImageReader;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.things.contrib.driver.gps.NmeaGpsDriver;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManager;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.zaranzalabs.zcar.board.BoardDefaults;
import com.zaranzalabs.zcar.camera.ZCamera;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity
{
    private static final String HEADLIGHTS_KEY = "headlights";
    private static final String GPS_KEY = "gps";

    private static final String TAG = MainActivity.class.getSimpleName();
    public static final int UART_BAUD = 9600;
    public static final float ACCURACY = 2.5f;

    private Gpio ledGpio;
    DocumentReference docHeadlightsRef;
    DocumentReference docGPSRef;

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

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);

        configCamera();
        configGPS();
        configLed();

        CollectionReference collectionReference = firestore.collection("commands");
        docHeadlightsRef = collectionReference.document(HEADLIGHTS_KEY);
        docHeadlightsRef.addSnapshotListener(documentSnapshotEventListener);

        connectWifi();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCamera.shutDown();

        mCameraThread.quitSafely();
        mCloudThread.quitSafely();
    }

    private void connectWifi()
    {
        String networkSSID = "Trojan Horse";
        String networkPass = "mel.zs@123";

        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + networkSSID + "\"";
        conf.wepKeys[0] = "\"" + networkPass + "\"";
        conf.wepTxKeyIndex = 0;
        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        conf.preSharedKey = "\""+ networkPass +"\"";
        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

        WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        wifiManager.addNetwork(conf);

        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        for( WifiConfiguration i : list ) {
            if(i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
                wifiManager.disconnect();
                wifiManager.enableNetwork(i.networkId, true);
                wifiManager.reconnect();

                break;
            }
        }

        Log.i("test", wifiManager.getConnectionInfo().toString());
        Log.i("test", String.valueOf(wifiManager.getWifiState()));
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

                Boolean status = (Boolean) snapshot.get("status");

                if (reference.getId().equals(HEADLIGHTS_KEY)) {
                    try {
                        ledGpio.setValue(status.booleanValue());
                    } catch (IOException ioe) {
                        Log.e("LOG", "Error updating GPIO value", ioe);
                    }
                }
            } else {
                Log.d(TAG, "Current data: null");
            }
        }
    };

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

    private void configLed()
    {
        PeripheralManager pioService = PeripheralManager.getInstance();
        try {
            ledGpio = pioService.openGpio(BoardDefaults.getGPIOForLED());
            ledGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
        } catch (IOException e) {
            Log.e("LOG", "Error configuring GPIO pins", e);
        }
    }

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (lastLocation == null) {
                lastLocation = location;

                sendLocationToFirestore(location);

                return;
            }

            if (lastLocation.distanceTo(location) > 1.0) {
                Log.v(TAG, "Location update: " + lastLocation.distanceTo(location));

                lastLocation = location;

                sendLocationToFirestore(location);
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

    private void sendLocationToFirestore(Location location)
    {
        CollectionReference collectionReference = firestore.collection("devices");
        docGPSRef = collectionReference.document(GPS_KEY);
        Map<String, Object> map = new HashMap<>();

        map.put("lat", location.getLatitude());
        map.put("lon", location.getLongitude());
        map.put("speed", location.getSpeed());
        map.put("altitude", location.getAltitude());
        map.put("time", location.getTime());
        map.put("accuracy", location.getAccuracy());
        map.put("bearing", location.getBearing());
        map.put("bearing_accuracy_degrees", location.getBearingAccuracyDegrees());
        map.put("vertical_accuracy_meters", location.getVerticalAccuracyMeters());
        map.put("speed_accuracy_meters_per_second", location.getSpeedAccuracyMetersPerSecond());

        docGPSRef.set(map);
    }
}
