package com.superdan.app.aileplayer.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by dsz on 16/4/14.
 */
public class BitmapHelper {
    private static  final  String TAG=LogHelper.makeLogTag(BitmapHelper.class);

    // Max read limit that we allow our input stream to mark/reset.
    //最大读限度，让我们的输入流标记位、复位
    private static final int MAX_READ_LIMIT_PER_IMG=1024*1024;//1MB

    public static Bitmap scaleBitmap(Bitmap src,int maxWidth,int maxHeight){
        double scaleFactor=Math.min(
                ((double)maxWidth)/src.getWidth(),((double)maxHeight)/src.getHeight()
        );

        /**
         * Creates a new bitmap, scaled from an existing bitmap, when possible. If the
         * specified width and height are the same as the current width and height of
         * the source bitmap, the source bitmap is returned and no new bitmap is
         * created.
         *
         * @param src       The source bitmap.
         * @param dstWidth  The new bitmap's desired width.
         * @param dstHeight The new bitmap's desired height.
         * @param filter    true if the source should be filtered.
         * @return The new scaled bitmap or the source bitmap if no scaling is required.
         * @throws IllegalArgumentException if width is <= 0, or height is <= 0
         */

        return Bitmap.createScaledBitmap(src,(int)(scaleFactor*src.getWidth()),(int)(scaleFactor*src.getHeight()),false);

    }


    public static Bitmap scaleBitmap(int scaleFactor, InputStream is){
         // Get the dimensions of the bitmap
        //获得位图的尺寸
        BitmapFactory.Options bmOptions=new BitmapFactory.Options();

        //Decode the image file into a Bitmap sized to fill the View
        //解码图像文件转换成位图大小以填充视图
        /**
         * If set to true, the decoder will return null (no bitmap), but
         * the out... fields will still be set, allowing the caller to query
         * the bitmap without having to allocate the memory for its pixels.
         * 如果设置为true ，解码器将返回null（无图）,也不给其分配内存空间这样就避免内存溢，
         * 但是允许我们查询图片的信息这其中就包括图片的信息（宽和高）
         *
         *
         */
        bmOptions.inJustDecodeBounds=false;
        /**
         * If set to a value > 1, requests the decoder to subsample the original
         * image, returning a smaller image to save memory. The sample size is
         * the number of pixels in either dimension that correspond to a single
         * pixel in the decoded bitmap. For example, inSampleSize == 4 returns
         * an image that is 1/4 the width/height of the original, and 1/16 the
         * number of pixels. Any value <= 1 is treated the same as 1. Note: the
         * decoder uses a final value based on powers of 2, any other value will
         * be rounded down to the nearest power of 2.
         * 解码器使用基于2的幂最终值，任何其他值都将四舍五入到最接近2的幂
         */
        bmOptions.inSampleSize=scaleFactor;

        /**
         * Creates Bitmap objects from various sources, including files, streams,
         * and byte-arrays.
         */
        return BitmapFactory.decodeStream(is,null,bmOptions);


    }


    public static  int findScaleFactor(int targetW,int targetH,InputStream is){
        //获得位图的尺寸
        BitmapFactory.Options bmOptions=new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds=true;
        BitmapFactory.decodeStream(is,null,bmOptions);
        int actualW=bmOptions.outWidth;
        int actualH=bmOptions.outHeight;
        // Determine how much to scale down the image
        return Math.min(actualW/targetW,actualH/targetH);


    }


    public static  Bitmap fetchAndRescaleBitmap(String uri,int width,int height) throws IOException {
        URL url=new URL(uri);
        BufferedInputStream is=null;
        try{
            HttpURLConnection urlConnection=(HttpURLConnection)url.openConnection();
            is=new BufferedInputStream(urlConnection.getInputStream());
            is.mark(MAX_READ_LIMIT_PER_IMG);
            int scaleFactor=findScaleFactor(width,height,is);
            LogHelper.d(TAG,"Scaling bitmap ",uri,"by factor",scaleFactor," to support",
                    width,"x",height,"requested dimension");
            is.reset();
            return  scaleBitmap(scaleFactor,is);

        }finally {
            if(is!=null){
                is.close();
            }

        }


    }






}
