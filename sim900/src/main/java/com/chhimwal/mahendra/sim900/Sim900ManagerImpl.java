package com.chhimwal.mahendra.sim900;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.io.IOException;

import static com.chhimwal.mahendra.sim900.Sim900ManagerImpl.GsmSpecialCharacters.cr;
import static com.chhimwal.mahendra.sim900.Sim900ManagerImpl.GsmSpecialCharacters.ctrlz;

/**
 * Created by mahendra on 28/9/17.
 */

public class Sim900ManagerImpl implements Sim900Manager {
    private static final String TAG = "SIM900I";
    private static Sim900ManagerImpl mInst;
    private Sim900 mSim900;

    interface GsmSpecialCharacters {
        char ctrlz = 0x1a;     //Ascii character for ctr+z. End of a SMS.
        char cr = 0x0d;  //Ascii character for carriage return.
        char lf = 0b1010;  //Ascii character for line feed.
        char nl = '\n';
    }


    private static final String STAND_OK = "OK";
    private static final String STAND_ERROR = "ERROR";

    public static synchronized Sim900Manager getService(String uartName) {
        checkNotNull(uartName);
        if (mInst == null) {
            mInst = new Sim900ManagerImpl(uartName);
        }
        return mInst;
    }

    @Override
    public void setLogLevel(@Logger.LEVEL int level){
        Logger.setLogLevel(level);
    }

    private Sim900ManagerImpl(@NonNull String uartDevice) {
        try {
            mSim900 = new Sim900(uartDevice, 10 * 10000);
            mSim900.setUp();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public int sendSMS(@NonNull String msg, String phoneNum) {
        checkNotNull(msg);
        checkNotNull(phoneNum);
        if (mSim900 != null) {
            mSim900.setUp();
            if (!checkStatusOfModule()) {
                Logger.e(TAG, "Sim900 A module status is not right.");
                return FAILURE;
            }

             /*Set mode sms mode to GSM*/
            String modeCmd = "AT+CSCS=\"GSM\"" + cr;
            mSim900.executeCommand(modeCmd);//ignoring response form module

            /*Now as status is OK.
            * Instructing the GSM/GPRS Modem or Mobile Phone to Operate in SMS Text Mode*/
            String modeResp = mSim900.executeCommand("AT+CMGF=1" + cr);
            if (!isStandardCmdOk(modeResp)) {
                Logger.e(TAG, "AT+CMGF=1 command failed for setting mode GSM");
                return FAILURE;
            }

            /*Setting the SMSC Number to be Used to Send SMS Text Messages*/
            /*no need for now as I believe automatically correct*/

            /*Sending Text Messages*/

            String smsCommand = "AT+CMGS=\"" + phoneNum + "\"" + cr;

            String smsEnterResp = mSim900.executeCommand(smsCommand);
            if (!isSMSTextCmdSuccessful(smsEnterResp)) {
                Logger.e(TAG, "Error in command:" + smsCommand + ". Response is " + smsEnterResp);
                return FAILURE;
            }

            /*As we have valid prompt from GSM module. Lets write sms followed by CTRL+Z command*/

            String writeSMSCmd = msg + ctrlz + cr;
            String smsSentStatus = mSim900.executeCommand(writeSMSCmd);
            if (!isSmsSentSuccessfully(smsSentStatus)) {
                Logger.e(TAG, "write SMS cmd failed :" + writeSMSCmd + ". Response is " + smsSentStatus);
                return FAILURE;
            }

            /*It means SMS sent successfully. Cheers and return success*/
            return SUCCESS;
        }
        return FAILURE;
    }

    private static void checkNotNull(Object o) {
        if (o == null) {
            throw new NullPointerException();
        }
    }

    /**
     * Return if sim900 module is properly set up or not
     *
     * @return true if, set up properly, including sim status, signal strength etc
     */
    private boolean checkStatusOfModule() {

        String resp = mSim900.executeCommand("AT" + cr);
        if (TextUtils.isEmpty(resp) || !resp.contains(STAND_OK)) {
            Logger.e(TAG, " Command: AT response is " + resp);
            return false;
        }
        //need to check other things as well like sim card status, signal strength etc.TODO for future
        return true;

    }

    private boolean isStandardCmdOk(String response) {
        return response != null && response.contains(STAND_OK);
    }

    private boolean isSMSTextCmdSuccessful(String response) {
        return response != null && response.contains(">");
    }

    private boolean isSmsSentSuccessfully(String response) {
        return response != null && !response.contains(STAND_ERROR);
    }
}
