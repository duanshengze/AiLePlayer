package com.superdan.app.aileplayer.ui;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.superdan.app.aileplayer.R;

/**
 * Created by Administrator on 2016/4/19.
 */
public class MediaItemViewHolder {
    static final int STATE_INVALID=-1;
    static final int STATE_NONE=0;
    static final  int STATE_PALYABLE=1;
    static final int STATE_PAUSED=2;
    static final int STATE_PLAYING=3;

    private static ColorStateList sColorStatePlaying;
    private static  ColorStateList sColorStateNotPlaying;
    ImageView mImageView;
    TextView mTitleView;
    TextView mDescriptionView;

    static View setupView(Activity activity, View convertView,ViewGroup parent,
                          MediaDescriptionCompat description,int state){
        if(sColorStatePlaying==null||sColorStateNotPlaying==null){
            initializeColorStateLists(activity);
        }


        MediaItemViewHolder holder;
        Integer cacheState=STATE_INVALID;
        if(convertView==null){
            convertView= LayoutInflater.from(activity).inflate(R.layout.media_list_item,parent,false);
            holder=new MediaItemViewHolder();
            holder.mImageView=(ImageView)convertView.findViewById(R.id.paly_eq);
            holder.mTitleView=(TextView)convertView.findViewById(R.id.title);
            holder.mDescriptionView=(TextView)convertView.findViewById(R.id.description);
            convertView.setTag(holder);

        }else {
            holder=(MediaItemViewHolder)convertView.getTag();
            cacheState=(Integer) convertView.getTag(R.id.tag_mediaitem_state_cache);
        }

            holder.mTitleView.setText(description.getSubtitle());
            holder.mDescriptionView.setText(description.getSubtitle());
        // If the state of convertView is different, we need to adapt the view to the
        // new state.
        if(cacheState==null||cacheState!=state){

            switch (state){
                case  STATE_PALYABLE:
                    Drawable pauseDrawable= ContextCompat.getDrawable(activity,R.drawable.ic_play_arrow_googblue_36dp);
                    DrawableCompat.setTintList(pauseDrawable,sColorStateNotPlaying);
                    holder.mImageView.setImageDrawable(pauseDrawable);
                    holder.mImageView.setVisibility(View.VISIBLE);
                    break;
                case STATE_PLAYING:
                    AnimationDrawable animation=(AnimationDrawable)ContextCompat.getDrawable(activity,R.drawable.ic_equalizer_white_36dp);
                    DrawableCompat.setTintList(animation,sColorStatePlaying);
                    holder.mImageView.setImageDrawable(animation);
                    holder.mImageView.setVisibility(View.VISIBLE);
                    animation.start();
                    break;
                case STATE_PAUSED:
                    Drawable playDrawable=ContextCompat.getDrawable(activity,R.mipmap.ic_equalizer1_white_36dp);
                    DrawableCompat.setTintList(playDrawable,sColorStatePlaying);
                    holder.mImageView.setImageDrawable(playDrawable);
                    holder.mImageView.setVisibility(View.VISIBLE);
                    break;
                default:
                    holder.mImageView.setVisibility(View.INVISIBLE);

            }
            convertView.setTag(R.id.tag_mediaitem_state_cache,state);
        }


        return  convertView;
    }


    private  static void initializeColorStateLists(Context ctx){
        sColorStateNotPlaying=ColorStateList.valueOf(ctx.getResources().getColor(R.color.media_item_icon_not_playing));
        sColorStatePlaying=ColorStateList.valueOf(ctx.getResources().getColor(R.color.media_item_icon_playing));
    }
}
