package com.example.mahendra.sim900sample;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.chhimwal.mahendra.sim900.Logger;
import com.chhimwal.mahendra.sim900.Sim900Manager;
import com.chhimwal.mahendra.sim900.Sim900ManagerImpl;

public class SendSmsActivity extends Activity {


    private static String TAG = "SendSmsActivity";

    private HandlerThread mCommunicationThread;
    private Handler mCommuncHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        {
            Log.d(TAG, "onCreate starts");
            startBackGroundThread();
            startSendingSMSEveryMinute();
        }
    }

    private void startSendingSMSEveryMinute() {
        mCommuncHandler.postDelayed(mSendSmsTask, 60 * 1000); //every minute
    }

    private void startBackGroundThread() {
        if (mCommunicationThread == null
                || mCommuncHandler == null) {
            mCommunicationThread = new HandlerThread("SendSmsBackgroundThread");
            mCommunicationThread.start();
            mCommuncHandler = new Handler(mCommunicationThread.getLooper());
            Log.d(TAG, "SendSmsBackgroundThread started");
        }
    }

    private Runnable mSendSmsTask = new Runnable() {
        @Override
        public void run() {
            //get service
            Sim900Manager sim900Manager = Sim900ManagerImpl.getService(UartBoard.getUartName());
            //customize log level of library
            sim900Manager.setLogLevel(Logger.DEBUG);
            String sms = "This is sample sms message from SIm900 module";//need to be in plain text i.e. ASCII
            String phoneNumber = "+91xxxxxxx50"; //phone number to send sms
            //send sms
            int status = sim900Manager.sendSMS(sms, phoneNumber);
            String logMessage;
            switch (status) {
                case Sim900Manager.SUCCESS:
                    logMessage = "Message sent successfully to " + phoneNumber;
                    break;
                case Sim900Manager.FAILURE:
                default:
                    logMessage = " Some error in sending message to no " + phoneNumber;
                    break;
            }
            Log.d(TAG, logMessage);

            //rescheduling sms
            mCommuncHandler.postDelayed(mSendSmsTask, 60 * 1000);
        }
    };

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        stopSMSTask();
        super.onDestroy();
    }

    /**
     * Stop background thread and remove pending callback
     */
    private void stopSMSTask() {
        if (mCommunicationThread != null) {
            mCommunicationThread.quitSafely();
        }
        if (mCommuncHandler != null) {
            mCommuncHandler.removeCallbacks(mSendSmsTask);
            mSendSmsTask = null;
        }
    }

}
