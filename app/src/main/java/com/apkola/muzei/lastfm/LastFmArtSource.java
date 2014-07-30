package com.apkola.muzei.lastfm;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.apps.muzei.api.Artwork;
import com.google.android.apps.muzei.api.RemoteMuzeiArtSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import retrofit.ErrorHandler;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;

public class LastFmArtSource extends RemoteMuzeiArtSource {
    private static final String TAG = "Last.fm";
    private static final String SOURCE_NAME = "LastFmArtSource";

    private static final String PREF_API_USERNAME = "apiUsername";
    private static final String PREF_API_METHOD = "apiMethod";
    private static final String PREF_API_PERIOD = "apiPeriod";
    public static final String PREF_ROTATE_INTERVAL_MIN = "rotate_interval_min";
    public static final int DEFAULT_ROTATE_INTERVAL_MIN = 60 * 3; //3 hours

    public static final String ACTION_PUBLISH_NEXT_LAST_FM_ITEM =
            "com.apkola.muzei.lastfm.action.PUBLISH_NEXT_LAST_FM_ITEM";

    public enum ApiMethod {
        TOP_ALBUMS("Top albums"),
        TOP_ARTISTS("Top artists"),
        TOP_TRACKS("Top Tracks");

        private String mName;
        ApiMethod(String name) {
            mName = name;
        }
        @Override
        public String toString() {
            return mName;
        }
    }

    public enum ApiPeriod {
        SEVEN_DAYS("7day"),
        ONE_MONTH("1month"),
        THREE_MONTHS("3month"),
        SIX_MONTHS("6month"),
        TWELVE_MONTHS("12month"),
        OVERALL("overall");

        private String mName;
        ApiPeriod(String name) {
            mName = name;
        }
        @Override
        public String toString() {
            return mName;
        }
    }

    public LastFmArtSource() {
        super(SOURCE_NAME);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setUserCommands(BUILTIN_COMMAND_ID_NEXT_ARTWORK);
        setDescription(getApiMethod(this).toString());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            super.onHandleIntent(intent);
            return;
        }

        String action = intent.getAction();
        if (ACTION_PUBLISH_NEXT_LAST_FM_ITEM.equals(action)) {
            onUpdate(UPDATE_REASON_USER_NEXT);
            return;
        }
        super.onHandleIntent(intent);
    }

    @Override
    protected void onTryUpdate(int reason) throws RetryException {
        setDescription(getApiMethod(this).toString());
        String currentToken = (getCurrentArtwork() != null) ? getCurrentArtwork().getToken() : null;

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("http://ws.audioscrobbler.com")
                .setRequestInterceptor(new RequestInterceptor() {
                    @Override
                    public void intercept(RequestFacade request) {
                        request.addQueryParam("api_key", Config.API_KEY);
                    }
                })
                .setLogLevel(RestAdapter.LogLevel.FULL)
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

        String username = getUsernameOrMine();
        ApiMethod apiMethod = getApiMethod(this);
        ApiPeriod apiPeriod = getApiPeriod(this);

        Log.i(TAG, String.format("Trying update %s for last %s for %s", apiMethod, apiPeriod, username));

        LastFmService service = restAdapter.create(LastFmService.class);
        List<? extends LastFmService.EntityWithImage> items = null;
        try {
            switch (apiMethod) {
                case TOP_ALBUMS:
                    LastFmService.AlbumsResponse albumsResponse =
                            service.getTopAlbums(username, apiPeriod.toString(), Config.LIMIT);
                    if (albumsResponse == null || albumsResponse.topalbums == null || albumsResponse.topalbums.album == null) {
                        throw new RetryException();
                    }
                    items = albumsResponse.topalbums.album;
                    break;

                case TOP_ARTISTS:
                    LastFmService.ArtistsResponse artistsResponse =
                            service.getTopArtists(username, apiPeriod.toString(), Config.LIMIT);
                    if (artistsResponse == null || artistsResponse.topartists == null || artistsResponse.topartists.artist == null) {
                        throw new RetryException();
                    }
                    items = artistsResponse.topartists.artist;
                    break;

                case TOP_TRACKS:
                    LastFmService.TracksResponse tracksResponse =
                            service.getTopTracks(username, apiPeriod.toString(), Config.LIMIT);
                    if (tracksResponse == null || tracksResponse.toptracks == null || tracksResponse.toptracks.track == null) {
                        throw new RetryException();
                    }
                    items = tracksResponse.toptracks.track;
                    break;
            }
        } catch (RetrofitError e) {
            Log.e(TAG, "Failed: " + e.getMessage());
            throw new RetryException(e);
        }

        if (items.size() == 0) {
            Log.w(TAG, "No albums returned from API.");
            scheduleNext();
            return;
        }

        Random random = new Random();
        LastFmService.EntityWithImage item;
        String token;

        while (true) {
            item = items.get(random.nextInt(items.size()));
            token = item.mbid;
            if (items.size() <= 1 ||
                    (!TextUtils.equals(token, currentToken)
                            && !TextUtils.isEmpty(item.getImageUrl()))) {
                break;
            }
        }

        publishArtwork(new Artwork.Builder()
                .title(item.name)
                .byline(item.getByLine())
                .imageUri(Uri.parse(item.getImageUrl()))
                .token(token)
                .viewIntent(new Intent(Intent.ACTION_VIEW,
                        Uri.parse(item.url)))
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

    private String getUsernameOrMine() {
        String username = getUsername(this);
        if (TextUtils.isEmpty(username)) {
            return Config.USERNAME;
        }
        return username;
    }

    static SharedPreferences getSharedPreferences(Context context) {
        return getSharedPreferences(context, SOURCE_NAME);
    }

    public static String getUsername(Context context) {
        return getSharedPreferences(context).getString(PREF_API_USERNAME, "");
    }

    public static void setUsername(Context context, String username) {
        getSharedPreferences(context).edit()
                .putString(PREF_API_USERNAME, username)
                .apply();
    }

    public static void setApiMethod(Context context, ApiMethod apiMethod) {
        getSharedPreferences(context).edit()
                .putInt(PREF_API_METHOD, apiMethod.ordinal())
                .apply();
    }

    public static ApiMethod getApiMethod(Context context) {
        return ApiMethod.values()[getSharedPreferences(context).getInt(PREF_API_METHOD, 0)];
    }

    public static void setApiPeriod(Context context, ApiPeriod apiPeriod) {
        getSharedPreferences(context).edit()
                .putInt(PREF_API_PERIOD, apiPeriod.ordinal())
                .apply();
    }

    public static ApiPeriod getApiPeriod(Context context) {
        return ApiPeriod.values()[getSharedPreferences(context).getInt(PREF_API_PERIOD, 0)];
    }
}
