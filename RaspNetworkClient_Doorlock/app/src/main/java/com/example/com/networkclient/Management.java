package com.example.com.networkclient;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Management extends Activity {
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_management_layer);

        Button addBtn = (Button)findViewById(R.id.addBtn);
        final EditText snText = (EditText)findViewById(R.id.snText);
        final EditText ipText = (EditText)findViewById(R.id.ipText);
        final EditText portText = (EditText)findViewById(R.id.portText);
        final EditText a_pwText = (EditText)findViewById(R.id.a_pwText);
        final EditText d_pwText = (EditText)findViewById(R.id.d_pwText);

        if(snText.getText().toString() != null || ipText.getText().toString() != null || portText.getText().toString() != null || a_pwText.getText().toString() != null || d_pwText.getText().toString() != null ){
            addBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        String result2;
                        CustomTask task = new CustomTask();
                        result2 = task.execute(snText.getText().toString(), ipText.getText().toString(), portText.getText().toString(), a_pwText.getText().toString(), d_pwText.getText().toString()).get();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
    class CustomTask extends AsyncTask<String, Void, String> {
        String sendMsg, receiveMsg;

        @Override
        // doInBackground의 매개값이 문자열 배열인데요. 보낼 값이 여러개일 경우를 위해 배열로 합니다.
        protected String doInBackground(String... strings) {
            try {
                String str;
                URL url = new URL("http://192.168.0.3:8119/dl_proj/AtoW_Management.jsp");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");//데이터를 POST 방식으로 전송합니다.
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                sendMsg = "sn=" + strings[0] + "&ip=" + strings[1] + "&port=" + strings[2] + "&a_pw=" + strings[3] + "&d_pw=" + strings[4];//보낼 정보인데요. GET방식으로 작성합니다. ex) "id=rain483&pwd=1234";
                System.out.println("센드 : " + sendMsg);
                //회원가입처럼 보낼 데이터가 여러 개일 경우 &로 구분하여 작성합니다.
                osw.write(sendMsg);//OutputStreamWriter에 담아 전송합니다.
                osw.flush();
                //jsp와 통신이 정상적으로 되었을 때 할 코드들입니다.
                if(conn.getResponseCode() == conn.HTTP_OK) {
                    InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "EUC-KR");
                    BufferedReader reader = new BufferedReader(tmp);
                    StringBuffer buffer = new StringBuffer();
                    //jsp에서 보낸 값을 받겠죠?
                    while ((str = reader.readLine()) != null) {
                        buffer.append(str);
                    }
                    receiveMsg = buffer.toString();

                } else {
                    Log.i("통신 결과", conn.getResponseCode()+"에러");
                    // 통신이 실패했을 때 실패한 이유를 알기 위해 로그를 찍습니다.
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //jsp로부터 받은 리턴 값입니다.
            return receiveMsg;
        }
    }
}
