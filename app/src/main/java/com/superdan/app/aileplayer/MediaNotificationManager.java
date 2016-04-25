package com.superdan.app.aileplayer;



import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.superdan.app.aileplayer.utils.LogHelper;
import com.superdan.app.aileplayer.utils.ResourceHelper;

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

    public static final  String ACTION_PLAY=APP_PACKAGE+".play";

    public static  final  String  ACTION_PREV=APP_PACKAGE+".prev";

    public static  final  String ACTION_NEXT=APP_PACKAGE+".next";

    public  static  final  String ACTION_STOP_CASTING=APP_PACKAGE+".stop_cast";

    private  final  MusicService mService;

    private MediaSessionCompat.Token mSessionToken;

    private  MediaControllerCompat mController;

    private MediaControllerCompat.TransportControls mTransportControls;

    private PlaybackStateCompat mPlaybackState;

    private MediaMetadataCompat mMetadata;

    private final NotificationManagerCompat mNotificationManager;

    private final PendingIntent mPauseIntent;
    private final PendingIntent mPlayIntent;
    private final  PendingIntent mPreviousIntent;
    private final  PendingIntent mNextIntent;

    private final  PendingIntent mStopCastIntent;

    private final int mNotificationColor;

    private boolean mStarted=false;

    public MediaNotificationManager(MusicService service){

        mService=service;
        updateSessionToken();
        mNotificationColor= ResourceHelper.getThemeColor(mService,R.attr.colorPrimary, Color.DKGRAY);

        mNotificationManager= NotificationManagerCompat.from(service);
        String pkg=mService.getPackageName();
        mPauseIntent=PendingIntent.getBroadcast(mService,REQUEST_CODE,new Intent(ACTION_PAUSE).setPackage(pkg),PendingIntent.FLAG_CANCEL_CURRENT);

        mPlayIntent=PendingIntent.getBroadcast(mService,REQUEST_CODE,new Intent(ACTION_PLAY).setPackage(pkg),PendingIntent.FLAG_CANCEL_CURRENT);
        mPreviousIntent=PendingIntent.getBroadcast(mService,REQUEST_CODE,new Intent(ACTION_PREV).setPackage(pkg),PendingIntent.FLAG_CANCEL_CURRENT);

        mNextIntent=PendingIntent.getBroadcast(mService,REQUEST_CODE,new Intent(ACTION_NEXT).setPackage(pkg),PendingIntent.FLAG_CANCEL_CURRENT);

        mStopCastIntent=PendingIntent.getBroadcast(mService,REQUEST_CODE,new Intent(ACTION_STOP_CASTING).setPackage(pkg),PendingIntent.FLAG_CANCEL_CURRENT);


        // Cancel all notifications to handle the case where the Service was killed and
        // restarted by the system.
        mNotificationManager.cancelAll();


    }

    /**
     * Posts the notification and starts tracking the session to keep it
     * updated. The notification will automatically be removed if the session is
     * destroyed before {@link #stopNotification} is called.
     */
    private  void startNotification(){

        if(!mStarted){
            mMetadata=mController.getMetadata();
            mPlaybackState=mController.getPlaybackState();
            // The notification must be updated after setting started to true
        }

    }


    /**
     * Update the state based on a change on the session token. Called either when
     * we are running for the first time or when the media session owner has destroyed the session
     * (see {@link android.media.session.MediaController.Callback#onSessionDestroyed()})
     */





    private Notification createNotication(){

        LogHelper.d(TAG,"updateNotificationMetadata.mMedate="+mMetadata);
        if(mMetadata==null||mPlaybackState==null){
            return  null;
        }
        NotificationCompat.Builder notificationBuilder=new NotificationCompat.Builder(mService);
        int playPauseButtonPostion=0;
        // If skip to previous action is enabled
        if((mPlaybackState.getActions()&PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)!=0){

            notificationBuilder.addAction(R.drawable.ic_skip_previous_white_24dp,mService.getString(R.string.label_previous),mPreviousIntent);

            // If there is a "skip to previous" button, the play/pause button will
            // be the second one. We need to keep track of it, because the MediaStyle notification
            // requires to specify the index of the buttons (actions) that should be visible
            // when in compact view.
            playPauseButtonPostion=1;

        }

        addPlayPauseAction(notificationBuilder);

        if((mPlaybackState.getActions()&PlaybackStateCompat.ACTION_SKIP_TO_NEXT)!=0){


            notificationBuilder.addAction(R.drawable.ic_skip_previous_white_24dp,mService.getString(R.string.label_next),mNextIntent);

        }
        MediaDescriptionCompat description=mMetadata.getDescription();



        String fetchArtUrl=null;

        Bitmap art=null;
        if(description.getIconUri()!=null){
            // This sample assumes the iconUri will be a valid URL formatted String, but
            // it can actually be any valid Android Uri formatted String.
            // async fetch the album art icon
            String artUrl=description.getIconUri().toString();
            art=AlbumArtCache.getInstance().getBitImage(artUrl);
            if(art==null){

                fetchArtUrl=artUrl;
                // use a placeholder art while the remote art is being downloaded
                art= BitmapFactory.decodeResource(mService.getResources(),R.mipmap.ic_default_art);

            }
        }

        notificationBuilder.setStyle(new NotificationCompat.MediaStyle()
                                    .setShowActionsInCompactView(new int[]{playPauseButtonPostion})
                                    .setMediaSession(mSessionToken))
                .setColor(mNotificationColor)
                .setSmallIcon(R.mipmap.ic_notification)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setUsesChronometer(true)
                .setContentIntent(creatContentIntent(description))


        )



    }



    @Override
    public void onReceive(Context context, Intent intent) {

    }
}