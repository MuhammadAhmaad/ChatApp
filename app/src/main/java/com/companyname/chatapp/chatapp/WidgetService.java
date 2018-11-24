package com.companyname.chatapp.chatapp;

import android.content.Intent;
import android.widget.RemoteViewsService;

/**
 * Created by Mohamed Ahmed on 11/24/2018.
 */

public class WidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsService.RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new WidgetDataProvider(this, intent);
    }
}
