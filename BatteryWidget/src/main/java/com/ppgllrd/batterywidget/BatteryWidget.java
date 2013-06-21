package com.ppgllrd.batterywidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

/**
 * Created by pepeg on 18/06/13.
 */
public class BatteryWidget extends AppWidgetProvider {

    public static final String SHARED_PREFS_NAME = "BATTERYWIDGET_PREFS";
    public static final String KEY_LEVEL = "LEVEL";
    public static final String KEY_CHARGING = "CHARGING";
    public static final String KEY_SCALE = "SCALE";
    public static final String LogTag = BatteryWidget.class.getName();


    public BatteryWidget() {
        super();
    }


    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        BatteryWidget.clearSettings(context);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        try {
            context.stopService(new Intent(context, ScreenUpdateService.class));
        } catch (Exception e) {
            Log.d(LogTag, "Exception on disable: ", e);
        }
        BatteryWidget.clearSettings(context);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        try {
            context.stopService(new Intent(context, ScreenUpdateService.class));
        } catch (Exception e) {
            Log.d(LogTag, "Exception on delete: ", e);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // To prevent any ANR timeouts, we perform the update in a service
        context.startService(new Intent(context, ScreenUpdateService.class));
    }

    private static void clearSettings(Context context) {
        if (context != null) {
            SharedPreferences settings = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
            Editor editor = settings.edit();
            editor.remove(KEY_LEVEL);
            editor.remove(KEY_CHARGING);
            editor.remove(KEY_SCALE);
            editor.commit();
        }
    }

}

