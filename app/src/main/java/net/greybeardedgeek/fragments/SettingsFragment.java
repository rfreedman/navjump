package net.greybeardedgeek.fragments;

import net.greybeardedgeek.R;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.View;

public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}