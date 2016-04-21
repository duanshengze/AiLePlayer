package com.superdan.app.aileplayer.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.superdan.app.aileplayer.R;
import com.superdan.app.aileplayer.utils.LogHelper;
import com.superdan.app.aileplayer.utils.MediaIDHelper;
import com.superdan.app.aileplayer.utils.NetworkHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * A Fragment that lists all the various browsable queues available
 * from a {@link android.service.media.MediaBrowserService}.
 * <p/>
 * It uses a {@link MediaBrowserCompat} to connect to the MediaService.
 * Once connected, the fragment subscribes to get all the children.
 * All {@link MediaBrowserCompat.MediaItem}'s that can be browsed are shown in a ListView.
 */
public class MediaBrowserFragment extends Fragment{
    private static  final String TAG=LogHelper.makeLogTag(MediaBrowserFragment.class);
    private static final String ARG_MEDIA_ID="media_id";

    private  BrowseAdapter mBrowseAdapter;
    private  String mMediaId;
    private MediaFragmentListener mMediaFragmentListener;
    private View mErrorView;
    private TextView mErrorMessage;

    private final BroadcastReceiver mConnectivityChangeReceiver=new BroadcastReceiver() {
        private boolean oldOnline=false;
        @Override
        public void onReceive(Context context, Intent intent) {
            if(mMediaId!=null){
                boolean isOnline= NetworkHelper.isOnline(context);
                if(isOnline!=oldOnline){
                    oldOnline=isOnline;
                    checkForUserVisibleErrors(false);
                    if(isOnline){
                        mBrowseAdapter.notifyDataSetChanged();
                    }
                }
            }
        }
    };

    // Receive callbacks from the MediaController. Here we update our state such as which queue
    // is being shown, the current title and description and the PlaybackState.

