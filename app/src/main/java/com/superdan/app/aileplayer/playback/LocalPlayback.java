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

import java.io.IOException;


/**
 * A class that implements local media playback using {@link android.media.MediaPlayer}
 * 实现本地媒体播放类{@link android.media.MediaPlayer}
 */
public class LocalPlayback implements Playback,AudioManager.OnAudioFocusChangeListener,MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener,MediaPlayer.OnPreparedListener,MediaPlayer.OnSeekCompleteListener
{

    private static final String TAG= LogHelper.makeLogTag(LocalPlayback.class);
//The volume we set the media player to when we lose audio focus,
// but are allowed to reduce the volume instead of stopping playback.
    public static final float VOLUME_DUCK=0.2f;//无声
    // The volume we set the media player when we have audio focus.
    public static  final float VOLUME_NORMAL=1.0f;
    // we don't have audio focus, and can't duck (play at a low volume)
    private  static final  int AUDIO_NO_FOCUS_NO_DUCK=0;

    private static final  int AUDIO_NO_FUCUS_CAN_DUCK=1;

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
       giveUpAudioFocus();
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

            try{
                createMediaPlayerIdNeeded();
                mState=PlaybackStateCompat.STATE_BUFFERING;
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                // Starts preparing the media player in the background. When
                // it's done, it will call our OnPreparedListener (that is,
                // the onPrepared() method on this class, since we set the
                // listener to 'this'). Until the media player is prepared,
                // we *cannot* call start() on it!
                mMediaPlayer.setDataSource(source);


                mMediaPlayer.prepareAsync();

                // If we are streaming from the internet, we want to hold a
                // Wifi lock, which prevents the Wifi radio from going to
                // sleep while the song is playing.
                mWifiLock.acquire();

                if (mCallback!=null){
                    mCallback.onPlaybackStatusChanged(mState);
                }
            }catch (IOException ex){
                LogHelper.e(TAG,ex,"Exception playing song");
                if(mCallback!=null){
                    mCallback.onError(ex.getMessage());
                }
            }

        }
    }
    @Override
    public void pause() {
        if(mState==PlaybackStateCompat.STATE_PLAYING){
            if(mMediaPlayer!=null&&mMediaPlayer.isPlaying()){
                mMediaPlayer.pause();;
                mCurrentPosition=mMediaPlayer.getCurrentPosition();
            }
            relaxResources(false);
            giveUpAudioFocus();
        }
        mState=PlaybackStateCompat.STATE_PAUSED;
        if(mCallback!=null){
            mCallback.onPlaybackStatusChanged(mState);
        }
        unregisterAudioNosisyReceiver();


    }

    @Override
    public void seekTo(int position) {
        LogHelper.d(TAG,"seekTo called with",position);
        if(mMediaPlayer==null){
            mCurrentPosition=position;
        }else {
            if(mMediaPlayer.isPlaying()){
                mState=PlaybackStateCompat.STATE_BUFFERING;
            }
            mMediaPlayer.seekTo(position);
            if(mCallback!=null){
                mCallback.onPlaybackStatusChanged(mState);
            }
        }
    }
    @Override
    public void setCallback(Callback callback) {
            mCallback=callback;
    }

    @Override
    public String getCurrentMediaId() {
        return mCurrentMediaId;
    }
    @Override
    public void setCurrentMediaId(String mediaId) {
        mCurrentMediaId=mediaId;
    }
    @Override
    public int getCurrentStreamPosition() {

        return mMediaPlayer!=null?mMediaPlayer.getCurrentPosition():mCurrentPosition;
    }

    @Override
    public void setCurrentStreamPosition(int pos) {
        mCurrentPosition=pos;
    }

    private void tryToGetAudioFocus() {
        LogHelper.d(TAG,"tryToGetAudioFocus");
        if(mAudioFocus!=AUDIO_FOCUSED){
            int result=mAudioManager.requestAudioFocus(this,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN);
            if(result==AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
                mAudioFocus=AUDIO_FOCUSED;
            }
        }
    }

    private void giveUpAudioFocus(){
        LogHelper.d(TAG,"giveUpAudioFocus");
        if(mAudioFocus==AUDIO_FOCUSED){
            if(mAudioManager.abandonAudioFocus(this)==AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
                mAudioFocus=AUDIO_NO_FUCUS_CAN_DUCK;
            }
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
        LogHelper.d(TAG,"configMediaPlayerState.mAudioFocus=",mAudioFocus);
        if(mAudioFocus==AUDIO_NO_FOCUS_NO_DUCK){
            if(mState==PlaybackStateCompat.STATE_PLAYING){
                pause();
            }
        }else {
            if(mAudioFocus==AUDIO_NO_FUCUS_CAN_DUCK){
                if (mMediaPlayer!=null){
                    mMediaPlayer.setVolume(VOLUME_DUCK,VOLUME_DUCK);//我们将相对安静
                }

            }else {
                if (mMediaPlayer!=null){
                    mMediaPlayer.setVolume(VOLUME_NORMAL,VOLUME_NORMAL);
                }
            }
            //当我们失去焦点时我们正在播放，我们需要恢复播放
            if(mPlayOnFocusGain){
                if(mMediaPlayer!=null&&!mMediaPlayer.isPlaying()){
                    LogHelper.d(TAG,"configMediaPlayerState startMediaPlayer. seeking to ",mCurrentPosition);
                    if(mCurrentPosition==mMediaPlayer.getCurrentPosition()){
                        mMediaPlayer.start();
                        mState=PlaybackStateCompat.STATE_PLAYING;
                    }else {
                        mMediaPlayer.seekTo(mCurrentPosition);
                        mState=PlaybackStateCompat.STATE_BUFFERING;
                    }

                }//end if
                mPlayOnFocusGain=false;
            }
        }
        if(mCallback!=null){
            mCallback.onPlaybackStatusChanged(mState);
        }
    }

    /**
     * Called by AudioManager on audio focus changes.
     * Implementation of {@link android.media.AudioManager.OnAudioFocusChangeListener}
     */
    @Override
    public void onAudioFocusChange(int focusChange) {
        LogHelper.d(TAG,"onAudioFocusChange,focusChange=",focusChange);
        //用于指示音频焦点的增益，或音频设备的请求，持续时间未知
        if(focusChange==AudioManager.AUDIOFOCUS_GAIN){
            mAudioFocus=AUDIO_FOCUSED;
        }else if(focusChange==AudioManager.AUDIOFOCUS_LOSS||focusChange==AudioManager.AUDIOFOCUS_LOSS_TRANSIENT
                ||focusChange==AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK){
            //我们已经失去了焦点。
            //如果我们能duck（低播放音量），我们就可以继续播放。
            //否则，我们需要暂停播放。
            boolean canDuck=focusChange==AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK;
            mAudioFocus=canDuck?AUDIO_NO_FUCUS_CAN_DUCK:AUDIO_NO_FOCUS_NO_DUCK;
            // If we are playing, we need to reset media player by calling configMediaPlayerState
            // with mAudioFocus properly set.
            if(mState==PlaybackStateCompat.STATE_PLAYING&&!canDuck){
                mPlayOnFocusGain=true;
            }

        }else {
            LogHelper.e(TAG,"onAudioFocusChange: Ignoring unsupported focusChange: ",focusChange);
        }
        configMediaPlayerState();
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        LogHelper.d(TAG, "onSeekComplete from MediaPlayer:", mp.getCurrentPosition());
        mCurrentPosition=mp.getCurrentPosition();
        if(mState==PlaybackStateCompat.STATE_BUFFERING){
            mMediaPlayer.start();
            mState=PlaybackStateCompat.STATE_PLAYING;
        }
        if(mCallback!=null){
            mCallback.onPlaybackStatusChanged(mState);
        }
    }
    /**
     * Called when media player is done playing current song.
     *
     * @see MediaPlayer.OnCompletionListener
     */

    @Override
    public void onCompletion(MediaPlayer mp) {
            LogHelper.d(TAG,"completion from MediaPlayer");
        if(mCallback!=null){
            mCallback.onCompletion();
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        LogHelper.d(TAG,"onPrepared from MediaPlayer");
        configMediaPlayerState();
    }

    /**
     * Called when there's an error playing media. When this happens, the media
     * player goes to the Error state. We warn the user about the error and
     * reset the media player.
     *
     * @see MediaPlayer.OnErrorListener
     */

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {

        LogHelper.d(TAG, "Media player error: what=" + what + ", extra=" + extra);
        if(mCallback!=null){
            mCallback.onError("MediaPlayer error " + what + " (" + extra + ")");
        }
        return false;// true indicates we handled the error
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

    /**
     * Releases resources used by the service for playback. This includes the
     * "foreground service" status, the wake locks and possibly the MediaPlayer.
     *
     * @param releaseMediaPlayer Indicates whether the Media Player should also
     *            be released or not
     */
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
