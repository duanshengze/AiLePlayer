package com.superdan.app.aileplayer.ui;

import android.os.Bundle;

import com.superdan.app.aileplayer.utils.LogHelper;

/**
 * Created by Administrator on 2016/4/8.
 * Main activity for the music player.
 * This class hold the MediaBrowser and the MediaController instances. It will create a MediaBrowser
 * when it is created and connect/disconnect on start/stop. Thus, a MediaBrowser will be always
 * connected while this activity is running.
 *音乐播放器的主类
 * 这个播放器包含媒体浏览器和媒体控制器实例，当它被创建时将创建媒体浏览器并且在onStart（）和onStop（）方法类连接或者断开
 * 因此，媒体浏览器将一直连接当activity在运行时
 */
public class MusicPlayerActivity extends BaseActivity {

    private static final String TAG= LogHelper.makeLogTag(MusicPlayerActivity.class);
    private static final String SAVED_MEDIA_ID="com.superdan.app.aileplayer.MEDIA_ID";
    private static  final String FRAGMENT_TAG="aile_list_container";

    public static final String EXTRA_START_FULLSCREEN="com.superdan.app.aileplayer.EXTRA_START_FULLSCREEN";


    public static final String EXTRA_CURRENT_MEDIA_DESCRIPTION="com.superdan.app.aileplayer.EXTRA_CURRENT_MEDIA_DESCRIPTION";

    private  Bundle mVoiceSearchParams;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogHelper.d(TAG,"Activity onCreate");

    }
}
