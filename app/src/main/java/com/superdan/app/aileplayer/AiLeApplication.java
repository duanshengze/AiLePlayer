package com.superdan.app.aileplayer;

import android.app.Application;

import com.google.android.libraries.cast.companionlibrary.cast.CastConfiguration;
import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.superdan.app.aileplayer.ui.FullScreenPlayerActivity;

/**
 * Created by Administrator on 2016/4/22.
 */
public class AiLeApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        String applicationId=getResources().getString(R.string.cast_application_id);
        VideoCastManager.initialize(getApplicationContext(),

                new CastConfiguration.Builder(applicationId)
                    .enableWifiReconnection()
                    .enableAutoReconnect()
                    .enableDebug()
                .setTargetActivity(FullScreenPlayerActivity.class)
                .build()

                );
    }
}
