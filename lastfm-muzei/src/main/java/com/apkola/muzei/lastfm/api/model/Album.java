package com.apkola.muzei.lastfm.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Album extends EntityWithImage {
    int rank;
    int playcount;
    Artist artist;
    List<Image> image;
    @SerializedName("@attrs")
    AlbumAttrs attrs;

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
