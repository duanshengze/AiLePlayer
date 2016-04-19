package com.superdan.app.aileplayer.ui;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.media.MediaBrowserCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.superdan.app.aileplayer.R;
import com.superdan.app.aileplayer.utils.LogHelper;

import java.util.ArrayList;

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












    private static class BrowseAdapter extends ArrayAdapter<MediaBrowserCompat.MediaItem>{
        public BrowseAdapter(Activity context){
            super(context, R.layout.media_list_item,new ArrayList<MediaBrowserCompat.MediaItem>());
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            MediaBrowserCompat.MediaItem item=getItem(position);
            int itemState=MediaItemViewHolder
        }
    }

}
