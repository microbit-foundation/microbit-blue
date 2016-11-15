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
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;

import com.bluetooth.mwoolley.microbitbledemo.Constants;
import com.bluetooth.mwoolley.microbitbledemo.R;
import com.bluetooth.mwoolley.microbitbledemo.Settings;

public class MainSettingsActivity extends AppCompatActivity {

    public static final int START_MAIN_SETTINGS = 1;
    private CheckBox cb_fud;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_settings);
        cb_fud = (CheckBox) this.findViewById(R.id.cb_scan_filter_unpaired);
        cb_fud.setChecked(Settings.getInstance().isFilter_unpaired_devices());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Save");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(Constants.TAG, "MainSettingsActivity onOptionsItemSelected");
        switch (item.getItemId()) {
            case android.R.id.home:
                this.setResult(RESULT_OK);
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onFilterChange(View view) {
        Log.d(Constants.TAG,"onFilterChange: "+cb_fud.isChecked());
        boolean checked = cb_fud.isChecked();
        Settings settings = Settings.getInstance();
        settings.setFilter_unpaired_devices(checked);
    }

}
