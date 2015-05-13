package com.sahils.sparsereconstruction;

import java.io.IOException;
import java.io.InputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ConfigurationInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class ViewPointCloud extends Activity  {

	/** Hold a reference to our GLSurfaceView */
	private MyGLSurfaceView mGLSurfaceView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
//		mGLSurfaceView = (MyGLSurfaceView) findViewById(R.id.pcv);
		
		String recon_json = null;
		
//		Intent intent = getIntent();
//		recon_json = intent.getExtras().getString("recon_data");
		
		// Check if the system supports OpenGL ES 2.0.
		final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		final ConfigurationInfo configurationInfo = activityManager
				.getDeviceConfigurationInfo();

		JSONArray a;
		JSONObject pnt_obj = null;
		try {
			InputStream is = this.getAssets().open("reconstruction.json");
			int size = is.available();
			byte[] buffer = new byte[size];
			is.read(buffer);
			is.close();
			recon_json = new String(buffer, "UTF-8");
			Log.i("READ", "Success: " + recon_json);
		} catch (IOException ex) {
			Log.i("READ", "Fail: " + recon_json);
		}
		try {
			a = new JSONArray(recon_json);
			pnt_obj = (JSONObject) a.get(0);
			Log.i("JSON", "Success: " + pnt_obj.toString());
		} catch (JSONException e) {
			Log.i("JSON", "Fail");
		}

		if (pnt_obj != null) {
			mGLSurfaceView = new MyGLSurfaceView(this,pnt_obj);
//			mGLSurfaceView.init(this,pnt_obj);
		} else {
			Log.i("TAG1", "some parsing error");
			return;
		}

		setContentView(mGLSurfaceView);
	}

	@Override
	protected void onResume() {
		// The activity must call the GL surface view's onResume() on activity
		// onResume().
		super.onResume();
		mGLSurfaceView.onResume();
	}

	@Override
	protected void onPause() {
		// The activity must call the GL surface view's onPause() on activity
		// onPause().
		super.onPause();
		mGLSurfaceView.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
