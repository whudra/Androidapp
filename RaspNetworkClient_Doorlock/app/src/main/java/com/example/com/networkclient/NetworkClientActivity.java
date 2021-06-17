package com.example.com.networkclient;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Message;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

//import java.io.IOException;
//import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;


public class NetworkClientActivity extends Activity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_network_client, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private static final String TAG = "SensorQuery";
    static String serial;

    final static int SERVER_SELECT = 2001;
    static int SERVER_PORT = 8888;

    final static String  PREF_FILE_NAME = "ServerInfo";
    final static String  PREF_KEY_SERVERIP = "ServerIp";

    int  phoneID = 1; // phone id 1~128
    SharedPreferences prefs;
    static boolean s_flag = false;

    String   ServerIP;

    Timer QuerySensorTimer;
    byte[]  packet;

    NetManager NetMgr;

    TextView NetStatus;
    TextView DoorStatus;

    final static int DOOR_CLOSE = 0;
    final static int DOOR_OPEN = 1;

    byte nSendDoorVal;

    boolean  connectingActionDoneFlag = false;

    int nSensingVal = 0;
    int nSensingVal1 = 0;
    int nSensingVal2 = 0;

//    private OutputStream outs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_client);

        Intent it = getIntent();
        serial = it.getStringExtra("2");
        String msg = it.getStringExtra("1");
        String[] s_msg = msg.split("/");

        NetMgr = new NetManager();
        NetMgr.setRxHandler(mNetHandler);

        packet = new byte[100];

        // get server ip , port
        prefs = getSharedPreferences(PREF_FILE_NAME,MODE_PRIVATE);
        ServerIP = prefs.getString(PREF_KEY_SERVERIP, "192.168.10.56");

        SERVER_PORT = Integer.parseInt(s_msg[1]);
        ServerIP = s_msg[0];

        // Server IP와 Server Port를 Activity에 표시
        disSeverSet(ServerIP, SERVER_PORT);
        Log.d(TAG, "ServerIP:" + ServerIP);
        Log.d(TAG, "ServerPort:" + SERVER_PORT);


        NetStatus = (TextView)findViewById(R.id.textViewNetStatus);
        DoorStatus = (TextView)findViewById(R.id.textViewDoorStatus);

        displaySensingVal(nSensingVal);

        Button btn = (Button)findViewById(R.id.buttonServiceStart);
        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                // UI 버튼 클릭 가능
                arg0.setClickable(false);
                // 버튼 활성화
                arg0.setEnabled(false);

                NetMgr.setIpAndPort(ServerIP, SERVER_PORT);
                NetMgr.startThread();

                connectingActionDoneFlag = true;
                // set Timer 4초후 실행, 1초마다 반복, SendCmd()실행
                startQuerySensorTimer();
            }
        });

        btn = (Button)findViewById(R.id.buttonServerSet);
        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                serverSel();
            }
        });

        Switch sw = (Switch)findViewById(R.id.switchChangeDoor);
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    changeDoorStatus(DOOR_OPEN);
                    s_flag = true;
                }
                else {
                    changeDoorStatus(DOOR_CLOSE);
                }
            }
        });
    }

    public void serverSel()
    {
        // Intent로 다른 Activity(AnotherActivity.class)를 실행시킬 때,
        Intent intent = new Intent(this, ServerSetActivity.class);

        // AnotherActivity에 데이터를 전달할 때 사용하는 putExtra
        // AnotherActivity에서는 getExtras()를 이용 데이터를 수신 - 보통 onCreate() 매서드에 구현
        intent.putExtra(ServerSetActivity.SERVER_IP, ServerIP);
        startActivityForResult(intent, SERVER_SELECT);
    }
    private void startQuerySensorTimer()
    {
        QuerySensorTimer = new Timer();
        QuerySensorTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                SendCmd();
                Log.d(TAG, "Send SensorQuery1");
            }

        }, 4000, 1000); // schedule(TimerTask task , long delay , long period)
    }
    private void stopQuerySensorTimer()
    {
        QuerySensorTimer.cancel();
        QuerySensorTimer.purge();
    }

    final static int PKT_INDEX_STX = 0;
    final static int PKT_INDEX_CMD = 1;
