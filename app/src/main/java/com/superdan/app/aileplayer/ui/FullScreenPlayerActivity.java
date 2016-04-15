package com.superdan.app.aileplayer.ui;

import android.content.ComponentName;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.superdan.app.aileplayer.AlbumArtCache;
import com.superdan.app.aileplayer.MusicService;
import com.superdan.app.aileplayer.R;
import com.superdan.app.aileplayer.utils.LogHelper;

import org.w3c.dom.Text;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2016/4/8.
 * <p>
 * A full screen player that shows the current playing music with a background image
 * depicting the album art. The activity also has controls to seek/pause/play the audio.
 * 全部播放器，显示当前音乐播放，带有背景图片，描绘专辑封面，该活动也具有控制（快进/暂停/播放）功能
 */
public class FullScreenPlayerActivity extends ActionBarCastActivity {
    private static final String TAG = LogHelper.makeLogTag(FullScreenPlayerActivity.class);
    private static final long PROGRESS_UPDATE_INTERAL = 1000;//更新进度间隔
    private static final long PROGRESS_UPDATE_INITIAL_INTERVAL = 100;//更新进度初始间隔


    private ImageView mSkipPrev;
    private ImageView mSkipNext;
    private ImageView mPlayPause;
    private TextView mStart;
    private TextView mEnd;
    private SeekBar mSeekbar;
    private TextView mLine1;
    private TextView mLine2;
    private TextView mLine3;

    private ProgressBar mLoading;
    private View mControllors;
    private Drawable mPauseDrawable;
    private Drawable mPlayDrawable;
    private ImageView mBackgroundImage;

    private String mCurrentArtUrl;
    private final Handler mHandler = new Handler();
    private MediaBrowserCompat mMediaBrowser;

    private final Runnable mUpdateProgressTask = new Runnable() {
        @Override
        public void run() {
            updateProgress();
        }
    };

    /**
     * An ExecutorService that can schedule commands to run after a given delay,
     * or to execute periodically.
     * The schedule methods create tasks with various delays and return a task object
     * that can be used to cancel or check execution. The scheduleAtFixedRate and
     * scheduleWithFixedDelay methods
     * create and execute tasks that run periodically until cancelled.
     **/

    private final ScheduledExecutorService mExecutorService = Executors.newSingleThreadScheduledExecutor();

