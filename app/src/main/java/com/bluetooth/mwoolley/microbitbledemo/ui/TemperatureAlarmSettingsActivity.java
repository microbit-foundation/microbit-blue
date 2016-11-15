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
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bluetooth.mwoolley.microbitbledemo.Constants;
import com.bluetooth.mwoolley.microbitbledemo.R;
import com.bluetooth.mwoolley.microbitbledemo.Settings;

public class TemperatureAlarmSettingsActivity extends AppCompatActivity {

    public static final int START_TEMPERATURE_ALARM_SETTINGS = 1;
    private TextView upper;
    private TextView lower;
    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature_alarm_settings);
        lower = (TextView) this.findViewById(R.id.lower_temperature_limit);
        upper = (TextView) this.findViewById(R.id.upper_temperature_limit);
        lower.setText(Short.toString(Settings.getInstance().getLower_temperature_limit()));
        upper.setText(Short.toString(Settings.getInstance().getUpper_temperature_limit()));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Save");
    }

    private void simpleToast(String message, int duration) {
        toast = Toast.makeText(this, message, duration);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(Constants.TAG, "TemperatureAlarmSettingsActivity onOptionsItemSelected");
        short lower_value = Short.parseShort(lower.getText().toString());
        short upper_value = Short.parseShort(upper.getText().toString());
        if (lower_value < -128 || lower_value > 127 || upper_value < -128 || upper_value > 127) {
            simpleToast("Temperature values must be in the range -128 to +127",3000);
            return false;
        }
        if (lower_value > upper_value) {
            simpleToast("Lower limit must be less than or equal to upper limit",3000);
            return false;
        }
        Settings.getInstance().setLower_temperature_limit((byte) lower_value);
        Settings.getInstance().setUpper_temperature_limit((byte) upper_value);
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
