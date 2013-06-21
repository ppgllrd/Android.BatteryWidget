package com.ppgllrd.batterywidget;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.BatteryManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * Created by pepeg on 21/06/13.
 */
public class ScreenUpdateService extends Service {

    public static final String LogTag = ScreenUpdateService.class.getName();

    private static final int bitmapSz = 150;

    private static RectF mkRectF(int radius) {
        int margin = (bitmapSz - radius) / 2;
        return new RectF(margin, margin, bitmapSz - margin, bitmapSz - margin);
    }

    private static int radius = 100;

    private final RectF rectF1 = mkRectF(radius);
    private final RectF rectF2 = mkRectF(radius + 4);


    private BatteryInfoBroadcastReceiver biBR = null;

    @Override
    public void onStart(Intent intent, int startId) {

        if (biBR == null) {
            biBR = new BatteryInfoBroadcastReceiver(this);
            IntentFilter mIntentFilter = new IntentFilter();
            mIntentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
            registerReceiver(biBR, mIntentFilter);
        }
        updateWidget();
        //stopSelf();
    }

    public void updateWidget() {
        // update the widget
        RemoteViews updatedViews = buildUpdate(this);
        if (updatedViews != null) {
            try {
                // Push update for this widget to the home screen
                ComponentName thisWidget = new ComponentName(this, BatteryWidget.class);
                if (thisWidget != null) {
                    AppWidgetManager manager = AppWidgetManager.getInstance(this);
                    if (manager != null) {
                        manager.updateAppWidget(thisWidget, updatedViews);
                    }
                }
            } catch (Exception e) {
                Log.e(LogTag, "Update Service Failed to Start", e);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (biBR != null) unregisterReceiver(biBR);
        } catch (Exception e) {
            Log.e(LogTag, "Failed to unregister", e);
        }
    }

    public RemoteViews buildUpdate(Context context) {
        // Build an update that holds the updated widget contents
        RemoteViews updatedViews = new RemoteViews(context.getPackageName(), R.layout.widget);
        try {
            //Log.d(LogTag,"Updating Views");
            int level = 0;
            boolean charging = false;
            SharedPreferences settings = getSharedPreferences(BatteryWidget.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
            if (settings != null) {
                level = settings.getInt(BatteryWidget.KEY_LEVEL, 0);

                //update level based on scale
                int scale = settings.getInt(BatteryWidget.KEY_SCALE, 100);
                if (scale != 100) {
                    if (scale <= 0) scale = 100;
                    level = (100 * level) / scale;
                }
                charging = (settings.getInt(BatteryWidget.KEY_CHARGING, BatteryManager.BATTERY_STATUS_UNKNOWN) == BatteryManager.BATTERY_STATUS_CHARGING);
            }

            //create a bitmap
            Bitmap bitmap = Bitmap.createBitmap(bitmapSz, bitmapSz, Bitmap.Config.ARGB_4444);

            //create a canvas from existing bitmap that will be used for drawing
            Canvas canvas = new Canvas(bitmap);

            //create new paint
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setDither(true);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeCap(Paint.Cap.ROUND);

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(28, 0, 0, 0));
            canvas.drawArc(rectF1, 0, 360, false, paint);

            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(charging ? Color.GREEN : Color.GRAY);
            paint.setStrokeWidth(charging ? 6 : 3);

            canvas.drawArc(charging ? rectF1 : rectF2, 0, 360, false, paint);

            int color;
            if (level < 15)
                color = Color.argb(255, 255, 10, 10);
            else if (level < 30)
                color = Color.argb(255, 255, 128, 0);
            else
                color = Color.argb(255, 0, 128, 255);
            paint.setColor(color);
            paint.setStrokeWidth(10);
            //canvas.drawArc(new RectF(25,25,125,125),-90+(100-level)*360/100,level*360/100,false,paint);
            canvas.drawArc(rectF1, -90, -level * 360 / 100, false, paint);

            updatedViews.setImageViewBitmap(R.id.circleImageView, bitmap);

            updatedViews.setTextViewText(R.id.batteryLevel, Integer.toString(level));

        } catch (Exception e) {
            Log.e(LogTag, "Error Updating Views", e);
        }
        return updatedViews;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
