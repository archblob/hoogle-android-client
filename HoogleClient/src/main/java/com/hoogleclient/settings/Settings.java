package com.hoogleclient.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.hoogleclient.R;

public class Settings extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);
    }
}
