package com.eigendaksh.boundserviceapp;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private String KEY_IS_PLAYING = "IS_PLAYING";

    private Button playButton;

    private boolean isBound;
    private PlayerService mPlayerService;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            isBound = true;
            PlayerService.LocalBinder localBinder = (PlayerService.LocalBinder) binder;
            mPlayerService = localBinder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String toastMessage = "Jingle completed at " + intent.getStringExtra(PlayerService.DATE_TIME_KEY);
            showToast(toastMessage);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playButton = findViewById(R.id.playButton);
        playButton.setOnClickListener(v -> {
            if (isBound) {
                if (mPlayerService.isPlaying()) {
                    mPlayerService.pause();
                    playButton.setText(R.string.play);
                } else {
                    Intent intent = new Intent(MainActivity.this, PlayerService.class);
                    startService(intent);
                    mPlayerService.play();
                    playButton.setText(R.string.pause);
                }
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, PlayerService.class);
        bindService(intent, mServiceConnection, Service.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Register receiver
        IntentFilter intentFilter = new IntentFilter(PlayerService.CUSTOM_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, intentFilter);

        // Get music status to show appropriate text on button
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isMusicPlaying = preferences.getBoolean(KEY_IS_PLAYING, false);
        if(isMusicPlaying) {
            playButton.setText(R.string.pause);
        } else {
            playButton.setText(R.string.play);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Unregister Broadcast
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);

        // Save current music status in SharedPreferences
        if(mPlayerService != null) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(KEY_IS_PLAYING, mPlayerService.isPlaying());
            editor.apply();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isBound) {
            unbindService(mServiceConnection);
        }
    }

    private void showToast(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
