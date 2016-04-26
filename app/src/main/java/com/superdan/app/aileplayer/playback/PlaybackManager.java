package com.superdan.app.aileplayer.playback;

import android.content.res.Resources;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.superdan.app.aileplayer.R;
import com.superdan.app.aileplayer.model.MusicProvider;
import com.superdan.app.aileplayer.utils.LogHelper;
import com.superdan.app.aileplayer.utils.MediaIDHelper;

/**
 * Created by dsz on 16/4/10.
 * <p>
 * Manage the interactions among the container service, the queue manager and the actual playback.
 * 管理在服务容器之间的相互作用，队列管理器和实际重放
 */
public class PlaybackManager implements Playback.Callback {


    private static final String TAG = LogHelper.makeLogTag(PlaybackManager.class);
    //点赞的Action
    private static final String CUSTOM_ACTION_THUMBS_UP = "com.superdan.app.aileplayer.THUMBS_UP";


    private MusicProvider mMusicProvider;

    private QueueManager mQueueManager;


    private Resources mResources;

    private Playback mPlayback;
    private PlaybackServiceCallback mServiceCallback;


    private MediaSessionCallback mMediaSessionCallback;


    public PlaybackManager(PlaybackServiceCallback serviceCallback, Resources resources, MusicProvider musicProvider
            , QueueManager queueManager, Playback playback) {

        mMusicProvider = musicProvider;
        mServiceCallback = serviceCallback;
        mResources = resources;
        mQueueManager = queueManager;
        mMediaSessionCallback = new MediaSessionCallback();
        mPlayback = playback;
        mPlayback.setCallback(this);

    }

    public Playback getPlayback() {
        return mPlayback;
    }

    public MediaSessionCompat.Callback getMediaSeesionCallback() {
        return mMediaSessionCallback;
    }


    /**
     * Handle a request to play music
     */
    public void handlePlayRequest() {

        LogHelper.d(TAG, "handlePlayRequest: mState=" + mPlayback.getState());
        MediaSessionCompat.QueueItem currentMusic = mQueueManager.getCurrentMusic();
        if (currentMusic != null) {
            mServiceCallback.onPlaybackStart();
            mPlayback.play(currentMusic);
        }

    }


    /**
     * Handle a request to pause music
     */
    public void handlePauseRequest() {
        LogHelper.d(TAG, "handlePauseRequest:mState=" + mPlayback.getState());
        if (mPlayback.isPlaying()) {
            mPlayback.pause();
            mServiceCallback.onPlaybackStop();
        }
    }


    /**
     * Handle a request to stop music
     *
     * @param withError Error message in case the stop has an unexpected cause,
     *                  The error message will be set in the PlaybackState and will be visible to
     *                  MediaController clients
     */
    public void handleStopRequest(String withError) {
        LogHelper.d(TAG, "handlestopRequest:mState=" + mPlayback.getState() + "error", withError);
        mPlayback.stop(true);
        mServiceCallback.onPlaybackStop();
        updatePlaybackState(withError);
    }

    /**
     * 更新当前媒体播放器播放状态，选择性显示错误消息
     *
     * @param error if not null   如果不为空，错误信息将呈现给用户。
     */
    public void updatePlaybackState(String error) {
        LogHelper.d(TAG, "updatePlaybackState,playback state= " + mPlayback.getState());
        long position = PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN;
        if (mPlayback != null && mPlayback.isConnected()) {
            position = mPlayback.getCurrentStreamPosition();
        }
        //noinspection ResourceType
        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(getAvailableActions());

        setCustomAction(stateBuilder);
        int state = mPlayback.getState();


        //如果存在错误信息，发送它给播放状态
        if (error != null) {
            //错误状态只应该运用导致播放错误意外的停止，持续到用户采取措施去解决它
            stateBuilder.setErrorMessage(error);
            state = PlaybackStateCompat.STATE_ERROR;
        }
        stateBuilder.setState(state, position, 1.0f, SystemClock.elapsedRealtime());

        //设置活跃的队列单元id 如果当前索引是有效的
        MediaSessionCompat.QueueItem currentMusic = mQueueManager.getCurrentMusic();
        if (currentMusic != null) {
            stateBuilder.setActiveQueueItemId(currentMusic.getQueueId());
        }
        mServiceCallback.onPlaybackStateUpdated(stateBuilder.build());
        if (state == PlaybackStateCompat.STATE_PLAYING || state == PlaybackStateCompat.STATE_PAUSED) {
            mServiceCallback.onNotificationRequired();
        }

    }


