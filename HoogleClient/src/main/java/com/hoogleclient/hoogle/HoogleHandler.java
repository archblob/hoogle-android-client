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

package com.hoogleclient.hoogle;

import android.os.AsyncTask;
import android.util.Log;

import com.hoogleclient.results.Result;
import com.hoogleclient.search.SearchBox;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class HoogleHandler extends AsyncTask<String, String, ArrayList<Result>> {

    private static final String IMPLEMENTED_VERSION = "4.2.26";

    private static final String HGURL   = "http://www.haskell.org/hoogle/";
    private static final String HGMODE  = "?mode=";
    private static final String HGQUERY = "&hoogle=";
    private static final String HGSTART = "&start=";
    private static final String HGCOUNT = "&count=";

    private static final String  DMODE  = "json";
    private static final Integer DSTART = 1;

    private static final String VERSION  = "version";
    private static final String RESULTS  = "results";
    private static final String LOCATION = "location";
    private static final String SELF     = "self";
    private static final String DOCS     = "docs";

    private final Integer resultCount;

    private SearchBox mContext;

    private HttpClient   mHoogleClient;

    private OnHoogleSearchTask mOnHoogleSearch;

    public HoogleHandler (SearchBox context, Integer resultCount) {

        this.mContext    = context;
        this.resultCount = resultCount;
        mHoogleClient    = new DefaultHttpClient();

        try {
            mOnHoogleSearch = this.mContext;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnHoogleSearchTask");
        }

    }

    private String hoogleGet(String query) {

        String response = null;

        if (query != null) {

            try {
                final String encodedQuery = URLEncoder.encode(query, "UTF-8");

                final String request = HGURL + HGMODE + DMODE + HGQUERY +
                                       encodedQuery + HGSTART + DSTART.toString() +
                                       HGCOUNT + resultCount.toString();

                final HttpGet mHoogleGet           = new HttpGet(request);
                final HttpResponse mHoogleResponse = mHoogleClient.execute(mHoogleGet);
                final HttpEntity mHoogleEntity     = mHoogleResponse.getEntity();

                response = EntityUtils.toString(mHoogleEntity);

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                //TODO: handle me
                e.printStackTrace();
            }

        }

        //TODO: could be null, handle it!
        return response;
    }

    private ArrayList<Result> jsonToResult(String response) {

        ArrayList<Result> data = new ArrayList<Result>();

        String currentHoogleVersion = "";

        if (response != null) {
            try {

                final JSONObject jsonObj = new JSONObject(response);

                final JSONArray results = jsonObj.getJSONArray(RESULTS);
                currentHoogleVersion    = jsonObj.getString(VERSION);

                for(int i = 0; i < results.length(); i++) {
                    final JSONObject r = results.getJSONObject(i);

                    Result result = new Result(r.getString(LOCATION),
                                               r.getString(SELF),
                                               r.getString(DOCS));

                    data.add(i,result);

                }


            } catch (JSONException e) {

                if (currentHoogleVersion.isEmpty()) {
                    currentHoogleVersion = IMPLEMENTED_VERSION;
                }

                Log.e("HOOGLE_API_VERSION","Implemented Version: " + IMPLEMENTED_VERSION + "\n" +
                                           "Current Version: " + currentHoogleVersion, e);
                e.printStackTrace();
            }
        }

        return data;
    }

    @Override
    public ArrayList<Result> doInBackground(String... queries) {
        //TODO: handle edge cases here
        return jsonToResult(hoogleGet(queries[0]));
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(ArrayList<Result> results) {

        if (mOnHoogleSearch != null) {
            mOnHoogleSearch.onHoogleSearchTaskResult(results);
        }

        mOnHoogleSearch = null;
        mContext = null;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        mOnHoogleSearch = null;
        mContext = null;
    }

    public interface OnHoogleSearchTask {
        public void onHoogleSearchTaskResult (ArrayList<Result> results);
    }
}
