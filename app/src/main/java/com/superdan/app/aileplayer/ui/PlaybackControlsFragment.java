package com.superdan.app.aileplayer.ui;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.superdan.app.aileplayer.R;
import com.superdan.app.aileplayer.utils.LogHelper;

import java.util.List;

/**
 * Created by Administrator on 2016/4/7.
 */
public class PlaybackControlsFragment extends Fragment {
    private static final String TAG= LogHelper.makeLogTag(PlaybackControlsFragment.class);
    private ImageButton mPlayPause;
    private TextView mTitle;
    private TextView mSubtitle;
    private TextView mExtraInfo;
    private ImageView mAlbumArt;
    private String mArtUrl;
    //在这里收到来自MediaController的回调，在这里我们更新状态，例如正在显示
    //的队列，当前的标题和描述已经PlaybackState

    private  final MediaControllerCompat.Callback mCallback=new MediaControllerCompat.Callback() {
        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);
        }

        @Override
        public void onQueueChanged(List<MediaSessionCompat.QueueItem> queue) {
            super.onQueueChanged(queue);
        }

        @Override
        public void onQueueTitleChanged(CharSequence title) {
            super.onQueueTitleChanged(title);
        }

        @Override
        public void onAudioInfoChanged(MediaControllerCompat.PlaybackInfo info) {
            super.onAudioInfoChanged(info);
        }

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View rootView=inflater.inflate(R.layout.fragment_playback_controls,container,false);
        mPlayPause = (ImageButton) rootView.findViewById(R.id.play_pause);
        mPlayPause.setEnabled(true);
        mPlayPause.setOnClickListener(mButtonListener);

        mTitle = (TextView) rootView.findViewById(R.id.title);
        mSubtitle = (TextView) rootView.findViewById(R.id.artist);
        mExtraInfo = (TextView) rootView.findViewById(R.id.extra_info);
        mAlbumArt = (ImageView) rootView.findViewById(R.id.album_art);

        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getActivity(),FullScreenPlayerActivity.class);
                //设置，如果activity已经运行在历史栈的顶部将不启动
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                MediaControllerCompat controller=((FragmentActivity)getActivity()).getSupportMediaController();
                MediaMetadataCompat metadata=controller.getMetadata();
                if (metadata!=null){

                    intent.putExtra(MusicPlayerActivity.EXTRA_CURRENT_MEDIA_DESCRIPTION,metadata.getDescription());

                }
                startActivity(intent);
            }
        });

        return  rootView;
    }


    //TODO? 为什么要用final
    private final View.OnClickListener mButtonListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            MediaControllerCompat controller=((FragmentActivity)getActivity()).getSupportMediaController();
            PlaybackStateCompat stateObj=controller.getPlaybackState();
            final  int state=stateObj==null?PlaybackStateCompat.STATE_NONE:stateObj.getState();
            switch (v.getId()){
                case R.id.play_pause:
                     LogHelper.d(TAG,"播放键点击，现在状态播放是"+state);
                    if (state==PlaybackStateCompat.STATE_PAUSED||state==PlaybackStateCompat.STATE_STOPPED
                            ||state== PlaybackStateCompat.STATE_NONE){
                        playMedia();
                    }else if(state==PlaybackStateCompat.STATE_PLAYING||state==PlaybackStateCompat.STATE_BUFFERING
                            ||state==PlaybackStateCompat.STATE_CONNECTING){
                        pauseMedia();
                    }

                break;


            }
        }
    };

    private void playMedia(){
        MediaControllerCompat controller=((FragmentActivity)getActivity()).getSupportMediaController();
        if(controller!=null){
            //Request that the player start its playback at its current position.
            //要求播放器开始播放在当前位置
            //来自session的控制实例
            controller.getTransportControls().play();
        }
    }

    private void pauseMedia(){
        MediaControllerCompat controller = ((FragmentActivity)getActivity()).getSupportMediaController();
        if(controller!=null){
            //Request that the player start its playback at its current position.
            //要求播放器在当前位置暂停
            controller.getTransportControls().play();
        }

    }

}