    private void setCustomAction(PlaybackStateCompat.Builder stateBuilder) {
        MediaSessionCompat.QueueItem currentMusic = mQueueManager.getCurrentMusic();
        if (currentMusic == null) {
            return;
        }
        String mediaId = currentMusic.getDescription().getMediaId();
        if (mediaId == null) {
            return;
        }
        String musicId = MediaIDHelper.extractMusicIDFromMediaID(mediaId);
        int favoriteIcon = mMusicProvider.isFavorite(musicId) ? R.mipmap.ic_star_on : R.mipmap.ic_star_off;
        LogHelper.d(TAG, "updatePlaybackState,setting Favorite custom action of music", musicId,
                " current favorite", mMusicProvider.isFavorite(musicId)
        );
        Bundle customActionExtras = new Bundle();
        //   WearHelper.setShowCustomActionOnWear(customActionExtras, true);
        stateBuilder.addCustomAction(new PlaybackStateCompat.CustomAction.Builder(
                CUSTOM_ACTION_THUMBS_UP, mResources.getString(R.string.favorite), favoriteIcon
        ).setExtras(customActionExtras)
                .build());
    }

    private long getAvailableActions() {
        long actions = PlaybackStateCompat.ACTION_PLAY |
                PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID |
                PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH |
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT;
        if (mPlayback.isPlaying()) {
            actions |= PlaybackStateCompat.ACTION_PAUSE;
        }
        return actions;
    }

    /**
     * Implementation of the Playback.Callback interface
     */
    @Override
    public void onCompletion() {
        // The media player finished playing the current song, so we go ahead
        // and start the next.
        if (mQueueManager.skipQueuePosition(1)) {

            handlePauseRequest();
            mQueueManager.updateMetadata();
        } else {
            //如果跳跃不可能，我们将停止和释放资源：
            handleStopRequest(null);
        }
    }

    @Override
    public void onPlaybackStatusChanged(int state) {
        updatePlaybackState(null);
    }

    @Override
    public void onError(String error) {
        updatePlaybackState(error);
    }

    @Override
    public void setCurrentMediaId(String mediaId) {
        mQueueManager.setQueueFromMusic(mediaId);
    }


    /**
     * 切换不同的播放实例，保存所有的播放状态 如果可以
     *
     * @param playback
     * @param resumePlaying
     */
    public void switchToPlayback(Playback playback, boolean resumePlaying) {
        if (playback == null) {
            throw new IllegalArgumentException("playback cannot be null");
        }
        //suspend the current one
        int oldState = mPlayback.getState();
        int pos = mPlayback.getCurrentStreamPosition();
        String currentMediaId = mPlayback.getCurrentMediaId();
        mPlayback.stop(false);
        playback.setCallback(this);
        playback.setCurrentStreamPosition(pos < 0 ? 0 : pos);
        playback.setCurrentMediaId(currentMediaId);
        playback.start();
        //finally swap the instance
        mPlayback = playback;
        switch (oldState) {
            case PlaybackStateCompat.STATE_BUFFERING:
            case PlaybackStateCompat.STATE_CONNECTING:
            case PlaybackStateCompat.STATE_PAUSED:
                mPlayback.pause();
                break;
            case PlaybackStateCompat.STATE_PLAYING:
                MediaSessionCompat.QueueItem currentMusic = mQueueManager.getCurrentMusic();
                if (resumePlaying && currentMusic != null) {
                    mPlayback.play(currentMusic);
                } else if (!resumePlaying) {
                    mPlayback.pause();
                } else {
                    mPlayback.stop(true);
                }
                break;
            case PlaybackStateCompat.STATE_NONE:
                break;
            default:
                LogHelper.d(TAG, "deflaut called old state is", oldState);


        }

    }

