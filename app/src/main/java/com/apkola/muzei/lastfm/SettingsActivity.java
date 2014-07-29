package com.apkola.muzei.lastfm;

import android.app.Activity;
import android.os.Bundle;
import android.util.SparseIntArray;
import android.view.Menu;
import android.view.MenuItem;

public class SettingsActivity extends Activity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
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
