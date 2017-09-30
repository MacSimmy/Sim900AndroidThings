package com.chhimwal.mahendra.sim900;

import android.support.annotation.NonNull;

import com.google.android.things.pio.PeripheralManagerService;
import com.google.android.things.pio.UartDevice;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created by mahendra on 28/9/17.
 */

class Sim900 implements AutoCloseable {
    private static final String TAG = "Sim900";
    private UartDevice mDevice;

    private final int mTimoutDur;


    Sim900(@NonNull String uartName, int timeOut) throws IOException {
        checkNotNull(uartName);
        mTimoutDur = timeOut;
        init(uartName);
    }

    private void checkNotNull(Object obj) {
        if(obj == null)
            throw new NullPointerException("This can't be null");
    }

    public void setUp() {
        if (mDevice != null) {
            //disable ECHO of
            String stopEchoCmdResp = executeCommand("ATE0\r");
            Logger.d(TAG, "Stop echo cmd response:" + stopEchoCmdResp);
        }
    }

    private void init(String uartName) throws IOException {
        try {
            PeripheralManagerService manager = new PeripheralManagerService();
            mDevice = manager.openUartDevice(uartName);
            mDevice.setBaudrate(Sim900Constants.BAUD_RATE);
            mDevice.setDataSize(Sim900Constants.DATA_BITS);
            mDevice.setParity(UartDevice.PARITY_NONE);
            mDevice.setStopBits(Sim900Constants.STOP_BITS);
        } catch (IOException | RuntimeException e) {
            close();
            throw e;
        }

    }


    /**
     * This method will execute given command to serial port of SIM900A module
     * And return result back to caller. This method would be blocking and must be called from background thread
     *
     * @param command AT command
     * @return true false, if command executes successful or not
     */
    public boolean writeComand(@NonNull String command) throws IOException {
        checkNotNull(command);
        String response = "";
        if (mDevice != null) {
            byte[] commandBytes = command.getBytes();
            int byteToSend = commandBytes.length;
            int sentBytes = 0;
            long startTime = System.currentTimeMillis();
            Logger.d(TAG, "Sending " + byteToSend + " bytes for command: " + command);
            try {
                while (sentBytes < byteToSend) {
                    if (timeDelta(startTime) >= mTimoutDur) {
                        Logger.e(TAG, "Timeout in writing command " + command);
                        return false;
                    }

                    sentBytes += mDevice.write(commandBytes, commandBytes.length);
                    if (sentBytes == 0) {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                throw e;
            }
        }
        return false;
    }

    public String executeCommand(@NonNull String command) {
        return executeCommand(command, 1000);
    }

    /**
     * This method will execute given command to serial port of SIM900A module
     * And return response from serial port.
     *
     * @param command  need to be executed
     * @param waitTime max timeout for this operation.
     * @return response from Sim900 A module
     */
    public String executeCommand(@NonNull String command, long waitTime) {
        checkNotNull(command);
        if (waitTime < 10) {
            throw new IllegalArgumentException("Be realistic on timeout argument");
        }

        String response = "";

        try {
            mDevice.flush(UartDevice.FLUSH_IN_OUT);
            this.writeComand(command);

            //sleep for 1000 ms to fill the buffer
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //now read output response from Sim900 module
            //continuously poll UART buffer
            int readBytes;
            int maxBytes = 512;
            byte[] outBuffer = new byte[maxBytes];
            while ((readBytes = mDevice.read(outBuffer, outBuffer.length)) > 0) {
                byte[] respBytes = Arrays.copyOf(outBuffer, readBytes);
                Logger.d(TAG, "Read " + readBytes + " bytes from peripheral " + Arrays.toString(respBytes));

                //remove line feed and \r from the front of each response
                if (readBytes >= 2 && (respBytes[0] == 13 && respBytes[1] == 10)) {
                    byte[] output = new byte[respBytes.length - 2];
                    System.arraycopy(respBytes, 2, output, 0, output.length);
                    response = response + new String(output);
                } else {
                    response = response + new String(respBytes);
                }
            }
            Logger.d(TAG, "Command=" + command + ". Response is=" + response);
            return response;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }


    private long timeDelta(long startTime) {
        return System.currentTimeMillis() - startTime;
    }

    @Override
    public void close() throws IOException {
        if (mDevice != null) {
            try {
                mDevice.close();
            } finally {
                mDevice = null;
            }
        }
    }
}

