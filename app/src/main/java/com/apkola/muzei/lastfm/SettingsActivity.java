package com.apkola.muzei.lastfm;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

public class SettingsActivity extends Activity {

    private static final String TAG = "SettingsActivity";
    private static SparseIntArray sRotateMenuIdsByMin = new SparseIntArray();
    private static SparseIntArray sRotateMinsByMenuId = new SparseIntArray();

    static {
        sRotateMenuIdsByMin.put(0, R.id.action_rotate_interval_none);
        sRotateMenuIdsByMin.put(60, R.id.action_rotate_interval_1h);
        sRotateMenuIdsByMin.put(60 * 3, R.id.action_rotate_interval_3h);
        sRotateMenuIdsByMin.put(60 * 6, R.id.action_rotate_interval_6h);
        sRotateMenuIdsByMin.put(60 * 24, R.id.action_rotate_interval_24h);
        sRotateMenuIdsByMin.put(60 * 72, R.id.action_rotate_interval_72h);
        for (int i = 0; i < sRotateMenuIdsByMin.size(); i++) {
            sRotateMinsByMenuId.put(sRotateMenuIdsByMin.valueAt(i), sRotateMenuIdsByMin.keyAt(i));
        }
    }

    private TextView mUsername;
    private Spinner mApiMethod;
    private Spinner mApiPeriod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        mUsername = (TextView)findViewById(R.id.lastfm_username);
        mApiMethod = (Spinner)findViewById(R.id.lastfm_api_method);
        mApiPeriod= (Spinner)findViewById(R.id.lastfm_api_period);

        setupView();
    }

    private void setupView() {
        mUsername.setText(LastFmArtSource.getUsername(this));
        mUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String username = String.valueOf(s);
                LastFmArtSource.setUsername(SettingsActivity.this, username);
            }
        });

        mApiMethod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                LastFmArtSource.setApiMethod(SettingsActivity.this,
                        LastFmArtSource.ApiMethod.values()[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        mApiPeriod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                LastFmArtSource.setApiPeriod(SettingsActivity.this,
                        LastFmArtSource.ApiPeriod.values()[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        int rotateIntervalMin = LastFmArtSource.getSharedPreferences(this)
                .getInt(LastFmArtSource.PREF_ROTATE_INTERVAL_MIN,
                        LastFmArtSource.DEFAULT_ROTATE_INTERVAL_MIN);
        int menuId = sRotateMenuIdsByMin.get(rotateIntervalMin);
        if (menuId != 0) {
            MenuItem item = menu.findItem(menuId);
            if (item != null) {
                item.setChecked(true);
            }
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        }
        int rotateMin = sRotateMinsByMenuId.get(itemId, -1);
        if (rotateMin != -1) {
            LastFmArtSource.getSharedPreferences(this).edit()
                    .putInt(LastFmArtSource.PREF_ROTATE_INTERVAL_MIN, rotateMin)
                    .apply();
            invalidateOptionsMenu();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
