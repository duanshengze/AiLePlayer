package com.superdan.app.aileplayer;

/**
 * Created by Administrator on 2016/4/9.
 */


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;

import com.superdan.app.aileplayer.playback.PlaybackManager;
import com.superdan.app.aileplayer.utils.LogHelper;

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

public class MusicService extends MediaBrowserServiceCompat implements PlaybackManager{



    private static final String TAG= LogHelper.makeLogTag(MusicService.class);

    public static final String EXTRA_CONNECTED_CAST="com.superdan.app.aileplayer.CAST_NAME";

    @Nullable
    @Override
    public BrowserRoot onGetRoot(String clientPackageName, int clientUid, Bundle rootHints) {
        return null;
    }

    @Override
    public void onLoadChildren(String parentId, Result<List<MediaBrowserCompat.MediaItem>> result) {

    }
}
