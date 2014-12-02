/*
 * Copyright (c) 2014 Nathaniel Bennett.
 *
 * This file is part of Anoted android application project.
 *
 * Anoted is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Anoted is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Anoted.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.co.humbell.anoted;

import android.app.*;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.*;
import android.util.Log;
import android.view.View;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends Activity {

    public void showRestartWarning() {
        findViewById(R.id.settings_restart_alert).setVisibility(View.VISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setupTheme();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        findViewById(R.id.settings_restart_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.getContext().startActivity(new Intent(getBaseContext(), MainActivity.class));
            }
        });

        findViewById(R.id.settings_dont_restart_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.settings_restart_alert).setVisibility(View.GONE);
            }
        });

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_fragment_container, new SettingsFragment())
                .commit();
    }

    private class SettingsFragment extends PreferenceFragment {

        Preference.OnPreferenceChangeListener onPreferenceChangeListener = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                Resources res = getResources();

                if (preference.getKey().equals(res.getString(R.string.pref_key_enable_transparency))
                        || preference.getKey().equals(res.getString(R.string.pref_key_theme))) {
                    showRestartWarning();
                }

                if(preference.getKey().equals(res.getString(R.string.pref_key_theme))) {
                    ListPreference lp = (ListPreference)preference;
                    preference.setSummary(lp.getEntry());
                }

                return true;
            }
        };

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.pref_general);

            ListPreference themeList = (ListPreference)findPreference("pref_theme");
            themeList.setOnPreferenceChangeListener(onPreferenceChangeListener);
            themeList.setSummary(themeList.getEntry());

            CheckBoxPreference transparencyCheckbox = (CheckBoxPreference)findPreference("pref_transparency");
            transparencyCheckbox.setOnPreferenceChangeListener(onPreferenceChangeListener);
        }

    }

    /**
     * From MainActivity.
     */
    private void setupTheme(){

        /*
         * We can't use android's resources before we set the theme ({@link #setTheme})
         * The constraints below need to be the same as the constants defined in
         * "res/values/strings_activity_settings.xml"
        */
        final String PREF_KEY_THEME="pref_theme";
        final String PREF_VALUE_THEME_LIGHT = "light";
        final String PREF_VALUE_THEME_DARK = "dark";

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = prefs.getString(PREF_KEY_THEME, PREF_VALUE_THEME_LIGHT);

        if(theme.equals(PREF_VALUE_THEME_DARK)) {
            setTheme(R.style.AppTheme);
        }
        else if (theme.equals(PREF_VALUE_THEME_LIGHT)) {
            setTheme(R.style.AppTheme_Light);
        }
    }
}
