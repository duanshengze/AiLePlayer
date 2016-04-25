package com.superdan.app.aileplayer.playback;

import android.support.v4.media.session.MediaSessionCompat;

/**
 * Created by dsz on 16/4/10.
 * Interface representing either Local or Remote Playback. The {@link com.superdan.app.aileplayer.MusicService} works
 * directly with an instance of the Playback object to make the various calls such as
 * play, pause etc.
 *
 * 接口代表本地和远程播放，MusicService直接与Playback实例交互，产生不同的调用例如 播放，暂停等
 */
public interface Playback {

    /**
     * Start/setup the playback.
     * Resources/listeners would be allocated by implementations.
     * 开始播放
     * 资源/监听器将被实现类分配
     */
    void start();

    /**
     * Stop the playback. All resources can be de-allocated by implementations here.
     * @param notifyListeners if true and a callback has been set by setCallback,
     *                        callback.onPlaybackStatusChanged will be called after changing
     *                        the state.
     * 停止播放，所有资源可以被解除分配在这里通过实现类
     *  如果设置true而且callback已经setCallback被设置，如果改变此状态，callback.onPlaybackStatusChanged将被调用
     */
    void stop(boolean notifyListeners);

    /**
     * Set the latest playback state as determined by the caller.
     *
     * 设置被调用者确定的最新的播放状态
     */

    void setState(int state);

    /**
     * Get the current {@link android.media.session.PlaybackState#getState()}
     * 获得目前的播放状态 调用droid.media.session.PlaybackState#getState()}
     *
     */
    int getState();

    /**
     * 返回 boolean类型来表明是否准备好被使用
     * @return boolean that indicates that this is ready to be used.
     */
    boolean isConnected();



    /**
     * @return boolean indicating whether the player is playing or is supposed to be
     * playing when we gain audio focus.
     *
     *返回boolean 表明是否播放器正在播放，或者当我们获得音频焦点时被认为正在播放
     */
    boolean isPlaying();

    /**
     * @return pos if currently playing an item
     * 如果正在播放一个项目 则返回播放位置
     */
    int getCurrentStreamPosition();

    /**
     * Set the current position. Typically used when switching players that are in
     * paused state.
     *设置当前位置，当切换播放器在暂停暂停，通常被使用
     * @param pos position in the stream
     *流中位置
     */
    void setCurrentStreamPosition(int pos);

    /**
     * Query the underlying stream and update the internal last known stream position.
     *
     * 查询底层流和更新内部最后已知的流的位置
     */
    void updateLastKnownStreamPosition();


    /**
     * @param item to play
     *       需要播放的项目
     */
    void play(MediaSessionCompat.QueueItem item);


    /**
     *pause the current playing item
     */
    void  pause();
    /**
     * Seek to the given position
     *
     *快进到给定的位置
     */
    void seekTo(int position);

    /**
     * Set the current mediaId. This is only used when switching from one
     * playback to another.
     *设置当前媒体id，只在一个播放切换到另一个播放才使用
     * @param mediaId to be set as the current.
     *
     */
    void setCurrentMediaId(String mediaId);

    /**
     *
     * @return the current media Id being processed in any state or null.
     *
     * 返回Media id正在被处理的播放。在任何状态或者没有状态都可以
     */
    String getCurrentMediaId();


    interface  Callback{

        /**
         * On current music completed.
         * 当前音乐已经被完成
         *
         */
        void onCompletion();
        /**
         * on Playback status changed
         * Implementations can use this callback to update
         * playback state on the media sessions.
         * 在播放状态改变实现类能使用这个回调去更新播放状态在媒体回话时
         */
        void onPlaybackStatusChanged(int state);
        /**
         * @param error to be added to the PlaybackState
         *  错误被添加到播放状态
         */
        void onError(String error);
        /**
         * @param mediaId being currently played
         *设置mediaId正在被播放
         */
        void setCurrentMediaId(String mediaId);


    }

    /**
     * @param callback to be called
     *  回调被调用
     */
    void setCallback(Callback callback);
}
