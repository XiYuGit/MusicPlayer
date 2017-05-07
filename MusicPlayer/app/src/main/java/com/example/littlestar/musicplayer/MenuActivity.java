package com.example.littlestar.musicplayer;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;

/**
 * Created by little star on 2016/10/7.
 */
public class MenuActivity  extends Activity implements View.OnClickListener,AdapterView.OnItemClickListener,AbsListView.OnScrollListener{
    private ImageButton imageButton;
    private static int[] _ids;
    private static int[] _album_id;
    private static int[] durations;
    private static String[] _titles;
    private static String[] albums;
    private static String[] artists;
    private static String[] _path;
    private static ListView listView; //列表框，用来展示音乐名称
    private Cursor c;  //用来查询媒体库
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_menu);
//        this.requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
//        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//去掉信息栏
        imageButton = (ImageButton) findViewById(R.id.imageButton);
        listView = (ListView) findViewById(R.id.listView);
        //设置监听
        imageButton.setOnClickListener(this);
        listView.setOnItemClickListener(this);
        listView.setOnScrollListener(this);
       // ActivityCompat.requestPermissions(MenuActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
    }
    public void setListData() {
        //查询数据库
        c = this.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.ALBUM,
                        MediaStore.Audio.Media.DATA,
                        MediaStore.Audio.Media.ALBUM_ID
                }, null, null, null);
        c.moveToFirst(); //将游标放到第一处
        int count = c.getCount();//得到媒体库中音乐的个数
        //初始化数组
        _ids = new int[count];
        _album_id = new int[count];
        durations = new int[count];
        _titles = new String[count];
        albums = new String[count];
        artists = new String[count];
        _path = new String[count];
        //为数组赋值
        for (int i = 0; i < count; i++) {
            _ids[i] = c.getInt(3);
            _album_id[i] = c.getInt(6);
            durations[i] = c.getInt(1);
            _titles[i] = c.getString(0).trim();
            albums[i] = c.getString(4).trim();
            artists[i] = c.getString(2).trim();
            _path[i] = c.getString(5).trim();
            c.moveToNext();
        }
        // 给列表加载数据
        listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, _titles));
    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.imageButton:
                finish();
                break;

        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setListData();
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        int text=position;
        Intent data = new Intent();
        data.putExtra("data", text);
        setResult(2, data);
        finish();
    }
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }
}

