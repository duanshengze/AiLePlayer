package com.superdan.app.aileplayer.model;

import android.media.MediaMetadata;

import java.util.Iterator;

/**
 * Created by dsz on 16/4/10.
 */
public interface MusicProviderSource {
    String CUSTOM_METADATA_TRACK_SOURCE="__SOURCE__";
    Iterator<MediaMetadata>iterator();
}
