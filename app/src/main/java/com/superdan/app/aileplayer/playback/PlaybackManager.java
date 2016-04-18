package com.superdan.app.aileplayer.playback;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.superdan.app.aileplayer.model.MusicProvider;
import com.superdan.app.aileplayer.utils.LogHelper;

/**
 * Created by dsz on 16/4/10.
 *
 * Manage the interactions among the container service, the queue manager and the actual playback.
 * 管理在服务容器之间的相互作用，队列管理器和实际重放
 *
 */
public class PlaybackManager implements Playback.Callback {


    private static final String TAG=LogHelper.makeLogTag(PlaybackManager.class);
    //点赞的Action
    private static final String CUSTOM_ACTION_THUMBS_UP="com.superdan.app.aileplayer.THUMBS_UP";


    private MusicProvider mMusicProvider;

    private QueueManager mQueueManager;


    private Resources mResources;
    private PlaybackServiceCallback mServiceCallback;


    private MediaSessionCallback mMediaSessionCallback;






    @Override
    public void onCompletion() {

    }

    @Override
    public void onPlaybackStatusChanged(int state) {

    }

    @Override
    public void onError(String error) {

    }

    @Override
    public void setCurrentMediaId(String mediaId) {

    }

    private class MediaSessionCallback extends MediaSessionCompat.Callback{

        @Override
        public void onPlay() {
            LogHelper.d(TAG,"play");

        }

        @Override
        public void onSkipToQueueItem(long id) {

        }


        @Override
        public void onSeekTo(long pos) {

        }

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {

        }

        @Override
        public void onPause() {

        }

        @Override
        public void onStop() {

        }


        @Override
        public void onSkipToNext() {

        }

        @Override
        public void onSkipToPrevious() {

        }
    }




    public  interface PlaybackServiceCallback{

        void onPlaybackStart();

        void onNotificationRequired();

        void onPlaybackStop();

        void onPlaybackStateUpdated(PlaybackStateCompat newState);
    }
}
