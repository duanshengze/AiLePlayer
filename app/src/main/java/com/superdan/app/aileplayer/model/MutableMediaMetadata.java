package com.superdan.app.aileplayer.model;

import android.support.v4.media.MediaMetadataCompat;
import android.text.TextUtils;

/**
 * Created by dsz on 16/4/10.
 * Holder class that encapsulates a MediaMetadata and allows the actual metadata to be modified
 * without requiring to rebuild the collections the metadata is in.
 * 持有类封装一个MediaMetadata类并允许实际的元数据被修改无需重新建立元数据所在的Collections
 *
 *
 */
public class MutableMediaMetadata {
    public MediaMetadataCompat metadata;
    public final String trackId;
    public MutableMediaMetadata(String trackId, MediaMetadataCompat metadata){
        this.trackId=trackId;
        this.metadata=metadata;
    }

    @Override
    public boolean equals(Object o) {
        if(this==o){
            return true;
        }
        if(o==null||o.getClass()!=MutableMediaMetadata.class){
            return false;
        }
        MutableMediaMetadata that=(MutableMediaMetadata)o;
        return TextUtils.equals(trackId,that.trackId);
    }

    @Override
    public int hashCode() {
        return trackId.hashCode();
    }
}
