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

public class MagnetometerSettingsActivity extends AppCompatActivity {

    public static final int START_MAGNETOMETER_SETTINGS = 1;
    private TextView current_font_size;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_magnetometer_settings);
        RadioButton ap1 = (RadioButton) this.findViewById(R.id.radio_ap1);
        RadioButton ap2 = (RadioButton) this.findViewById(R.id.radio_ap2);
        RadioButton ap5 = (RadioButton) this.findViewById(R.id.radio_ap5);
        RadioButton ap10 = (RadioButton) this.findViewById(R.id.radio_ap10);
        RadioButton ap20 = (RadioButton) this.findViewById(R.id.radio_ap20);
        RadioButton ap80 = (RadioButton) this.findViewById(R.id.radio_ap80);
        RadioButton ap160 = (RadioButton) this.findViewById(R.id.radio_ap160);
        RadioButton ap640 = (RadioButton) this.findViewById(R.id.radio_ap640);
        switch (Settings.getInstance().getMagnetometer_period()) {
            case 1:
                ap1.setChecked(true);
                ap2.setChecked(false);
                ap5.setChecked(false);
                ap10.setChecked(false);
                ap20.setChecked(false);
                ap80.setChecked(false);
                ap160.setChecked(false);
                ap640.setChecked(false);
                break;
            case 2:
                ap1.setChecked(false);
                ap2.setChecked(true);
                ap5.setChecked(false);
                ap10.setChecked(false);
                ap20.setChecked(false);
                ap80.setChecked(false);
                ap160.setChecked(false);
                ap640.setChecked(false);
                break;
            case 5:
                ap1.setChecked(false);
                ap2.setChecked(false);
                ap5.setChecked(true);
                ap10.setChecked(false);
                ap20.setChecked(false);
                ap80.setChecked(false);
                ap160.setChecked(false);
                ap640.setChecked(false);
                break;
            case 10:
                ap1.setChecked(false);
                ap2.setChecked(false);
                ap5.setChecked(false);
                ap10.setChecked(true);
                ap20.setChecked(false);
                ap80.setChecked(false);
                ap160.setChecked(false);
                ap640.setChecked(false);
                break;
            case 20:
                ap1.setChecked(false);
                ap2.setChecked(false);
                ap5.setChecked(false);
                ap10.setChecked(false);
                ap20.setChecked(true);
                ap80.setChecked(false);
                ap160.setChecked(false);
                ap640.setChecked(false);
                break;
            case 80:
                ap1.setChecked(false);
                ap2.setChecked(false);
                ap5.setChecked(false);
                ap10.setChecked(false);
                ap20.setChecked(false);
                ap80.setChecked(true);
                ap160.setChecked(false);
                ap640.setChecked(false);
                break;
            case 160:
                ap1.setChecked(false);
                ap2.setChecked(false);
                ap5.setChecked(false);
                ap10.setChecked(false);
                ap20.setChecked(false);
                ap80.setChecked(false);
                ap160.setChecked(true);
                ap640.setChecked(false);
                break;
            case 640:
                ap1.setChecked(false);
                ap2.setChecked(false);
                ap5.setChecked(false);
                ap10.setChecked(false);
                ap20.setChecked(false);
                ap80.setChecked(false);
                ap160.setChecked(false);
                ap640.setChecked(true);
                break;
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Save");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(Constants.TAG, "MagnetometerSettingsActivity onOptionsItemSelected");
        switch (item.getItemId()) {
            case android.R.id.home:
                this.setResult(RESULT_OK);
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();
        Settings settings = Settings.getInstance();
        short ap = Short.parseShort(((RadioButton) view).getText().toString());
        settings.setMagnetometer_period(ap);
    }
}
