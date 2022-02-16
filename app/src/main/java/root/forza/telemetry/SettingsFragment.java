package root.forza.telemetry;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

public class SettingsFragment extends PreferenceFragmentCompat {
    private Context ctx;
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        Preference preference = getPreferenceScreen().findPreference("reset");
        preference.setOnPreferenceClickListener(preference1 -> {
            if (preference1.getKey().equals("reset")) {
                new AlertDialog.Builder(ctx)
                        .setMessage("Reset?")
                        .setCancelable(true)
                        .setPositiveButton("Yes", (dialog, which) -> PreferenceManager.getDefaultSharedPreferences(ctx).edit().clear().putBoolean("dark_mode", false).apply())
                        .setNegativeButton("No", (DialogInterface.OnClickListener) (dialog, which) -> dialog.cancel())
                        .show();
            }
            return false;
        });
    }

    public SettingsFragment setContext(Context ctx){
        this.ctx = ctx;
        return this;
    }
}