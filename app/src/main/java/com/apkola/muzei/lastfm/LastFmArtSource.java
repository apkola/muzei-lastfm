package com.apkola.muzei.lastfm;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.apps.muzei.api.Artwork;
import com.google.android.apps.muzei.api.RemoteMuzeiArtSource;

import java.util.List;
import java.util.Random;

import retrofit.ErrorHandler;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;

public class LastFmArtSource extends RemoteMuzeiArtSource {
    private static final String TAG = "Last.fm";
    private static final String SOURCE_NAME = "LastFmArtSource";

    private static final int ROTATE_TIME_MILLIS = 3 * 60 * 60 * 1000; // rotate every 3 hours

    public LastFmArtSource() {
        super(SOURCE_NAME);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setUserCommands(BUILTIN_COMMAND_ID_NEXT_ARTWORK);
    }

    @Override
    protected void onTryUpdate(int reason) throws RetryException {
        String currentToken = (getCurrentArtwork() != null) ? getCurrentArtwork().getToken() : null;

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("http://ws.audioscrobbler.com")
                .setRequestInterceptor(new RequestInterceptor() {
                    @Override
                    public void intercept(RequestFacade request) {
                        request.addQueryParam("api_key", Config.API_KEY);
                    }
                })
                .setErrorHandler(new ErrorHandler() {
                    @Override
                    public Throwable handleError(RetrofitError retrofitError) {
                        if (retrofitError.getResponse() == null) {
                            Log.e(TAG, retrofitError.toString());
                            return retrofitError;
                        }
                        int statusCode = retrofitError.getResponse().getStatus();
                        if (retrofitError.isNetworkError()
                                || (500 <= statusCode && statusCode < 600)) {
                            return new RemoteMuzeiArtSource.RetryException();
                        }
                        scheduleUpdate(System.currentTimeMillis() + ROTATE_TIME_MILLIS);
                        return retrofitError;
                    }
                })
                .build();

        LastFmService service = restAdapter.create(LastFmService.class);
        LastFmService.AlbumsResponse response =
                service.getTopAlbums(Config.USER, Config.PERIOD, Config.LIMIT);

        if (response == null || response.topalbums == null || response.topalbums.album == null) {
            throw new RetryException();
        }

        List<LastFmService.Album> albums = response.topalbums.album;

        if (albums.size() == 0) {
            Log.w(TAG, "No albums returned from API.");
            scheduleUpdate(System.currentTimeMillis() + ROTATE_TIME_MILLIS);
            return;
        }

        Random random = new Random();
        LastFmService.Album album;
        String token;
        while (true) {
            album = albums.get(random.nextInt(albums.size()));
            token = album.mbid;
            if (albums.size() <= 1 ||
                    (!TextUtils.equals(token, currentToken)
                            && !TextUtils.isEmpty(album.getImageUrl()))) {
                break;
            }
        }

        publishArtwork(new Artwork.Builder()
                .title(album.name)
                .byline(album.artist.name)
                .imageUri(Uri.parse(album.getImageUrl()))
                .token(token)
                .viewIntent(new Intent(Intent.ACTION_VIEW,
                        Uri.parse(album.url)))
                .build());

        scheduleUpdate(System.currentTimeMillis() + ROTATE_TIME_MILLIS);
    }
}
