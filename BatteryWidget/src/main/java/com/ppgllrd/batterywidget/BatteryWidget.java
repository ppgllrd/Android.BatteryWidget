package com.ppgllrd.batterywidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by pepeg on 18/06/13.
 */
public class BatteryWidget extends AppWidgetProvider {

    public static final String LogTag = BatteryWidget.class.getName();

    public BatteryWidget() {
        super();
    }


    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        try {
            context.stopService(new Intent(context, ScreenUpdateService.class));
        } catch (Exception e) {
            Log.d(LogTag, "Exception on disable: ", e);
        }
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
        // ask service to update widget
        context.startService(new Intent(context, ScreenUpdateService.class));
    }
}

