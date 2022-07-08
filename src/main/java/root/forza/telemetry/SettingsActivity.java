package root.forza.telemetry;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("dark_mode", false)) {
            setTheme(R.style.Theme_ForzaTelemetryLight);
        } else setTheme(R.style.Theme_ForzaTelemetryDark); */
        setContentView(R.layout.settings_activity);

        //Set toolbar and "up" action (back button)
        Toolbar toolbar = findViewById(R.id.back_toolbar);
        toolbar.setTitle("Settings");
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Begin replacing settings_activity with preference fragment declared in SettingsActivity
        getSupportFragmentManager().beginTransaction().replace(R.id.prefscreen, new SettingsFragment().setContext(this)).commit();
    }

    @Override
    public boolean onSupportNavigateUp(){
        //Call finish() instead of startActivity(..) to keep activity from rebinding to the forza socket if a connection is still established
        finish();
        return true;
    }
}