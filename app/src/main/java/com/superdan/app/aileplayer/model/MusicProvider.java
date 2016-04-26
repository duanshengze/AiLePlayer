package com.superdan.app.aileplayer.model;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.media.MediaMetadata;
import android.media.MediaMetadataRetriever;
import android.media.browse.MediaBrowser;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;

import com.superdan.app.aileplayer.R;
import com.superdan.app.aileplayer.utils.LogHelper;
import com.superdan.app.aileplayer.utils.MediaIDHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by dsz on 16/4/10.
 * 音乐曲目的简单的数据提供者。数据源被托管给一个MusicProviderSource（通过构造函数参数定义）
 */
public class MusicProvider {
    private static final String TAG = LogHelper.makeLogTag(MusicProvider.class);


    private MusicProviderSource mSource;

    //为音乐曲目数据分类缓存
    private ConcurrentMap<String, List<MediaMetadataCompat>> mMusicListByGenre;

    private final ConcurrentMap<String, MutableMediaMetadata> mMusicListById;

    private final Set<String> mFavoriteTracks;


    enum State {

        NON_INITIALIZED, INITIALIZING, INITIALIZED
    }

    private volatile State mCurrentState = State.NON_INITIALIZED;

    public interface Callback {
        void onMusicCatalogReady(boolean success);
    }


    public MusicProvider() {
        this(new RemoteJSONSource());
    }


    public MusicProvider(MusicProviderSource source) {
        mSource = source;
        mMusicListByGenre = new ConcurrentHashMap<>();
        mMusicListById = new ConcurrentHashMap<>();
        mFavoriteTracks = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    }


    /**
     * 获得迭代器 关于流派列表的
     *
     * @return genres
     */
    public Iterable<String> getGenres() {
        if (mCurrentState != State.INITIALIZED) {
            return Collections.emptyList();
        }
        return mMusicListByGenre.keySet();
    }


    /**
     * 获得一个iterator 通过所有歌曲的集合
     */
    public Iterable<MediaMetadataCompat> getShuffleMusic() {

        if (mCurrentState != State.INITIALIZED) {
            //Returns a type-safe empty, immutable List.
            //返回一个安全的空，不可变列表
            return Collections.emptyList();
        }

// Constructs a new instance of {@code ArrayList} with the specified initial capacity
        //指定初始容量

        List<MediaMetadataCompat> shuffled = new ArrayList<>(mMusicListById.size());
        for (MutableMediaMetadata mutableMediaMetadata : mMusicListById.values()) {
            shuffled.add(mutableMediaMetadata.metadata);
        }
        //Moves every element of the list to a random new position in the list.
        //列表的每个元素移动到列表中的随机的新位置
        Collections.shuffle(shuffled);
        return shuffled;
    }


    /**
     * Get music tracks of the given genre
     * 获取给定题材的音乐曲目
     */
    public Iterable<MediaMetadataCompat> getMusicByGenre(String genre) {
        if (mCurrentState != State.INITIALIZED || !mMusicListByGenre.containsKey(genre)) {
            return Collections.emptyList();
        }
        return mMusicListByGenre.get(genre);


    }


    public Iterable<MediaMetadataCompat> searchMusicBySongTitle(String query) {
        return searchMusic(MediaMetadataCompat.METADATA_KEY_TITLE, query);
    }


    public Iterable<MediaMetadataCompat> searchMusicByAlbum(String query) {
        return searchMusic(MediaMetadataCompat.METADATA_KEY_ALBUM, query);
    }

    public Iterable<MediaMetadataCompat> searchMusicByArtisit(String query) {
        return searchMusic(MediaMetadataCompat.METADATA_KEY_ARTIST, query);
    }

    /**
     * @param metadataField
     * @param query
     * @return
     */
    Iterable<MediaMetadataCompat> searchMusic(String metadataField, String query) {
        if (mCurrentState != State.INITIALIZED) {
            return Collections.emptyList();
        }
        ArrayList<MediaMetadataCompat> result = new ArrayList<>();
        query = query.toLowerCase(Locale.UK);
        for (MutableMediaMetadata track : mMusicListById.values()) {
            if (track.metadata.getString(metadataField).toLowerCase(Locale.US).contains(query)) {
                result.add(track.metadata);
            }
        }
        return result;
    }


    public MediaMetadataCompat getMusic(String musicId) {
        return mMusicListById.containsKey(musicId) ? mMusicListById.get(musicId).metadata : null;
    }

