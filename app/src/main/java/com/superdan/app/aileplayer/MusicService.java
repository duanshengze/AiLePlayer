package com.superdan.app.aileplayer;

/**
 * Created by Administrator on 2016/4/9.
 */


import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.media.MediaRouter;

import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.google.android.libraries.cast.companionlibrary.cast.callbacks.VideoCastConsumerImpl;
import com.superdan.app.aileplayer.model.MusicProvider;
import com.superdan.app.aileplayer.playback.CastPlayback;
import com.superdan.app.aileplayer.playback.LocalPlayback;
import com.superdan.app.aileplayer.playback.Playback;
import com.superdan.app.aileplayer.playback.PlaybackManager;
import com.superdan.app.aileplayer.playback.QueueManager;
import com.superdan.app.aileplayer.ui.NowPlayingActivity;
import com.superdan.app.aileplayer.utils.LogHelper;
import com.superdan.app.aileplayer.utils.MediaIDHelper;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * This class provides a MediaBrowser through a service. It exposes the media library to a browsing
 * client, through the onGetRoot and onLoadChildren methods. It also creates a MediaSession and
 * exposes it through its MediaSession.Token, which allows the client to create a MediaController
 * that connects to and send control commands to the MediaSession remotely. This is useful for
 * user interfaces that need to interact with your media session, like Android Auto. You can
 * (should) also use the same service from your app's UI, which gives a seamless playback
 * experience to the user.
 * 此类提供一个MediaBrowser 通过一个服务。它暴露媒体库给浏览客户端，通过onGetRoot()和onLoadChildren()方法。
 * 它也创造一个MediaSession并通过他的MediaSession.Token暴露它。这可以允许客户端去创造一个媒体控制器，连接或远程发送
 * 控制命令给MediaSession。这对用户接口很有用，同步锁需要与媒体会话的接口，像Android汽车的用户界面。你可以（应该）也使用相同的服务
 * 在您的应用程序的用户界面，这给用户一种无缝播放体验
 *
 * To implement a MediaBrowserService, you need to:
 * 为了实现一个MediaBrowserService 你需要：
 *
 *
 * <ul>
 *
 * <li> Extend {@link android.service.media.MediaBrowserService}, implementing the media browsing
 *      related methods {@link android.service.media.MediaBrowserService#onGetRoot} and
 *      {@link android.service.media.MediaBrowserService#onLoadChildren};
 *      需要继承{@link android.service.media.MediaBrowserService}，实现Media浏览相关方法
 *      {@link android.service.media.MediaBrowserService#onGetRoot}和 {@link android.service.media.MediaBrowserService#onLoadChildren}
 *
 *
 * <li> In onCreate, start a new {@link android.media.session.MediaSession} and notify its parent
 *      with the session's token {@link android.service.media.MediaBrowserService#setSessionToken};
 *      在onCreate（），开始一个新的{@link android.media.session.MediaSession}并且使用回话的token通知他的父类
 *      {@link android.service.media.MediaBrowserService#setSessionToken};
 * <li> Set a callback on the
 *      {@link android.media.session.MediaSession#setCallback(android.media.session.MediaSession.Callback)}.
 *      The callback will receive all the user's actions, like play, pause, etc;
 *
 *      设置一个回调在{@link android.media.session.MediaSession#setCallback(android.media.session.MediaSession.Callback)}.
 *      这个回调将接受用户的所有动作，例如 播放，暂停，等。
 *
 * <li> Handle all the actual music playing using any method your app prefers (for example,
 *      {@link android.media.MediaPlayer})
 *      处理实际的音乐播放，通过使用任何你偏爱的应用（例如{@link android.media.MediaPlayer}）
 *
 *
 *
 * <li> Update playbackState, "now playing" metadata and queue, using MediaSession proper methods
 *      {@link android.media.session.MediaSession#setPlaybackState(android.media.session.PlaybackState)}
 *      {@link android.media.session.MediaSession#setMetadata(android.media.MediaMetadata)} and
 *      {@link android.media.session.MediaSession#setQueue(java.util.List)})
 *
 *      更新播放状态，“正在播放”元数据和队列，使用MediaSession 合适方法
 *
 *
 *
 * <li> Declare and export the service in AndroidManifest with an intent receiver for the action
 *      android.media.browse.MediaBrowserService
 *  在AndroidManifest声明和暴露服务，并带有一个intent 接收器为action设置android.media.browse.MediaBrowserService
 *
 *
 * </ul>
 *
 * To make your app compatible with Android Auto, you also need to:
 *为了你的app兼容Android汽车，你需要做：
 *
 * <ul>
 *
 * <li> Declare a meta-data tag in AndroidManifest.xml linking to a xml resource
 *      with a &lt;automotiveApp&gt; root element. For a media app, this must include
 *      an &lt;uses name="media"/&gt; element as a child.
 *      For example, in AndroidManifest.xml:
 *          &lt;meta-data android:name="com.google.android.gms.car.application"
 *              android:resource="@xml/automotive_app_desc"/&gt;
 *      And in res/values/automotive_app_desc.xml:
 *          &lt;automotiveApp&gt;
 *              &lt;uses name="media"/&gt;
 *          &lt;/automotiveApp&gt;
 *
 * </ul>
 *
 * 声明一个元数据标签在AndroidManifest

 * @see <a href="README.md">README.md</a> for more details.
 *
 */

public class MusicService extends MediaBrowserServiceCompat implements PlaybackManager.PlaybackServiceCallback{

    private static final String TAG= LogHelper.makeLogTag(MusicService.class);

    public static final String APP_PACKAGE="com.superdan.app.aileplayer";
    //当前cast 连接的设备
    public static final String EXTRA_CONNECTED_CAST="com.superdan.app.aileplayer.CAST_NAME";
    //包含需要执行的操作
    public static final String ACTION_CMD=APP_PACKAGE+".ACTION_CMD";

    public static  final String CMD_NAME="CMD_NAME";
    //表明播放停止
    public static  final  String CMD_PAUSE="CMD_PAUSE";

    //CMD_NAME 的键，用来指示 音乐播放应该切换到本地
    public static  final  String CMD_STOP_CASTING="CMD_STOP_CASTING";
    //延迟关掉自己 通过Handler
    public static final int STOP_DELAY=30000;

    private MusicProvider mMusicProvider;

    private PlaybackManager mPlaybackManager;

    private MediaSessionCompat mSession;

    private  MediaNotificationManager mMediaNotificationManager;

    private Bundle mSessionExtras;

    private final  DelayedStopHandler mDelayedStopHandler=new DelayedStopHandler(this);

    private MediaRouter mMediaRouter;

    private PackageValidator mPackageValidator;
    private boolean mIsConnectedToCar;
    private BroadcastReceiver mBroadcastReceiver;


    /**
     负责切换播放状态的实例的客户端，依据是否连接到远程播放器
    * */
    private final VideoCastConsumerImpl mCastConsumer=new VideoCastConsumerImpl(){
        @Override
        public void onApplicationConnected(ApplicationMetadata appMetadata, String sessionId, boolean wasLaunched) {
          //如果我们正在casting，发送设备的名称作为extra在MediaSession metadata
            mSessionExtras.putString(EXTRA_CONNECTED_CAST, VideoCastManager.getInstance().getDeviceName());
            mSession.setExtras(mSessionExtras);
            //现在切换palyback
            Playback playback=new CastPlayback(mMusicProvider);
            mMediaRouter.setMediaSessionCompat(mSession);
            mPlaybackManager.switchToPlayback(playback,true);
        }

        @Override
        public void onDisconnectionReason(int reason) {
            LogHelper.d(TAG,"onDistance");
            //这是我们更新基础流最后机会，在onDisconnected(),底层CastPlayback#mVideoCastConsumer 断开，因此
            //我们更新本地流的位置
            mPlaybackManager.getPlayback().updateLastKnownStreamPosition();
        }

        @Override
        public void onDisconnected() {
            LogHelper.d(TAG,"onDistanested");
            mSessionExtras.remove(EXTRA_CONNECTED_CAST);
            mSession.setExtras(mSessionExtras);
            Playback playback=new LocalPlayback(MusicService.this,mMusicProvider);
            mMediaRouter.setMediaSessionCompat(null);
            mPlaybackManager.switchToPlayback(playback,false);

        }
    };


    @Override
    public void onCreate() {
        super.onCreate();
        LogHelper.d(TAG,"onCreate");
        mMusicProvider=new MusicProvider();

        //为了使应用程序的响应，立即抓取和缓存目录信息
        //这可以提高改善方法//响应时间 {@link #onLoadChildren(String, Result<List<MediaItem>>) onLoadChildren()}
        mMusicProvider.retrieveMedisAsync(null);
        mPackageValidator=new PackageValidator(this);

        QueueManager queueManager=new QueueManager(mMusicProvider, getResources(), new QueueManager.MetadataUpdateListener() {
            @Override
            public void onMetadatChanged(MediaMetadataCompat metadata) {
                mSession.setMetadata(metadata);
            }

            /**
             * 当前Metadata为空时更新播放状态
             */
            @Override
            public void onMetadataRetrieveError() {
                mPlaybackManager.updatePlaybackState(getString(R.string.error_no_metadata));
            }

            @Override
            public void onCurrentQueueIndexUpdated(int queueIndex) {
                mPlaybackManager.handlePlayRequest();
            }

            @Override
            public void onQueueUpdated(String title, List<MediaSessionCompat.QueueItem> newQueue) {
                mSession.setQueue(newQueue);
                mSession.setQueueTitle(title);
            }
        });
        LocalPlayback playback=new LocalPlayback(this,mMusicProvider);
        mPlaybackManager=new PlaybackManager(this,getResources(),mMusicProvider,queueManager,playback);

        //开启一个MediaSeesion
        mSession=new MediaSessionCompat(this,"MusicService");

        //?? TODO: 2016/4/27
        mSession.setCallback(mPlaybackManager.getMediaSeesionCallback());
        //为session设置flag 表明可以控制媒体按钮，可以通过回调 MediaSessionCompat.Callback开控制
        mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS|MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        Context context=getApplicationContext();
        Intent intent=new Intent(context, NowPlayingActivity.class);
        PendingIntent pi=PendingIntent.getActivity(context,99/*request code*/,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        mSession.setSessionActivity(pi);

        mSessionExtras=new Bundle();
//        CarHelper.setSlotReservationFlags(mSessionExtras, true, true, true);
//        WearHelper.setSlotReservationFlags(mSessionExtras, true, true);
//        WearHelper.setUseBackgroundFromTheme(mSessionExtras, true);
            mSession.setExtras(mSessionExtras);
        mPlaybackManager.updatePlaybackState(null);
        try{
            mMediaNotificationManager=new MediaNotificationManager(this);
        }catch (RemoteException e){
            throw  new IllegalStateException("Could not create a MediaNotificationManager",e);
        }
            VideoCastManager.getInstance().addVideoCastConsumer(mCastConsumer);
        mMediaRouter=MediaRouter.getInstance(getApplicationContext());


    }


    @Override
    public int onStartCommand(Intent startIntent, int flags, int startId) {
        if(startIntent!=null){
            String action=startIntent.getAction();
            String command=startIntent.getStringExtra(CMD_NAME);
            if(ACTION_CMD.equals(action)){
                if(CMD_PAUSE.equals(command)){
                    mPlaybackManager.handlePauseRequest();
                }else if(CMD_STOP_CASTING.equals(command)){
                    VideoCastManager.getInstance().disconnect();
                }
            }else {
                MediaButtonReceiver.handleIntent(mSession,startIntent);
            }
        }
        //复位延时处理程序排队消息（停止服务），如果服务不在播放

        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mDelayedStopHandler.sendEmptyMessageDelayed(0,STOP_DELAY);
        return  START_STICKY;
    }

    @Override
    public void onDestroy() {
        LogHelper.d(TAG,"onDestory");

        // 服务正在被消掉，因此我们要释放我们的资源
        mPlaybackManager.handleStopRequest(null);
        mMediaNotificationManager.stopNotification();
        VideoCastManager.getInstance().removeVideoCastConsumer(mCastConsumer);
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mSession.release();
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(String clientPackageName, int clientUid, Bundle rootHints) {

        LogHelper.d(TAG,"OnGetRoot:clientPackageName="+clientPackageName,"; clientuid="+clientUid
        +"; rootHints=",rootHints);
        if(!mPackageValidator.isCallerAllowed(this,clientPackageName,clientUid)){
            LogHelper.w(TAG,"OnGetRoot: IGNORING request from untrusted package"+clientPackageName);
            return  null;

        }

        return new BrowserRoot(MediaIDHelper.MEDIA_ID_ROOT,null);
    }

    @Override
    public void onLoadChildren(String parentId, Result<List<MediaBrowserCompat.MediaItem>> result) {
            LogHelper.d(TAG,"OnLoadChildren:parentId=",parentId);
        result.sendResult(mMusicProvider.getChildren(parentId,getResources()));
    }

    /**
     * Callback 方法（PackbackManager）只要音乐即将播放
     */
    @Override
    public void onPlaybackStart() {
        if(!mSession.isActive()){
            mSession.setActive(true);
        }
    }

    @Override
    public void onNotificationRequired() {
        mMediaNotificationManager.startNotification();
    }

    @Override
    public void onPlaybackStop() {
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mDelayedStopHandler.sendEmptyMessageDelayed(0,STOP_DELAY);
        stopForeground(true);
    }

    @Override
    public void onPlaybackStateUpdated(PlaybackStateCompat newState) {
        mSession.setPlaybackState(newState);
    }




    private static class DelayedStopHandler extends Handler{
        private final WeakReference<MusicService>mWeakReference;
        private  DelayedStopHandler(MusicService service){
            mWeakReference=new WeakReference<MusicService>(service);
        }

        @Override
        public void handleMessage(Message msg) {
           MusicService service=mWeakReference.get();
            if(service!=null&&service.mPlaybackManager.getPlayback()!=null){
                if(service.mPlaybackManager.getPlayback().isPlaying()){
                    LogHelper.d(TAG,"Ignoring delayed stop since the media player is in use.");
                    return;
                }
                LogHelper.d(TAG,"Stopping service with delay handler.");
                service.stopSelf();
            }
        }
    }
}
