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
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.bluetooth.mwoolley.microbitbledemo.Constants;
import com.bluetooth.mwoolley.microbitbledemo.R;
import com.bluetooth.mwoolley.microbitbledemo.Settings;

public class GamepadControllerSettingsActivity extends AppCompatActivity {

    EditText et_event_code;
    EditText et_gamepad_1_up_on;
    EditText et_gamepad_1_up_off;
    EditText et_gamepad_1_down_on;
    EditText et_gamepad_1_down_off;
    EditText et_gamepad_1_left_on;
    EditText et_gamepad_1_left_off;
    EditText et_gamepad_1_right_on;
    EditText et_gamepad_1_right_off;
    EditText et_gamepad_2_up_on;
    EditText et_gamepad_2_up_off;
    EditText et_gamepad_2_down_on;
    EditText et_gamepad_2_down_off;
    EditText et_gamepad_2_left_on;
    EditText et_gamepad_2_left_off;
    EditText et_gamepad_2_right_on;
    EditText et_gamepad_2_right_off;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gamepad_settings);
        et_event_code = (EditText) this.findViewById(R.id.gamepad_event_code);
        et_gamepad_1_up_on = (EditText) this.findViewById(R.id.gamepad_1_up_on);
        et_gamepad_1_up_off = (EditText) this.findViewById(R.id.gamepad_1_up_off);
        et_gamepad_1_down_on = (EditText) this.findViewById(R.id.gamepad_1_down_on);
        et_gamepad_1_down_off = (EditText) this.findViewById(R.id.gamepad_1_down_off);
        et_gamepad_1_left_on = (EditText) this.findViewById(R.id.gamepad_1_left_on);
        et_gamepad_1_left_off = (EditText) this.findViewById(R.id.gamepad_1_left_off);
        et_gamepad_1_right_on = (EditText) this.findViewById(R.id.gamepad_1_right_on);
        et_gamepad_1_right_off = (EditText) this.findViewById(R.id.gamepad_1_right_off);
        et_gamepad_2_up_on = (EditText) this.findViewById(R.id.gamepad_2_up_on);
        et_gamepad_2_up_off = (EditText) this.findViewById(R.id.gamepad_2_up_off);
        et_gamepad_2_down_on = (EditText) this.findViewById(R.id.gamepad_2_down_on);
        et_gamepad_2_down_off = (EditText) this.findViewById(R.id.gamepad_2_down_off);
        et_gamepad_2_left_on = (EditText) this.findViewById(R.id.gamepad_2_left_on);
        et_gamepad_2_left_off = (EditText) this.findViewById(R.id.gamepad_2_left_off);
        et_gamepad_2_right_on = (EditText) this.findViewById(R.id.gamepad_2_right_on);
        et_gamepad_2_right_off = (EditText) this.findViewById(R.id.gamepad_2_right_off);

        Settings settings = Settings.getInstance();
        et_event_code.setText(Short.toString(settings.getMes_dpad_controller()));
        et_gamepad_1_up_on.setText(Short.toString(settings.getMes_dpad_1_button_up_on()));
        et_gamepad_1_up_off.setText(Short.toString(settings.getMes_dpad_1_button_up_off()));
        et_gamepad_1_down_on.setText(Short.toString(settings.getMes_dpad_1_button_down_on()));
        et_gamepad_1_down_off.setText(Short.toString(settings.getMes_dpad_1_button_down_off()));
        et_gamepad_1_left_on.setText(Short.toString(settings.getMes_dpad_1_button_left_on()));
        et_gamepad_1_left_off.setText(Short.toString(settings.getMes_dpad_1_button_left_off()));
        et_gamepad_1_right_on.setText(Short.toString(settings.getMes_dpad_1_button_right_on()));
        et_gamepad_1_right_off.setText(Short.toString(settings.getMes_dpad_1_button_right_off()));
        et_gamepad_2_up_on.setText(Short.toString(settings.getMes_dpad_2_button_up_on()));
        et_gamepad_2_up_off.setText(Short.toString(settings.getMes_dpad_2_button_up_off()));
        et_gamepad_2_down_on.setText(Short.toString(settings.getMes_dpad_2_button_down_on()));
        et_gamepad_2_down_off.setText(Short.toString(settings.getMes_dpad_2_button_down_off()));
        et_gamepad_2_left_on.setText(Short.toString(settings.getMes_dpad_2_button_left_on()));
        et_gamepad_2_left_off.setText(Short.toString(settings.getMes_dpad_2_button_left_off()));
        et_gamepad_2_right_on.setText(Short.toString(settings.getMes_dpad_2_button_right_on()));
        et_gamepad_2_right_off.setText(Short.toString(settings.getMes_dpad_2_button_right_off()));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Save");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(Constants.TAG, "GamepadControllerSettingsActivity onOptionsItemSelected");
        switch (item.getItemId()) {
            case android.R.id.home:
                Settings settings = Settings.getInstance();
                settings.setMes_dpad_controller(Short.parseShort(et_event_code.getText().toString()));
                settings.setMes_dpad_1_button_up_on(Short.parseShort(et_gamepad_1_up_on.getText().toString()));
                settings.setMes_dpad_1_button_up_off(Short.parseShort(et_gamepad_1_up_off.getText().toString()));
                settings.setMes_dpad_1_button_down_on(Short.parseShort(et_gamepad_1_down_on.getText().toString()));
                settings.setMes_dpad_1_button_down_off(Short.parseShort(et_gamepad_1_down_off.getText().toString()));
                settings.setMes_dpad_1_button_left_on(Short.parseShort(et_gamepad_1_left_on.getText().toString()));
                settings.setMes_dpad_1_button_left_off(Short.parseShort(et_gamepad_1_left_off.getText().toString()));
                settings.setMes_dpad_1_button_right_on(Short.parseShort(et_gamepad_1_right_on.getText().toString()));
                settings.setMes_dpad_1_button_right_off(Short.parseShort(et_gamepad_1_right_off.getText().toString()));
                settings.setMes_dpad_2_button_up_on(Short.parseShort(et_gamepad_2_up_on.getText().toString()));
                settings.setMes_dpad_2_button_up_off(Short.parseShort(et_gamepad_2_up_off.getText().toString()));
                settings.setMes_dpad_2_button_down_on(Short.parseShort(et_gamepad_2_down_on.getText().toString()));
                settings.setMes_dpad_2_button_down_off(Short.parseShort(et_gamepad_2_down_off.getText().toString()));
                settings.setMes_dpad_2_button_left_on(Short.parseShort(et_gamepad_2_left_on.getText().toString()));
                settings.setMes_dpad_2_button_left_off(Short.parseShort(et_gamepad_2_left_off.getText().toString()));
                settings.setMes_dpad_2_button_right_on(Short.parseShort(et_gamepad_2_right_on.getText().toString()));
                settings.setMes_dpad_2_button_right_off(Short.parseShort(et_gamepad_2_right_off.getText().toString()));
                settings.save(this);
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
