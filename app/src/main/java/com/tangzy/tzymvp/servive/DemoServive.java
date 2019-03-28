package com.tangzy.tzymvp.servive;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.tangzy.tzymvp.util.Logger;

public class DemoServive extends Service {
    Thread thread =new Thread(() -> {
        while (true){
            try {
                Thread.sleep(1000);
                Logger.d("tangzy", "sleep");

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    });
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Logger.d("tangzy", "onBind");
//        thread.start();
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.d("tangzy", "onStartCommand");
        thread.start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.d("tangzy", "onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.d("tangzy", "Service - onDestroy");
    }
}
