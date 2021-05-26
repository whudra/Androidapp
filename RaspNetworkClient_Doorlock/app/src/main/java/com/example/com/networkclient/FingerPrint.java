package com.example.com.networkclient;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Executor;

public class FingerPrint extends AppCompatActivity {

    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private String userInfo;
    boolean flag_pwbtn = false;

    public String getUserinfo(){
        return userInfo;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_layer );
        final String serial = Settings.Secure.getString(getBaseContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        final EditText edtPW = (EditText)findViewById(R.id.input_pw);
        final Button pwBtn = (Button)findViewById(R.id.buttonAuthWithPassword);
        Button maBtn = (Button)findViewById(R.id.managementPageBtn);
        TextView mySerial = (TextView)findViewById(R.id.mySerial);

        mySerial.setText(serial);

        maBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String result2;
                    CustomTask task = new CustomTask();
                    Log.i("시리얼 : ", serial);
                    result2 = task.execute(serial, "serial2").get();
                    Log.i("리턴값 : ", result2);
                    if(result2.equals("1")){
                        Intent it = new Intent(FingerPrint.this, ManagementList.class);
                        it.putExtra("1", "um");
                        startActivity(it);
                        finish();
                    }else if(result2.equals("2")){
                        Intent it = new Intent(FingerPrint.this, ManagementList.class);
                        it.putExtra("1", "pm");
                        startActivity(it);
                        finish();
                    }else{
                        Toast.makeText(getApplicationContext(),
                                "등록되지 않은 관리자입니다", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });



        pwBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!flag_pwbtn){ // 버튼을 처음 눌렀을때
                    edtPW.setVisibility(View.VISIBLE);
                    pwBtn.setText("확인");
                    flag_pwbtn = true;
                }else{
                    try {
                        String result2;
                        String pw = edtPW.getText().toString();
                        CustomTask task = new CustomTask();
                        Log.i("패스워드 : ", pw);
                        result2 = task.execute(pw, "password").get();
                        Log.i("리턴값 : ", result2);
                        if(result2.equals("비밀번호 인증 실패!")){
                            Toast.makeText(getApplicationContext(),
                                    "등록되지 않은 비밀번호입니다 \n비밀번호 등록 후 사용 가능합니다!", Toast.LENGTH_SHORT).show();
                        }else{
                            CustomTask task2 = new CustomTask();
                            userInfo = task2.execute(pw, "info2").get();
                            Intent it = new Intent(FingerPrint.this, NetworkClientActivity.class);
                            it.putExtra("1",userInfo);
                            it.putExtra("2",serial);
                            startActivity(it);
                            finish();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    edtPW.setVisibility(View.INVISIBLE);
                    pwBtn.setText("");
                    flag_pwbtn = false;
                }
            }
        });

        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(),
                        "에러", Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                try {
                    String result2;
                    CustomTask task = new CustomTask();
                    Log.i("시리얼 : ", serial);
                    result2 = task.execute(serial, "serial").get();
                    Log.i("리턴값 : ", result2);
                    if(result2.equals("시리얼 인증 실패!")){
                        Toast.makeText(getApplicationContext(),
                                "등록되지 않은 시리얼입니다 \n시리얼 등록 후 사용 가능합니다!", Toast.LENGTH_SHORT).show();
                    }else{
                        CustomTask task2 = new CustomTask();
                        Log.i("시리얼 인증성공 : ", "phase2");
                        userInfo = task2.execute(serial, "info1").get();
                        Log.i("userinfo : ", userInfo);
                        Intent it = new Intent(FingerPrint.this, NetworkClientActivity.class);
                        it.putExtra("1",userInfo);
                        it.putExtra("2",serial);
                        startActivity(it);
                        finish();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), "실패",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("지문 인증")
                .setSubtitle("기기에 등록된 지문을 이용하여 지문을 인증해주세요.")
                .setNegativeButtonText("취소")
                .setDeviceCredentialAllowed(false)
                .build();

        //  사용자가 다른 인증을 이용하길 원할 때 추가하기

        Button biometricLoginButton = findViewById(R.id.buttonAuthWithFingerprint);
        biometricLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                biometricPrompt.authenticate(promptInfo);
            }
        });
    }

    class CustomTask extends AsyncTask<String, Void, String> {
        String sendMsg, receiveMsg;

        public URL getURL(String str){
            Log.i("getURL() 실행", "");
            URL url = null;
            try {
                switch (str) {
                    case "serial": // 안드로이드에서 웹서버로 시리얼값 넘기기
                        url = new URL("http://192.168.0.3:8119/dl_proj/AtoW_Serial.jsp");
                        break;
                    case "password": // 웹서버로 비밀번호 넘기기
                        url = new URL("http://192.168.0.3:8119/dl_proj/AtoW_Password.jsp");
                        break;
                    case "info1": case "info2": // 데이터베이스에서 아이피 포트정보 가져오기
                        url = new URL("http://192.168.0.3:8119/dl_proj/AtoW_Userinfo.jsp");
                        break;
                    case "serial2": // 안드로이드에서 웹서버로 시리얼값 넘기기
                        url = new URL("http://192.168.0.3:8119/dl_proj/AtoW_CheckAdmin.jsp");
                        break;
                        default: break;
                }
            }catch (MalformedURLException e){
                e.printStackTrace();
            }
            Log.i("getURL() 실행종료", "");
            return url;
        }

        public String getSendMsg(String str){
            Log.i("getSendMsg() 실행", "");
            String returns = null;
            switch (str) {
                case "serial": case "info1": case "serial2": // 안드로이드에서 웹서버로 시리얼값 넘기기
                    returns = "Serial";
                    break;
                case "password": case "info2": // 웹서버로 비밀번호 넘기기
                    returns = "Password";
                    break;
                default: break;
            }
            Log.i("getSendMsg() 실행종료", "");
            return returns;
        }
        @Override
        // doInBackground의 매개값이 문자열 배열인데요. 보낼 값이 여러개일 경우를 위해 배열로 합니다.
        protected String doInBackground(String... strings) {
            try {
                String str;
                URL url = getURL(strings[1]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");//데이터를 POST 방식으로 전송합니다.
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                sendMsg = getSendMsg(strings[1])+"="+strings[0];//보낼 정보인데요. GET방식으로 작성합니다. ex) "id=rain483&pwd=1234";
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
