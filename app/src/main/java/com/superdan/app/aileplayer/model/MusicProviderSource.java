package com.superdan.app.aileplayer.model;

import android.media.MediaMetadata;
import android.support.v4.media.MediaMetadataCompat;

import java.util.Iterator;

/**
 * Created by dsz on 16/4/10.
 */
public interface MusicProviderSource {
    String CUSTOM_METADATA_TRACK_SOURCE="__SOURCE__";
    Iterator<MediaMetadataCompat>iterator();
}
