package com.superdan.app.aileplayer.ui;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.graphics.BitmapFactory;
import android.media.session.MediaSession;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.superdan.app.aileplayer.R;
import com.superdan.app.aileplayer.utils.LogHelper;
import com.superdan.app.aileplayer.utils.NetworkHelper;
import com.superdan.app.aileplayer.utils.ResourceHelper;

/**
 * Created by Administrator on 2016/4/9.
 * Base activity for activities that need to show a playback control fragment when media is playing.
 * 为当媒体播放时需要显示一个播放器控制fragment的activity们的定义的基类
 */
public class BaseActivity extends ActionBarCastActivity implements MediaBrowserProvider {
    private static final String TAG = LogHelper.makeLogTag(BaseActivity.class);


    private MediaBrowserCompat mMediaBrowser;

    private PlaybackControlsFragment mControlFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 21) {
            // Since our app icon has the same color as colorPrimary, our entry in the Recent Apps
            // list gets weird. We need to change either the icon or the color
            // of the TaskDescription.
            //由于我们的应用程序图标和colorPrimary一样的颜色，我们在最近应用列表会很奇怪
            //我们需要改变该图标或者任务描述的颜色。
            ActivityManager.TaskDescription taskDesc = new ActivityManager.TaskDescription(
                    getTitle().toString(),
                    BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_white),
                    ResourceHelper.getThemeColor(this, R.attr.colorPrimary, android.R.color.darker_gray)
            );
            setTaskDescription(taskDesc);
        }

        // Connect a media browser just to get the media session token. There are other ways
        // this can be done, for example by sharing the session token directly.
        //连接一个媒体浏览只是为了得到一个媒体回话令牌，还有其他方法可以被做，例如通过令牌直接分享


        /**
          *
          *@author duanshengze
          *created at 16/4/10 下午4:00
         * Creates a media browser for the specified media browse service.
         *为指定的媒体浏览服务创建媒体浏览
         * @param context The context.
         * @param serviceComponent The component name of the media browse service.
         * @param callback The connection callback.
         * @param rootHints An optional bundle of service-specific arguments to send
         * to the media browse service when connecting and retrieving the root id
         * for browsing, or null if none. The contents of this bundle may affect
         * the information returned when browsing.

          */

        mMediaBrowser = new MediaBrowserCompat(this, new ComponentName(this, Musi),mConnectionCallback,null);
    }


    @Override
    protected void onStart() {
        super.onStart();
        LogHelper.d(TAG, "Activity onStart");
        mControlFragment=(PlaybackControlsFragment)getFragmentManager().findFragmentById(R.id.fragment_playback_controls);
        if(mControlFragment==null){
            throw  new IllegalStateException("Missing fragment with id 'coontrols'. connot continue.");
        }
        hidePlaybackControls();

        /**
          ** Connects to the media browse service.
         * The connection callback specified in the constructor will be invoked
         * when the connection completes or fails.
         * 连接到媒体浏览服务，连接失败或者成功时，在构造函数中指定的连接回调将会被调用
         *
          */
        mMediaBrowser.connect();

    }


    @Override
    protected void onStop() {
        super.onStop();
        LogHelper.d(TAG,"Activity onStop");
        if(getSupportMediaController()!=null){

            /**
              *
              *@author duanshengze
              *created at 16/4/10 下午3:55
              * Stop receiving updates on the specified callback. If an update has
             * already been posted you may still receive it after calling this method.
             * @param callback The callback to remove
             * 停止接受在指定的回调的更新，如果更新已经发布，当调用这个方法后，你可以仍然接受到它
              */
            getSupportMediaController().unregisterCallback(mMediaControllerCallback);
        }
        /**
          *
          *@author duanshengze
          *created at 16/4/10 下午3:58
          *Disconnects from the media browse service.
         * After this, no more callbacks will be received
         * 断掉浏览服务器，在此之后，回调将不会接受
          */
        mMediaBrowser.disconnect();
    }

    /**
     * 检查MediaSession是否活跃而且是在回放状态（补位NONE，不为停止）
     *
     * @return 如果MediaSession的状态需要回放控制可见则返回 true
     * @author duanshengze
     * created at 16/4/10 下午2:26
     * @params
     */
    protected boolean shouldShowControls() {
        MediaControllerCompat mediaController = getSupportMediaController();
        if (mediaController == null || mediaController.getMetadata() == null || mediaController.getPlaybackState() == null) {
            return false;
        }
        switch (mediaController.getPlaybackState().getState()) {
                case PlaybackStateCompat.STATE_ERROR:
                case PlaybackStateCompat.STATE_NONE:
            case PlaybackStateCompat.STATE_STOPPED:
                return false;
            default:
                return true;
        }


    }

    private void connectToSession(MediaSessionCompat.Token token) throws RemoteException {

        MediaControllerCompat mediaController = new MediaControllerCompat(this, token);
        /**
         *Sets a MediaControllerCompat for later retrieval via getSupportMediaController().
         * 设置媒体控制器 之后通过 getSupportMediaController()索引
         On API 21 and later, this controller will be tied to the window of the activity
         and media key and volume events which are received while the Activity is
         in the foreground will be forwarded to the controller and used to invoke transport controls
         or adjust the volume. Prior to API 21, the global handling of media key and volume events
         through an active android.support.v4.media.session.MediaSessionCompat
         and media button receiver will still be respected.
         在API 21 或者更高，该控制器将捆绑到活动窗口和媒体秘钥，而且音量时间将被接收当Activit在前台时，将传输给控制器，被用来出发控制转移和调节
         音量。
         在API21 之前，通过android.support.v4.media.session.MediaSessionCompat和媒体按钮接收器来处理全局的媒体秘钥和音量事件
         书被接受的
         */

        setSupportMediaController(mediaController);
        mediaController.registerCallback(mMediaControllerCallback);

        if (shouldShowControls()){
            showPlaybackControls();
        }else {

            LogHelper.d(TAG,"connectionCallback.onConnected: " +
            "hiding controls because metadata is null");
            hidePlaybackControls();

        }

        onMediaControllerConnected();

    }


    protected void onMediaControllerConnected(){
        //TODO 空实现，可以被客户端实现
    }

