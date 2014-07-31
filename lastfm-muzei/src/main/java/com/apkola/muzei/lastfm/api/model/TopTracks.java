package com.apkola.muzei.lastfm.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TopTracks {
    public List<Track> track;
    @SerializedName("@attrs")
    ResponseAttrs attrs;
}
