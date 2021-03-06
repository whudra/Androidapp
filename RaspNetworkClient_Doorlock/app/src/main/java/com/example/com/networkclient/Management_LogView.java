package com.example.com.networkclient;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class Management_LogView extends Activity implements ListViewBtnAdapter.ListBtnClickListener {
    boolean toggle = true;
    static int count = 0;
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_management_logview);

        final ArrayList<String> items1 = new ArrayList<String>();
        final ArrayList<String> items2 = new ArrayList<String>();
        final ArrayList<String> items3 = new ArrayList<String>();
        final ArrayList<String> items4 = new ArrayList<String>();
        final ArrayAdapter adapter1 = new ArrayAdapter(this, R.layout.listview_text, items1);
        final ArrayAdapter adapter2 = new ArrayAdapter(this, R.layout.listview_text, items2);
        final ArrayAdapter adapter3 = new ArrayAdapter(this, R.layout.listview_text, items3);
        final ListViewBtnAdapter adapter4;
        final ListView s_list = (ListView)findViewById(R.id.s_list);
        final ListView d_list = (ListView)findViewById(R.id.d_list);
        final ListView p_list = (ListView)findViewById(R.id.p_list);
        final ListView v_list = (ListView)findViewById(R.id.view_list);
        ArrayList<ListViewBtnItem> items = new ArrayList<ListViewBtnItem>();

        final ScrollView scv = (ScrollView)findViewById(R.id.scv);

        int totalHeight = 0;

        s_list.setAdapter(adapter1);
        d_list.setAdapter(adapter2);
        p_list.setAdapter(adapter3);

        String rs = null;
        try {
            toggle = true;
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

        count = items1.size();
        loadItemsFromDB(items);
        adapter4 = new ListViewBtnAdapter(this, R.layout.list_btn_item, items, this);
        v_list.setAdapter(adapter4);

        setListViewHeightBasedOnItems(s_list);
        setListViewHeightBasedOnItems(d_list);
        setListViewHeightBasedOnItems(p_list);
        setListViewHeightBasedOnItems(v_list);
    }

    @Override
    public void onListBtnClick(int position){
        Toast.makeText(this, Integer.toString(position+1), Toast.LENGTH_SHORT).show();
        /*String res = null;
        try {
            toggle = false;
            CustomTask task3 = new CustomTask();
            res = task3.execute(position+"").get();
        }catch (Exception e){
            e.printStackTrace();
        }*/

        View dialogView = (View) View.inflate(Management_LogView.this, R.layout.dialog, null);
        AlertDialog.Builder dlg = new AlertDialog.Builder(Management_LogView.this);
        ImageView image = (ImageView) dialogView.findViewById(R.id.imageViews);
        dlg.setView(dialogView);
        dlg.setNegativeButton("??????", null);
        String url = "http://192.168.86.252:9002/dl_proj/AtoW_Photo1.jsp?parameter=" + (position+1);

        ImageLoadTask task = new ImageLoadTask(url,image);
        task.execute();
        dlg.show();
    }

    class CustomTask extends AsyncTask<String, Void, String> {
        String sendMsg, receiveMsg;
        @Override
        // doInBackground??? ???????????? ????????? ???????????????. ?????? ?????? ???????????? ????????? ?????? ????????? ?????????.
        protected String doInBackground(String... strings) {
            try {
                String str;
                URL url;
                if(toggle) url = new URL("http://192.168.86.252:9002/dl_proj/AtoW_LogView.jsp");
                else url = new URL("http://192.168.86.252:9002/dl_proj/AtoW_Photos.jsp");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");//???????????? POST ???????????? ???????????????.
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                if(toggle) sendMsg = "View=1" ;//?????? ???????????????. GET???????????? ???????????????. ex) "id=rain483&pwd=1234";
                else sendMsg = "imageID=" + strings[0];
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

    private void setListViewHeightBasedOnItems(ListView listView) {
        // Get list adpter of listview;
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) return;

        int numberOfItems = listAdapter.getCount();
        Log.i("nisssss", ""+numberOfItems);

        // Get total height of all items.
        int totalItemsHeight = 0;
        for (int itemPos = 0; itemPos < numberOfItems; itemPos++) {
            View item = listAdapter.getView(itemPos, null, listView);
            item.measure(0, 0);
            totalItemsHeight += item.getMeasuredHeight();
        }
        Log.i("totalh", ""+totalItemsHeight);

        // Get total height of all item dividers.
        int totalDividersHeight = listView.getDividerHeight() * (numberOfItems-1);
        Log.i("totaldh", ""+totalDividersHeight);

        // set list height.
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = (totalItemsHeight + totalDividersHeight) * 2;
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    public boolean loadItemsFromDB(ArrayList<ListViewBtnItem> list) {
        ListViewBtnItem item ;
        int i;

        if (list == null) {
            list = new ArrayList<ListViewBtnItem>() ;
        }
        Log.i("" + count, "dd");
        for(i = 1; i < count+1; i++) {
            // ????????? ??????.
            item = new ListViewBtnItem();
            list.add(item);
        }
        return true ;
    }

    public class ImageLoadTask extends AsyncTask<Void,Void, Bitmap> {

        private String urlStr;
        private ImageView imageView;
        private HashMap<String, Bitmap> bitmapHash = new HashMap<String, Bitmap>();

        public ImageLoadTask(String urlStr, ImageView imageView) {
            this.urlStr = urlStr;
            this.imageView = imageView;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            Bitmap bitmap = null;
            try {
                if (bitmapHash.containsKey(urlStr)) {
                    Bitmap oldbitmap = bitmapHash.remove(urlStr);
                    if(oldbitmap != null) {
                        oldbitmap.recycle();
                        oldbitmap = null;
                    }
                }
                URL url = new URL(urlStr);
                bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());

                bitmapHash.put(urlStr,bitmap);

            } catch (Exception e) {
                e.printStackTrace();
            }

            return bitmap;
        }
        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);

            imageView.setImageBitmap(bitmap);
            imageView.invalidate();
        }
    }
}
