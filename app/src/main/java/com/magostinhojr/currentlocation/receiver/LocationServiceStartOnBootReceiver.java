package com.magostinhojr.currentlocation.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.magostinhojr.currentlocation.service.CurrentLocationService;

/**
 * Created by marceloagostinho.
 */
public class LocationServiceStartOnBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent serviceIntent = new Intent(context, CurrentLocationService.class);
            context.startService(serviceIntent);
        }
    }



}
