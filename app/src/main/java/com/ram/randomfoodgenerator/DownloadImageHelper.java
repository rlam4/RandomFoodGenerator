package com.ram.randomfoodgenerator;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.URL;

/**
 * Created by Michael on 9/19/2016.
 */
public class DownloadImageHelper extends AsyncTask<String, Void, Bitmap> {
    ImageView im;

    public DownloadImageHelper(ImageView im) {
        this.im = im;
    }


    @Override
    protected Bitmap doInBackground(String... params) {
        String imgURL = params[0];

        Bitmap bm = null;
        try {
            InputStream in = new java.net.URL(imgURL).openStream();
            bm = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e(MainActivity.TAG, "Image download encountered error: " + e.getMessage());
        }

        return bm;
    }

    protected void onPostExecute(Bitmap result) {
        im.setImageBitmap(result);
    }
}
