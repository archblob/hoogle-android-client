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

package com.hoogleclient.search;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.hoogleclient.R;
import com.hoogleclient.hoogle.HoogleHandler;
import com.hoogleclient.results.Result;

import java.util.ArrayList;

public class SearchBox extends Fragment implements HoogleHandler.OnHoogleSearchTask{

    private OnHoogleSearchListener mListener;

    private EditText mSearchBox;

    public SearchBox() {
    }

    public static SearchBox newInstance() {
        return new SearchBox();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_box, container, false);

        if (mSearchBox == null && view != null) {
            mSearchBox = (EditText) view.findViewById(R.id.search_box);
        }

        if (mSearchBox != null) {

            final SearchBox that = this;

            mSearchBox.setOnEditorActionListener(new EditText.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    boolean handled = false;

                    /* TODO: the soft keyboard won't go away if action is no DONE */
                    /* also if imeActionLabel is set, actionId will always be zero */

                    if (actionId == EditorInfo.IME_ACTION_DONE) {

                        HoogleHandler hoogleHandler = new HoogleHandler(that);
                        hoogleHandler.execute(String.valueOf(mSearchBox.getText()));

                        handled = true;
                    }

                    return handled;
                }
            });

        }

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener  = (OnHoogleSearchListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHoogleSearchListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener  = null;
        mSearchBox = null;
    }

    @Override
    public void onHoogleSearchTaskResult(ArrayList<Result> results) {
        mListener.onHoogleSearchResult(results);
    }

    public interface OnHoogleSearchListener {
        public void onHoogleSearchResult(ArrayList<Result> results);
    }

}
