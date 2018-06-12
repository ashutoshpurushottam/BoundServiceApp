package com.eigendaksh.boundserviceapp;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.Date;

/**
 * Created by Ashutosh Purushottam on 12/06/18.
 * Eigendaksh Development Studio
 * ashu@eigendaksh.com
 */
public class PlayerService extends Service {

    private static final String TAG = PlayerService.class.getSimpleName();
    public static final String CUSTOM_ACTION = "com.eigendaksh.boundserviceapp.CUSTOM_ACTION";
    public static final String DATE_TIME_KEY = "key_datetime";


    private MediaPlayer mPlayer;
    private IBinder mBinder = new LocalBinder();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate called");
        mPlayer = MediaPlayer.create(this, R.raw.jingle);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mPlayer.setOnCompletionListener(mp -> {
            Intent localIntent = new Intent(CUSTOM_ACTION);
            localIntent.putExtra(DATE_TIME_KEY, new Date().toString());
            LocalBroadcastManager.getInstance(PlayerService.this).sendBroadcast(localIntent);
            stopSelf();
        });
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind called");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind called");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy called");
        mPlayer.release();
    }

    class LocalBinder extends Binder {
        public PlayerService getService() {
            return PlayerService.this;
        }
    }

    // Client methods
    public void play() {
        mPlayer.start();
    }

    public void pause() {
        mPlayer.pause();
    }

    public boolean isPlaying() {
        return mPlayer.isPlaying();
    }
}
