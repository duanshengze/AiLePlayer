package com.superdan.app.aileplayer.ui;

import android.app.Activity;
import android.app.UiModeManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import com.superdan.app.aileplayer.utils.LogHelper;

/**
 * Created by Administrator on 2016/4/27.
 */
public class NowPlayingActivity extends Activity {
    private static final String TAG= LogHelper.makeLogTag(NowPlayingActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogHelper.d(TAG,"onCreate");
        Intent newIntent;
        UiModeManager uiModeManager=(UiModeManager)getSystemService(UI_MODE_SERVICE);
        if(uiModeManager.getCurrentModeType()== Configuration.UI_MODE_TYPE_TELEVISION){

            LogHelper.d(TAG,"Running on a TV Device");
//本应该是TvPlaybackActivity
            newIntent=new Intent(this,MusicPlayerActivity.class);

        }else {
            LogHelper.d(TAG,"Running on a non-TV Device");
            newIntent=new Intent(this,MusicPlayerActivity.class);
        }
        startActivity(newIntent);
        finish();
    }
}
