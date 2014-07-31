package com.apkola.muzei.lastfm.api;

import com.apkola.muzei.lastfm.api.model.AlbumsResponse;
import com.apkola.muzei.lastfm.api.model.Artist;
import com.apkola.muzei.lastfm.api.model.ArtistsResponse;
import com.apkola.muzei.lastfm.api.model.EntityWithImage;
import com.apkola.muzei.lastfm.api.model.ResponseAttrs;
import com.apkola.muzei.lastfm.api.model.TrackAttrs;
import com.apkola.muzei.lastfm.api.model.TracksResponse;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import retrofit.http.GET;
import retrofit.http.Query;

public interface LastFmService {

    @GET("/2.0/?method=user.gettopalbums&format=json")
    AlbumsResponse getTopAlbums(
            @Query("user") String user,
            @Query("period") String period,
            @Query("limit") int limit
    );

    @GET("/2.0/?method=user.gettopartists&format=json")
    ArtistsResponse getTopArtists(
            @Query("user") String user,
            @Query("period") String period,
            @Query("limit") int limit
    );

    @GET("/2.0/?method=user.gettoptracks&format=json")
    TracksResponse getTopTracks(
            @Query("user") String user,
            @Query("period") String period,
            @Query("limit") int limit
    );

}
