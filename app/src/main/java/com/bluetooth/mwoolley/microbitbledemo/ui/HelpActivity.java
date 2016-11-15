package com.bluetooth.mwoolley.microbitbledemo.ui;
/*
 * Author: Martin Woolley
 * Twitter: @bluetooth_mdw
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.webkit.WebView;

import com.bluetooth.mwoolley.microbitbledemo.Constants;
import com.bluetooth.mwoolley.microbitbledemo.R;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Back");

        String uri = Constants.NO_HELP;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            uri = extras.getString(Constants.URI);
        }

        WebView help_content = (WebView)findViewById(R.id.general_help);
        help_content.getSettings().setBuiltInZoomControls(true);
        help_content.loadUrl(uri);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.setResult(RESULT_OK);
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