    private  final  MediaControllerCompat.Callback mMediaControllerCallback=new MediaControllerCompat.Callback() {
        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);
            if (metadata==null){
                return;
            }
            LogHelper.d(TAG,"Received metadata change to media",metadata.getDescription().getMediaId());
        }

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
            LogHelper.d(TAG,"Received sate change:",state);
            checkForUserVisibleErrors(false);
            mBrowseAdapter.notifyDataSetChanged();

        }
    };

    private final MediaBrowserCompat.SubscriptionCallback mSubscriptionCallback=new MediaBrowserCompat.SubscriptionCallback() {
        @Override
        public void onChildrenLoaded(@NonNull String parentId, List<MediaBrowserCompat.MediaItem> children) {
            try{
                LogHelper.d(TAG,"fragment onChildrenLoaded,parentId="+parentId+" count="+children.size());

                checkForUserVisibleErrors(children.isEmpty());
                mBrowseAdapter.clear();
                for (MediaBrowserCompat.MediaItem item:children){
                    mBrowseAdapter.add(item);
                }
                mBrowseAdapter.notifyDataSetChanged();
            }catch (Throwable t){
                LogHelper.e(TAG,"Error onchildrenloaded,",t);
            }
        }

        @Override
        public void onError(@NonNull String parentId) {
            super.onError(parentId);
            LogHelper.e(TAG,"browse fragment subscription onError,id="+parentId);
            Toast.makeText(getContext(),"ERROR LOADING MEDIA",Toast.LENGTH_LONG).show();
            checkForUserVisibleErrors(true);
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // If used on an activity that doesn't implement MediaFragmentListener, it
        // will throw an exception as expected:
        mMediaFragmentListener=(MediaFragmentListener)activity;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LogHelper.d(TAG,"fragment.onCreateView");
        View rootView=inflater.inflate(R.layout.fragment_list,container,false);
        mErrorView=rootView.findViewById(R.id.playback_error);
        mErrorMessage=(TextView)mErrorView.findViewById(R.id.error_message);
        mBrowseAdapter=new BrowseAdapter(getActivity());

        ListView listView=(ListView)rootView.findViewById(R.id.list_view);
        listView.setAdapter(mBrowseAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                checkForUserVisibleErrors(false);
                MediaBrowserCompat.MediaItem item=mBrowseAdapter.getItem(position);
                mMediaFragmentListener.onMediaItemSelected(item);
            }
        });
        return  rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        MediaBrowserCompat mediaBrowser=mMediaFragmentListener.getMediaBrowser();
        LogHelper.d(TAG,"fragment.OnStart,mediaId=",mMediaId," onConnected="+mediaBrowser.isConnected());
        if(mediaBrowser.isConnected()){
            onConnected();
        }
        // Registers BroadcastReceiver to track network connection changes.

        this.getActivity().registerReceiver(mConnectivityChangeReceiver,new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    public void onStop() {
        super.onStop();
        MediaBrowserCompat mediaBrowser=mMediaFragmentListener.getMediaBrowser();
        if(mediaBrowser!=null&&mediaBrowser.isConnected()&&mMediaId!=null){
            mediaBrowser.unsubscribe(mMediaId);
        }
        MediaControllerCompat controller=((FragmentActivity)getActivity()).getSupportMediaController();
        if(controller!=null){
            controller.unregisterCallback(mMediaControllerCallback);
        }
        getActivity().unregisterReceiver(mConnectivityChangeReceiver);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mMediaFragmentListener=null;
    }

    // Called when the MediaBrowser is connected. This method is either called by the
    // fragment.onStart() or explicitly by the activity in the case where the connection
    // completes after the onStart()

    public void onConnected(){

        if(isDetached()){
            return;
        }
        mMediaId=getMediaId();
        if(mMediaId==null){
            mMediaId=mMediaFragmentListener.getMediaBrowser().getRoot();
        }
        updateTitle();

        // Unsubscribing before subscribing is required if this mediaId already has a subscriber
        // on this MediaBrowser instance. Subscribing to an already subscribed mediaId will replace
        // the callback, but won't trigger the initial callback.onChildrenLoaded.
        //
        // This is temporary: A bug is being fixed that will make subscribe
        // consistently call onChildrenLoaded initially, no matter if it is replacing an existing
        // subscriber or not. Currently this only happens if the mediaID has no previous
        // subscriber or if the media content changes on the service side, so we need to
        // unsubscribe first.



        mMediaFragmentListener.getMediaBrowser().unsubscribe(mMediaId);
        mMediaFragmentListener.getMediaBrowser().subscribe(mMediaId,mSubscriptionCallback);
        //Add MediaController callback so we can redraw the list when metadata changes:
        MediaControllerCompat controller=((FragmentActivity)getActivity()).getSupportMediaController();
        if(controller!=null){
            controller.registerCallback(mMediaControllerCallback);
        }

    }
    private void checkForUserVisibleErrors(boolean forceError){
        boolean showError=forceError;
        if (!NetworkHelper.isOnline(getActivity())){
            mErrorMessage.setText(R.string.error_on_connection);
            showError=true;

        }else {
            // otherwise, if state is ERROR and metadata!=null, use playback state error message:
            MediaControllerCompat controller=((FragmentActivity)getActivity()).getSupportMediaController();
            if(controller!=null
                    && controller.getMetadata()!=null
                    &&controller.getPlaybackState()!=null
                    &&controller.getPlaybackState().getState()==PlaybackStateCompat.STATE_ERROR
                    &&controller.getPlaybackState().getErrorMessage()!=null){

                mErrorMessage.setText(controller.getPlaybackState().getErrorMessage());

            }else if(forceError){
                // Finally, if the caller requested to show error, show a generic message:
                mErrorMessage.setText("Error Loading Media");
                showError=true;

            }

        }
        mErrorView.setVisibility(showError?View.VISIBLE:View.GONE);
        LogHelper.d(TAG,"checkForUserVisibleErrors,forceError=",forceError,
                " showError=",showError," isOnline= ",NetworkHelper.isOnline(getActivity()));

    }






    public String getMediaId(){
        Bundle args=getArguments();
        if(args!=null){
            return  args.getString(ARG_MEDIA_ID);
        }
        return  null;
    }


    public void setMediaId(String mediaId){
        Bundle args=new Bundle(1);
        args.putString(MediaBrowserFragment.ARG_MEDIA_ID,mediaId);
        setArguments(args);
    }

    private void updateTitle(){

        if(MediaIDHelper.MEDIA_ID_ROOT.equals(mMediaId)){
            mMediaFragmentListener.setToolbarTitle(null);
            return;
        }
        MediaBrowserCompat mediaBrowser=mMediaFragmentListener.getMediaBrowser();
        mediaBrowser.getItem(mMediaId, new MediaBrowserCompat.ItemCallback() {
            @Override
            public void onItemLoaded(MediaBrowserCompat.MediaItem item) {
                mMediaFragmentListener.setToolbarTitle(
                        item.getDescription().getTitle()
                );
            }
        });

    }


    private static class BrowseAdapter extends ArrayAdapter<MediaBrowserCompat.MediaItem>{
        public BrowseAdapter(Activity context){
            super(context, R.layout.media_list_item,new ArrayList<MediaBrowserCompat.MediaItem>());
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            MediaBrowserCompat.MediaItem item=getItem(position);
            int itemState=MediaItemViewHolder.STATE_NONE;
            if(item.isPlayable()){
                itemState=MediaItemViewHolder.STATE_PALYABLE;
                MediaControllerCompat controller=((FragmentActivity)getContext()).getSupportMediaController();
                if(controller!=null&&controller.getMetadata()!=null){
                    String currentPlaying=controller.getMetadata().getDescription().getMediaId();
                    String musicId= MediaIDHelper.extractMusicIDFromMediaID(item.getDescription().getMediaId());
                    if(currentPlaying!=null&&currentPlaying.equals(musicId)){
                        PlaybackStateCompat pbState=controller.getPlaybackState();
                        if(pbState==null||pbState.getState()==PlaybackStateCompat.STATE_ERROR){
                            itemState=MediaItemViewHolder.STATE_NONE;
                        }else if(pbState.getState()==PlaybackStateCompat.STATE_PLAYING){
                            itemState=MediaItemViewHolder.STATE_PLAYING;
                        }else {
                            itemState=MediaItemViewHolder.STATE_PAUSED;
                        }
                    }

                }
            }
            return MediaItemViewHolder.setupView((Activity) getContext(),convertView,parent,item.getDescription(),itemState);
        }
    }


    public interface  MediaFragmentListener extends MediaBrowserProvider{
        void onMediaItemSelected(MediaBrowserCompat.MediaItem item);
        void setToolbarTitle(CharSequence title);
    }
}
