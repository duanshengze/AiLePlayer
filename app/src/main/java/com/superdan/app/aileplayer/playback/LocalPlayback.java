package com.superdan.app.aileplayer.playback;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;

import com.superdan.app.aileplayer.MusicService;
import com.superdan.app.aileplayer.model.MusicProvider;
import com.superdan.app.aileplayer.model.MusicProviderSource;
import com.superdan.app.aileplayer.utils.LogHelper;
import com.superdan.app.aileplayer.utils.MediaIDHelper;


/**
 * A class that implements local media playback using {@link android.media.MediaPlayer}
 * 实现本地媒体播放类{@link android.media.MediaPlayer}
 */
public class LocalPlayback implements Playback {

    private static final String TAG= LogHelper.makeLogTag(LocalPlayback.class);
//The volume we set the media player to when we lose audio focus,
// but are allowed to reduce the volume instead of stopping playback.
    public static final float VOLUME_DUCK=0.2f;//无声
    // The volume we set the media player when we have audio focus.
    public static  final float VOLUME_NORMAL=1.0f;
    // we don't have audio focus, and can't duck (play at a low volume)
    private  static final  int AUDIO_NO_FOCUS_NO_DUCK=0;

    private static final  int AUDIO_NO_FUCUS_DUCK=1;

    private static final int AUDIO_FOCUSED=2;

    private Context mContext;

    private final WifiManager.WifiLock mWifiLock;

    private int mState;

    private boolean mPlayOnFocusGain;

    private Callback mCallback;

    private  final MusicProvider mMusicProvider;
    private  volatile  boolean mAudioNosiyReciverRegistered;
    private volatile int mCurrentPosition;
    private volatile  String mCurrentMediaId;
    //我们有音频设置类型：
    private int mAudioFocus=AUDIO_NO_FOCUS_NO_DUCK;
    private final AudioManager mAudioManager;
    private MediaPlayer mMediaPlayer;

    private final IntentFilter mAudioNosiyIntentFilter=new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);





    private final BroadcastReceiver mAudioNosiyReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())){
                LogHelper.d(TAG,"Headphones disconnected.");
                if(isPlaying()){
                    Intent i=new Intent(context, MusicService.class);
                    i.setAction(MusicService.ACTION_CMD);
                    i.putExtra(MusicService.CMD_NAME,MusicService.CMD_PAUSE);
                    mContext.startService(i);
                }


            }
        }
    };

    public  LocalPlayback(Context context,MusicProvider musicProvider){
        mContext=context;
        mMusicProvider=musicProvider;
        mAudioManager=(AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        mWifiLock=((WifiManager)context.getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL,"aile_lock");
        mState= PlaybackStateCompat.STATE_NONE;

    }





    @Override
    public void start() {

    }

    @Override
    public void stop(boolean notifyListeners) {
        mState=PlaybackStateCompat.STATE_STOPPED;
        if(notifyListeners&&mCallback!=null){
            mCallback.onPlaybackStatusChanged(mState);
        }
        mCurrentPosition=getCurrentStreamPosition();
        //放弃音频焦点
        giveUpAudiFocus();
        unregisterAudioNosisyReceiver();

        //释放资源
        relaxResources(true);

    }

    @Override
    public void setState(int state) {
        mState=state;
    }

    @Override
    public int getState() {
        return mState;
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public boolean isPlaying() {

        return mPlayOnFocusGain||(mMediaPlayer!=null&&mMediaPlayer.isPlaying());
    }

    @Override
    public int getCurrentStreamPosition() {

        return mMediaPlayer!=null?mMediaPlayer.getCurrentPosition():mCurrentPosition;
    }

    @Override
    public void setCurrentStreamPosition(int pos) {

    }

    @Override
    public void updateLastKnownStreamPosition() {
            if (mMediaPlayer!=null){
                mCurrentPosition=mMediaPlayer.getCurrentPosition();
            }
    }

    @Override
    public void play(MediaSessionCompat.QueueItem item) {
        mPlayOnFocusGain=true;
        tryToGetAudioFocus();
        registerAudioNosiyReceiver();
        String mediaId=item.getDescription().getMediaId();
        boolean mediaHasChanged=!TextUtils.equals(mediaId,mCurrentMediaId);
        if(mediaHasChanged){
            mCurrentPosition=0;
            mCurrentMediaId=mediaId;
        }
        if(mState==PlaybackStateCompat.STATE_PAUSED&&!mediaHasChanged&&mMediaPlayer!=null){
            configMediaPlayerState();
        }else {
            mState=PlaybackStateCompat.STATE_PAUSED;
            relaxResources(false);//释放资源除 MediaPlayer外
            MediaMetadataCompat track=mMusicProvider.getMusic(MediaIDHelper.extractMusicIDFromMediaID(item.getDescription().getMediaId()));
            String source=track.getString(MusicProviderSource.CUSTOM_METADATA_TRACK_SOURCE);

        }
    }


    /**
     * Reconfigures MediaPlayer according to audio focus settings and
     * starts/restarts it. This method starts/restarts the MediaPlayer
     * respecting the current audio focus state. So if we have focus, it will
     * play normally; if we don't have focus, it will either leave the
     * MediaPlayer paused or set it to a low volume, depending on what is
     * allowed by the current focus settings. This method assumes mPlayer !=
     * null, so if you are calling it, you have to do so from a context where
     * you are sure this is the case.
     */


    private  void configMediaPlayerState(){

    }
    @Override
    public void pause() {

    }

    @Override
    public void seekTo(int position) {

    }

    @Override
    public void setCurrentMediaId(String mediaId) {

    }

    @Override
    public String getCurrentMediaId() {
        return null;
    }

    @Override
    public void setCallback(Callback callback) {

    }

    /**
     * 确保媒体播放器获得唤醒锁在播放时，如果我们不这么做，音乐播放时CPU休眠时，会导致播放停止
     */
    private void createMediaPlayerIdNeeded(){
        LogHelper.d(TAG,"createMediaPlayerIfNeed ?",(mMediaPlayer==null));
        if(mMediaPlayer==null){
            mMediaPlayer=new MediaPlayer();
            mMediaPlayer.setWakeMode(mContext.getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

            //我们希望媒体播放器准备好播放，完成播放时 通知我们。
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.setOnSeekCompleteListener(this);

        }else {
            mMediaPlayer.reset();
        }
    }
    private  void relaxResources(boolean releaseMediaPlayer){
        LogHelper.d(TAG,"relaxResources, releaseMediaPlater=",releaseMediaPlayer);


        if(releaseMediaPlayer&&mMediaPlayer!=null){

            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer=null;
        }
        //检查是否该WifiLock正在举行.
        if(mWifiLock.isHeld()){
            mWifiLock.release();
        }
    }


    private void registerAudioNosiyReceiver(){
        if(!mAudioNosiyReciverRegistered){
            mContext.registerReceiver(mAudioNosiyReceiver,mAudioNosiyIntentFilter);
            mAudioNosiyReciverRegistered=true;
        }
    }

    private  void unregisterAudioNosisyReceiver(){

        if(mAudioNosiyReciverRegistered){
            mContext.unregisterReceiver(mAudioNosiyReceiver);
            mAudioNosiyReciverRegistered=false;
        }

    }

}
