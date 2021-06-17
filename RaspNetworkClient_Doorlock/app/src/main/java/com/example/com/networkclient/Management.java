package com.example.com.networkclient;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Management extends Activity {
    static int check = 1;
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_management_layer);

        Button addBtn = (Button)findViewById(R.id.addBtn);

        final EditText snName = (EditText)findViewById(R.id.snName);
        final EditText snText = (EditText)findViewById(R.id.snText);
        final EditText ipText = (EditText)findViewById(R.id.ipText);
        final EditText portText = (EditText)findViewById(R.id.portText);
        final EditText a_pwText = (EditText)findViewById(R.id.a_pwText);
        final EditText admin = (EditText)findViewById(R.id.admin);

        Intent it = getIntent();
        String manager = it.getStringExtra("1");
        Log.i("test", manager);
        if(manager.equals("um")){
            ipText.setEnabled(false);
            portText.setEnabled(false);
            admin.setEnabled(false);
            check = 1;
            try {
                String result2;
                CustomTask task2 = new CustomTask();
                final String serial = Settings.Secure.getString(getBaseContext().getContentResolver(), Settings.Secure.ANDROID_ID);
                result2 = task2.execute(serial).get();
                ipText.setText(result2.split("/")[0]);
                portText.setText(result2.split("/")[1]);
                admin.setText(result2.split("/")[2]);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

         if(snName.getText().toString() != null || snText.getText().toString() != null || ipText.getText().toString() != null || portText.getText().toString() != null || a_pwText.getText().toString() != null || admin.getText().toString() != null ){
            addBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if(snText.getText().toString().equals("") || ipText.getText().toString().equals("") || portText.getText().toString().equals("") || a_pwText.getText().toString().equals("") || admin.getText().toString().equals("") || snName.getText().toString().equals("")){
                                Toast.makeText(getApplicationContext(),
                                        "값을 입력해주세요", Toast.LENGTH_SHORT).show();
                        }else {
                            check = 2;
                            String result2;
                            CustomTask task = new CustomTask();
                            result2 = task.execute(snText.getText().toString(), ipText.getText().toString(), portText.getText().toString(), a_pwText.getText().toString(), admin.getText().toString(), snName.getText().toString()).get();
                            if (result2.equals("추가성공!")) {
                                Toast.makeText(getApplicationContext(),
                                        "등록되었습니다.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(),
                                        "이미 존재하는 시리얼 번호입니다. \n시리얼 번호를 확인해주세요.", Toast.LENGTH_SHORT).show();
                            }
                        }
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
                URL url;
                if(check == 1)  url = new URL("http://192.168.0.3:8119/dl_proj/AtoW_Management1.jsp");
                else url = new URL("http://192.168.0.3:8119/dl_proj/AtoW_Management2.jsp");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");//데이터를 POST 방식으로 전송합니다.
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                if(check == 1) sendMsg = "sn=" + strings[0];
                else sendMsg = "sn=" + strings[0] + "&ip=" + strings[1] + "&port=" + strings[2] + "&a_pw=" + strings[3] + "&admin=" + strings[4] + "&name=" + strings[5];//보낼 정보인데요. GET방식으로 작성합니다. ex) "id=rain483&pwd=1234";
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
