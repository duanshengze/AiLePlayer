package com.superdan.app.aileplayer.model;

import android.support.v4.media.MediaMetadataCompat;

import com.superdan.app.aileplayer.utils.LogHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by dsz on 16/4/10.
 * 音乐曲目的简单的数据提供者。时间元数据源被托管给一个MusicProviderSource（通过构造函数参数定义）
 *
 */
public class MusicProvider {
    private static final String TAG= LogHelper.makeLogTag(MusicProvider.class);


    private MusicProviderSource mSource;

    //为音乐曲目数据分类缓存
    private ConcurrentMap<String,List<MediaMetadataCompat>>mMusicListByGenre;

    private final ConcurrentMap<String,MutableMediaMetadata>mMusicListById;

    private final Set<String> mFavoriteTracks;


    enum State{

        NON_INITIALIZED,INITIALIZING,INITIALIZED
    }

    private volatile State mCurrentState=State.NON_INITIALIZED;

    public interface Callback{
        void onMusicCatalogReady(boolean sucess);
    }


    public  MusicProvider(){
        this(new RemoteJSONSource() );
    }


    public MusicProvider(MusicProviderSource source){
        mSource=source;
        mMusicListByGenre=new ConcurrentHashMap<>();
        mMusicListById=new ConcurrentHashMap<>();
        mFavoriteTracks= Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    }


    /**
     *获得一个iterator 通过所有歌曲的集合
     */
    public Iterable<MediaMetadataCompat>getShuffleMusic(){

        if(mCurrentState!=State.INITIALIZED){
            //Returns a type-safe empty, immutable List.
            //返回一个安全的空，不可变列表
            return Collections.emptyList();
        }

// Constructs a new instance of {@code ArrayList} with the specified initial capacity
        //指定初始容量

        List<MediaMetadataCompat>shuffled=new ArrayList<>(mMusicListById.size());
        for(MutableMediaMetadata mutableMediaMetadata:mMusicListById.values()){
            shuffled.add(mutableMediaMetadata.metadata);
        }
        //Moves every element of the list to a random new position in the list.
        //列表的每个元素移动到列表中的随机的新位置
        Collections.shuffle(shuffled);
        return shuffled;
    }


    /**
     * Get music tracks of the given genre
     *获取给定题材的音乐曲目
     */
    public Iterable<MediaMetadataCompat>getMusicByGenre(String genre){
        if(mCurrentState!=State.INITIALIZED||!mMusicListByGenre.containsKey(genre)){
            return  Collections.emptyList();
        }
            return mMusicListByGenre.get(genre);


    }







}
