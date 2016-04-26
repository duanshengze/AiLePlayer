package com.superdan.app.aileplayer.playback;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;

import com.superdan.app.aileplayer.AlbumArtCache;
import com.superdan.app.aileplayer.R;
import com.superdan.app.aileplayer.model.MusicProvider;
import com.superdan.app.aileplayer.utils.LogHelper;
import com.superdan.app.aileplayer.utils.MediaIDHelper;
import com.superdan.app.aileplayer.utils.QueueHelper;

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

    public  boolean isSameBrowsingCategory(@NonNull String mediaId){
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

    private  void setCurrentQueueIndex(int index){
        if (index>=0&&index<mPlayingQueue.size()){
            mCurrentIndex=index;
            mListener.onCurrentQueueIndexUpdated(mCurrentIndex);
        }
    }

    public  boolean setCurrentQueueItem(String mediaId){
        //为音乐id在队列中设置id
        int index=QueueHelper.getMusicIndexOnQueue(mPlayingQueue,mediaId);
        setCurrentQueueIndex(index);
        return  index>=0;
    }

    public boolean setCurrentQueueItem(long mediaId){
        int index=QueueHelper.getMusicIndexOnQueue(mPlayingQueue,mediaId);
        setCurrentQueueIndex(index);
        return  index>=0;
    }

    protected void setQueueFromSearch(String query, Bundle extras){
        setCurrentQueue(mResources.getString(R.string.search_queue_title),QueueHelper.getPlayingQueueFromSearch(query,extras,mMusicProvider));

    }


    public  void setQueueFromMusic(String mediaId){
        LogHelper.d(TAG,"setQueueFromMusic",mediaId);
        // The mediaId used here is not the unique musicId. This one comes from the
        // MediaBrowser, and is actually a "hierarchy-aware mediaID": a concatenation of
        // the hierarchy in MediaBrowser and the actual unique musicID. This is necessary
        // so we can build the correct playing queue, based on where the track was
        // selected from.
        //这里使用的mediaId，不是独一的音乐Id，这来自媒体浏览，实际上是层次 可知的mediaID
        //一串层次在MediaBrowser 并且实际独一的MusicID，这是必须的，我可以建立正确的播放列表
        boolean canReuseQueue=false;

        if(isSameBrowsingCategory(mediaId)){
            canReuseQueue=setCurrentQueueItem(mediaId);
        }
        if(!canReuseQueue){
            String queueTitle=mResources.getString(R.string.browse_musics_by_genres_subtitle,
                    MediaIDHelper.extractBrowseCategoryValueFromMediaID(mediaId)
                    );
            setCurrentQueue(queueTitle,QueueHelper.getPalyingQueue(mediaId,mMusicProvider),mediaId);
        }
        updateMetadata();

    }

    public boolean skipQueuePosition(int amount){
        int index=mCurrentIndex+amount;
        if(index<0){
            //当歌曲向会跳一首时，如果在第一首歌曲则将播放第一首歌
            index=0;
        }else {
            //保持轮询
            index%=mPlayingQueue.size();
        }
        if (!QueueHelper.isIndexPlayable(index,mPlayingQueue)){
            LogHelper.e(TAG,"Cannot increment queue index by ",amount," ,Current=",mCurrentIndex,
                    " queue length= ",mPlayingQueue.size());
            return  false;
        }
        mCurrentIndex=index;
        return  true;
    }
    public  MediaSessionCompat.QueueItem getCurrentMusic(){

        if(!QueueHelper.isIndexPlayable(mCurrentIndex,mPlayingQueue)){
            return  null;
        }
        return  mPlayingQueue.get(mCurrentIndex);

    }

    public void setRandomQueue(){
        setCurrentQueue(mResources.getString(R.string.random_queue_title),QueueHelper.getRandomQueue(mMusicProvider));
    }



    protected  void setCurrentQueue(String title,List<MediaSessionCompat.QueueItem>newQueue){
        setCurrentQueue(title,newQueue,null);
    }

    protected  void setCurrentQueue(String title,List<MediaSessionCompat.QueueItem>newQueue,String initialMediaId){
        mPlayingQueue=newQueue;
        int index=0;
        if (initialMediaId!=null){
            index=QueueHelper.getMusicIndexOnQueue(mPlayingQueue,initialMediaId);
        }
        mCurrentIndex=Math.max(index,0);
        mListener.onQueueUpdated(title,newQueue);

    }


    /**
     * 更新媒体数据，当前列表歌曲信息（专辑封面下载）
     */
    public  void updateMetadata(){

        MediaSessionCompat.QueueItem currentMusic=getCurrentMusic();
        if(currentMusic==null){
            mListener.onMetadataRetrieveError();
            return;
        }
        final  String musicId=MediaIDHelper.extractMusicIDFromMediaID(currentMusic.getDescription().getMediaId());
        MediaMetadataCompat metadata=mMusicProvider.getMusic(musicId);
        if (metadata==null){
            throw  new IllegalArgumentException("Invoid musicId"+musicId);
        }
        mListener.onMetadatChanged(metadata);
        //设置媒体会话合适的专辑封面，所以它可以显示在锁屏和其他地方
        if(metadata.getDescription().getIconBitmap()==null&&metadata.getDescription().getIconUri()!=null){
            String albumUri=metadata.getDescription().getIconUri().toString();
            AlbumArtCache.getInstance().fetch(albumUri, new AlbumArtCache.FetchListener() {
                @Override
                public void onFecthed(String artUrl, Bitmap bitImgae, Bitmap iconImage) {
                    //如果我们仍然播放同一首歌，通知监听者
                    MediaSessionCompat.QueueItem currentMusic=getCurrentMusic();
                    if(currentMusic==null){
                        return;
                    }
                    String currentPlayingId=MediaIDHelper.extractMusicIDFromMediaID(currentMusic.getDescription().getMediaId());
                    if(musicId.equals(currentPlayingId)){
                        mListener.onMetadatChanged(mMusicProvider.getMusic(currentPlayingId));
                    }
                }
            });



        }

    }

    public interface MetadataUpdateListener{
        void onMetadatChanged(MediaMetadataCompat metadata);

        void onMetadataRetrieveError();

        void onCurrentQueueIndexUpdated(int queueIndex);

        void onQueueUpdated(String title, List<MediaSessionCompat.QueueItem>newQueue);



    }

}
