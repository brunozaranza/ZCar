package com.zaranzalabs.zcar.board;

import android.os.Build;

public class BoardDefaults
{
    private static final String DEVICE_RPI3 = "rpi3";

    public static String getUartName() {
        return "UART0";
    }

    public static String getGPIOForLED() {
        switch (Build.DEVICE) {
            case DEVICE_RPI3:
                return "BCM6";
            default:
                throw new IllegalStateException("Unknown Build.DEVICE " + Build.DEVICE);
        }
    }
}
