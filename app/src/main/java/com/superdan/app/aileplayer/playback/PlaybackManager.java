package com.superdan.app.aileplayer.playback;

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
}
