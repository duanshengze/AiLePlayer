package com.superdan.app.aileplayer.ui;

import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.superdan.app.aileplayer.utils.LogHelper;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

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
    private MediaBrowserCompat mediaBrowser;

    private final Runnable mUpdateProgressTask = new Runnable() {
        @Override
        public void run() {

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
    private PlaybackStateCompat mLastPlaybackState;
    private final MediaControllerCompat.Callback mCallback=new MediaControllerCompat.Callback(){




    };


}
