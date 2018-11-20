package com.companyname.chatapp.chatapp;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.RemoteViews;

import com.companyname.chatapp.chatapp.Activities.SettingsActivity;
import com.companyname.chatapp.chatapp.Database.UserProvider;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Implementation of App Widget functionality.
 */
public class ChatAppWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {


        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.chat_app_widget);

        Intent intent = new Intent(context, SettingsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        Cursor c = context.getContentResolver().query(UserProvider.CONTENT_URI, null, null, null, "name");

        if (c.moveToFirst()) {
            views.setTextViewText(R.id.appwidget_user_name, c.getString(c.getColumnIndex(UserProvider.NAME)));
            views.setTextViewText(R.id.appwidget_user_status, c.getString(c.getColumnIndex(UserProvider.STATUS)));
            new DownloadBitmap(views, appWidgetManager, c.getString(c.getColumnIndex(UserProvider.PHOTO_URL)), appWidgetId).execute();
        }
        views.setOnClickPendingIntent(R.id.appwidget_user_name, pendingIntent);
        views.setOnClickPendingIntent(R.id.appwidget_user_status, pendingIntent);
        views.setOnClickPendingIntent(R.id.appWidget_profile_picture, pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    public static class DownloadBitmap extends AsyncTask<String, Void, Bitmap> {

        private int appwidgetId;
        private AppWidgetManager appwidgetManager;
        RemoteViews views;
        Context context;
        String url = "";

        public DownloadBitmap(RemoteViews views, AppWidgetManager appWidgetManager, String url, int appWidgetID) {
            this.views = views;
            this.url = url;
            this.appwidgetManager = appWidgetManager;
            this.appwidgetId = appWidgetID;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            try {
                URL ur = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) ur.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                return myBitmap;
            } catch (IOException e) {
                return null;
            }
        }


        @Override
        protected void onPostExecute(Bitmap bitmap) {
            views.setImageViewBitmap(R.id.appWidget_profile_picture, bitmap);
            appwidgetManager.updateAppWidget(appwidgetId, views);
        }
    }
}


