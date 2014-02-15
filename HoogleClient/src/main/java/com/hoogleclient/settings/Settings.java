package com.hoogleclient.settings;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.hoogleclient.R;

public class Settings extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{

    private OnPreferenceFragmentChangeListener mOnPreferenceFragmentChangeListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);

        setPreferenceChangeListener();
    }

    private void setPreferenceChangeListener() {

        final SharedPreferences pSharedPref = getPreferenceScreen().getSharedPreferences();

        if (pSharedPref != null) {
            pSharedPref.registerOnSharedPreferenceChangeListener(this);
        }

    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mOnPreferenceFragmentChangeListener  = (OnPreferenceFragmentChangeListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnPreferenceFragmentChangeListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mOnPreferenceFragmentChangeListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();

        setPreferenceChangeListener();
    }

    @Override
    public void onPause() {
        super.onPause();

        setPreferenceChangeListener();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        mOnPreferenceFragmentChangeListener.onPreferenceChanged(sharedPreferences, key);
    }

   public interface OnPreferenceFragmentChangeListener {
       public void onPreferenceChanged(SharedPreferences sharedPreferences, String key);
   }
}
