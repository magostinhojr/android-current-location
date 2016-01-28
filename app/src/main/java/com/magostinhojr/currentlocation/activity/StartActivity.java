package com.magostinhojr.currentlocation.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.magostinhojr.currentlocation.service.CurrentLocationService;

/**
 * Created by marceloagostinho on 1/28/16.
 */
public class StartActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        Intent serviceIntent = new Intent(this, CurrentLocationService.class);
//        startService(serviceIntent);
    }
}
