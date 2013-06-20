package com.ppgllrd.batterywidget;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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
 * Created by pepeg on 18/06/13.
 */
public class BatteryWidget extends AppWidgetProvider {

    public static final String SHARED_PREFS_NAME = "BATTERYWIDGET_PREFS";
    public static final String KEY_LEVEL = "LEVEL";
    public static final String KEY_CHARGING = "CHARGING";
    public static final String KEY_SCALE = "SCALE";
    public static final String LogTag = BatteryWidget.class.getName();

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

    public static class ScreenUpdateService extends Service {

        public static final String LogTag = ScreenUpdateService.class.getName();

        private static final int bitmapSz = 150;

        private static RectF mkRectF(int radius) {
            int margin = (bitmapSz - radius)/2;
            return new RectF(margin,margin,bitmapSz-margin,bitmapSz-margin);
        }

        private static int radius = 100;

        final RectF rectF1 = mkRectF(radius);
        final RectF rectF2 = mkRectF(radius+4);


        BatteryInfo mBI = null;

        @Override
        public void onStart(Intent intent, int startId) {

            if (mBI == null) {
                mBI = new BatteryInfo();
                IntentFilter mIntentFilter = new IntentFilter();
                mIntentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
                registerReceiver(mBI, mIntentFilter);
            }

            // update the widget
            RemoteViews updateViews = buildUpdate(this);
            if (updateViews != null) {
                try {
                    // Push update for this widget to the home screen
                    ComponentName thisWidget = new ComponentName(this, BatteryWidget.class);
                    if (thisWidget != null) {
                        AppWidgetManager manager = AppWidgetManager.getInstance(this);
                        if (manager != null) {
                            manager.updateAppWidget(thisWidget, updateViews);
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
                if (mBI != null) unregisterReceiver(mBI);
            } catch (Exception e) {
                Log.e(LogTag, "Failed to unregister", e);
            }
        }

        public RemoteViews buildUpdate(Context context) {
            // Build an update that holds the updated widget contents
            RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.widget);
            try {
                //Log.d(LogTag,"Updating Views");
                int level = 0;
                boolean charging = false;
                SharedPreferences settings = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
                if (settings != null) {
                    level = settings.getInt(KEY_LEVEL, 0);

                    //update level based on scale
                    int scale = settings.getInt(KEY_SCALE, 100);
                    if (scale != 100) {
                        if (scale <= 0) scale = 100;
                        level = (100 * level) / scale;
                    }

                    charging = (settings.getInt(KEY_CHARGING, BatteryManager.BATTERY_STATUS_UNKNOWN) == BatteryManager.BATTERY_STATUS_CHARGING);
                }


                //create a bitmap

                Bitmap bitmap = Bitmap.createBitmap(bitmapSz, bitmapSz, Bitmap.Config.ARGB_4444);

                //create a canvas from existant bitmap that will be used for drawing
                Canvas canvas = new Canvas(bitmap);

                //create new paint
                Paint paint = new Paint();
                paint.setAntiAlias(true);
                paint.setDither(true);
                paint.setStrokeJoin(Paint.Join.ROUND);
                paint.setStrokeCap(Paint.Cap.ROUND);


                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.argb(28,0,0,0));
                canvas.drawArc(rectF1,0,360,false,paint);


                paint.setStyle(Paint.Style.STROKE);
                paint.setColor(charging ? Color.YELLOW : Color.GRAY);
                paint.setStrokeWidth(charging ? 6 : 3);

                canvas.drawArc(charging ? rectF1 : rectF2,0,360,false,paint);


                int color;
                if(level < 15)
                    color = Color.RED;
                else if(level < 30)
                    color = Color.argb(255,255,128,0);
                else
                    color = Color.argb(217,0,189,255);
                paint.setColor(color);
                paint.setStrokeWidth(10);
                //canvas.drawArc(new RectF(25,25,125,125),-90+(100-level)*360/100,level*360/100,false,paint);
                canvas.drawArc(rectF1,-90,-level*360/100,false,paint);





                updateViews.setImageViewBitmap(R.id.circleImageView, bitmap);


                String levelText = Integer.toString(level); 
                updateViews.setTextViewText(R.id.batteryLevel, levelText);

            } catch (Exception e) {
                Log.e(LogTag, "Error Updating Views", e);
            }

            return updateViews;
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    }

}

