package com.example.com.networkclient;

import android.app.Activity;
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

    }

}
