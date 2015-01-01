package com.intelimina.test_launcher;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.content.Intent;

import java.util.List;
import java.io.File;
import java.io.FileOutputStream;

import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.Context;

import android.view.View;
import android.widget.EditText;

import android.util.Log;

public class MainActivity extends Activity{
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
  }

  public void launch_app(View view){
    if (create_shared_file()){
      //Build the intent
      Intent app_intent = getPackageManager().getLaunchIntentForPackage("com.intelimina.westmont");
      app_intent.putExtra("LaunchedFrom", "CRS");
      startActivity(app_intent);
    }
  }

  public File getStorageDir() {
    File file = new File(Environment.getExternalStorageDirectory(), "crs_middleware");
    if (!file.mkdirs()) {
    }
    return file;
  }

  private boolean create_shared_file(){
    EditText md_code = (EditText) findViewById(R.id.md_crs_code);
    EditText psr_code = (EditText) findViewById(R.id.psr_crs_code);
    
    String md_crs_code = md_code.getText().toString();
    String psr_crs_code = psr_code.getText().toString();
    String string = "[{\"psr\":\"" + psr_crs_code + "\",\"md\":\""+ md_crs_code +"\"}]";
    
    String filename = "com.intelimina.westmont.json";
    File file = new File (getStorageDir(), filename);
    
    FileOutputStream outputStream;

    try {
      outputStream = new FileOutputStream(file);
      outputStream.write(string.getBytes());
      outputStream.close();

    } catch (Exception e) {
    }
    return true;
  }
}