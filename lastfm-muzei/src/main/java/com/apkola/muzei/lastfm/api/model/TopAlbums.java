package com.apkola.muzei.lastfm.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TopAlbums {
    public List<Album> album;
    @SerializedName("@attrs")
    ResponseAttrs attrs;
}
