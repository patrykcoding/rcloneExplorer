package ca.pkay.rcloneexplorer.RemoteConfig;

import android.app.ActivityManager;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;

import ca.pkay.rcloneexplorer.CustomColorHelper;
import ca.pkay.rcloneexplorer.R;

public class RemoteConfig extends AppCompatActivity implements RemotesConfigList.ProviderSelectedListener {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyTheme();
        setContentView(R.layout.activity_remote_config);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        Fragment fragment = RemotesConfigList.newInstance();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.flFragment, fragment, "config list");
        fragmentTransaction.commit();
    }

    private void applyTheme() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int customPrimaryColor = sharedPreferences.getInt(getString(R.string.pref_key_color_primary), -1);
        int customAccentColor = sharedPreferences.getInt(getString(R.string.pref_key_color_accent), -1);
        boolean isDarkTheme = sharedPreferences.getBoolean(getString(R.string.pref_key_dark_theme), false);
        getTheme().applyStyle(CustomColorHelper.getPrimaryColorTheme(this, customPrimaryColor), true);
        getTheme().applyStyle(CustomColorHelper.getAccentColorTheme(this, customAccentColor), true);
        if (isDarkTheme) {
            getTheme().applyStyle(R.style.DarkTheme, true);
        } else {
            getTheme().applyStyle(R.style.LightTheme, true);
        }

        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorPrimaryDark, typedValue, true);
        getWindow().setStatusBarColor(typedValue.data);

        // set recents app color to the primary color
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round);
        ActivityManager.TaskDescription taskDesc = new ActivityManager.TaskDescription(getString(R.string.app_name), bm, customPrimaryColor);
        setTaskDescription(taskDesc);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onProviderSelected(int provider) {
        if (provider < 0) {
            finish();
            return;
        }

        Fragment fragment;
        String s = RemotesConfigList.providers.get(provider);
        switch (s) {
            case "AMAZON CLOUD DRIVE":
                fragment = AmazonCloudStorageConfig.newInstance();
                break;
            case "B2":
                fragment = B2Config.newInstance();
                break;
            case "BOX":
                fragment = BoxConfig.newInstance();
                break;
            case "FTP":
                fragment = ftpConfig.newInstance();
                break;
            case "DROPBOX":
                fragment = DropboxConfig.newInstance();
                break;
            default:
                return;
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.flFragment, fragment, "new config");
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
