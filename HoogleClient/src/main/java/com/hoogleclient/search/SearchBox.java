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
import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.hoogleclient.R;
import com.hoogleclient.hoogle.HoogleHandler;
import com.hoogleclient.results.Result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SearchBox extends Fragment implements HoogleHandler.OnHoogleSearchTask{

    private static final String RESULT_COUNT = "resultCount";
    private static final String START_COUNT  = "startCount";

    private static final Map<Character,Character> mAcMap;

    static {
        mAcMap = new HashMap<Character, Character>();
        mAcMap.put('(',')');
        mAcMap.put('[',']');
    }


    private OnHoogleSearchListener mListener;

    private EditText    mSearchBox;
    private TextWatcher mTextWatcher;

    private int mResultCount;
    private int mStartCount;

    public SearchBox() {
    }

    public static SearchBox newInstance(int resultCount, int startCount) {
        SearchBox fragment = new SearchBox();

        Bundle args = new Bundle();

        args.putInt(RESULT_COUNT, resultCount);
        args.putInt(START_COUNT, startCount);

        fragment.setArguments(args);

        return fragment;
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

        final Bundle args = getArguments();

        if (args != null) {
            mResultCount = args.getInt(RESULT_COUNT);
            mStartCount  = args.getInt(START_COUNT);
        }



        if (mSearchBox != null) {

            final SearchBox that = this;

            mSearchBox.setOnEditorActionListener(new EditText.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    boolean handled = false;

                    /* TODO: when Start value settings are implemented, check to see if the
                       query string is the same and then for the required number of results.
                       Don't request results again, just those offset by the start value.
                     */
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                        HoogleHandler hoogleHandler = new HoogleHandler(that, mResultCount, mStartCount);
                        hoogleHandler.execute(String.valueOf(mSearchBox.getText()));

                        final Activity activity = that.getActivity();

                        if (activity != null) {
                            final Context context   = activity.getApplicationContext();

                            if (context != null) {
                                final InputMethodManager inputManager = (InputMethodManager)
                                        context.getSystemService(Context.INPUT_METHOD_SERVICE);

                                final View currentFocus = activity.getCurrentFocus();

                                if (currentFocus != null && inputManager != null) {
                                    inputManager.hideSoftInputFromWindow(
                                            currentFocus.getWindowToken(),
                                            InputMethodManager.RESULT_UNCHANGED_SHOWN
                                            );
                                }
                            }
                        }

                        handled = true;
                    }

                    return handled;
                }
            });

            if (mTextWatcher == null) {
                mTextWatcher = new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                        if (start < s.length() &&  mAcMap.containsKey(s.charAt(start))) {

                            final int sStart = mSearchBox.getSelectionStart();

                            final Editable currentText = mSearchBox.getText();

                            if (currentText != null) {
                                currentText.insert(sStart, mAcMap.get(s.charAt(start)).toString());
                            }


                            mSearchBox.setSelection(start + count);
                        }

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                };
            }

            mSearchBox.addTextChangedListener(mTextWatcher);

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

    public void setResultCount(Integer resultCount) {
        this.mResultCount = resultCount;
    }

    public void setStartCount(int startCount) {
        this.mStartCount = startCount;
    }

    public interface OnHoogleSearchListener {
        public void onHoogleSearchResult(ArrayList<Result> results);
    }

}
