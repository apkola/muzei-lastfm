package com.apkola.muzei.lastfm;

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

    static class AlbumsResponse {
        TopAlbums topalbums;
    }

    static class TopAlbums {
        List<Album> album;
        @SerializedName("@attrs")
        TopAlbumsAttrs attrs;
    }

    static class TopAlbumsAttrs {
        String user;
        String type;
    }

    static class Album {
        int rank;
        String name;
        int playcount;
        String mbid;
        String url;
        Artist artist;
        List<Image> image;
        @SerializedName("@attrs")
        AlbumAttrs attrs;

        public String getImageUrl() {
            for (Image i : image) {
                if ("extralarge".equals(i.size)) {
                    return i.text;
                }
            }
            return null;
        }
    }

    static class AlbumAttrs {
        int rank;
    }

    static class Artist {
        String name;
        String mbid;
        String url;
    }

    static class Image {
        @SerializedName("#text")
        String text;
        String size;
    }
}
