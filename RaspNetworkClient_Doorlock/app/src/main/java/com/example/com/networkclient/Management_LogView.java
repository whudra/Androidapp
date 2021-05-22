package com.example.com.networkclient;

import android.app.Activity;
import android.bluetooth.BluetoothManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class Management_LogView extends Activity {
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_management_logview);

        final ArrayList<String> items1 = new ArrayList<String>();
        final ArrayList<String> items2 = new ArrayList<String>();
        final ArrayList<String> items3 = new ArrayList<String>();
        final ArrayAdapter adapter1 = new ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, items1);
        final ArrayAdapter adapter2 = new ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, items2);
        final ArrayAdapter adapter3 = new ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, items3);
        final ListView s_list = (ListView)findViewById(R.id.s_list);
        final ListView d_list = (ListView)findViewById(R.id.d_list);
        final ListView p_list = (ListView)findViewById(R.id.p_list);

        final ScrollView scv = (ScrollView)findViewById(R.id.scv);

        /*View.OnTouchListener touchListener = new View.OnTouchListener() {
            boolean dispatched = false;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (v.equals(s_list) && !dispatched) {
                    dispatched = true;
                    d_list.dispatchTouchEvent(event);
                    p_list.dispatchTouchEvent(event);
                } else if (v.equals(d_list) && !dispatched) {
                    dispatched = true;
                    s_list.dispatchTouchEvent(event);
                    p_list.dispatchTouchEvent(event);
                } else if (v.equals(p_list) && !dispatched) {
                    dispatched = true;
                    s_list.dispatchTouchEvent(event);
                    d_list.dispatchTouchEvent(event);
                }// similarly for listViewThree & listViewFour
                dispatched = false;
                return false;
            }
        };

        s_list.setOnTouchListener(touchListener);
        d_list.setOnTouchListener(touchListener);
        p_list.setOnTouchListener(touchListener); */

        int totalHeight = 0;

        s_list.setAdapter(adapter1);
        d_list.setAdapter(adapter2);
        p_list.setAdapter(adapter3);
        String rs = null;

        try {
            CustomTask task = new CustomTask();
            rs = task.execute().get();
        }catch (Exception e){
            e.printStackTrace();
        }

        String[] lines = rs.split("/");
        for(String line : lines) {
            items1.add(line.toString().split(",")[0]);
            items2.add(line.toString().split(",")[1]);
            items3.add(line.toString().split(",")[2]);
        }
        setListViewHeightBasedOnItems(s_list);
        setListViewHeightBasedOnItems(d_list);
        setListViewHeightBasedOnItems(p_list);
    }

    class CustomTask extends AsyncTask<String, Void, String> {
        String sendMsg, receiveMsg;
        @Override
        // doInBackground의 매개값이 문자열 배열인데요. 보낼 값이 여러개일 경우를 위해 배열로 합니다.
        protected String doInBackground(String... strings) {
            try {
                String str;
                URL url = new URL("http://192.168.0.3:8119/dl_proj/AtoW_LogView.jsp");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");//데이터를 POST 방식으로 전송합니다.
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                sendMsg = "View=1" ;//보낼 정보인데요. GET방식으로 작성합니다. ex) "id=rain483&pwd=1234";
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

    private void setListViewHeightBasedOnItems(ListView listView) {
        // Get list adpter of listview;
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) return;

        int numberOfItems = listAdapter.getCount();

        // Get total height of all items.
        int totalItemsHeight = 0;
        for (int itemPos = 0; itemPos < numberOfItems; itemPos++) {
            View item = listAdapter.getView(itemPos, null, listView);
            item.measure(0, 0);
            totalItemsHeight += item.getMeasuredHeight();
        }

        // Get total height of all item dividers.
        int totalDividersHeight = listView.getDividerHeight() * (numberOfItems - 1);

        // set list height.
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalItemsHeight + totalDividersHeight;
        listView.setLayoutParams(params);
        listView.requestLayout();
    }
}