//    final static int PKT_INDEX_DATA = 2;
    final static int PKT_INDEX_DATA1 = 2;
    final static int PKT_INDEX_DATA2 = 3;
    final static int PKT_INDEX_ETX = 4;
    final static int PKT_INDEX_SERIAL = 5;

    final static byte PKT_STX = 0x01;
    final static byte PKT_ETX = 0x05;

    // data cmd
    final static int CMD_SENSOR_REQ = 0x10;
    final static int CMD_SENSOR_RES = 0x90;

    private int unSignedByteToInt(byte value)
    {
        int nTemp;
        if ( value >= 0)
            nTemp = (int)value;
        else
            nTemp = (int)value + 256;
        return nTemp;
    }
    private int SendCmd()
    {
        if ( NetMgr.getNetStatus() != NetManager.NET_CONNECTED)
        {
            return -1;
        }
        if(s_flag){
            s_flag = false;
            String serial2 = serial+"\0";
            NetMgr.SendData(serial2.getBytes(), serial2.length());
        }
        packet[PKT_INDEX_STX] = PKT_STX;
        packet[PKT_INDEX_CMD] = CMD_SENSOR_REQ;
//        packet[PKT_INDEX_DATA] = 0x00;
        packet[PKT_INDEX_DATA1] = nSendDoorVal;
        packet[PKT_INDEX_DATA2] = 0x00;
        packet[PKT_INDEX_ETX] = PKT_ETX;

        return NetMgr.SendData(packet,  5);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if ( requestCode == SERVER_SELECT)
        {
            if (resultCode == RESULT_OK)
            {
                ServerIP = data.getStringExtra(ServerSetActivity.SERVER_IP);

                Log.d(TAG,"setting ServerIP:" + ServerIP);
                phoneID = data.getIntExtra(ServerSetActivity.PHONEID,1);

                disSeverSet(ServerIP,SERVER_PORT);
                // save
                SharedPreferences.Editor ed = prefs.edit();
                ed.putString(PREF_KEY_SERVERIP, ServerIP);

                ed.commit();

            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
    private void disSeverSet(String ip , int port)
    {
        TextView tv = (TextView)findViewById(R.id.textViewServerIP);
        tv.setText("Server IP:" + ip + ", PORT:" + Integer.toString(port));
    }
    private Handler mNetHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);

            switch(msg.what)
            {
                case NetManager.HANDLE_RXCMD:
                    doRxCmd(msg.getData());
                    break;
                case NetManager.HANDLE_NETSTATUS:
                    doNetStatus(msg.arg1);
                    break;
            }
        }
    };

    private void displaySensingVal(int value)
    {
        //textViewSensorValue
        TextView sensor = (TextView)findViewById(R.id.textViewSensingVal);
        sensor.setText("Sensing Value : " + value);
    }
    private void doRxCmd(Bundle data)
    {
        int len = data.getInt(NetManager.RX_LENGHT);
        if ( len < 5)
            return;
        byte[] dataArr = data.getByteArray(NetManager.RX_DATA);

        if (unSignedByteToInt(dataArr[PKT_INDEX_STX]) != PKT_STX)
        {
            Log.d(TAG,"doRxCmd - PKT_STX fail");
            return ;
        }
        Log.d(TAG,"dataArr[PKT_INDEX_CMD] : " + unSignedByteToInt(dataArr[PKT_INDEX_CMD]));
        Log.d(TAG,"CMD_SENSOR_RES : " + CMD_SENSOR_RES);
        if (unSignedByteToInt(dataArr[PKT_INDEX_CMD]) != CMD_SENSOR_RES)
        {
            Log.d(TAG,"doRxCmd - CMD_SENSOR_RES fail");
            return ;
        }
        if (unSignedByteToInt(dataArr[PKT_INDEX_ETX]) != PKT_ETX)
        {
            Log.d(TAG,"doRxCmd - PKT_ETX fail");
            return;
        }

        // SensingVal query
//        nSensingVal1 = dataArr[PKT_INDEX_DATA];
        nSensingVal1 = unSignedByteToInt(dataArr[PKT_INDEX_DATA1]);
        nSensingVal2 = unSignedByteToInt(dataArr[PKT_INDEX_DATA2]);

        Log.d(TAG, "nSensingVal1" + nSensingVal1);
        Log.d(TAG, "nSensingVal2" + nSensingVal2);

        nSensingVal = (nSensingVal2*256)+nSensingVal1;
        Log.d(TAG, "nSensingVal" + nSensingVal);

        doDoorStatus(nSensingVal);

//        displaySensingVal(nSensingVal1);
        displaySensingVal(nSensingVal);
        }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        Log.d(TAG,"OnDestroy");
        if (connectingActionDoneFlag) {
            NetMgr.stopThread();
        }
        super.onDestroy();
    }
    @Override
    protected void onRestart() {
        // TODO Auto-generated method stub
        super.onRestart();
        Log.d(TAG, "onRestart");
        if (connectingActionDoneFlag)
        {
            startQuerySensorTimer();
        }
    }

    @Override
    protected void onStop() {
        Log.d(TAG,"onStop");
        // TODO Auto-generated method stub
        if (connectingActionDoneFlag)
        {
            stopQuerySensorTimer();
        }
        super.onStop();
    }

    private void doNetStatus(int status)
    {
        switch(status)
        {
            case NetManager.NET_NONE:
                NetStatus.setText("Unkwown Network Status");
                break;
            case NetManager.NET_DISCONNECT:
                NetStatus.setText("Disconnected");
                break;
            case NetManager.NET_CONNECTING:
                NetStatus.setText("Connecting ...");
                break;
            case NetManager.NET_CONNECTED:
                NetStatus.setText("Connected");
                break;
        }
    }

    private void doDoorStatus(int status)
    {
        Switch swc = (Switch)findViewById(R.id.switchChangeDoor);

        swc.setEnabled(true);

        switch(status)
        {
            case DOOR_CLOSE:
                DoorStatus.setText("Door Closed");
                swc.setChecked(false);
                break;
            case DOOR_OPEN:
                DoorStatus.setText("Door Opened");
                swc.setChecked(true);
                break;
        }
    }
    private int changeDoorStatus(int txCmd)
    {
        CustomTask task = new CustomTask();
        switch(txCmd)
        {
            case DOOR_CLOSE:
                DoorStatus.setText("Door Closed");
                //task.execute("Door","Closed","Serial",serial);
                nSendDoorVal = DOOR_CLOSE;
                break;
            case DOOR_OPEN:
                DoorStatus.setText("Door Opened");
                //task.execute("Door","Opened","Serial",serial);
                nSendDoorVal = DOOR_OPEN;
                break;
        }
        return nSendDoorVal;
    }

    class CustomTask extends AsyncTask<String, Void, String> {
        String sendMsg, receiveMsg;

        @Override
        // doInBackground의 매개값이 문자열 배열인데요. 보낼 값이 여러개일 경우를 위해 배열로 합니다.
        protected String doInBackground(String... strings) {
            try {
                String str;
                URL url = new URL("http://192.168.86.252:9002/dl_proj/AtoW_Door.jsp");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");//데이터를 POST 방식으로 전송합니다.
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                sendMsg = strings[0] + "=" + strings[1] + "&" + strings[2] + "=" + strings[3];//보낼 정보인데요. GET방식으로 작성합니다. ex) "id=rain483&pwd=1234";
                System.out.println("센드 : " + sendMsg);
                //회원가입처럼 보낼 데이터가 여러 개일 경우 &로 구분하여 작성합니다.
                osw.write(sendMsg);//OutputStreamWriter에 담아 전송합니다.
                osw.flush();
                //jsp와 통신이 정상적으로 되었을 때 할 코드들입니다.
                if (conn.getResponseCode() == conn.HTTP_OK) {
                    InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "EUC-KR");
                    BufferedReader reader = new BufferedReader(tmp);
                    StringBuffer buffer = new StringBuffer();
                    //jsp에서 보낸 값을 받겠죠?
                    while ((str = reader.readLine()) != null) {
                        buffer.append(str);
                    }
                    receiveMsg = buffer.toString();

                } else {
                    Log.i("통신 결과", conn.getResponseCode() + "에러");
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
