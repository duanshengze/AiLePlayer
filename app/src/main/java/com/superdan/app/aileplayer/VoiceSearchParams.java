package com.superdan.app.aileplayer;

import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;

/**
 * For more information about voice search parameters,
 * check https://developer.android.com/guide/components/intents-common.html#PlaySearch
 */
public final class VoiceSearchParams {
    public final String query;
    public boolean isAny;
    public  boolean isUnstructured;
    public boolean isGenereFocus;
    public  boolean isArtistFocus;
    public boolean isAlbumFocus;
    public boolean isSongFocus;
    public String genre;
    public String artist;
    public String album;
    public String song;

    /**
     * Creates a simple object describing the search criteria from the query and extras.
     * @param query the query parameter from a voice search
     * @param extras the extras parameter from a voice search
     */
    public  VoiceSearchParams(String query, Bundle extras){

        this.query=query;
        if(TextUtils.isEmpty(query)){
            //如“播放音乐”的通用搜索将空查询
            isAny=true;
        }else {
            if(extras==null){
                isUnstructured=true;
            }else {
                String genreKey;
                if(Build.VERSION.SDK_INT>=21){
                    genreKey= MediaStore.EXTRA_MEDIA_GENRE;
                }else {
                    genreKey="android.intent.extra.genre";
                }
            }






        }


    }

    @Override
    public String toString() {
        return "query="+query
                +" isAny="+isAny
                +" isUnstructures="+isUnstructured
                +" isGenreFocus="+isGenereFocus
                +" isArtistFocus="+isArtistFocus
                +" isSongFocus="+isSongFocus
                +" genre="+genre
                + "artist="+artist
                +" album="+album
                +" song="+song;


    }
}