    public synchronized void updateMusicArt(String musicId, Bitmap ablumArt, Bitmap icon) {
        MediaMetadataCompat metadata = getMusic(musicId);
        metadata = new MediaMetadataCompat.Builder(metadata)
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM, ablumArt)
                .putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, icon)
                .build();

        MutableMediaMetadata mutableMediaMetadata = mMusicListById.get(musicId);
        if (mutableMediaMetadata == null) {
            throw new IllegalStateException("Unexpected error :Inconsistent data structure in" + "MusicProvider");

        }
        mutableMediaMetadata.metadata = metadata;


    }


    public void setFavorite(String musicId, boolean favorite) {
        if (favorite) {
            mFavoriteTracks.add(musicId);
        } else {
            mFavoriteTracks.remove(musicId);
        }

    }


    public boolean isFavorite(String musicId) {
        return mFavoriteTracks.contains(musicId);
    }

    /**
     * 获得音乐队列 从服务，并缓存音乐队列信息在缓存用于以后，将musicId作为键，通过类型组合。
     *
     * @param callback
     */
    public void retrieveMedisAsync(final Callback callback) {
        LogHelper.d(TAG, "retrieveMedisSync called");
        if (mCurrentState == State.INITIALIZED) {
            if (callback != null) {
                callback.onMusicCatalogReady(true);
            }
            return;
        }

        new AsyncTask<Void, Void, State>() {

            @Override
            protected State doInBackground(Void... params) {
                retrieveMedia();
                return mCurrentState;
            }

            @Override
            protected void onPostExecute(State state) {
                if (callback != null) {
                    callback.onMusicCatalogReady(state == State.INITIALIZED);
                }
            }
        }.execute();


    }

    private synchronized void buildListByGenre() {
        ConcurrentMap<String, List<MediaMetadataCompat>> newMusicListByGenre = new ConcurrentHashMap<>();
        for (MutableMediaMetadata m : mMusicListById.values()) {
            String genre = m.metadata.getString(MediaMetadataCompat.METADATA_KEY_GENRE);
            List<MediaMetadataCompat> list = newMusicListByGenre.get(genre);
            if (list == null) {
                list = new ArrayList<>();
                newMusicListByGenre.put(genre, list);
            }
            list.add(m.metadata);
        }
        mMusicListByGenre = newMusicListByGenre;
    }


    private synchronized void retrieveMedia() {
        try {


            if (mCurrentState == State.NON_INITIALIZED) {
                mCurrentState = State.INITIALIZING;
                Iterator<MediaMetadataCompat> tracks = mSource.iterator();
                while (tracks.hasNext()) {
                    MediaMetadataCompat item = tracks.next();
                    String musicId = item.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
                    mMusicListById.put(musicId, new MutableMediaMetadata(musicId, item));
                }
                buildListByGenre();
                mCurrentState = State.INITIALIZED;
            }
        }finally {


                if (mCurrentState!=State.INITIALIZED){
                    mCurrentState=State.NON_INITIALIZED;
                }

        }// end finally

    }

    public  List<MediaBrowserCompat.MediaItem>getChildren(String mediaId, Resources resources){
        List<MediaBrowserCompat.MediaItem>mediaItems=new ArrayList<>();
        if (!MediaIDHelper.isBrowseable(mediaId)) {
            return  mediaItems;
        }
        if (MediaIDHelper.MEDIA_ID_ROOT.equals(mediaId)){

            mediaItems.add(createBrowsableMediaItemForRoot(resources));
        }else if (MediaIDHelper.MEDIA_ID_MUSIC_BY_GENRE.equals(mediaId)){
            for(String genre:getGenres()){
                mediaItems.add(createbrowsableMediaItemForGenre(genre,resources));
            }
        }else



    }
    private  MediaBrowserCompat.MediaItem createBrowsableMediaItemForRoot(Resources resources){
        MediaDescriptionCompat description=new MediaDescriptionCompat.Builder()
                .setMediaId(MediaIDHelper.MEDIA_ID_MUSIC_BY_GENRE)
                .setTitle(resources.getString(R.string.browse_genres))
                .setSubtitle(resources.getString(R.string.browse_genre_subtitle))
                .setIconUri(Uri.parse("android.resource://" +
                        "com.superdan.app.aileplayer/mipmap/ic_by_genre"))
                .build();
        return  new MediaBrowserCompat.MediaItem(description,MediaBrowserCompat.MediaItem.FLAG_BROWSABLE);

    }


    private  MediaBrowserCompat.MediaItem createbrowsableMediaItemForGenre(String genre,Resources resources){

        MediaDescriptionCompat description=new MediaDescriptionCompat.Builder()
                .setMediaId(MediaIDHelper.createMediaID(null,MediaIDHelper.MEDIA_ID_MUSIC_BY_GENRE,genre))
                .setTitle(genre)
                .setSubtitle(resources.getString(R.string.browse_musics_by_genres_subtitle,genre))
                .build();
        return  new MediaBrowserCompat.MediaItem(description, MediaBrowserCompat.MediaItem.FLAG_BROWSABLE);

    }


    private MediaBrowserCompat.MediaItem createMediaItem(MediaMetadataCompat metadata){
        // Since mediaMetadata fields are immutable, we need to create a copy, so we
        // can set a hierarchy-aware mediaID. We will need to know the media hierarchy
        // when we get a onPlayFromMusicID call, so we can create the proper queue based
        // on where the music was selected from (by artist, by genre, random, etc)

        String genre=metadata.getString(MediaMetadataCompat.METADATA_KEY_GENRE);
        String hierarchyAwareMediaID=MediaIDHelper.createMediaID(metadata.getDescription().getMediaId(),MediaIDHelper.MEDIA_ID_MUSIC_BY_GENRE,genre);
        MediaMetadataCompat copy=new MediaMetadataCompat.Builder(metadata)
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID,hierarchyAwareMediaID)
                .build();
        return new MediaBrowserCompat.MediaItem(copy.getDescription(), MediaBrowser.MediaItem.FLAG_PLAYABLE);

    }




}