    private class MediaSessionCallback extends MediaSessionCompat.Callback {

        @Override
        public void onPlay() {
            LogHelper.d(TAG, "play");
            if (mQueueManager.getCurrentMusic() == null) {
                mQueueManager.setRandomQueue();
            }
            handlePauseRequest();
        }

        @Override
        public void onSkipToQueueItem(long queueId) {
            LogHelper.d(TAG, "OnSkipToQueueItem：" + queueId);
            mQueueManager.setCurrentQueueItem(queueId);
            handlePlayRequest();
            mQueueManager.updateMetadata();
        }


        @Override
        public void onSeekTo(long pos) {
            LogHelper.d(TAG, "onSeekTo:", pos);
            mPlayback.seekTo((int) pos);

        }

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            LogHelper.d(TAG, "playFromMeidaId mediaId:", mediaId, " extras=", extras);
            mQueueManager.setQueueFromMusic(mediaId);
            handlePlayRequest();
        }

        @Override
        public void onPause() {
            LogHelper.d(TAG, "pasue, current state= " + mPlayback.getState());
            handlePauseRequest();
        }

        @Override
        public void onStop() {
            LogHelper.d(TAG, "stop. current state= " + mPlayback.getState());

        }


        @Override
        public void onSkipToNext() {
            LogHelper.d(TAG, "skipToNext");
            if (mQueueManager.skipQueuePosition(1)) {
                handlePlayRequest();
            } else {
                handleStopRequest("Cannot skip");

            }
            mQueueManager.updateMetadata();


        }

        @Override
        public void onSkipToPrevious() {
            if (mQueueManager.skipQueuePosition(-1)) {

                handlePlayRequest();

            } else {
                handleStopRequest("Cannot skip");
            }
            mQueueManager.updateMetadata();

        }

        @Override
        public void onCustomAction(String action, Bundle extras) {
            if (CUSTOM_ACTION_THUMBS_UP.equals(action)) {

                LogHelper.i(TAG, "onCustomAction:favorite for current track");
                MediaSessionCompat.QueueItem currentMusic = mQueueManager.getCurrentMusic();
                if (currentMusic != null) {
                    String mediaId = currentMusic.getDescription().getMediaId();
                    if (mediaId != null) {
                        String musicId = MediaIDHelper.extractMusicIDFromMediaID(mediaId);
                        mMusicProvider.setFavorite(musicId, !mMusicProvider.isFavorite(musicId));
                    }
                }
//palyback state 需要被更新 因为“Favorite”icon在自定义action 将会影响新的favorite state
                updatePlaybackState(null);
            } else {
                LogHelper.e(TAG, "Unsupport action: ", action);
            }
        }

        /**
         *
         * @param query
         * @param extras
         */
        @Override
        public void onPlayFromSearch(String query, Bundle extras) {
            LogHelper.d(TAG,"playFromSearch query=",query,"extras= ",extras);
            mPlayback.setState(PlaybackStateCompat.STATE_CONNECTING);
            mQueueManager.setQueueFromSearch(query,extras);
            handlePlayRequest();
            mQueueManager.updateMetadata();
        }
    }


    public interface PlaybackServiceCallback {

        void onPlaybackStart();

        void onNotificationRequired();

        void onPlaybackStop();

        void onPlaybackStateUpdated(PlaybackStateCompat newState);
    }
}
