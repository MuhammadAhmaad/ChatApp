package com.companyname.chatapp.chatapp;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.RemoteViews;

import com.companyname.chatapp.chatapp.Activities.ChatActivity;
import com.companyname.chatapp.chatapp.Activities.MainActivity;
import com.companyname.chatapp.chatapp.Activities.SettingsActivity;
import com.companyname.chatapp.chatapp.Database.ChatsProvider;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of App Widget functionality.
 */
public class ChatAppWidget extends AppWidgetProvider {
    public static final String EXTRA_ITEM = "position";
    static List<String> mCollection = new ArrayList<>();
    static List<String> mIDS = new ArrayList<>();

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {


        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.chat_app_widget);
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        Cursor c = context.getContentResolver().query(ChatsProvider.CONTENT_URI, null, null, null, null);
        for(int i = 0;i<c.getCount();i++)
        {
            c.moveToNext();
            String name = c.getString(c.getColumnIndex(ChatsProvider.NAME));
            String message = c.getString(c.getColumnIndex(ChatsProvider.MESSAGE));
            String id = c.getColumnName(c.getColumnIndex(ChatsProvider.FIREBASEID));
            mCollection.add(name+context.getString(R.string.colon)+message);
            mIDS.add(id);

        }


        // Set up the collection
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            setRemoteAdapter(context, views);
        } else {
            setRemoteAdapterV11(context, views);
        }
        views.setOnClickPendingIntent(R.id.widget_layout,pendingIntent);

//        Intent svcIntent=new Intent(context, WidgetService.class);
//
//        svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
//        svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));
//
//        Intent clickIntent=new Intent(context, MainActivity.class);
//        PendingIntent clickPI=PendingIntent
//                .getActivity(context, 0,
//                        clickIntent,
//                        PendingIntent.FLAG_UPDATE_CURRENT);
//
//        views.setPendingIntentTemplate(R.id.widget_list, clickPI);
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

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private static void setRemoteAdapter(Context context, @NonNull final RemoteViews views) {
        views.setRemoteAdapter(R.id.widget_list,
                new Intent(context, WidgetService.class));
    }

    /**
     * Sets the remote adapter used to fill in the list items
     *
     * @param views RemoteViews to set the RemoteAdapter
     */
    @SuppressWarnings("deprecation")
    private static void setRemoteAdapterV11(Context context, @NonNull final RemoteViews views) {
        views.setRemoteAdapter(0, R.id.widget_list,
                new Intent(context, WidgetService.class));
    }
}


