package com.zaranzalabs.zcar.pojo;

public class GPSLocation
{
    public float accuracy;
    public float altitude;
    public float bearing;
    public float bearing_accuracy_degrees;
    public double lat;
    public double lon;
    public float speed;
    public float speed_accuracy_meters_per_second;
    public long time;
    public float vertical_accuracy_meters;

    public GPSLocation()
    { }

    public GPSLocation(float accuracy, float altitude, float bearing, float bearing_accuracy_degrees, double lat, double lon, float speed, float speed_accuracy_meters_per_second, long time, float vertical_accuracy_meters) {
        this.accuracy = accuracy;
        this.altitude = altitude;
        this.bearing = bearing;
        this.bearing_accuracy_degrees = bearing_accuracy_degrees;
        this.lat = lat;
        this.lon = lon;
        this.speed = speed;
        this.speed_accuracy_meters_per_second = speed_accuracy_meters_per_second;
        this.time = time;
        this.vertical_accuracy_meters = vertical_accuracy_meters;
    }
}
