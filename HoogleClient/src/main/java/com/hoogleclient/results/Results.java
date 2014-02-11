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

package com.hoogleclient.results;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.hoogleclient.R;

import java.util.ArrayList;

public class Results extends ListFragment {

    private static final String SEARCH_RESULTS = "searchResults";

    private OnResultsFragmentInteractionListener mListener;

    private ArrayList<Result> searchResults;
    private ResultAdapter     resultAdapter;

    public static Results newInstance(ArrayList<Result> searchResults) {
        Results fragment = new Results();

        Bundle args = new Bundle();

        final ArrayList<Result> results;

        if (searchResults == null) {
            results = new ArrayList<Result>();
        } else {
            results = searchResults;
        }

        args.putParcelableArrayList(SEARCH_RESULTS, results);

        fragment.setArguments(args);

        return fragment;
    }

    public Results() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.result_list, container, false);

        if (savedInstanceState != null) {
            searchResults = savedInstanceState.getParcelableArrayList(SEARCH_RESULTS);
        } else {

            final Bundle args = getArguments();

            if (args != null) {
                searchResults = getArguments().getParcelableArrayList(SEARCH_RESULTS);
            }
        }

        /* searchResults can't be null, only an empty array,
           see the 2 points of entry , newResults and newInstance
         */

        /*TODO: display some text or a dialog if the results array is empty */

        if (view != null) {

            final Context context = view.getContext();

            resultAdapter = new ResultAdapter(context, searchResults);

            resultAdapter.notifyDataSetChanged();

            setListAdapter(resultAdapter);
        }

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(SEARCH_RESULTS, searchResults);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnResultsFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                + " must implement OnResultsFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public ArrayList<Result> getSearchResults() {
        return searchResults;
    }

    public void newResults(ArrayList<Result> searchResults) {

        if (searchResults != null) {

            this.searchResults = searchResults;

            if (resultAdapter != null) {
                resultAdapter.clear();
                resultAdapter.addAll(this.searchResults);
                resultAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (null != mListener) {
            mListener.onResultsFragmentInteraction(position);
        }
    }

    public interface OnResultsFragmentInteractionListener {
        public void onResultsFragmentInteraction(int position);
    }

}
