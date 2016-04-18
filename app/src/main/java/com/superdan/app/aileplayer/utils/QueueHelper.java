package com.superdan.app.aileplayer.utils;

import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;

import com.superdan.app.aileplayer.model.MusicProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dsz on 16/4/17.
 * Utility class to help on queue related tasks.
 */
public class QueueHelper {

    private static  final  String TAG=LogHelper.makeLogTag(QueueHelper.class);
    private static  final int RANDOM_QUEUE_SIZE=10;
    public static List<MediaSessionCompat.QueueItem> getPalyinfQueue(String mediaId, MusicProvider musicProvider){





    }


    private static List<MediaSessionCompat.QueueItem>convertToQueue(Iterable<MediaMetadataCompat>tracks,String...categories){
        List<MediaSessionCompat.QueueItem>queue=new ArrayList<>();
        int count=0;
        for(MediaMetadataCompat track:tracks){
            // We create a hierarchy-aware mediaID, so we know what the queue is about by looking
            // at the QueueItem media IDs.
            String hierarchyAwareMediaID=MediaIDHelper.createMediaID(track.getDescription().getMediaId(),categories);
            MediaMetadataCompat trackCopy=new MediaMetadataCompat.Builder(track)
                                                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID,hierarchyAwareMediaID)
                    .build();
            // We don't expect queues to change after created, so we use the item index as the
            // queueId. Any other number unique in the queue would work.
            //我们不期望改变在创建队列后，因此我们使用元素索引做为队列的id，任何其他独一无二的数字也可以。


        }


    }

}
