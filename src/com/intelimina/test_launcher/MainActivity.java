package com.intelimina.test_launcher;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.content.Intent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.content.pm.PackageManager;

import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import android.util.Log;
import android.widget.Toast;

public class MainActivity extends Activity{
    private static String SYNC_SERVICE_ACTION = "com.intelimina.unilab.START_SYNC_SERVICE";
    private static final int EXTERNAL_STORAGE_PERMISSION_CODE = 1;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        EditText appId = findViewById(R.id.app_id);
        EditText mdCode = findViewById(R.id.md_crs_code);
        EditText psrCode = findViewById(R.id.psr_crs_code);

        appId.setText("com.intelimina.pediatrica");
        mdCode.setText("202003P1441117");
        psrCode.setText("P1441");

//        appId.setText("com.intelimina.biofemme");
//        mdCode.setText("201909F2411127");
//        psrCode.setText("F2411");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case EXTERNAL_STORAGE_PERMISSION_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    callDetailingApp();
                } else {
                    finish();
                }
            }
        }
    }

    public void launchApp(View view) {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    EXTERNAL_STORAGE_PERMISSION_CODE);
        } else {
            callDetailingApp();
        }
    }

    public void launchGetUpdates(View view) {
        // SYNC INTENT SERVICE
        Intent msgIntent = new Intent(SYNC_SERVICE_ACTION);
        msgIntent.putExtra("action", "pull");

        sendBroadcast(msgIntent);
    }

    public void launchSendUpdates(View view) {
        // SYNC INTENT SERVICE
        Intent msgIntent = new Intent(SYNC_SERVICE_ACTION);
        msgIntent.putExtra("action", "push");

        startService(msgIntent);
    }

    public void backupDB(View view) {
        EditText appId = findViewById(R.id.app_id);
        String[] divisions = appId.getText().toString().split("\\.");

        final String inFileName = "/data/data/com.intelimina." + divisions[2] + "/databases/" + divisions[2] + "_edetailing";
        File dbFile = new File(inFileName);
        try {

            FileInputStream fis = new FileInputStream(dbFile);
            String outFileName = Environment.getExternalStorageDirectory() + "/" + divisions[2] +"_db_copy.db";

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        EXTERNAL_STORAGE_PERMISSION_CODE);
            } else {
                // Open the empty db as the output stream
                OutputStream output = new FileOutputStream(outFileName);

                // Transfer bytes from the inputfile to the outputfile
                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer))>0){
                    output.write(buffer, 0, length);
                }

                // Close the streams
                output.flush();
                output.close();
                fis.close();

                Toast.makeText(
                    this,
                    "Copied "+ divisions[2] +"_db_copy.db to your storage.",
                    Toast.LENGTH_SHORT
                ).show();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public File getStorageDir() {
        File file = new File(Environment.getExternalStorageDirectory(), "crs_middleware");
        if (MainActivity.isSDCARDAvailable()){
            if (!file.mkdirs()) {
                Log.e("DIRECTORY ERROR", "Can't create directory.");
            }
        }
        return file;
    }

    private void callDetailingApp(){
        EditText appId = findViewById(R.id.app_id);
        if (createSharedFile()) {
            //Build the intent
            Intent app_intent = getPackageManager().getLaunchIntentForPackage(appId.getText().toString());
            app_intent.putExtra("LaunchedFrom", "CRS");
            app_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(app_intent);
        }
    }

    private boolean createSharedFile(){
        EditText appId = findViewById(R.id.app_id);
        EditText mdCode = findViewById(R.id.md_crs_code);
        EditText psrCode = findViewById(R.id.psr_crs_code);
        CheckBox sendEmpty = findViewById(R.id.send_empty);
        CheckBox sendWrongFile = findViewById(R.id.send_wrong_file);

        String mdCodeStr = mdCode.getText().toString();
        String psrCodeStr = psrCode.getText().toString();
        String filename = "incorrect_file_name.json";
        String content = "";

        if (!sendEmpty.isChecked()) {
            content = "[{\"psr\":\"" + psrCodeStr + "\",\"md\":\""+ mdCodeStr +"\"}]";
        }

        if (!sendWrongFile.isChecked()) {
            filename = appId.getText().toString() + ".json";
        }

        File file = new File (getStorageDir(), filename);
        FileOutputStream outputStream;

        try {
            outputStream = new FileOutputStream(file);
            outputStream.write(content.getBytes());
            outputStream.close();
        } catch (Exception e) {
            Log.e("FILE ERROR", e.getMessage());
            return false;
        }

        return true;
    }


    public static boolean isSDCARDAvailable(){
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }
}