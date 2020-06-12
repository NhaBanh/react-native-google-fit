/**
 * Copyright (c) 2017-present, Stanislav Doskalenko - doskalenko.s@gmail.com
 * All rights reserved.
 *
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 *
 * Based on Asim Malik android source code, copyright (c) 2015
 *
 **/

package com.reactnative.googlefit;

import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;
import com.google.android.gms.fitness.result.DataSourcesResult;
import com.google.android.gms.fitness.data.Device;

import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.ArrayList;


import static com.google.android.gms.fitness.data.Device.TYPE_WATCH;

public class ActivityHistory {

    private ReactContext mReactContext;
    private GoogleFitManager googleFitManager;

    private static final String STEPS_FIELD_NAME = "steps";
    private static final String DISTANCE_FIELD_NAME = "distance";
    private static final String HIGH_LONGITUDE = "high_longitude";
    private static final String LOW_LONGITUDE = "low_longitude";
    private static final String HIGH_LATITUDE = "high_latitude";
    private static final String LOW_LATITUDE = "low_latitude";


    private static final int KCAL_MULTIPLIER = 1000;
    private static final int ONGOING_ACTIVITY_MIN_TIME_FROM_END = 10 * 60000;
    private static final String CALORIES_FIELD_NAME = "calories";

    private static final String TAG = "RNGoogleFit";

    public ActivityHistory(ReactContext reactContext, GoogleFitManager googleFitManager){
        this.mReactContext = reactContext;
        this.googleFitManager = googleFitManager;
    }

    public void getActivitySamples(long startTime, long endTime, final Callback successCallback) {
        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_MOVE_MINUTES, DataType.AGGREGATE_MOVE_MINUTES)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

        Fitness.getHistoryClient(mReactContext, googleFitManager.getGSA())
                .readData(readRequest)
                .addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                    @Override
                    public void onSuccess(DataReadResponse dataReadResponse) {
                        WritableMap map = Arguments.createMap();
                        if(dataReadResponse.getBuckets().size()>0){
                            for (Bucket bucket: dataReadResponse.getBuckets() ) {
                                for (DataSet dataset: bucket.getDataSets()) {
                                    for (DataPoint dp : dataset.getDataPoints()) {
                                        for (Field f: dp.getDataType().getFields()) {
                                            Log.i(TAG, "Field: "+ f.getName() + " Value: " + dp.getValue(f));
                                            map.putString("moveMins", dp.getValue(f).toString());
                                        }
                                    }
                                }
                            }
                        }else if(dataReadResponse.getDataSets().size() > 0){
                            for (DataSet dataset: dataReadResponse.getDataSets()) {
                                for (DataPoint dp : dataset.getDataPoints()) {
                                    for (Field f: dp.getDataType().getFields()) {
                                        Log.i(TAG, "Field: "+ f.getName() + " Value: " + dp.getValue(f));
                                        map.putString("moveMins", dp.getValue(f).toString());
                                    }
                                }
                            }
                        }
                        WritableArray results = Arguments.createArray();
                        results.pushMap(map);
                        successCallback.invoke(results);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        WritableArray results = Arguments.createArray();
                        successCallback.invoke(results);
                        Log.e(TAG, "There was a problem reading the data.", e);
                    }
                });


    }
}