    private ScheduledFuture<?>mScheduleFuture;
    /**
     * Playback state for a {@link MediaSessionCompat}. This includes a state like
     * 为MediaSessionCompat 设置一个回放状态。
     * {@link PlaybackStateCompat#STATE_PLAYING}, the current playback position,
     * and the current control capabilities.
     * 这包括 像 STATE_PLAYING状态，当前播放状态的位置，和当前控制电量
     *
     */
    private PlaybackStateCompat mLastPlaybackState;
    private final MediaControllerCompat.Callback mCallback=new MediaControllerCompat.Callback(){

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            LogHelper.d(TAG,"播放状态发生改变（onPlaybackstate changed）",state);
            updatePlaybackState(state);
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            if(metadata!=null){
                updateMediaDescription(metadata.getDescription());
                updateDuration(metadata);
            }
        }
    };


    private final MediaBrowserCompat.ConnectionCallback mConnectionCallback=new MediaBrowserCompat.ConnectionCallback(){

        @Override
        public void onConnected() {
            LogHelper.d(TAG,"连接上 onConnected");
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_player);
        initializeToolbar();
        if (getSupportActionBar()!=null){
            /**
             * Set whether home should be displayed as an "up" affordance.
             * Set this to true if selecting "home" returns up by a single level in your UI
             * rather than back to the top level or front page.
             *
             * <p>To set several display options at once, see the setDisplayOptions methods.
             *
             * @param showHomeAsUp true to show the user that selecting home will return one
             *                     level up rather than to the top level of the app.
             *
             * @see #setDisplayOptions(int)
             * @see #setDisplayOptions(int, int)
             */
            // 给左上角图标的左边加上一个返回的图标
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            getSupportActionBar().setTitle("");
        }
        mBackgroundImage=(ImageView)findViewById(R.id.background_image);
        mPauseDrawable= ContextCompat.getDrawable(this,R.mipmap.uamp_ic_pause_white_48dp);
        mPauseDrawable=ContextCompat.getDrawable(this,R.mipmap.uamp_ic_play_arrow_white_48dp);
        mSkipNext=(ImageView)findViewById(R.id.next);
        mSkipPrev=(ImageView)findViewById(R.id.prev);
        mStart=(TextView)findViewById(R.id.startText);
        mEnd=(TextView)findViewById(R.id.endText);
        mLine1=(TextView)findViewById(R.id.line1);
        mLine2=(TextView)findViewById(R.id.line2);
        mLine3=(TextView)findViewById(R.id.line3);
        mLoading=(ProgressBar)findViewById(R.id.progressBar1);
        mControllors=findViewById(R.id.controllers);



        mSkipNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaControllerCompat.TransportControls controls=getSupportMediaController().getTransportControls();
                controls.skipToNext();

            }
        });

        mSkipPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaControllerCompat.TransportControls controls=getSupportMediaController().getTransportControls();
                controls.skipToPrevious();
            }
        });

        mPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlaybackStateCompat state=getSupportMediaController().getPlaybackState();
                if(state!=null){

                    MediaControllerCompat.TransportControls controls=getSupportMediaController().getTransportControls();

                    switch (state.getState()){

                        case PlaybackStateCompat.STATE_PLAYING:
                        case PlaybackStateCompat.STATE_BUFFERING:
                            controls.pause();
                            stopSeekbarUpdate();
                            break;
                        case PlaybackStateCompat.STATE_PAUSED:
                            PlaybackStateCompat.STATE_STOPPED:
                            controls.play();
                            scheduleSeekbarUpdate();
                        break;
                        default:
                            LogHelper.d(TAG,"onClick with state",state.getState());



                    }

                }

            }
        });

        mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                /**
                 * Formats an elapsed time in the form "MM:SS" or "H:MM:SS"
                 * for display on the call-in-progress screen.
                 * 格式化经历的时间“MM：SS”或“H:MM:SS”
                 * @param elapsedSeconds the elapsed time in seconds.
                 */
                 mStart.setText(DateUtils.formatElapsedTime(progress/1000));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                getSupportMediaController().getTransportControls().seekTo(seekBar.getProgress());
                scheduleSeekbarUpdate();

            }
        });
