package com.example.com.networkclient;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class ManagementList extends Activity {
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_management_list);
        setTitle("ManagementList");

        Button useraddbtn = (Button)findViewById(R.id.useraddbtn);
        Button userdelbtn = (Button)findViewById(R.id.userdelbtn);
        Button logview = (Button)findViewById(R.id.logview);
        Button logDel = (Button)findViewById(R.id.del_log);
        Intent it = getIntent();
        final String manger = it.getStringExtra("1");

        useraddbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(ManagementList.this, Management.class);
                it.putExtra("1", manger);
                startActivity(it);
                finish();
            }
        });

        userdelbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(ManagementList.this, Management_Del.class);
                startActivity(it);
                finish();
            }
        });

        logview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent it = new Intent(ManagementList.this, Management_LogView.class);
                startActivity(it);
                finish();
            }
        });

        logDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMessage();
            }
        });
    }

    public void showMessage(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("????????????");
        builder.setMessage("????????? ?????? ????????? ?????????????????????????");
        builder.setIcon(android.R.drawable.ic_dialog_alert);

        builder.setPositiveButton("???", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.i("check","one");
                String rs = null;
                try {
                    CustomTask task = new CustomTask();
                    rs = task.execute().get();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        builder.setNegativeButton("?????????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    class CustomTask extends AsyncTask<String, Void, String> {
        String sendMsg, receiveMsg;
        @Override
        // doInBackground??? ???????????? ????????? ???????????????. ?????? ?????? ???????????? ????????? ?????? ????????? ?????????.
        protected String doInBackground(String... strings) {
            try {
                String str;
                URL url;
                url = new URL("http://192.168.86.252:9002/dl_proj/AtoW_DelLog.jsp");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");//???????????? POST ???????????? ???????????????.
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                sendMsg = "" ;//?????? ???????????????. GET???????????? ???????????????. ex) "id=rain483&pwd=1234";
                System.out.println("?????? : " + sendMsg);
                //?????????????????? ?????? ???????????? ?????? ?????? ?????? &??? ???????????? ???????????????.
                osw.write(sendMsg);//OutputStreamWriter??? ?????? ???????????????.
                osw.flush();
                //jsp??? ????????? ??????????????? ????????? ??? ??? ??????????????????.
                if(conn.getResponseCode() == conn.HTTP_OK) {
                    InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "EUC-KR");
                    BufferedReader reader = new BufferedReader(tmp);
                    StringBuffer buffer = new StringBuffer();
                    //jsp?????? ?????? ?????? ??????????
                    while ((str = reader.readLine()) != null) {
                        buffer.append(str);
                    }
                    receiveMsg = buffer.toString();

                } else {
                    Log.i("?????? ??????", conn.getResponseCode()+"??????");
                    // ????????? ???????????? ??? ????????? ????????? ?????? ?????? ????????? ????????????.
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //jsp????????? ?????? ?????? ????????????.
            return receiveMsg;
        }
    }

}
