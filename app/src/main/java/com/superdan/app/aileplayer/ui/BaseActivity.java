package com.superdan.app.aileplayer.ui;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.graphics.BitmapFactory;
import android.media.session.MediaSession;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;

import com.superdan.app.aileplayer.R;
import com.superdan.app.aileplayer.utils.LogHelper;
import com.superdan.app.aileplayer.utils.ResourceHelper;

/**
 * Created by Administrator on 2016/4/9.
 *Base activity for activities that need to show a playback control fragment when media is playing.
 *为当媒体播放时需要显示一个播放器控制fragment的activity们的定义的基类
 */
public class BaseActivity extends  ActionBarCastActivity implements MediaBrowserProvider{
    private static final String TAG= LogHelper.makeLogTag(BaseActivity.class);


    private MediaBrowserCompat mMediaBrowser;

    private PlaybackControlsFragment mControlFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(Build.VERSION.SDK_INT>=21){
            // Since our app icon has the same color as colorPrimary, our entry in the Recent Apps
            // list gets weird. We need to change either the icon or the color
            // of the TaskDescription.
            //由于我们的应用程序图标和colorPrimary一样的颜色，我们再最近应用列表会很奇怪
            //我们需要改变该图标或者任务描述的颜色。
            ActivityManager.TaskDescription taskDesc=new ActivityManager.TaskDescription(
              getTitle().toString(),
                    BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_white),
                    ResourceHelper.getThemeColor(this,R.attr.colorPrimary,android.R.color.darker_gray)
            );
            setTaskDescription(taskDesc);
        }

        // Connect a media browser just to get the media session token. There are other ways
        // this can be done, for example by sharing the session token directly.
        //连接一个媒体浏览只是为了得到一个媒体回话令牌，还有其他方法可以被做，例如通过令牌直接分享
        mMediaBrowser=new MediaBrowserCompat(this,new ComponentName(this,Musi))
    }


    private void connectToSession(MediaSession.Token token){

        MediaControllerCompat mediaController=new MediaControllerCompat(this,token);
        /**
         *Sets a MediaControllerCompat for later retrieval via getSupportMediaController().
         * 设置媒体控制器 之后通过 getSupportMediaController()索引
         On API 21 and later, this controller will be tied to the window of the activity
         and media key and volume events which are received while the Activity is
         in the foreground will be forwarded to the controller and used to invoke transport controls
         or adjust the volume. Prior to API 21, the global handling of media key and volume events
         through an active android.support.v4.media.session.MediaSessionCompat
         and media button receiver will still be respected.
         */

        setSupportMediaController(mediaController);


    }


    private final MediaBrowserCompat.ConnectionCallback mConnectionCallback=new MediaBrowserCompat.ConnectionCallback(){
        @Override
        public void onConnected() {
            LogHelper.d(TAG,"onConnected");


            try{


            }
        }
    };

    @Override
    public MediaBrowserCompat getMediaBrowser() {
        return null;
    }
}
