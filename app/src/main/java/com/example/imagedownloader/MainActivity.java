package com.example.imagedownloader;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.attribute.DosFileAttributes;

public class MainActivity extends AppCompatActivity {

    EditText txtUrl;
    Button btnDownload;
    ImageView imgView;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    private static String[] PERMISSION_STORAGE = {
        Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtUrl = findViewById(R.id.txtURL);
        btnDownload = findViewById(R.id.btnDownload);
        imgView = findViewById(R.id.imgView);



        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int permission = ActivityCompat.checkSelfPermission(
                        MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    // We don't have permission so prompt the user
                    ActivityCompat.requestPermissions(
                            MainActivity.this,
                            PERMISSION_STORAGE,
                            REQUEST_EXTERNAL_STORAGE);
                } else {
                    DownloadTask task = new DownloadTask();
                    String[] urls = new String[1];
                    urls[0] = txtUrl.getText().toString();
                    task.execute(urls);
                }
            }
        });
    }

    private Bitmap scaleBitmap(String imagePath) {
        Bitmap image = BitmapFactory.decodeFile(imagePath);
        float w = image.getWidth();
        float h = image.getHeight();
        int W = 400;
        int H = (int)((h*W)/w);
        Bitmap b = Bitmap.createScaledBitmap(image, W, H, false);
        return b;
    }

    private void downloadFile(String strUrl, String imagePath){
        try {
            URL url = new URL(strUrl);
            URLConnection connection = url.openConnection();
            connection.connect();

            InputStream input = new BufferedInputStream(url.openStream(), 8192);
            OutputStream output = new FileOutputStream(imagePath);

            byte data[] = new byte[1024];
            int count;
            while((count = input.read(data)) != -1){
                output.write(data, 0, count);
            }
            output.flush();
            output.close();
            input.close();

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("downloadFile", "can not download " + strUrl, e);
        }
    }
    class DownloadTask extends AsyncTask<String,Integer,Bitmap>{

        @Override
        protected Bitmap doInBackground(String... urls) {
            String filename = "temp.jpg";
            String imagePath = (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)).toString();

            downloadFile(urls[0], imagePath+"/"+filename);
            return scaleBitmap(imagePath+"/"+filename);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            imgView.setImageBitmap(bitmap);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                DownloadTask task = new DownloadTask();
                String[] urls = new String[1];
                urls[0] = txtUrl.getText().toString();
                task.execute(urls);
            } else {
                Toast.makeText(this, "External Storage permission not granted",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}

