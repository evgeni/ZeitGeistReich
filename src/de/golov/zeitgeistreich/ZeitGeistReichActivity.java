package de.golov.zeitgeistreich;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore.Images.ImageColumns;
import android.widget.Toast;

import java.io.File;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;

public class ZeitGeistReichActivity extends Activity {
	
	private class ZeitGeistReichUploaderTask extends AsyncTask<File,Void,Void>{
        private ProgressDialog Dialog = new ProgressDialog(ZeitGeistReichActivity.this);
        private String Error = null;

        protected void onPreExecute() {
            Dialog.setMessage("Uploading image...");
            Dialog.show();
        }
        
        protected void onPostExecute(Void unused) {
            Dialog.dismiss();
            if (Error != null) {
                Toast.makeText(ZeitGeistReichActivity.this, Error, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ZeitGeistReichActivity.this, "Image uploaded.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        
		protected Void doInBackground(File...files) {
			for (File file: files) {
				try {
                	
                    HttpClient httpclient = new DefaultHttpClient();

                    HttpPost httppost = new HttpPost("http://zeitgeist.li/new");

                    MultipartEntity mpEntity = new MultipartEntity();
                    ContentBody cbFile = new FileBody(file);
                    mpEntity.addPart("image_upload", cbFile);
                    httppost.setEntity(mpEntity);
                    HttpResponse response = httpclient.execute(httppost);
                } catch (Exception e) {
                    Error = e.toString();
                }

			}
			return null;
		}
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main); 
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        Uri mImageUri = null;
        File mFilename = null;
        if (Intent.ACTION_SEND.equals(intent.getAction()) && extras != null) {
        	if (extras.containsKey(Intent.EXTRA_STREAM)) {
                mImageUri = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);
                if (mImageUri != null) {
                    Cursor cursor = getContentResolver().query(mImageUri, null, null, null, null);
                    if (cursor.moveToFirst()) {
                            mFilename = new File( cursor.getString(cursor.getColumnIndexOrThrow(ImageColumns.DATA)));
                    }
                    cursor.close();
                    if (mFilename != null) {
                    	ZeitGeistReichUploaderTask task = new ZeitGeistReichUploaderTask();
                    	task.execute(mFilename);
                    }
                }
        	}
        }
    }
}