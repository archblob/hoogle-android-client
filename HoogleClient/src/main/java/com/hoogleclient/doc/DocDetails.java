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

package com.hoogleclient.doc;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.hoogleclient.R;

public class DocDetails extends Fragment {

    private static final String DOC_DETAILS_URL = "docDetailsURL";

    private WebView     mWebView;
    private ProgressBar mProgress;
    private String      mCurrentUrl;

    public static DocDetails newInstance(String docDetailsURL) {
        DocDetails fragment = new DocDetails();

        Bundle args = new Bundle();

        args.putString(DOC_DETAILS_URL, docDetailsURL);

        fragment.setArguments(args);

        return fragment;
    }
    public DocDetails() {
    }

    public void setUrl (String newUrl) {
        /* Should we check if mWebView is null ?
           it is always initialized in the factory method.
           (unless the inflater returns null, but that will be the root of the
           failure)
         */

        if (!(newUrl == null || newUrl.equals(mCurrentUrl))) {
            mCurrentUrl = newUrl;
            mWebView.loadUrl(newUrl);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.doc_details, container, false);

        if (view != null) {
            mWebView = (WebView) view.findViewById(R.id.doc_details);

            if (mProgress == null) {
                mProgress = (ProgressBar) view.findViewById(R.id.doc_progress);
            }

            mWebView.setWebViewClient( new WebViewClient() {

                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                    mProgress.setVisibility(View.VISIBLE);
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    mProgress.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                    super.onReceivedError(view, errorCode, description, failingUrl);
                        //TODO: Handle me
                }
            });

            mWebView.setWebChromeClient(new WebChromeClient() {
                @Override
                public void onProgressChanged(WebView view, int newProgress) {
                        super.onProgressChanged(view, newProgress);

                 }
            });

            mWebView.getSettings().setJavaScriptEnabled(false);
        }

        final Bundle args = getArguments();

        if (args != null) {
            mCurrentUrl = (String) args.get(DOC_DETAILS_URL);
        }

        if (mCurrentUrl != null) {
            mWebView.loadUrl(mCurrentUrl);
        }

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
