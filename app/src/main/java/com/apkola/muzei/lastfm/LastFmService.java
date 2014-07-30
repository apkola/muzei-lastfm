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

    static class AlbumsResponse {
        TopAlbums topalbums;
    }

    static class ArtistsResponse {
        TopArtists topartists;
    }

    static class TracksResponse {
        TopTracks toptracks;
    }

    static class TopTracks {
        List<Track> track;
        @SerializedName("@attrs")
        ResponseAttrs attrs;
    }

    static class TopAlbums {
        List<Album> album;
        @SerializedName("@attrs")
        ResponseAttrs attrs;
    }

    static class ResponseAttrs {
        String user;
        String type;
    }

    static class TopArtists {
        List<Artist> artist;
        @SerializedName("@attrs")
        ResponseAttrs attrs;
    }

    static abstract class EntityWithImage {
        public String mbid;
        public String name;
        public String url;
        public String getByLine() { return  ""; }
        public String getImageUrl() { return  ""; }
    }

    static class Album extends EntityWithImage {
        int rank;
//        String name;
        int playcount;
//        String mbid;
//        String url;
        Artist artist;
        List<Image> image;
        @SerializedName("@attrs")
        AlbumAttrs attrs;

        @Override
        public String getImageUrl() {
            for (Image i : image) {
                if ("extralarge".equals(i.size)) {
                    return i.text;
                }
            }
            return null;
        }

        @Override
        public String getByLine() {
            return artist.name;
        }
    }

    static class AlbumAttrs {
        int rank;
    }

    static class ArtistAttrs {
        int rank;
    }

    static class TrackAttrs {
        int rank;
    }

    static class Artist extends EntityWithImage {
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

    static class Track extends EntityWithImage {
        int playcount;
        int duration;
        Artist artist;
        List<Image> image;
        @SerializedName("@attrs")
        TrackAttrs attrs;

        @Override
        public String getImageUrl() {
            for (Image i : image) {
                if ("extralarge".equals(i.size)) {
                    return i.text;
                }
            }
            return null;
        }

        @Override
        public String getByLine() {
            return artist.name;
        }
    }

    static class Image {
        @SerializedName("#text")
        String text;
        String size;
    }
}