//回调确保我们正在显示控制
    private final MediaControllerCompat.Callback mMediaControllerCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
                if(shouldShowControls()){
                    showPlaybackControls();

                }else {
                    LogHelper.d(TAG,"mediaControllerCallback.onPlaybackStateChanged:"+
                    "hiding controls because state is"+state.getState()
                    );
                    hidePlaybackControls();
                }
        }
    };


    protected  void showPlaybackControls(){
        LogHelper.d(TAG,"showPlaybackControls");
        if(NetworkHelper.isOnline(this)){

            getFragmentManager().beginTransaction()
                    .setCustomAnimations(
                            R.animator.slide_in_from_bottom,R.animator.slide_out_to_bottom,
                            R.animator.slide_in_from_bottom,R.animator.slide_out_to_bottom

                    ).show(mControlFragment)
                    .commit();

        }

    }


    protected  void hidePlaybackControls(){

        LogHelper.d(TAG,"hidePlaybackControls");
        getFragmentManager().beginTransaction()
                .hide(mControlFragment)
                .commit();


    }


    private  final  MediaBrowserCompat.ConnectionCallback mConnectionCallback=new MediaBrowserCompat.ConnectionCallback(){


        @Override
        public void onConnected() {
            LogHelper.d(TAG,"onConnected");
            try {
                connectToSession(mMediaBrowser.getSessionToken());
            }catch (RemoteException e){
                LogHelper.e(TAG,e,"could not connect media controller");
                hidePlaybackControls();
            }
        }
    };

    @Override
    public MediaBrowserCompat getMediaBrowser() {
        return mMediaBrowser;
    }
}
