package com.superdan.app.aileplayer.model;

import android.support.v4.media.MediaMetadataCompat;

import com.superdan.app.aileplayer.utils.LogHelper;

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
        this(new );
    }


    public MusicProvider(MusicProviderSource source){
        mSource=source;
        mMusicListByGenre=new ConcurrentHashMap<>();
        mMusicListById=new ConcurrentHashMap<>();
        mFavoriteTracks= Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    }


}
