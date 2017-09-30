package com.chhimwal.mahendra.sim900;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Created by mahendra on 28/9/17.
 */

public interface Sim900Manager {

    int SUCCESS = 0;
    int FAILURE = -1;
    @Retention(SOURCE)
    @IntDef({SUCCESS, FAILURE})
    @interface Sim900Result {}

    /**
     * This method will send SMS synchronously in text format. Better to call from worker thread.
     * @param msg message to be send
     * @param phoneNum phone number to be send
     * @return SUCCESS or FAILURE{@link Sim900Manager.Sim900Result }
     */
    @Sim900Result
    int sendSMS(@NonNull String msg, String phoneNum);

    void setLogLevel(@Logger.LEVEL int level);
}
