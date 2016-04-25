package com.superdan.app.aileplayer.utils;

import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;

import com.superdan.app.aileplayer.VoiceSearchParams;
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
    public static List<MediaSessionCompat.QueueItem> getPalyingQueue(String mediaId, MusicProvider musicProvider){
        // extract the browsing hierarchy from the media ID:
        String[]hierarchy=MediaIDHelper.getHierarchy(mediaId);
        if(hierarchy.length!=2){
            LogHelper.e(TAG,"Could not build a playing queue for this mediaId",mediaId);
            return  null;
        }
        String categoryType=hierarchy[0];
        String categoryValue=hierarchy[1];
        LogHelper.d(TAG,"creating playing queue for ",categoryType,", ",categoryValue);
        Iterable<MediaMetadataCompat>tracks=null;
        // This sample only supports genre and by_search category types.
        if(categoryType.equals(MediaIDHelper.MEDIA_ID_MUSIC_BY_GENRE)){
            tracks=musicProvider.getMusicByGenre(categoryValue);
        }else if(categoryType.equals(MediaIDHelper.MEDIA_ID_MUSIC_BY_SEARCH)){
            tracks=musicProvider.searchMusicBySongTitle(categoryValue);
        }
        if (tracks==null){
            LogHelper.e(TAG,"Unrecognized category type: ",categoryType," for media ",mediaId);
            return  null;
        }
        return  convertToQueue(tracks,hierarchy[0],hierarchy[1]);

    }


    public static List<MediaSessionCompat.QueueItem>getPlayingQueueFromSearch(String query, Bundle queueParams,MusicProvider musicProvider){

        LogHelper.d(TAG,"Creating playing queue for musics from search: ",query," params:",queueParams)
        VoiceSearchParams params=new VoiceSearchParams(query,queueParams);
        LogHelper.e(TAG,"VoiceSearchParams: ",params);

        if(params.isAny){
            // If isAny is true, we will play anything. This is app-dependent, and can be,
            // for example, favorite playlists, "I'm feeling lucky", most recent, etc.
            return getRandomQueue(musicProvider);
        }
        Iterable<MediaMetadataCompat>result=null;
        if(params.isAlbumFocus){
            result=musicProvider.searchMusicByAlbum(params.album);
        }else if(params.isGenreFocus){
            result=musicProvider.getMusicByGenre(params.genre);
        }else if(params.isArtistFocus){
            result=musicProvider.searchMusicArtist(params.artist);
        }else if(params.isSongFocus){
            result=musicProvider.searchMusicBysongTitle(params.song);
        }
        // If there was no results using media focus parameter, we do an unstructured query.
        // This is useful when the user is searching for something that looks like an artist
        // to Google, for example, but is not. For example, a user searching for Madonna on
        // a PodCast application wouldn't get results if we only looked at the
        // Artist (podcast author). Then, we can instead do an unstructured search.
        if(params.isUnstructed||result==null||!result.iterator().hasNext()){
            // To keep it simple for this example, we do unstructured searches on the
            // song title only. A real world application could search on other fields as well.
            result=musicProvider.searchMusicBySongTitle(query);
        }
        return  convertToQueue(result,MediaIDHelper.MEDIA_ID_MUSIC_BY_SEARCH,query);

    }


    /**
     * 获得队列中 mediaID所对应的序列号
     * @param queue
     * @param mediaID
     * @return
     */
    public static int getMusicIndexOnQueue(Iterable<MediaSessionCompat.QueueItem>queue,String mediaID){

        int index=0;
        for(MediaSessionCompat.QueueItem item:queue){
            if(mediaID.equals(item.getDescription().getMediaId())){
                return  index;
            }
            index++;
        }
        return -1;
    }

    public static  int getMusicIndexQueue(Iterable<MediaSessionCompat.QueueItem>queue,long queueId){


        int index=0;
        for(MediaSessionCompat.QueueItem item:queue){
            if(queueId==item.getQueueId()){
                return  index;
            }
            index++;
        }
        return -1;

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

            MediaSessionCompat.QueueItem item=new MediaSessionCompat.QueueItem(trackCopy.getDescription(),count++);
            queue.add(item);


        }
            return queue;

    }
    /**
     * Create a random queue with at most {@link #RANDOM_QUEUE_SIZE} elements.
     *
     * @param musicProvider the provider used for fetching music.
     * @return list containing {@link MediaSessionCompat.QueueItem}'s
     */

    public static  List<MediaSessionCompat.QueueItem>getRandomQueue(MusicProvider musicProvider){
        List<MediaMetadataCompat>result=new ArrayList<>(RANDOM_QUEUE_SIZE);
        Iterable<MediaMetadataCompat>shuffled=musicProvider.getShuffleMusic();
        for(MediaMetadataCompat metadataCompat:shuffled){
            if(result.size()==RANDOM_QUEUE_SIZE){
                break;
            }
            result.add(metadataCompat);
        }
        LogHelper.d(TAG,"getRandomQueue:result.size ",result.size());
        return  convertToQueue(result,MediaIDHelper.MEDIA_ID_MUSIC_BY_SEARCH,"random");
    }

public  static boolean isIndexPlayable(int index,List<MediaSessionCompat.QueueItem>queueItems){
    return (queueItems!=null&&index>=0&&index<queueItems.size());
}


}
