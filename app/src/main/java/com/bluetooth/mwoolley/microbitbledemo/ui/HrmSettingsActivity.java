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
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bluetooth.mwoolley.microbitbledemo.Constants;
import com.bluetooth.mwoolley.microbitbledemo.R;
import com.bluetooth.mwoolley.microbitbledemo.Settings;

public class HrmSettingsActivity extends AppCompatActivity {

    public static final int START_HRM_SETTINGS = 1;
    private EditText zone1;
    private EditText zone2;
    private EditText zone3;
    private EditText zone4;
    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hrm_settings);
        zone1 = (EditText) this.findViewById(R.id.zone1_upper_limit);
        zone1.setText(Integer.toString(Settings.getInstance().getHrm_zone_1_ceiling()));
        zone2 = (EditText) this.findViewById(R.id.zone2_upper_limit);
        zone2.setText(Integer.toString(Settings.getInstance().getHrm_zone_2_ceiling()));
        zone3 = (EditText) this.findViewById(R.id.zone3_upper_limit);
        zone3.setText(Integer.toString(Settings.getInstance().getHrm_zone_3_ceiling()));
        zone4 = (EditText) this.findViewById(R.id.zone4_upper_limit);
        zone4.setText(Integer.toString(Settings.getInstance().getHrm_zone_4_ceiling()));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Save");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(Constants.TAG, "HrmSettingsActivity onOptionsItemSelected");
        switch (item.getItemId()) {
            case android.R.id.home:
                // validate
                // The XML should ensure we only have decimal integers so we just need to check for reasonableness and relative values
                int z1 = Integer.parseInt(zone1.getText().toString());
                int z2 = Integer.parseInt(zone2.getText().toString());
                int z3 = Integer.parseInt(zone3.getText().toString());
                int z4 = Integer.parseInt(zone4.getText().toString());
                if (z1 >= z2) {
                    simpleToast("Zone 1 must be less than Zone 2",3000);
                    return false;
                }
                if (z2 >= z3) {
                    simpleToast("Zone 2 must be less than Zone 3",3000);
                    return false;
                }
                if (z3 >= z4) {
                    simpleToast("Zone 3 must be less than Zone 4",3000);
                    return false;
                }

                // All OK so save and return
                this.setResult(RESULT_OK);
                Settings.getInstance().setHrm_zone_1_ceiling(Integer.parseInt(zone1.getText().toString()));
                Settings.getInstance().setHrm_zone_2_ceiling(Integer.parseInt(zone2.getText().toString()));
                Settings.getInstance().setHrm_zone_3_ceiling(Integer.parseInt(zone3.getText().toString()));
                Settings.getInstance().setHrm_zone_4_ceiling(Integer.parseInt(zone4.getText().toString()));
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void simpleToast(String message, int duration) {
        toast = Toast.makeText(this, message, duration);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

}
