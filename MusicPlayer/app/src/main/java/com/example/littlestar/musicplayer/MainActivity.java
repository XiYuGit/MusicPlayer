package com.example.littlestar.musicplayer;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class MainActivity extends Activity implements View.OnClickListener{
    ImageButton Play;
    ImageButton Last;
    ImageButton Next;
    ImageButton Menu;
    TextView tv;
    private long[] _ids;
    private MyReciver receiver;
    private long[] _album_id;
    private int[] durations;
    private String[] _titles;
    private String[] albums;
    private String[] artists;
    private ImageView imageView;
    private String[] _path;
    private ContentResolver cr;        //获得共享数据库
    private Cursor mCursor;            //歌曲信息列表
    private ListView listView; //列表框，用来展示音乐名称
    private Cursor c;  //用来查询媒体库
    private SeekBar seekBar;
    private MediaPlayer mediaPlayer =new MediaPlayer();
    Myservice ms=new Myservice();
    int content =1;
    int length=2000;
    int count;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        Play = (ImageButton) findViewById(R.id.play);
        Last = (ImageButton) findViewById(R.id.last);
        Next = (ImageButton) findViewById(R.id.next);
        Menu = (ImageButton) findViewById(R.id.menu);
        tv = (TextView) findViewById(R.id.songname);
        imageView=(ImageView)findViewById(R.id.imageView);
        seekBar= (SeekBar) findViewById(R.id.seekBar);
        Play.setOnClickListener(this);
        Last.setOnClickListener(this);
        Next.setOnClickListener(this);
        Menu.setOnClickListener(this);
        tv.setOnClickListener(this);
        seekBar.setOnSeekBarChangeListener(new MySeekBarListener());
        Intent startIntent=new Intent(this,Myservice.class);
        startService(startIntent);
       // ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
       // ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        receiver=new MyReciver();
        IntentFilter filter=new IntentFilter();
        filter.addAction("musiclenght");
        filter.addAction("ACTION_MY");
        filter.addAction("musicPostion");
        registerReceiver(receiver, filter);

    }
    private final class MySeekBarListener implements SeekBar.OnSeekBarChangeListener{
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
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
        count = c.getCount();//得到媒体库中音乐的个数
        //初始化数组
        _ids = new long[count];
        _album_id = new long[count];
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
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.play:
                Intent intent=new Intent();
                intent.setAction("ACTION_MY");
                intent.setAction("musicPostion");
                intent.putExtra("id", content);
                intent.putExtra("musicPostion", "666");
                sendBroadcast(intent);
                break;
            case R.id.last:
                content=content-1;
                if(content<0) content=count;
                Play.setImageResource(R.drawable.pase);
                tv.setText(_titles[content]);
                imageView.setImageBitmap(getMusicBitemp(this, _ids[content], _album_id[content]));
                Intent intentl=new Intent();
                intentl.setAction("ACTION_MY");
                intentl.setAction("musicPostion");
                intentl.putExtra("id", content);
                intentl.putExtra("musicPostion", "666");
                sendBroadcast(intentl);
            case R.id.next:
                content=content+1;
                if(content>count) content=0;
                Play.setImageResource(R.drawable.pase);
                tv.setText(_titles[content]);
                imageView.setImageBitmap(getMusicBitemp(this, _ids[content], _album_id[content]));
                Intent intent2=new Intent();
                intent2.setAction("ACTION_MY");
                intent2.setAction("musicPostion");
                intent2.putExtra("id", content);
                intent2.putExtra("musicPostion", "666");
                sendBroadcast(intent2);
                break;
            case R.id.menu:
                Intent intent1=new Intent(MainActivity.this,MenuActivity.class);
                startActivityForResult(intent1, 1);
                break;
        }
    }
    private   Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
    public  Bitmap getMusicBitemp(Context context, long songid, long albumid) {   // 将MP3里图片读取出来
        Bitmap bm = null;
        if (albumid < 0 && songid < 0) {       // 专辑id和歌曲id小于0说明没有专辑、歌曲，并抛出异常
            throw new IllegalArgumentException("Must specify an album or a song id");
        }
        try {
            if (albumid < 0) {
                Uri uri = Uri.parse("content://media/external/audio/media/" + songid + "/albumart");
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
                if (pfd != null) {
                    FileDescriptor fd = pfd.getFileDescriptor();
                    bm = BitmapFactory.decodeFileDescriptor(fd);
                }
            } else {
                Uri uri = ContentUris.withAppendedId(sArtworkUri, albumid);
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
                if (pfd != null) {
                    FileDescriptor fd = pfd.getFileDescriptor();
                    bm = BitmapFactory.decodeFileDescriptor(fd);
                } else {
                    return null;
                }
            }
        } catch (FileNotFoundException ex) {
        }
        return bm;
    }
    @Override   //第二个页面返回值  requestCode请求的标志resultcode第二个页面返回的标志data是第二个页面回传数据
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try{
            content=data.getIntExtra("data",1);
        }catch (Exception e){
                return ;
        }
       // Log.i("activity       content",opera+"");
        Play.setImageResource(R.drawable.pase);
        tv.setText(_titles[content]);
        imageView.setImageBitmap(getMusicBitemp(this, _ids[content], _album_id[content]));
        Intent intent=new Intent();
        intent.setAction("ACTION_MY");
        intent.setAction("musicPostion");
        intent.putExtra("id", content);
        intent.putExtra("musicPostion", "666");
        sendBroadcast(intent);

//        Log.i("seekbarrrrrrrrrrrrrrrrrrrrrr", length + "]]]]");
//        seekBar.setMax(ms.mediaPlayerr.getCurrentPosition());
//        seekBar.getHandler().post(new Runnable() {
//            @Override
//            public void run() {
//                seekBar.setProgress(ms.mediaPlayerr.getCurrentPosition());
//                seekBar.getHandler().postDelayed(this, 1000);
//
//            }
//        });
//        Play.setImageResource(R.drawable.play);
//        tv.setText(_titles[content]);
//        imageView.setImageBitmap(getMusicBitemp(this, _ids[content], _album_id[content]));
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
   public class MyReciver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            length=intent.getIntExtra("myservicemusiclength", 1);
            Log.i("lllleeeeeeeeeeennnnnnnnnnggggggggghhhhhhhhhhttt1", length + "");
            Log.i("lllleeeeeeeeeeennnnnnnnnnggggggggghhhhhhhhhhttt1", intent.getIntExtra("myservicemusiclength", 1) + "");
            seekBar.setMax(ms.mediaPlayerr.getDuration());
            seekBar.getHandler().post(new Runnable() {
                @Override
                public void run() {
                    seekBar.setProgress(ms.mediaPlayerr.getCurrentPosition());
                    seekBar.getHandler().postDelayed(this, 1000);

                }
            });
        }
    }
}
