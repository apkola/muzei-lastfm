package com.apkola.muzei.lastfm.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Artist extends EntityWithImage {
    List<Image> image;
    @SerializedName("@attrs")
    ArtistAttrs attrs;

    @Override
    public String getImageUrl() {
        for (Image i : image) {
            if ("mega".equals(i.size)) {
                return i.text;
            }
        }
        return null;
    }
}
