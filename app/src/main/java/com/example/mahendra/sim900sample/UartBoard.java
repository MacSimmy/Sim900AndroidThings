package com.example.mahendra.sim900sample;

import android.os.Build;

/**
 * Created by mac on 30/4/17.
 */
@SuppressWarnings("WeakerAccess")
public class UartBoard {
    private static final String DEVICE_EDISON = "edison";
    private static final String DEVICE_JOULE = "joule";
    private static final String DEVICE_RPI3 = "rpi3";
    private static final String DEVICE_PICO = "imx6ul_pico";
    private static final String DEVICE_VVDN = "imx6ul_iopb";

    /**
     * Return the UART for pressure sensor.
     */
    public static String getUartName() {
        switch (Build.DEVICE) {
            case DEVICE_EDISON:
                return "UART1";
            case DEVICE_JOULE:
                return "UART1";
            case DEVICE_RPI3:
                return "UART0";
            case DEVICE_PICO:
                return "UART3";
            case DEVICE_VVDN:
                return "UART2";
            default:
                throw new IllegalStateException("Unknown Build.DEVICE " + Build.DEVICE);
        }
    }
}
