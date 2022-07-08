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

import androidx.appcompat.widget.SwitchCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

public class SettingsFragment extends PreferenceFragmentCompat {
    private Context ctx;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        //Shows dialog with tutorial when preference is clicked
        getPreferenceScreen().findPreference("tutorial")
                .setOnPreferenceClickListener(preference1 -> {
                    if (preference1.getKey().equals("tutorial")) {
                        new AlertDialog.Builder(getActivity())
                                .setTitle("How to use")
                                .setMessage("Open up the app and make sure you are connected to the same wifi as the device you have the game running on. You'll see an IP address at the top.\n\n" +
                                        "Open up your game, go to HUD AND GAMEPLAY and scroll down to the bottom. Type in the IP that's at the top of the app, and the default port (5300). Set DATA OUT to ON.\n\n" +
                                        "And you're done! The button should change to \"CONNECTED\". When you unpause the game, data should begin to come in. If you have any problems or crashes, " +
                                        "leave an issue in the github, or DM me on discord at root.#6923")
                                .setCancelable(true)
                                .setPositiveButton("GOT IT", null)
                                .show();
                    }
                    return false;
                });

        //Flip boolean for sharedpref "is_metric" when preference is clicked
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        getPreferenceScreen().findPreference("is_metric")
                .setOnPreferenceClickListener(preference1 -> {
                    if (preference1.getKey().equals("is_metric")) {
                        if(prefs.getBoolean("is_metric", false)) {
                            prefs.edit().putBoolean("is_metric", true).apply();
                        } else prefs.edit().putBoolean("is_metric", false).apply();
                    }
                    return false;
                });

        getPreferenceScreen().findPreference("show_fps")
                .setOnPreferenceClickListener(preference1 -> {
                    if (preference1.getKey().equals("show_fps")) {
                        if(prefs.getBoolean("show_fps", false)) {
                            prefs.edit().putBoolean("show_fps", true).apply();
                        } else prefs.edit().putBoolean("show_fps", false).apply();
                    }
                    return false;
                });

        /*getPreferenceScreen().findPreference("dark_mode")
                .setOnPreferenceClickListener(preference1 -> {
                    if (preference1.getKey().equals("dark_mode")) {
                        if(prefs.getBoolean("dark_mode", false)) {
                            prefs.edit().putBoolean("dark_mode", true).apply();
                        } else {
                            prefs.edit().putBoolean("dark_mode", false).apply();
                        }
                    }
                    return false;
        });
        */

        /*
        * Reset preferences button for hiding TextViews, unused for now
        getPreferenceScreen().findPreference("reset")
                .setOnPreferenceClickListener(preference1 -> {
            if (preference1.getKey().equals("reset")) {
                new AlertDialog.Builder(ctx)
                        .setMessage("Reset?")
                        .setCancelable(true)
                        .setPositiveButton("Yes", (dialog, which) -> PreferenceManager.getDefaultSharedPreferences(ctx).edit() ---> CLEAR ALL PREFS FUNCTION HERE
                        .setNegativeButton("No", (dialog, which) -> dialog.cancel())
                        .show();
            }
            return false;
        });
        */
    }

    public SettingsFragment setContext(Context ctx){
        this.ctx = ctx;
        return this;
    }
}