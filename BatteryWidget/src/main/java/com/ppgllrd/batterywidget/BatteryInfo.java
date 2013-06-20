package com.ppgllrd.batterywidget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.util.Log;

/**
 * Created by pepeg on 18/06/13.
 */
public class BatteryInfo extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            String action = intent.getAction();
            if (action != null && action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                SharedPreferences settings = context.getSharedPreferences(BatteryWidget.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
                if (settings != null) {
                    int prevLevel = settings.getInt(BatteryWidget.KEY_LEVEL, -1);
                    int prevStatus = settings.getInt(BatteryWidget.KEY_CHARGING, -1);

                    int currentLevel = intent.getIntExtra("level", 0);
                    int currentStatus = intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN);

                    // Only update display if something changed.
                    if (prevLevel != currentLevel || prevStatus != currentStatus) {
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putInt(BatteryWidget.KEY_LEVEL, currentLevel);
                        editor.putInt(BatteryWidget.KEY_CHARGING, currentStatus);
                        int scale = intent.getIntExtra("scale", 100); 
                        editor.putInt(BatteryWidget.KEY_SCALE, scale);
                        editor.commit();
                        Intent forceUpIntent = new Intent(context, BatteryWidget.ScreenUpdateService.class);
                        context.startService(forceUpIntent);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(BatteryWidget.LogTag, "onReceive", e);
        }
    }
}

