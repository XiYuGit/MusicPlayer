package com.example.littlestar.musicplayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Created by little star on 2016/10/6.
 */
public class Myservice extends Service{
    private MyReciver receiver;
    private int[] _ids;
    private int[] _album_id;
    private int[] durations;
    private String[] _titles;
    private String[] albums;
    private String[] artists;
    private static String[] _path;
    private ContentResolver cr;        //获得共享数据库
    private Cursor mCursor;            //歌曲信息列表
    private ListView listView; //列表框，用来展示音乐名称
    private Cursor c;  //用来查询媒体库
     static MediaPlayer mediaPlayerr =new MediaPlayer();

//    //定义个过滤器；
//    private IntentFilter intentFilter;
//    private Handler handler=new Handler();
//    static  int content =0;
    int count;

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
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate() {
        setListData();
        receiver=new MyReciver();
        IntentFilter filter=new IntentFilter();
        filter.addAction("ACTION_MY");
        filter.addAction("musicPostion");
        registerReceiver(receiver, filter);
//        MainActivity m=new MainActivity();
////        Log.i("service       content", m.opera + "");
////        Log.i("service       content", m.content + "");
//        //content=m.opera;
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }
    @Override
    public void onDestroy() {
        unregisterReceiver(receiver);
        receiver=null;
        super.onDestroy();
    }
       void play(int id){
        mediaPlayerr.reset();
        Log.i("play       content",id+"");
        try {
            mediaPlayerr.setDataSource(_path[id]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            mediaPlayerr.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayerr.start();
           Intent intent=new Intent();
           intent.setAction("musiclenght");
           intent.putExtra("myservicemusiclength", mediaPlayerr.getDuration());
           Log.i("myservice         length",intent.getIntExtra("myservicemusiclength",1)+"");
           sendBroadcast(intent);
//        Intent intent=new Intent();
//        intent.setAction("musiclenght");
//        intent.putExtra("musiclenght", mediaPlayer.getDuration());
//        sendBroadcast(intent);
//        Runnable Postion=new Runnable() {
//            @Override
//            public void run() {
//                Intent intent=new Intent();
//                intent.setAction("musicPosition");
//                intent.putExtra("musicPosition", mediaPlayer.getDuration());
//                sendBroadcast(intent);
//                handler.
//            }
//        }
//
//        intent.setAction("musicPostion");
//        intent.putExtra("musicPostion",  mediaPlayer.getDuration());

    }
 private  class MyReciver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            play(intent.getIntExtra("id", 1));
        }
    }


}

