package dev.emmaguy.pocketwidget;

import dev.emmaguy.pocketwidget.RetrieveCountOfUnreadArticlesAsyncTask.UnreadCountRetrievedListener;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

public class RetrieveUnreadArticlesCountService extends Service implements UnreadCountRetrievedListener {

    @Override
    public void onStart(Intent intent, int startId) {
	Log.i("RetrieveUnreadItems", "onStart");
	final SharedPreferences sharedPreferences = getSharedPreferences(
		UnreadArticlesConfigurationActivity.SHARED_PREFERENCES, 0);
	final String accessToken = sharedPreferences.getString("access_token", null);

	if (accessToken == null || accessToken.length() <= 0) {
	    return;
	}
	
	new RetrieveCountOfUnreadArticlesAsyncTask(getResources().getString(R.string.pocket_consumer_key_mobile),
		accessToken, this).execute();
    }

    @Override
    public void onUnreadCountRetrieved(Integer unreadCount) {
	Log.i("RetrieveUnreadItems", "Items retrieved: " + unreadCount);
	final SharedPreferences sharedPreferences = getSharedPreferences(
		UnreadArticlesConfigurationActivity.SHARED_PREFERENCES, 0);

	if (unreadCount >= 0) {
	    sharedPreferences.edit().putInt("unread_count", unreadCount).commit();

	    updateWidget(unreadCount);
	}
    }

    private void updateWidget(final int unreadCount) {
	final RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_layout);
	final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);

	ComponentName thisWidget = new ComponentName(getApplicationContext(), UnreadArticlesWidgetProvider.class);
	int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

	for (int appWidgetId : allWidgetIds) {
	    Log.i("UnreadArticlesWidgetProvider", "Updating widget id: " + appWidgetId);

	    Intent clickIntent = new Intent(this, UnreadArticlesWidgetProvider.class);
	    clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

	    PendingIntent pendingIntent = PendingIntent.getBroadcast(this, appWidgetId, clickIntent, 0);
	    views.setOnClickPendingIntent(R.id.widget_imageview, pendingIntent);
	    views.setViewVisibility(R.id.unread_count_textview, View.VISIBLE);
	    views.setTextViewText(R.id.unread_count_textview, Integer.valueOf(unreadCount).toString());

	    appWidgetManager.updateAppWidget(appWidgetId, views);
	}
    }

    @Override
    public IBinder onBind(Intent intent) {
	return null;
    }
}