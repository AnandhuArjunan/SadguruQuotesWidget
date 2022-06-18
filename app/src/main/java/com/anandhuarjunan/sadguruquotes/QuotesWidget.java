package com.anandhuarjunan.sadguruquotes;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.widget.RadioButton;
import android.widget.RemoteViews;
import android.widget.Toast;

/**
 * Implementation of App Widget functionality.
 */
public class QuotesWidget extends AppWidgetProvider {
    private static final String MyOnClick = "myOnClickTag";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int[] appWidgetIds,String quote) {
        int widgetAction = context.getSharedPreferences("MyPrefs",Context.MODE_PRIVATE).getInt("widgetAction",R.id.widgetConfig);
        RemoteViews views = getUpdatedView(context,quote);
        {

                if(widgetAction == R.id.changeQuotes){
                    ComponentName thisWidget = new ComponentName(context, QuotesWidget.class);
                    views.setOnClickPendingIntent(R.id.appwidget_text, getPendingSelfIntent(context, MyOnClick));
                    appWidgetManager.updateAppWidget(thisWidget, views);
                }else if(widgetAction == R.id.widgetConfig){
                    Intent configIntent = new Intent(context, Main.class);
                    PendingIntent configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, PendingIntent.FLAG_IMMUTABLE);
                    views.setOnClickPendingIntent(R.id.appwidget_text, configPendingIntent);

            }
        }
        appWidgetManager.updateAppWidget(appWidgetIds, views);
    }


    protected static PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, QuotesWidget.class);
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
    }
    private static RemoteViews getUpdatedView(Context context,String quote){
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.quotes);
        views.setTextViewText(R.id.appwidget_text, quote);
        return views;
    }



    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        SharedPreferences sharedpreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        QuoteLocalDatabase quoteLocalDatabase = new QuoteLocalDatabase(context);
        updateAppWidget(context, appWidgetManager, appWidgetIds, quoteLocalDatabase.fetchQuote());
        final Intent intent = new Intent(context,UpdateWidget.class);
        final PendingIntent pending = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        final AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pending);
        long interval = sharedpreferences.getLong("frequency",60000);
        alarm.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),interval, pending);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        QuoteLocalDatabase quoteLocalDatabase = new QuoteLocalDatabase(context);

        if(intent.getAction().equalsIgnoreCase(MyOnClick)){
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.quotes);
            ComponentName thisWidget = new ComponentName(context, QuotesWidget.class);
            updateAppWidget(context, appWidgetManager, appWidgetManager.getAppWidgetIds(thisWidget), quoteLocalDatabase.fetchQuote());


        }

    }

    public static class UpdateWidget extends Service {
        public UpdateWidget() {
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
        @Override
        public void onCreate() {
            super.onCreate();
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            buildUpdate();
            return super.onStartCommand(intent, flags, startId);
        }

        private void buildUpdate() {
            Context context = this;
            QuoteLocalDatabase quoteLocalDatabase = new QuoteLocalDatabase(context);
            AppWidgetManager mgr = AppWidgetManager.getInstance(context);
            ComponentName thisWidget = new ComponentName(context, QuotesWidget.class);
            updateAppWidget(context,mgr,mgr.getAppWidgetIds(thisWidget),quoteLocalDatabase.fetchQuote());
        }
    }

}