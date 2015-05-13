package com.sahils.sparsereconstruction;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;
import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.client.HttpClient;
import ch.boye.httpclientandroidlib.client.methods.HttpPost;
import ch.boye.httpclientandroidlib.entity.mime.MIME;
import ch.boye.httpclientandroidlib.entity.mime.MultipartEntityBuilder;
import ch.boye.httpclientandroidlib.impl.client.DefaultHttpClient;
import ch.boye.httpclientandroidlib.util.EntityUtils;

public class MainActivity extends Activity {

	ImageAdapter im;
	ArrayList<String> cur_images;
	Activity a;
	String resp_json_str = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// a = this;

		cur_images = new ArrayList<String>();
		im = new ImageAdapter(this);
		GridView gridview = (GridView) findViewById(R.id.gridview);
		gridview.setAdapter(im);

		gridview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				Toast.makeText(MainActivity.this, "" + position,
						Toast.LENGTH_SHORT).show();
			}
		});

		Button capture_btn = (Button) findViewById(R.id.btn_capture);
		capture_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dispatchTakePictureIntent();
			}
		});

		Button recon_btn = (Button) findViewById(R.id.btn_recon);
		recon_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				doThisInstead();
			}
		});

		Button display_btn = (Button) findViewById(R.id.btn_display);
		display_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(MainActivity.this, ViewPointCloud.class);
//				i.putExtra("recon_data", resp_json_str);
				Log.i("INTENT", "Something is wrong here too..");
				startActivity(i);
			}
		});

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

	String mCurrentPhotoPath;

	private File createImageFile() throws IOException {
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		String imageFileName = "JPEG_" + timeStamp + "_";
		File storageDir = Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		File image = File.createTempFile(imageFileName, /* prefix */
				".jpg", /* suffix */
				storageDir /* directory */
		);

		// Save a file: path for use with ACTION_VIEW intents
		mCurrentPhotoPath = "file:" + image.getAbsolutePath();
		return image;
	}

	static final int REQUEST_TAKE_PHOTO = 1;
	Uri pic_uri = null;

	private void dispatchTakePictureIntent() {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		// Ensure that there's a camera activity to handle the intent
		if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
			// Log.i("CAPTURE","Am I entering here?");
			// Create the File where the photo should go
			File photoFile = null;
			try {
				// Log.i("CAPTURE","What about here?");
				photoFile = createImageFile();
			} catch (IOException ex) {
				// Error occurred while creating the File
				Log.i("Image", "Some error");
			}
			// Continue only if the File was successfully created
			if (photoFile != null) {
				// Log.i("CAPTURE","And here?");
				takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
						Uri.fromFile(photoFile));
				pic_uri = Uri.fromFile(photoFile);
				cur_images.add(photoFile.getPath());
				Log.i("PATH", cur_images.toString());
				startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i("CAPTURE", "Pic URI: + " + pic_uri.toString());
		if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
			try {
				im.mThumbBmps.add(pic_uri);
				Log.i("CAPTURE", "Added Uri to Adapter..");
			} catch (Exception e) {
				e.printStackTrace();
			}

			im.notifyDataSetChanged();
		}
	}

	void resetImages() {

	}

	public void doThis() {
		cur_images.clear();
		File file = new File(Environment.getExternalStorageDirectory(),
				"important docs/T1.jpg");
		cur_images.add(file.getAbsolutePath());
		file = new File(Environment.getExternalStorageDirectory(),
				"important docs/T2.jpg");
		cur_images.add(file.getAbsolutePath());
		file = new File(Environment.getExternalStorageDirectory(),
				"important docs/T3.jpg");
		cur_images.add(file.getAbsolutePath());
		file = new File(Environment.getExternalStorageDirectory(),
				"important docs/T4.jpg");
		cur_images.add(file.getAbsolutePath());
		file = new File(Environment.getExternalStorageDirectory(),
				"important docs/T5.jpg");
		cur_images.add(file.getAbsolutePath());
		file = new File(Environment.getExternalStorageDirectory(),
				"important docs/T6.jpg");
		cur_images.add(file.getAbsolutePath());
		Log.i("TAG1", cur_images.toString());
		ServerCall nt = new ServerCall();
		nt.execute();
	}

	public void doThisInstead() {
		cur_images.clear();
		for (int i = 0; i < im.getCount(); i++) {
			cur_images.add(im.mThumbBmps.get(i).toString().substring(7));
		}
		Log.i("SEND_IMAGES", cur_images.toString());
		ServerCall nt = new ServerCall();
		nt.execute();
	}

	private class ServerCall extends AsyncTask<String, Void, Boolean> {

		@Override
		protected Boolean doInBackground(String... params) {
			return tryMultipartEntityBuilder();
		}

		boolean tryMultipartEntityBuilder() {
			try {
				String urlPath = "http://10.0.0.14:5000/upload";
				HttpClient httpClient = new DefaultHttpClient();
				HttpPost postRequest = new HttpPost(urlPath);

				MultipartEntityBuilder builder = MultipartEntityBuilder
						.create();
				builder.setCharset(MIME.UTF8_CHARSET);
				builder.addTextBody("tot_img",
						Integer.toString(cur_images.size()));
				for (int i = 0; i < cur_images.size(); i++) {
					builder.addBinaryBody("img" + Integer.toString(i),
							new File(cur_images.get(i)));
					Log.i("SEND_IMAGES", "getting image " + Integer.toString(i));
				}
				postRequest.setEntity(builder.build());
				HttpResponse response = httpClient.execute(postRequest);
				resp_json_str = EntityUtils.toString(response.getEntity());
				Log.i("RESPONSE", "Successfully saved json response as string");
				// JSONArray recon_json = new JSONArray(responseText);
				if(response.getStatusLine().getStatusCode() == 200)
					return true;
				else
					return false;

			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			Log.i("PB","Here with result: " + result.toString());
			runOnUiThread(new Runnable() {
				public void run() {
					 ProgressBar pb;
					 pb = (ProgressBar)findViewById(R.id.pb1);
					 pb.setVisibility(View.GONE);
				}
			});
		}

		@Override
		protected void onPreExecute() {
			runOnUiThread(new Runnable() {
				public void run() {
					 ProgressBar pb;
					 pb = (ProgressBar)findViewById(R.id.pb1);
					 pb.setVisibility(View.VISIBLE);
				}
			});
		}

		@Override
		protected void onProgressUpdate(Void... values) {

		}
	}
}
