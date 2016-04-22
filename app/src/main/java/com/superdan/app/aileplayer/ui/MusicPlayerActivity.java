package com.superdan.app.aileplayer.ui;

import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.media.MediaBrowserCompat;
import android.text.TextUtils;

import com.superdan.app.aileplayer.R;
import com.superdan.app.aileplayer.utils.LogHelper;

/**
 * Created by Administrator on 2016/4/8.
 * Main activity for the music player.
 * This class hold the MediaBrowser and the MediaController instances. It will create a MediaBrowser
 * when it is created and connect/disconnect on start/stop. Thus, a MediaBrowser will be always
 * connected while this activity is running.
 *音乐播放器的主类
 * 这个播放器包含媒体浏览器和媒体控制器实例，当它被创建时将创建媒体浏览器并且在onStart（）和onStop（）方法类连接或者断开
 * 因此，媒体浏览器将一直连接当activity在运行时
 */
public class MusicPlayerActivity extends BaseActivity implements MediaBrowserFragment.MediaFragmentListener {

    private static final String TAG= LogHelper.makeLogTag(MusicPlayerActivity.class);
    private static final String SAVED_MEDIA_ID="com.superdan.app.aileplayer.MEDIA_ID";
    private static  final String FRAGMENT_TAG="aile_list_container";

    public static final String EXTRA_START_FULLSCREEN="com.superdan.app.aileplayer.EXTRA_START_FULLSCREEN";


    public static final String EXTRA_CURRENT_MEDIA_DESCRIPTION="com.superdan.app.aileplayer.EXTRA_CURRENT_MEDIA_DESCRIPTION";

    private  Bundle mVoiceSearchParams;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogHelper.d(TAG,"Activity onCreate");
        setContentView(R.layout.activity_player);
        initializeToolbar();
        initializeFromParams(savedInstanceState,getIntent());
        //仅仅检查是否需要全屏播放
        if(savedInstanceState==null){
            startFullScreenActivityIfNeeded(getIntent());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        String mediaId=getMediaId();

        if(mediaId!=null){
            outState.putString(SAVED_MEDIA_ID,mediaId);
        }

        super.onSaveInstanceState(outState);
    }


    @Override
    protected void onNewIntent(Intent intent) {
        LogHelper.d(TAG,"onNewIntent, intent="+intent);
        initializeFromParams(null,intent);
        startFullScreenActivityIfNeeded(intent);
    }

    private  void startFullScreenActivityIfNeeded(Intent intent){
        if(intent!=null&&intent.getBooleanExtra(EXTRA_START_FULLSCREEN,false)){
            Intent fullScreenIntent=new Intent(this,FullScreenPlayerActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .putExtra(EXTRA_CURRENT_MEDIA_DESCRIPTION,intent.getParcelableExtra(EXTRA_CURRENT_MEDIA_DESCRIPTION));
            startActivity(fullScreenIntent);
        }
    }

    protected void initializeFromParams(Bundle savedInstanceState,Intent intent){
        String mediaId=null;
        // check if we were started from a "Play XYZ" voice search. If so, we save the extras
        // (which contain the query details) in a parameter, so we can reuse it later, when the
        // MediaSession is connected.
        if (intent.getAction()!=null&&intent.getAction().equals(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH)){
            mVoiceSearchParams=intent.getExtras();
            LogHelper.d(TAG,"Starting from voice search query=",mVoiceSearchParams.getString(SearchManager.QUERY));

        }else {
            if(savedInstanceState!=null){
                mediaId=savedInstanceState.getString(SAVED_MEDIA_ID);
            }
        }
       navigateToBrowser( mediaId);
    }
//TODo
        private void navigateToBrowser(String mediaId){

            LogHelper.d(TAG,"navigateToBrowser,mediaId="+mediaId);
            MediaBrowserFragment fragment=getBrowseFragment();
            if (fragment!=null||!TextUtils.equals(fragment.getMediaId(),mediaId)){
                fragment=new MediaBrowserFragment();
                fragment.setMediaId(mediaId);
                FragmentTransaction transaction=getFragmentManager().beginTransaction();
                transaction.setCustomAnimations(
                        R.animator.slide_in_from_right,R.animator.slide_in_from_left,
                        R.animator.slide_in_from_left,R.animator.slide_in_from_right
                );
                transaction.replace(R.id.container,fragment,FRAGMENT_TAG);
                // If this is not the top level media (root), we add it to the fragment back stack,
                // so that actionbar toggle and Back will work appropriately:

                if(mediaId!=null){

                    //加栈。。
                    transaction.addToBackStack(null);

                }
                transaction.commit();

            }

        }

    public  String getMediaId(){
        MediaBrowserFragment fragment=getBrowseFragment();
        if(fragment==null){
            return  null;
        }
        return fragment.getMediaId();
    }


    private MediaBrowserFragment getBrowseFragment(){
        return (MediaBrowserFragment)getFragmentManager().findFragmentByTag(FRAGMENT_TAG);
    }

    @Override
    protected void onMediaControllerConnected() {
        if(mVoiceSearchParams!=null){
            // If there is a bootstrap parameter to start from a search query, we
            // send it to the media session and set it to null, so it won't play again
            // when the activity is stopped/started or recreated:
            String query=mVoiceSearchParams.getString(SearchManager.QUERY);
            getSupportMediaController().getTransportControls()
                    .playFromSearch(query,mVoiceSearchParams);
            mVoiceSearchParams=null;
        }
        getBrowseFragment().onConnected();
    }

    @Override
    public void onMediaItemSelected(MediaBrowserCompat.MediaItem item) {
            LogHelper.d(TAG,"onMediaItemSelected,mediaId="+item.getMediaId());
        if(item.isPlayable()){
            getSupportMediaController().getTransportControls().playFromMediaId(item.getMediaId(),null);
        }else if(item.isBrowsable()){
            navigateToBrowser(item.getMediaId());
        }else {
            LogHelper.w(TAG,"Ignoring MediaItem that is neither browsable nor playable:","mediaId=",item.getMediaId());
        }
    }

    @Override
    public void setToolbarTitle(CharSequence title) {
        LogHelper.d(TAG,"Setting toolbar title to",title);
        if(title==null){
            title=getString(R.string.app_name);
        }
        setTitle(title);
    }
}
