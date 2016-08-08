package org.jason.autocamera;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import org.jason.autocamera.annotations.NeedUseCamera;
import org.jason.autocamera.annotations.OnImageReturn;
import org.jason.autocamera.annotations.PathGenerator;

@NeedUseCamera(savePath = "test.jpg")
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
    }

    private void initViews() {
        findViewById(R.id.btn_open_album).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivityAutoCamera.openAlbum(MainActivity.this);
            }
        });

        findViewById(R.id.btn_open_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivityAutoCamera.openCamera(MainActivity.this);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        MainActivityAutoCamera.onActivityResult(this,requestCode,resultCode,data);
    }

    @OnImageReturn
    void onImageReturn(Uri uri){
        Toast.makeText(this,uri.toString(),Toast.LENGTH_LONG).show();
    }

    @PathGenerator
    String generatePath(){
        return "aaa.jpg";
    }
}
