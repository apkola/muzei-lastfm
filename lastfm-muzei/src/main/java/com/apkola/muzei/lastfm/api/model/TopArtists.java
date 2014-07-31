package com.apkola.muzei.lastfm.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TopArtists {
    public List<Artist> artist;
    @SerializedName("@attrs")
    ResponseAttrs attrs;
}
