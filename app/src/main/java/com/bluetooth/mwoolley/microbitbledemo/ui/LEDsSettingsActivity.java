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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

import com.bluetooth.mwoolley.microbitbledemo.Constants;
import com.bluetooth.mwoolley.microbitbledemo.R;
import com.bluetooth.mwoolley.microbitbledemo.Settings;

public class LEDsSettingsActivity extends AppCompatActivity {

    public static final int START_LEDS_SETTINGS = 1;
    private TextView current_font_size;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leds_settings);
        TextView sd = (TextView) this.findViewById(R.id.scrolling_delay);
        sd.setText(Short.toString(Settings.getInstance().getScrolling_delay()));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Save");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(Constants.TAG, "LEDsSettingsActivity onOptionsItemSelected");
        switch (item.getItemId()) {
            case android.R.id.home:
                this.setResult(RESULT_OK);
                TextView sd = (TextView) this.findViewById(R.id.scrolling_delay);
                Settings.getInstance().setScrolling_delay(Short.parseShort(sd.getText().toString()));
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