//如果我们不从一个配置变化创造 只更新来至intent
        if(savedInstanceState==null){
            updateFromParams(getIntent());
        }


        mMediaBrowser=new MediaBrowserCompat(this,new ComponentName(this,MusicService.class),mConnectionCallback,null);

    }

    protected void initializeToolbar(){
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if(mToolbar==null){
            throw new IllegalStateException("Layout is required to include a Toolbar with id"+"'toolbar'");

        }
        mToolbar.inflateMenu(R.menu.main);
        mDrawerLayout=(DrawerLayout)findViewById(R.id.drawer_layout);
        if(mDrawerLayout!=null){

            NavigationView navigationView=(NavigationView)findViewById(R.id.nav_view);
            if(navigationView==null){
                throw  new IllegalStateException("Layout requires a NavigationView with id 'nav_view");
                
            }

        }




    }

    private void fetchImageAsync(@NonNull MediaDescriptionCompat description){
        if(description.getIconUri()==null){
            return;
        }
        String artUrl=description.getIconUri().toString();
        mCurrentArtUrl=artUrl;
        AlbumArtCache cache=AlbumArtCache.getInstance();
        Bitmap art=cache.getBitImage(artUrl);
        if(art==null){
            art=description.getIconBitmap();
        }

        if (art!=null){
            //如果我们有缓存或者来自MediaDescription 使用它：
            mBackgroundImage.setImageBitmap(art);
        }else {
            cache.fetch(artUrl,new AlbumArtCache.FetchListener(){

                @Override
                public void onFecthed(String artUrl, Bitmap bitImgae, Bitmap iconImage) {
                    //全面检查，以免新的读取请求已经完成，而以前还没有返回
                    if (artUrl.equals(mCurrentArtUrl)){
                        mBackgroundImage.setImageBitmap(bitImgae);
                    }


                }
            });

        }



    }


    private void updatePlaybackState(PlaybackStateCompat state){
        if(state==null){
            return;
        }
        mLastPlaybackState=state;
        if(getSupportMediaController()!=null&&getSupportMediaController().getExtras()!=null){
            String castName=getSupportMediaController().getExtras().getString(MusicService.EXTRA_CONNECTED_CAST);

            String line3Text=castName==null?"":getResources().getString(R.string.casting_to_device,castName);
            mLine3.setText(line3Text);
        }

        switch (state.getState()){


            case PlaybackStateCompat.STATE_PLAYING:
                mLoading.setVisibility(View.INVISIBLE);
                mPlayPause.setVisibility(View.VISIBLE);
                mPlayPause.setImageDrawable(mPauseDrawable);
                mControllors.setVisibility(View.VISIBLE);
                scheduleSeekbarUpdate();
                break;
            case PlaybackStateCompat.STATE_PAUSED:
                mControllors.setVisibility(View.VISIBLE);
                mLoading.setVisibility(View.INVISIBLE);
                mPlayPause.setVisibility(View.VISIBLE);
                mPlayPause.setImageDrawable(mPlayDrawable);
                stopSeekbarUpdate();
                break;
            case PlaybackStateCompat.STATE_NONE:
            case PlaybackStateCompat.STATE_STOPPED:
                mLoading.setVisibility(View.INVISIBLE);
                mPlayPause.setVisibility(View.VISIBLE);
                mPlayPause.setImageDrawable(mPlayDrawable);
                stopSeekbarUpdate();
                break;
            case PlaybackStateCompat.STATE_BUFFERING:
                mPlayPause.setVisibility(View.INVISIBLE);
                mLoading.setVisibility(View.VISIBLE);
                mLine3.setText(R.string.loading);
                stopSeekbarUpdate();
                break;
            default:
                LogHelper.d(TAG,"Unhandled state",state.getState());
        }
        /**
         * Get the current actions available on this session. This should use a
         * bitmask of the available actions.
         *获得本次回话的当前可以的操作，应该使用可用操作的位掩码
         * */
        mSkipNext.setVisibility((state.getActions()&PlaybackStateCompat.ACTION_SKIP_TO_NEXT)==0?View.INVISIBLE:View.VISIBLE);

        mSkipPrev.setVisibility((state.getActions()&PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)==0?View.INVISIBLE:View.VISIBLE);


    }


    private  void scheduleSeekbarUpdate(){
        stopSeekbarUpdate();
        if(!mExecutorService.isShutdown()){
            mScheduleFuture=mExecutorService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {

                    /**
                     * Causes the Runnable r to be added to the message queue.
                     * The runnable will be run on the thread to which this handler is
                     * attached.
                     *
                     * @param r The Runnable that will be executed.
                     *
                     * @return Returns true if the Runnable was successfully placed in to the
                     *         message queue.  Returns false on failure, usually because the
                     *         looper processing the message queue is exiting.
                     */
                        mHandler.post(mUpdateProgressTask);
                }
            },PROGRESS_UPDATE_INITIAL_INTERVAL,PROGRESS_UPDATE_INTERAL, TimeUnit.MILLISECONDS);



        }



    }



    private void stopSeekbarUpdate(){

        if(mScheduleFuture!=null){
            //取消但不中断
            mScheduleFuture.cancel(false);

        }

    }


    private  void updateProgress(){
        if(mLastPlaybackState==null){
            return;
        }
        long currentPosition=mLastPlaybackState.getPosition();
        if(mLastPlaybackState.getState()!=PlaybackStateCompat.STATE_PAUSED){
            // Calculate the elapsed time between the last position update and now and unless
            // paused, we can assume (delta * speed) + current position is approximately the
            // latest position. This ensure that we do not repeatedly call the getPlaybackState()
            // on MediaControllerCompat.
            //计算最后一次更新的位置和现在的位置之间 除非暂停，我们可以假设（增量*速度）+当前近似最新位置，这可以确保
            //我们不要反复呼吁媒体控制的getPlayState()
            long timeDelta= SystemClock.elapsedRealtime()-mLastPlaybackState.getLastPositionUpdateTime();
            /**
             * Get the current playback speed as a multiple of normal playback. This
             * should be negative when rewinding. A value of 1 means normal playback and
             * 0 means paused.
             *获取当前播放速度为正常播放的倍数，这个倒带应该为负数，值为1意味着正常播放，0意味着暂停
             * @return The current speed of playback.
             *
             */
            currentPosition+=(int)timeDelta*mLastPlaybackState.getPlaybackSpeed();



            mSeekbar.setProgress((int)currentPosition);
        }



    }




}
