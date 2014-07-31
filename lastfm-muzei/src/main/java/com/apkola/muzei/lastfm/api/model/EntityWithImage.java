package com.apkola.muzei.lastfm.api.model;

public abstract class EntityWithImage {
    public String mbid;
    public String name;
    public String url;
    public String getByLine() { return  ""; }
    public String getImageUrl() { return  ""; }
}
