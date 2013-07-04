package dev.emmaguy.pocketwidget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

public class PocketWidgetConfigure extends Activity implements View.OnClickListener, OnUrlRetrievedListener,
	OnAccessTokenRetrievedListener {

    public static final String SHARED_PREFERENCES = "pocketWidget";
    
    private SharedPreferences sharedPreferences;
    private int appWidgetId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	setContentView(R.layout.activity_configure);

	sharedPreferences = getSharedPreferences(SHARED_PREFERENCES, 0);
	
	Intent intent = getIntent();
	Bundle extras = intent.getExtras();

	if (extras != null) {
	    appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
	    
	    if(appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID){
		sharedPreferences.edit().putInt("appWidgetId", appWidgetId).commit();
	    } else {
		appWidgetId = sharedPreferences.getInt("appWidgetId", AppWidgetManager.INVALID_APPWIDGET_ID);
	    }
	}
	
	if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
	    Log.i("PocketWidgetConfigure", "Invalid appWidgetId found");
            finish();
        }
	
	String accessToken = sharedPreferences.getString("access_token", null);
	if(accessToken != null && accessToken.length() > 0) {
	    Log.i("PocketWidgetConfigure", "Token found in shared prefs");
	    refreshAndFinishActivity();
	    return;
	}

	findViewById(R.id.login_button).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
	if (view.getId() == R.id.login_button) {
	    Toast.makeText(getApplicationContext(), "Redirecting to browser", Toast.LENGTH_SHORT).show();
	    new RetrievePocketRequestTokenAsyncTask(getResources().getString(R.string.pocket_consumer_key_mobile),
		    (OnUrlRetrievedListener) this, sharedPreferences).execute();
	}
    }

    @Override
    public void onRetrievedUrl(String url) {
	Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
	setResult(RESULT_OK, intent);
	finish();
	startActivity(intent);
    }

    @Override
    public void onResume() {
	super.onResume();

	Uri uri = this.getIntent().getData();
	if (uri != null && uri.toString().startsWith("pocketwidget")) {
	    new RetrievePocketAccessTokenAsyncTask(getResources().getString(R.string.pocket_consumer_key_mobile),
		    (OnAccessTokenRetrievedListener) this, sharedPreferences).execute();
	}
    }

    @Override
    public void onRetrievedAccessToken() {
	Toast.makeText(getApplicationContext(), "token " + sharedPreferences.getString("access_token", null), Toast.LENGTH_SHORT).show();

	refreshAndFinishActivity();
    }

    private void refreshAndFinishActivity() {
	RemoteViews views = new RemoteViews(getApplicationContext().getPackageName(), R.layout.widget_layout);
	AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
	appWidgetManager.updateAppWidget(appWidgetId, views);

	Intent resultValue = new Intent();
	resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
	setResult(RESULT_OK, resultValue);
	
	finish();
    }
}
