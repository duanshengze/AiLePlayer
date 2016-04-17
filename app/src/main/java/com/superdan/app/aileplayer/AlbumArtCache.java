package com.superdan.app.aileplayer;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.LruCache;

import com.superdan.app.aileplayer.utils.BitmapHelper;
import com.superdan.app.aileplayer.utils.LogHelper;

import java.io.IOException;

/**
 * Created by dsz on 16/4/14.
 * Implements a basic cache of album arts, with async loading support.
 * <p/>
 * 实现专辑艺术的基本缓存，有异步加载的功能
 */
public final class AlbumArtCache {
    private static final String TAG = LogHelper.makeLogTag(AlbumArtCache.class);

    private static final int MAX_ALBUM_ART_CACHE_SIZE = 12 * 1024 * 1024;//12MB

    private static final int MAX_ART_WIDTH = 800;//pixels
    private static final int MAX_ART_HEIGHT = 480;//pixels 像素
    /**
     * Resolution reasonable for carrying around as an icon (generally in
     * MediaDescription.getIconBitmap). This should not be bigger than necessary, because
     * the MediaDescription object should be lightweight. If you set it too high and try to
     * serialize the MediaDescription, you may get FAILED BINDER TRANSACTION errors.
     * 对一个图标的合理的分辨率（一般 MediaDescription.getIconBitmap）。不应该比需要的还要打，因为MediaDescription
     * 对象应该轻量级，如果你设置太高，试图序列化MediaDescription，你将得到FAILED BINDER TRANSACTION错误
     */
    private static final int MAX_ART_WIDTH_ICON = 128;
    private static final int MAX_ART_HEIGHT_ICON = 128;//pixels 像素

    private static final int BIG_BITMAP_INDEX = 0;
    private static final int ICON_BITMAP_INDEX = 1;


    private final LruCache<String, Bitmap[]> mCache;

    private static final AlbumArtCache sInstance = new AlbumArtCache();

    public static AlbumArtCache getInstance() {
        return sInstance;
    }

    private AlbumArtCache() {
        // Holds no more than MAX_ALBUM_ART_CACHE_SIZE bytes, bounded by maxmemory/4 and
        // Integer.MAX_VALUE:
        // 不超过MAX_ALBUM_ART_CACHE_SIZE的大小，在最大内存的1/4到Integer.MAX_VALUE之间
        int maxSize = Math.max(MAX_ALBUM_ART_CACHE_SIZE, (int) (Math.max(Integer.MAX_VALUE, Runtime.getRuntime().maxMemory() / 4)));
        mCache = new LruCache<String, Bitmap[]>(maxSize) {
            //返回每个缓存对象的大小，这个用来判断缓存是否快要满了的情况，这个方法必须重写

            @Override
            protected int sizeOf(String key, Bitmap[] value) {
                return value[BIG_BITMAP_INDEX].getByteCount() + value[ICON_BITMAP_INDEX].getByteCount();
            }
        };





    }


    public Bitmap getBitImage(String artUrl) {
        Bitmap[] result = mCache.get(artUrl);
        return result == null ? null : result[BIG_BITMAP_INDEX];

    }


    public Bitmap getIconImage(String artUrl){
        Bitmap[]result=mCache.get(artUrl);
        return result==null?null:result[ICON_BITMAP_INDEX];


    }

    public void fetch(final String artUrl,final FetchListener listener){
        // WARNING: for the sake of simplicity, simultaneous multi-thread fetch requests
        // are not handled properly: they may cause redundant costly operations, like HTTP
        // requests and bitmap rescales. For production-level apps, we recommend you use
        // a proper image loading library, like Glide.
        /**
          *为了简单起见，同步多线程抓取请求处理不好。他们可能会导致昂贵的冗余操作，如Http请求和位图的缩放，对于产品级的应用程序，
         * 我们建议使用合适的图片加载库：Glide
          */
        Bitmap[]bitmap=mCache.get(artUrl);
        if(bitmap!=null){
            LogHelper.d(TAG,"getOrFetch:album art is in cache, using it",artUrl);
            listener.onFecthed(artUrl,bitmap[BIG_BITMAP_INDEX],bitmap[ICON_BITMAP_INDEX]);
            return ;
        }
        LogHelper.d(TAG,"getOrFetch:starting asynctask to fetch");

        new AsyncTask<Void,Void,Bitmap[]>(){

            @Override
            protected Bitmap[] doInBackground(Void... params) {
                Bitmap[]bitmaps;
                try{
                    Bitmap bitmap= BitmapHelper.fetchAndRescaleBitmap(artUrl,MAX_ART_WIDTH,MAX_ART_HEIGHT);
                    Bitmap icon=BitmapHelper.scaleBitmap(bitmap,MAX_ART_WIDTH_ICON,MAX_ART_HEIGHT_ICON);
                    bitmaps=new Bitmap[]{bitmap,icon};


                }catch (IOException e){
                    return null;
                }
                LogHelper.d(TAG,"doInBackground: putting bitmap in cache.cache size="+mCache.size());
                return bitmaps;

            }

            @Override
            protected void onPostExecute(Bitmap[] bitmaps) {
                if(bitmaps==null){
                    listener.onError(artUrl,new IllegalArgumentException("got null bitmaps"));
                }else {
                    listener.onFecthed(artUrl,bitmaps[BIG_BITMAP_INDEX],bitmaps[ICON_BITMAP_INDEX]);
                }

            }
        }.execute();

    }


    public static  abstract class FetchListener{
        public abstract void onFecthed(String artUrl,Bitmap bitImgae,Bitmap iconImage);

        public void onError(String artUrl,Exception e){

            LogHelper.e(TAG,e,"AlbumArtFetchListener: error while downloading"+artUrl);
        }


    }
}
