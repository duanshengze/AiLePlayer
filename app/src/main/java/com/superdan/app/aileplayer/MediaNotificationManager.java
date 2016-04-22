package com.superdan.app.aileplayer;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.superdan.app.aileplayer.utils.LogHelper;

/**
 * Created by Administrator on 2016/4/22.
 *
 */
/**
 * Keeps track of a notification and updates it automatically for a given
 * MediaSession. Maintaining a visible notification (usually) guarantees that the music service
 * won't be killed during playback.
 */
public class MediaNotificationManager extends BroadcastReceiver {

    private  static final String TAG= LogHelper.makeLogTag(MediaNotificationManager.class);

    public static final String APP_PACKAGE="com.superdan.app.aileplayer";
    private static  final  int NOTIFICATION_ID=412;

    private static  final  int REQUEST_CODE=100;

    public static final  String ACTION_PAUSE=APP_PACKAGE+".pause";

    public static  final  String  ACTION_PREV=APP_PACKAGE+".prev";

    public static  final  String ACTION_NEXT=APP_PACKAGE+".next";

    public  static  final  String ACTION_STOP_CASTING=APP_PACKAGE+".stop_cast";

    private  final  MusicService mService;

    private MediaSessionCompat mSessionCompat;

    private  MediaControllerCompat mController;
    private MediaControllerCompat.TransportControls mTransportControls;

    private PlaybackStateCompat mPlaybackState;

    private MediaMetadataCompat mMetadata;

    private final NotificationManager mNotificationManager;

    private final PendingIntent mPauseIntent;
    private final PendingIntent mPlayIntent;
    private final  PendingIntent mPreviousIntent;
    private final  PendingIntent mNextIntent;

    private final  PendingIntent mStopCastIntent;

    private final int mNotificationColor;

    private boolean mStarted=false;

    public MediaNotificationManager(MusicService service){

        mService=service;


    }


    /**
     * Update the state based on a change on the session token. Called either when
     * we are running for the first time or when the media session owner has destroyed the session
     * (see {@link android.media.session.MediaController.Callback#onSessionDestroyed()})
     */









    @Override
    public void onReceive(Context context, Intent intent) {

    }
}
