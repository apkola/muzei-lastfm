package com.apkola.muzei.lastfm;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

    public static final String PREF_ROTATE_INTERVAL_MIN = "rotate_interval_min";
    public static final int DEFAULT_ROTATE_INTERVAL_MIN = 60 * 3; //3 hours

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
                        scheduleNext();
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
            scheduleNext();
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

        scheduleNext();
    }

    private void scheduleNext() {
        int rotateIntervalMinutes = getSharedPreferences().getInt(PREF_ROTATE_INTERVAL_MIN,
                DEFAULT_ROTATE_INTERVAL_MIN);
        if (rotateIntervalMinutes > 0) {
            scheduleUpdate(System.currentTimeMillis() + rotateIntervalMinutes * 60 * 1000);
        }
    }

    static SharedPreferences getSharedPreferences(Context context) {
        return getSharedPreferences(context, SOURCE_NAME);
    }
}
