package com.superdan.app.aileplayer.playback;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;

import com.superdan.app.aileplayer.model.MusicProvider;
import com.superdan.app.aileplayer.utils.LogHelper;
import com.superdan.app.aileplayer.utils.MediaIDHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * Simple data provider for queues. Keeps track of a current queue and a current index in the
 * queue. Also provides methods to set the current queue based on common queries, relying on a
 * given MusicProvider to provide the actual media metadata.
 */
public class QueueManager {

    private static final String TAG = LogHelper.makeLogTag(QueueManager.class);

    private MusicProvider mMusicProvider;
    private MetadataUpdateListener mListener;
    private Resources mResources;

    // "Now playing" queue:
    //现在播放队列
    private List<MediaSessionCompat.QueueItem>mPlayingQueue;
    private int mCurrentIndex;
    public QueueManager(@NonNull MusicProvider musicProvider,@NonNull Resources resources,@NonNull MetadataUpdateListener listener){

        mMusicProvider=musicProvider;
        mListener=listener;
        mResources=resources;
        mPlayingQueue= Collections.synchronizedList(new ArrayList<MediaSessionCompat.QueueItem>());
        mCurrentIndex=0;

    }

    public  boolean isSomeBrowsingCategory(@NonNull String mediaId){
        String[]newBrowHierarchy= MediaIDHelper.getHierarchy(mediaId);
        //A single item that is part of the play queue.
        // It contains a description of the item and its id in the queue.
        MediaSessionCompat.QueueItem curenent=getCurrentMusic();
        if (curenent==null){
            return  false;
        }
        String[]currentBrowseHierarchy=MediaIDHelper.getHierarchy(curenent.getDescription().getMediaId());

        return Arrays.equals(currentBrowseHierarchy,newBrowHierarchy);

    }



    public  MediaSessionCompat.QueueItem getCurrentMusic(){

        if(!)

    }


    public interface MetadataUpdateListener{
        void onMetadatChanged(MediaMetadataCompat metadata);

        void onMetadataRetrieveError();

        void onCurrentQueueIndexUpdated(int queueIndex);

        void onQueueUpdated(String title, List<MediaSessionCompat.QueueItem>newQueue);



    }

}
