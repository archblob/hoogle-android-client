/*
  Copyright (c) 2014, Csernik Flaviu Andrei
  All rights reserved.

  Redistribution and use in source and binary forms, with or without modification, are permitted
  provided that the following conditions are met:

  1. Redistributions of source code must retain the above copyright notice, this list of conditions
     and the following disclaimer.

  2. Redistributions in binary form must reproduce the above copyright notice, this list of
     conditions and the following disclaimer in the documentation and/or other materials provided
     with the distribution.

  3. Neither the name of the copyright holder nor the names of its contributors may be used to
     endorse or promote products derived from this software without specific prior written permission.

  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
  ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package com.hoogleclient;

import android.animation.LayoutTransition;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.hoogleclient.doc.DocDetails;
import com.hoogleclient.results.Result;
import com.hoogleclient.results.Results;
import com.hoogleclient.search.SearchBox;
import com.hoogleclient.settings.Settings;

import java.util.ArrayList;

public class HCMain extends ActionBarActivity
    implements Results.OnResultsFragmentInteractionListener,
               SearchBox.OnHoogleSearchListener,
               Settings.OnPreferenceFragmentChangeListener {

    private static final String RESULT_COUNT_PREFERENCE = "results_number";

    private static final String SEARCH_FRAGMENT_TAG  = "search_fragment";
    private static final String RESULTS_FRAGMENT_TAG = "results_fragment";
    private static final String DOC_FRAGMENT_TAG     = "doc_fragment";
    private static final String STNGS_FRAGMENT_TAG   = "settings_fragment";

    private Results    mResultsFragment;
    private DocDetails mDocFragment;
    private Settings   mSettingsFragment;
    private SearchBox  mSearchFragment;

    private LinearLayout mSearchLinearLayout;
    private FrameLayout  mResultsFrameLayout;
    private FrameLayout  mDocFrameLayout;
    private LinearLayout mResultsSettingsLinearLayout;
    private LinearLayout mMainInterfaceLinearLayout;
    private FrameLayout  mSettingsFrameLayout;

    private FragmentManager mFM;

    private ActionBar mActionBar;
    private SharedPreferences mSharedPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_hcmain);

        if (mActionBar == null) {
            mActionBar = getSupportActionBar();
        }


        if (mSharedPreference == null) {
            mSharedPreference = PreferenceManager.getDefaultSharedPreferences(this);
        }

        mSearchLinearLayout          = (LinearLayout) findViewById(R.id.search_container);
        mResultsFrameLayout          = (FrameLayout)  findViewById(R.id.results_container);
        mDocFrameLayout              = (FrameLayout)  findViewById(R.id.doc_container);
        mResultsSettingsLinearLayout = (LinearLayout) findViewById(R.id.results_settings_container);
        mMainInterfaceLinearLayout   = (LinearLayout) findViewById(R.id.main_interface_container);
        mSettingsFrameLayout         = (FrameLayout) findViewById(R.id.settings_container);

        addFragmentsToLayout();

        //TODO: provide alternative for earlier api, use back button
        showUpNavigation();

        /* LayoutTransition.CHANGING is only available since API Level 16 */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {

            //TODO: this could be null, deal with it
            ViewGroup mainView = (ViewGroup) findViewById(R.id.container);

            if (mainView == null) {
                mainView = (ViewGroup) findViewById(R.id.land_container);
            }

            LayoutTransition transition = mainView.getLayoutTransition();
            transition.enableTransitionType(LayoutTransition.CHANGING);

        }

    }

    private void addFragmentsToLayout() {
        mFM = getFragmentManager();

        mSearchFragment  = (SearchBox) mFM.findFragmentByTag(SEARCH_FRAGMENT_TAG);
        mResultsFragment = (Results) mFM.findFragmentByTag(RESULTS_FRAGMENT_TAG);
        mDocFragment     = (DocDetails) mFM.findFragmentByTag(DOC_FRAGMENT_TAG);

        if (mSearchFragment != null) {

            mFM.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

            mFM.beginTransaction()
                    .remove(mSearchFragment)
                    .commit();

            mFM.executePendingTransactions();

            mFM.beginTransaction()
                    .add(R.id.search_container, mSearchFragment, SEARCH_FRAGMENT_TAG)
                    .commit();

            if (mResultsFragment != null) {

                mFM.beginTransaction()
                        .remove(mResultsFragment)
                        .commit();

                mFM.executePendingTransactions();

                mFM.beginTransaction()
                        .add(R.id.results_container, mResultsFragment, RESULTS_FRAGMENT_TAG)
                        .addToBackStack(RESULTS_FRAGMENT_TAG)
                        .commit();
            }

            if (mDocFragment != null) {

                mFM.beginTransaction()
                        .remove(mDocFragment)
                        .commit();

                mFM.executePendingTransactions();

                mFM.beginTransaction()
                        .add(R.id.doc_container, mDocFragment, DOC_FRAGMENT_TAG)
                        .addToBackStack(DOC_FRAGMENT_TAG)
                        .commit();
            }

        } else {
            //TODO: make custom preference class to avoid this nonsense
            mSearchFragment = SearchBox.newInstance(
                    Integer.parseInt(mSharedPreference.getString(RESULT_COUNT_PREFERENCE,
                            String.valueOf(R.integer.defResultCount))
                    ));

            mFM.beginTransaction()
                    .add(R.id.search_container, mSearchFragment, SEARCH_FRAGMENT_TAG)
                    .commit();

            mFM.executePendingTransactions();
        }

        mFM.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                setLayout();
            }
        });
    }

    private void showUpNavigation() {
        if ((mResultsFragment  != null && mResultsFragment.isAdded()) ||
            (mDocFragment      != null && mDocFragment.isAdded()) ||
            (mSettingsFragment != null && mSettingsFragment.isAdded())) {

            mActionBar.setDisplayHomeAsUpEnabled(true);

        } else {
            mActionBar.setDisplayHomeAsUpEnabled(false);
        }
    }

    /* TODO: simplify the logic */
    /* TODO: provide alternative using the back button */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public boolean onSupportNavigateUp() {

        if (mFM != null) {
            final String topTag = mFM.getBackStackEntryAt(mFM.getBackStackEntryCount() - 1).getName();

            if (RESULTS_FRAGMENT_TAG.equals(topTag)) {
                mResultsFragment = (Results) mFM.findFragmentByTag(topTag);
            } else if (DOC_FRAGMENT_TAG.equals(topTag)) {
                mDocFragment = (DocDetails) mFM.findFragmentByTag(topTag);
            } else if (STNGS_FRAGMENT_TAG.equals(topTag)) {
                mSettingsFragment = (Settings) mFM.findFragmentByTag(topTag);
            }

            if (!SEARCH_FRAGMENT_TAG.equals(topTag)) {
                mFM.popBackStack();
                mFM.executePendingTransactions();
            }

        }

        return super.onSupportNavigateUp();
    }

    private void maybeShowPortraitSettings() {
        if (mSettingsFragment != null && mSettingsFragment.isAdded()) {
            mSettingsFrameLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
        } else {
            mSettingsFrameLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
        }
    }

    private void setLayout() {

        final boolean inLayoutResults  = mResultsFragment != null && mResultsFragment.isAdded();
        final boolean inLayoutDoc      = mDocFragment != null && mDocFragment.isAdded();
        final boolean inLayoutSettings = mSettingsFragment != null && mSettingsFragment.isAdded();

        if (findViewById(R.id.container) != null) {

            if (inLayoutDoc) {

                mSearchLinearLayout.setLayoutParams( new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                ));

                mResultsFrameLayout.setLayoutParams( new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        0
                ));

                mDocFrameLayout.setLayoutParams( new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                ));

                maybeShowPortraitSettings();

            } else {

                mSearchLinearLayout.setLayoutParams( new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                ));

                mResultsFrameLayout.setLayoutParams( new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                ));

                mDocFrameLayout.setLayoutParams( new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        0
                ));

                maybeShowPortraitSettings();
            }

        } else {
          /* we are in landscape mode */

            if (inLayoutDoc) {
            /* sr + se + dc || sr + re + dc */

                mMainInterfaceLinearLayout.setLayoutParams( new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1f
                ));

                mDocFrameLayout.setLayoutParams( new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        1f
                ));

                mSearchLinearLayout.setLayoutParams( new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                ));

                mResultsSettingsLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                ));

                if (inLayoutSettings) {
                /* sr + se + dc */
                    mResultsFrameLayout.setLayoutParams( new LinearLayout.LayoutParams(
                            0,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    ));

                    mSettingsFrameLayout.setLayoutParams(new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    ));
                } else {
                /* sr + re + dc */
                    mResultsFrameLayout.setLayoutParams(new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    ));

                    mSettingsFrameLayout.setLayoutParams(new LinearLayout.LayoutParams(
                            0,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    ));
                }

            } else {
                mMainInterfaceLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                ));

                mDocFrameLayout.setLayoutParams(new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                ));

                mSearchLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                ));

                if (inLayoutResults || inLayoutSettings) {

                    mResultsSettingsLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    ));

                    if (inLayoutSettings && inLayoutResults) {

                        mResultsFrameLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                0,
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                2f
                        ));

                        mSettingsFrameLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                0,
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                1f
                        ));

                    } else if (inLayoutResults) {
                        mResultsFrameLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        ));

                        mSettingsFrameLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                0,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        ));
                    } else {
                        mResultsFrameLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                0,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        ));

                        mSettingsFrameLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        ));
                    }

                } else {
                    mResultsSettingsLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                            0,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    ));
                }

            }
        }
        showUpNavigation();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.hcmain, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO: back button does not work.
        int id = item.getItemId();
        if (id == R.id.action_settings) {

            if (mSettingsFragment == null) {
                mSettingsFragment = new Settings();
            }

            if (!mSettingsFragment.isAdded()) {

                mFM.beginTransaction()
                   .add(R.id.settings_container, mSettingsFragment, STNGS_FRAGMENT_TAG )
                   .addToBackStack(STNGS_FRAGMENT_TAG)
                   .commit();
            }
        }
        return super.onOptionsItemSelected(item);
    }


    /*TODO: get rid of the code duplication here and in places similar, with the transaction stuff */
    @Override
    public void onResultsFragmentInteraction(int position) {
        /* We are assuming here that all results have an url attached */
        final ArrayList<Result> results = mResultsFragment.getSearchResults();
        final String docDetailsURL      = results.get(position).getLocation();

        if (mFM != null) {
            if (mDocFragment != null) {
                mDocFragment.setUrl(docDetailsURL);

                if (!mDocFragment.isAdded()) {
                    mFM.beginTransaction().add(R.id.doc_container, mDocFragment, DOC_FRAGMENT_TAG)
                       .addToBackStack(DOC_FRAGMENT_TAG)
                       .commit();
                    mFM.executePendingTransactions();
                }

            } else {
                mDocFragment = DocDetails.newInstance(docDetailsURL);

                mFM.beginTransaction()
                   .add(R.id.doc_container, mDocFragment, DOC_FRAGMENT_TAG)
                   .addToBackStack(DOC_FRAGMENT_TAG)
                   .commit();

                mFM.executePendingTransactions();
            }
        }

    }

    //TODO: clean this the same way as above
    @Override
    public void onHoogleSearchResult(ArrayList<Result> results) {

        /* pop the backstack if documentation fragment is showing,
           on a new search it is no longer relavant.
         */

    if (!(mFM == null || results == null || results.isEmpty())) {

        if (mDocFragment != null && mDocFragment.isAdded()) {

            mDocFragment = (DocDetails) mFM.findFragmentByTag(DOC_FRAGMENT_TAG);
            mFM.popBackStack();
            mFM.executePendingTransactions();
        }

        if (mResultsFragment != null) {

            mResultsFragment.newResults(results);

            if (!mResultsFragment.isAdded()) {
                mFM.beginTransaction()
                   .add(R.id.results_container, mResultsFragment, RESULTS_FRAGMENT_TAG)
                   .addToBackStack(RESULTS_FRAGMENT_TAG)
                   .commit();
                mFM.executePendingTransactions();
            }

        } else {
            mResultsFragment = Results.newInstance(results);

            mFM.beginTransaction()
                    .add(R.id.results_container, mResultsFragment, RESULTS_FRAGMENT_TAG)
                    .addToBackStack(RESULTS_FRAGMENT_TAG)
                    .commit();

            mFM.executePendingTransactions();
        }
    } else {

        final Context context = getApplicationContext();

        if (context != null) {

            final int duration = Toast.LENGTH_LONG;
            final int text     = R.string.no_results;

            final Toast toast = Toast.makeText(context, text, duration);

            toast.setGravity(Gravity.CENTER,0,0);
            toast.show();
        }
    }

    }

    @Override
    public void onPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        mSharedPreference = sharedPreferences;
        Log.e("key", key);

        if(key.equals(RESULT_COUNT_PREFERENCE)) {
            final int resultCount = Integer.parseInt(sharedPreferences.getString(
                    RESULT_COUNT_PREFERENCE,
                    String.valueOf(R.integer.defResultCount)
            ));

            Log.e("newResultCount", String.valueOf(resultCount));
            if (mSearchFragment != null) {
                mSearchFragment.setResultCount(resultCount);
            }
        }
    }
}
