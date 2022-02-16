package root.forza.telemetry;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {
    private SharedPreferences.OnSharedPreferenceChangeListener listener;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getBool("dark_mode")) {
            setTheme(R.style.Theme_ForzaTelemetryDark);
        } else {
            setTheme(R.style.Theme_ForzaTelemetryLight);
        }
        setContentView(R.layout.settings_activity);

        //If you want to insert data in your settings
        SettingsFragment settingsFragment = new SettingsFragment();
        settingsFragment.setContext(this);
        getSupportFragmentManager().beginTransaction().replace(R.id.prefscreen, settingsFragment).commit();

        Toolbar toolbar = (Toolbar) findViewById(R.id.back_toolbar);
        toolbar.setTitle("Settings");
        //toolbar.setSubtitle("Subtitle");

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportFragmentManager().beginTransaction().replace(R.id.prefscreen, new SettingsFragment().setContext(this)).commit();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        listener = (pre, key) -> {
            if (key != null && key.equals("dark_mode") ) {
                if (prefs.getBoolean("dark_mode", false)) {
                    SettingsActivity.this.setTheme(R.style.Theme_ForzaTelemetryDark);
                } else {
                    SettingsActivity.this.setTheme(R.style.Theme_ForzaTelemetryLight);
                }
                SettingsActivity.this.recreate();
            }
        };
        prefs.registerOnSharedPreferenceChangeListener(listener);


    }

    @Override
    public boolean onSupportNavigateUp(){
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        return true;
    }

    public boolean getBool(String pref_name){
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(pref_name, false);

    }
}