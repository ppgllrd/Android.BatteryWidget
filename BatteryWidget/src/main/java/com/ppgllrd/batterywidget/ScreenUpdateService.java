package com.ppgllrd.batterywidget;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
    private static final String LogTag = ScreenUpdateService.class.getName();
    private static final int bitmapSz = 150;
    private static int radius = 140;
    private final RectF rectF1 = mkRectF(radius);
    private final RectF rectF2 = mkRectF(radius + 3);

    private BatteryInfoBroadcastReceiver biBR = null;

    // Currently shown battery status
    private int level = -1;
    private int status = -1;
    private int scale = 100;

    private static RectF mkRectF(int radius) {
        int margin = (bitmapSz - radius) / 2;
        return new RectF(margin, margin, bitmapSz - margin, bitmapSz - margin);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (biBR == null) {
            biBR = new BatteryInfoBroadcastReceiver();
            biBR.setScreenUpdateService(this);
            IntentFilter mIntentFilter = new IntentFilter();
            mIntentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
            registerReceiver(biBR, mIntentFilter);
        }
        updateWidget();
        return START_STICKY;
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

    private RemoteViews buildUpdate(Context context) {
        // Build an update that holds the updated widget contents
        RemoteViews updatedViews = new RemoteViews(context.getPackageName(), R.layout.widget);
        try {

            if (scale != 100) {
                if (scale <= 0) scale = 100;
                level = (100 * level) / scale;
            }
            boolean charging = (status == BatteryManager.BATTERY_STATUS_CHARGING);

            // Create a bitmap
            Bitmap bitmap = Bitmap.createBitmap(bitmapSz, bitmapSz, Bitmap.Config.ARGB_4444);

            // Create a canvas from existing bitmap that will be used for drawing
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
            paint.setStrokeWidth(12);
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

    public static class BatteryInfoBroadcastReceiver extends BroadcastReceiver {
        private ScreenUpdateService screenUpdateService;

        void setScreenUpdateService(ScreenUpdateService screenUpdateService) {
            this.screenUpdateService = screenUpdateService;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                if (action != null && action.equals(Intent.ACTION_BATTERY_CHANGED)) {

                    int currentLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                    int currentStatus = intent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN);

                    // Update display if something changed
                    if (screenUpdateService.level != currentLevel || screenUpdateService.status != currentStatus) {
                        screenUpdateService.scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
                        screenUpdateService.level = currentLevel;
                        screenUpdateService.status = currentStatus;
                        screenUpdateService.updateWidget();
                    }
                }
            } catch (Exception e) {
                Log.e(BatteryWidget.LogTag, "onReceive", e);
            }
        }
    }
}
