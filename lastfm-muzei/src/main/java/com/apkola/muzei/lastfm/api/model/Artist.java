package com.apkola.muzei.lastfm.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Artist extends EntityWithImage {
    int playcount;
    List<Image> image;
    @SerializedName("@attrs")
    ArtistAttrs attrs;

    @Override
    public String getImageUrl() {
        if (image == null) return null;
        for (Image i : image) {
            if ("mega".equals(i.size)) {
                return i.text;
            }
        }
        return null;
    }

    @Override
    public String getByLine() {
        return String.format("%s plays", playcount);
    }
}
