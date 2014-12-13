package com.apkola.muzei.lastfm.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Track extends EntityWithImage {
    int playcount;
    int duration;
    Artist artist;
    List<Image> image;
    @SerializedName("@attrs")
    TrackAttrs attrs;

    @Override
    public String getImageUrl() {
        if (image == null) return null;
        for (Image i : image) {
            if ("extralarge".equals(i.size)) {
                return i.text;
            }
        }
        return null;
    }

    @Override
    public String getByLine() {
        return String.format("%s - %s plays", artist.name, playcount);
    }
}
