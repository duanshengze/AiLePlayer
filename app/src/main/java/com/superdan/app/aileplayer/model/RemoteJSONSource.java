package com.superdan.app.aileplayer.model;


import android.support.v4.media.MediaMetadataCompat;

import com.superdan.app.aileplayer.utils.LogHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;


/**
 * Created by dsz on 16/4/10.
 *Utility class to get a list of MusicTrack's based on a server-side JSON
 * configuration.
 *实用类获得MusicTrack列表，基于服务端的JSON配置
 *
 */
public class RemoteJSONSource implements MusicProviderSource{
    private static final String TAG= LogHelper.makeLogTag(RemoteJSONSource.class);

    protected static final String CATALOG_URL="http://storage.googleapis.com/automotive-media/music.json";


    private static final String JSON_MUSIC="music";
    private static final String JSON_TITLE="title";
    // 唱片
    public static  final String JSON_ALNUM="album";
    private static final String JSON_ARTIST="artist";
    //类别
    private static final String JSON_GENRE="genre";

    //来源
    private static final String JSON_SOURCE="source";

    private static  final String JSON_IMAGE="image";
    //轨迹
    private static  final String JSON_TRACK_NUMBER="trackNumber";

    private static  final String JSON_TOTAL_TRACK_COUNT="totalTrackCount";

    private static  final String JSON_DURATION="duration";



    @Override
    public Iterator<MediaMetadataCompat> iterator() {

        try {

            /**
             *
             *@author duanshengze
             *created at 16/4/10 下午8:56
             *Returns the last index of the code point c, or -1.
             * The search starts at the end and moves towards the beginning of this string.
             *返回字符串总含有 '/'的最后一个的位置，内部实现方法是从字符串的最后开始搜索 找到第一个就是
             */
            int slashPos=CATALOG_URL.lastIndexOf('/');
            //第二个参数是不包含的 因此要加1
            String path=CATALOG_URL.substring(0,slashPos+1);
            JSONObject jsonObj=fetchJSONFromUrl(CATALOG_URL);
            ArrayList<MediaMetadataCompat>tracks=new ArrayList<>();
            if (jsonObj!=null){
                JSONArray jsonTracks=jsonObj.getJSONArray(JSON_MUSIC);


            }

            } catch (JSONException e) {
                e.printStackTrace();
            }


        return null;
    }

    /**
      * Download a JSON file from a server, parse the content and return the JSON
     * object.
     * 从服务端下载一个JSON文件，解析内容并返回JSON对象
      *@author duanshengze
      *created at 16/4/10 下午9:11
      *
      */
    private JSONObject fetchJSONFromUrl(String urlString) throws JSONException {

        BufferedReader read=null;
        try{
            URLConnection urlConnection=new URL(urlString).openConnection();
            read=new BufferedReader(new InputStreamReader(urlConnection.getInputStream(),"iso-8859-1"));
            //不同步的
            StringBuilder sb=new StringBuilder();
            String line;
            while ((line=read.readLine())!=null){
                sb.append(line);
            }
            return  new JSONObject(sb.toString());

        } catch (JSONException e) {
            throw  e;
        }catch (Exception e){
            LogHelper.e(TAG,"解析失败从媒体列表",e);
            return  null;
        }finally {
            if(read!=null){
                try {
                    read.close();
                }catch (IOException e){

                }
            }
        }


    }

    private MediaMetadataCompat buildFromJSON(JSONObject json,String basePath) throws JSONException {
        String title=json.getString(JSON_TITLE);
        String album=json.getString(JSON_ALNUM);
        String artist = json.getString(JSON_ARTIST);
        String artlist=json.getString(JSON_ARTIST);
//种类
        String genre=json.getString(JSON_GENRE);
        String source=json.getString(JSON_SOURCE);
        String iconUrl=json.getString(JSON_IMAGE);
        //曲目的号
        int trackNumber=json.getInt(JSON_TRACK_NUMBER);
        //曲目的总数
        int totalTrackCount=json.getInt(JSON_TOTAL_TRACK_COUNT);
        int duration=json.getInt(JSON_DURATION)*1000;//ms

        LogHelper.d(TAG,"发现音乐曲目：",json);

        //媒体被储存为json文件
        if(!source.startsWith("http")){
            source=basePath+source;
        }
        if (!iconUrl.startsWith("http")){
            iconUrl=basePath+iconUrl;
        }
        // Since we don't have a unique ID in the server, we fake one using the hashcode of
        // the music source. In a real world app, this could come from the server.
        // 由于我们没有唯一的id在服务器端，我们假定一个音乐源的哈希码。现实世界中的应用程序，唯一的id会来源于服务器
        String id=String.valueOf(source.hashCode());
        // Adding the music source to the MediaMetadata (and consequently using it in the
        // mediaSession.setMetadata) is not a good idea for a real world music app, because
        // the session metadata can be accessed by notification listeners. This is done in this
        // sample for convenience only.
        //noinspection ResourceType
        // 添加音乐源给MediaMetadata（已经因此使用它在mediaSession.setMetadata）这不是一个好主意在现实应用软件中，因为
        //元数据会话可以被通知监听器访问。这里这样做仅仅是为了方便
        return new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, id)
                .putString(MusicProviderSource.CUSTOM_METADATA_TRACK_SOURCE, source)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
                .putString(MediaMetadataCompat.METADATA_KEY_GENRE, genre)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, iconUrl)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, trackNumber)
                .putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, totalTrackCount)
                .build();

    }


}
