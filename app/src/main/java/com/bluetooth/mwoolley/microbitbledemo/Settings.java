package com.bluetooth.mwoolley.microbitbledemo;
/*
 * Author: Martin Woolley
 * Twitter: @bluetooth_mdw
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
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.SeekBar;

public class Settings {

    private static Settings instance;
    private short accelerometer_period=20;
    private short magnetometer_period=20;
    private short scrolling_delay=500;
    private boolean filter_unpaired_devices=true;
    private byte lower_temperature_limit=0;
    private byte upper_temperature_limit=30;

    private short mes_dpad_controller = 1104;
    private short mes_dpad_1_button_up_on = 1;
    private short mes_dpad_1_button_up_off = 2;
    private short mes_dpad_1_button_down_on = 3;
    private short mes_dpad_1_button_down_off = 4;
    private short mes_dpad_1_button_left_on = 5;
    private short mes_dpad_1_button_left_off = 6;
    private short mes_dpad_1_button_right_on = 7;
    private short mes_dpad_1_button_right_off = 8;

    private short mes_dpad_2_button_up_on = 9;
    private short mes_dpad_2_button_up_off = 10;
    private short mes_dpad_2_button_down_on = 11;
    private short mes_dpad_2_button_down_off = 12;
    private short mes_dpad_2_button_left_on = 13;
    private short mes_dpad_2_button_left_off = 14;
    private short mes_dpad_2_button_right_on = 15;
    private short mes_dpad_2_button_right_off = 16;

    private static final String SETTINGS_FILE = "com.bluetooth.mwoolley.microbitbledemo.settings_file";
    private static final String ACCELEROMETER_PERIOD = "accelerometer_period";
    private static final String MAGNETOMETER_PERIOD = "magnetometer_period";
    private static final String SCROLLING_DELAY = "scrolling_delay";
    private static final String FILTER_UNPAIRED_DEVICES = "filter_unpaired_devices";
    private static final String LOWER_TEMPERATURE_LIMIT = "lower_temperature_limit";
    private static final String UPPER_TEMPERATURE_LIMIT = "upper_temperature_limit";
    private static final String MES_DPAD_CONTROLLER = "mes_dpad_controller";
    private static final String MES_DPAD_1_BUTTON_UP_ON = "mes_dpad_1_button_up_on";
    private static final String MES_DPAD_1_BUTTON_UP_OFF = "mes_dpad_1_button_up_off";
    private static final String MES_DPAD_1_BUTTON_DOWN_ON = "mes_dpad_1_button_down_on";
    private static final String MES_DPAD_1_BUTTON_DOWN_OFF = "mes_dpad_1_button_down_off";
    private static final String MES_DPAD_1_BUTTON_LEFT_ON = "mes_dpad_1_button_left_on";
    private static final String MES_DPAD_1_BUTTON_LEFT_OFF = "mes_dpad_1_button_left_off";
    private static final String MES_DPAD_1_BUTTON_RIGHT_ON = "mes_dpad_1_button_right_on";
    private static final String MES_DPAD_1_BUTTON_RIGHT_OFF = "mes_dpad_1_button_right_off";
    private static final String MES_DPAD_2_BUTTON_UP_ON = "mes_dpad_2_button_up_on";
    private static final String MES_DPAD_2_BUTTON_UP_OFF = "mes_dpad_2_button_up_off";
    private static final String MES_DPAD_2_BUTTON_DOWN_ON = "mes_dpad_2_button_down_on";
    private static final String MES_DPAD_2_BUTTON_DOWN_OFF = "mes_dpad_2_button_down_off";
    private static final String MES_DPAD_2_BUTTON_LEFT_ON = "mes_dpad_2_button_left_on";
    private static final String MES_DPAD_2_BUTTON_LEFT_OFF = "mes_dpad_2_button_left_off";
    private static final String MES_DPAD_2_BUTTON_RIGHT_ON = "mes_dpad_2_button_right_on";
    private static final String MES_DPAD_2_BUTTON_RIGHT_OFF = "mes_dpad_2_button_right_off";
    private static final String HRM_ZONE_1 = "hrm_zone_1";
    private static final String HRM_ZONE_2 = "hrm_zone_2";
    private static final String HRM_ZONE_3 = "hrm_zone_3";
    private static final String HRM_ZONE_4 = "hrm_zone_4";

    private int hrm_zone_1_ceiling = 90;
    private int hrm_zone_2_ceiling = 110;
    private int hrm_zone_3_ceiling = 130;
    private int hrm_zone_4_ceiling = 160;

    private Settings() {
    }

    public static synchronized Settings getInstance() {
        if (instance == null) {
            instance = new Settings();
        }
        return instance;
    }

    public void save(Context context) {
        Log.d(Constants.TAG,"Saving preferences");
        SharedPreferences sharedPref = context.getSharedPreferences(SETTINGS_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(ACCELEROMETER_PERIOD, accelerometer_period);
        editor.putInt(MAGNETOMETER_PERIOD, magnetometer_period);
        editor.putInt(SCROLLING_DELAY, scrolling_delay);
        editor.putBoolean(FILTER_UNPAIRED_DEVICES, filter_unpaired_devices);
        editor.putInt(LOWER_TEMPERATURE_LIMIT, lower_temperature_limit);
        editor.putInt(UPPER_TEMPERATURE_LIMIT, upper_temperature_limit);
        editor.putInt(MES_DPAD_CONTROLLER,mes_dpad_controller);
        editor.putInt(MES_DPAD_1_BUTTON_UP_ON,mes_dpad_1_button_up_on);
        editor.putInt(MES_DPAD_1_BUTTON_UP_OFF,mes_dpad_1_button_up_off);
        editor.putInt(MES_DPAD_1_BUTTON_DOWN_ON,mes_dpad_1_button_down_on);
        editor.putInt(MES_DPAD_1_BUTTON_DOWN_OFF,mes_dpad_1_button_down_off);
        editor.putInt(MES_DPAD_1_BUTTON_LEFT_ON, mes_dpad_1_button_left_on);
        editor.putInt(MES_DPAD_1_BUTTON_LEFT_OFF,mes_dpad_1_button_left_off);
        editor.putInt(MES_DPAD_1_BUTTON_RIGHT_ON,mes_dpad_1_button_right_on);
        editor.putInt(MES_DPAD_1_BUTTON_RIGHT_OFF,mes_dpad_1_button_right_off);
        editor.putInt(MES_DPAD_2_BUTTON_UP_ON,mes_dpad_2_button_up_on);
        editor.putInt(MES_DPAD_2_BUTTON_UP_OFF,mes_dpad_2_button_up_off);
        editor.putInt(MES_DPAD_2_BUTTON_DOWN_ON,mes_dpad_2_button_down_on);
        editor.putInt(MES_DPAD_2_BUTTON_DOWN_OFF,mes_dpad_2_button_down_off);
        editor.putInt(MES_DPAD_2_BUTTON_LEFT_ON,mes_dpad_2_button_left_on);
        editor.putInt(MES_DPAD_2_BUTTON_LEFT_OFF,mes_dpad_2_button_left_off);
        editor.putInt(MES_DPAD_2_BUTTON_RIGHT_ON,mes_dpad_2_button_right_on);
        editor.putInt(MES_DPAD_2_BUTTON_RIGHT_OFF,mes_dpad_2_button_right_off);
        editor.putInt(HRM_ZONE_1,hrm_zone_1_ceiling);
        editor.putInt(HRM_ZONE_2,hrm_zone_2_ceiling);
        editor.putInt(HRM_ZONE_3,hrm_zone_3_ceiling);
        editor.putInt(HRM_ZONE_4,hrm_zone_4_ceiling);
        editor.commit();
    }


    public void restore(Context context) {
        Log.d(Constants.TAG,"Restoring preferences");
        SharedPreferences sharedPref = context.getSharedPreferences(SETTINGS_FILE, Context.MODE_PRIVATE);
        accelerometer_period = (short) sharedPref.getInt(ACCELEROMETER_PERIOD,20);
        magnetometer_period = (short) sharedPref.getInt(MAGNETOMETER_PERIOD,20);
        scrolling_delay = (short)  sharedPref.getInt(SCROLLING_DELAY,500);
        filter_unpaired_devices = sharedPref.getBoolean(FILTER_UNPAIRED_DEVICES,true);
        lower_temperature_limit = (byte)(sharedPref.getInt(LOWER_TEMPERATURE_LIMIT,0) & 0xff);
        upper_temperature_limit = (byte)(sharedPref.getInt(UPPER_TEMPERATURE_LIMIT,30) & 0xff);
        mes_dpad_controller = (short)  sharedPref.getInt(MES_DPAD_CONTROLLER,1104);
        mes_dpad_1_button_up_on = (short) sharedPref.getInt(MES_DPAD_1_BUTTON_UP_ON,1);
        mes_dpad_1_button_up_off = (short) sharedPref.getInt(MES_DPAD_1_BUTTON_UP_OFF,2);
        mes_dpad_1_button_down_on = (short) sharedPref.getInt(MES_DPAD_1_BUTTON_DOWN_ON,3);
        mes_dpad_1_button_down_off = (short) sharedPref.getInt(MES_DPAD_1_BUTTON_DOWN_OFF,4);
        mes_dpad_1_button_left_on = (short) sharedPref.getInt(MES_DPAD_1_BUTTON_LEFT_ON,5);
        mes_dpad_1_button_left_off = (short) sharedPref.getInt(MES_DPAD_1_BUTTON_LEFT_OFF,6);
        mes_dpad_1_button_right_on = (short) sharedPref.getInt(MES_DPAD_1_BUTTON_RIGHT_ON,7);
        mes_dpad_1_button_right_off = (short) sharedPref.getInt(MES_DPAD_1_BUTTON_RIGHT_OFF,8);
        mes_dpad_2_button_up_on = (short) sharedPref.getInt(MES_DPAD_2_BUTTON_UP_ON,9);
        mes_dpad_2_button_up_off = (short) sharedPref.getInt(MES_DPAD_2_BUTTON_UP_OFF,10);
        mes_dpad_2_button_down_on = (short) sharedPref.getInt(MES_DPAD_2_BUTTON_DOWN_ON,11);
        mes_dpad_2_button_down_off = (short) sharedPref.getInt(MES_DPAD_2_BUTTON_DOWN_OFF,12);
        mes_dpad_2_button_left_on = (short) sharedPref.getInt(MES_DPAD_2_BUTTON_LEFT_ON,13);
        mes_dpad_2_button_left_off = (short) sharedPref.getInt(MES_DPAD_2_BUTTON_LEFT_OFF,14);
        mes_dpad_2_button_right_on = (short) sharedPref.getInt(MES_DPAD_2_BUTTON_RIGHT_ON,15);
        mes_dpad_2_button_right_off = (short) sharedPref.getInt(MES_DPAD_2_BUTTON_RIGHT_OFF,16);
        hrm_zone_1_ceiling = sharedPref.getInt(HRM_ZONE_1,90);
        hrm_zone_2_ceiling = sharedPref.getInt(HRM_ZONE_2,110);
        hrm_zone_3_ceiling = sharedPref.getInt(HRM_ZONE_3,130);
        hrm_zone_4_ceiling = sharedPref.getInt(HRM_ZONE_4,160);
    }

    public short getAccelerometer_period() {
        return accelerometer_period;
    }

    public void setAccelerometer_period(short accelerometer_period) {
        this.accelerometer_period = accelerometer_period;
    }

    public short getMagnetometer_period() {
        return magnetometer_period;
    }

    public void setMagnetometer_period(short magnetometer_period) {
        this.magnetometer_period = magnetometer_period;
    }

    public short getScrolling_delay() {
        return scrolling_delay;
    }

    public void setScrolling_delay(short scrolling_delay) {
        this.scrolling_delay = scrolling_delay;
    }

    public boolean isFilter_unpaired_devices() {
        return filter_unpaired_devices;
    }

    public void setFilter_unpaired_devices(boolean filter_unpaired_devices) {
        this.filter_unpaired_devices = filter_unpaired_devices;
    }

    public byte getLower_temperature_limit() {
        return lower_temperature_limit;
    }

    public void setLower_temperature_limit(byte lower_temperature_limit) {
        this.lower_temperature_limit = lower_temperature_limit;
    }

    public byte getUpper_temperature_limit() {
        return upper_temperature_limit;
    }

    public void setUpper_temperature_limit(byte upper_temperature_limit) {
        this.upper_temperature_limit = upper_temperature_limit;
    }

    public short getMes_dpad_controller() {
        return mes_dpad_controller;
    }

    public void setMes_dpad_controller(short mes_dpad_controller) {
        this.mes_dpad_controller = mes_dpad_controller;
    }

    public short getMes_dpad_1_button_up_on() {
        return mes_dpad_1_button_up_on;
    }

    public void setMes_dpad_1_button_up_on(short mes_dpad_1_button_up_on) {
        this.mes_dpad_1_button_up_on = mes_dpad_1_button_up_on;
    }

    public short getMes_dpad_1_button_up_off() {
        return mes_dpad_1_button_up_off;
    }

    public void setMes_dpad_1_button_up_off(short mes_dpad_1_button_up_off) {
        this.mes_dpad_1_button_up_off = mes_dpad_1_button_up_off;
    }

    public short getMes_dpad_1_button_down_on() {
        return mes_dpad_1_button_down_on;
    }

    public void setMes_dpad_1_button_down_on(short mes_dpad_1_button_down_on) {
        this.mes_dpad_1_button_down_on = mes_dpad_1_button_down_on;
    }

    public short getMes_dpad_1_button_down_off() {
        return mes_dpad_1_button_down_off;
    }

    public void setMes_dpad_1_button_down_off(short mes_dpad_1_button_down_off) {
        this.mes_dpad_1_button_down_off = mes_dpad_1_button_down_off;
    }

    public short getMes_dpad_1_button_left_on() {
        return mes_dpad_1_button_left_on;
    }

    public void setMes_dpad_1_button_left_on(short mes_dpad_1_button_left_on) {
        this.mes_dpad_1_button_left_on = mes_dpad_1_button_left_on;
    }

    public short getMes_dpad_1_button_left_off() {
        return mes_dpad_1_button_left_off;
    }

    public void setMes_dpad_1_button_left_off(short mes_dpad_1_button_left_off) {
        this.mes_dpad_1_button_left_off = mes_dpad_1_button_left_off;
    }

    public short getMes_dpad_1_button_right_on() {
        return mes_dpad_1_button_right_on;
    }

    public void setMes_dpad_1_button_right_on(short mes_dpad_1_button_right_on) {
        this.mes_dpad_1_button_right_on = mes_dpad_1_button_right_on;
    }

    public short getMes_dpad_1_button_right_off() {
        return mes_dpad_1_button_right_off;
    }

    public void setMes_dpad_1_button_right_off(short mes_dpad_1_button_right_off) {
        this.mes_dpad_1_button_right_off = mes_dpad_1_button_right_off;
    }

    public short getMes_dpad_2_button_up_on() {
        return mes_dpad_2_button_up_on;
    }

    public void setMes_dpad_2_button_up_on(short mes_dpad_2_button_up_on) {
        this.mes_dpad_2_button_up_on = mes_dpad_2_button_up_on;
    }

    public short getMes_dpad_2_button_up_off() {
        return mes_dpad_2_button_up_off;
    }

    public void setMes_dpad_2_button_up_off(short mes_dpad_2_button_up_off) {
        this.mes_dpad_2_button_up_off = mes_dpad_2_button_up_off;
    }

    public short getMes_dpad_2_button_down_on() {
        return mes_dpad_2_button_down_on;
    }

    public void setMes_dpad_2_button_down_on(short mes_dpad_2_button_down_on) {
        this.mes_dpad_2_button_down_on = mes_dpad_2_button_down_on;
    }

    public short getMes_dpad_2_button_down_off() {
        return mes_dpad_2_button_down_off;
    }

    public void setMes_dpad_2_button_down_off(short mes_dpad_2_button_down_off) {
        this.mes_dpad_2_button_down_off = mes_dpad_2_button_down_off;
    }

    public short getMes_dpad_2_button_left_on() {
        return mes_dpad_2_button_left_on;
    }

    public void setMes_dpad_2_button_left_on(short mes_dpad_2_button_left_on) {
        this.mes_dpad_2_button_left_on = mes_dpad_2_button_left_on;
    }

    public short getMes_dpad_2_button_left_off() {
        return mes_dpad_2_button_left_off;
    }

    public void setMes_dpad_2_button_left_off(short mes_dpad_2_button_left_off) {
        this.mes_dpad_2_button_left_off = mes_dpad_2_button_left_off;
    }

    public short getMes_dpad_2_button_right_on() {
        return mes_dpad_2_button_right_on;
    }

    public void setMes_dpad_2_button_right_on(short mes_dpad_2_button_right_on) {
        this.mes_dpad_2_button_right_on = mes_dpad_2_button_right_on;
    }

    public short getMes_dpad_2_button_right_off() {
        return mes_dpad_2_button_right_off;
    }

    public void setMes_dpad_2_button_right_off(short mes_dpad_2_button_right_off) {
        this.mes_dpad_2_button_right_off = mes_dpad_2_button_right_off;
    }

    public int getHrm_zone_1_ceiling() {
        return hrm_zone_1_ceiling;
    }

    public void setHrm_zone_1_ceiling(int hrm_zone_1_ceiling) {
        this.hrm_zone_1_ceiling = hrm_zone_1_ceiling;
    }

    public int getHrm_zone_2_ceiling() {
        return hrm_zone_2_ceiling;
    }

    public void setHrm_zone_2_ceiling(int hrm_zone_2_ceiling) {
        this.hrm_zone_2_ceiling = hrm_zone_2_ceiling;
    }

    public int getHrm_zone_3_ceiling() {
        return hrm_zone_3_ceiling;
    }

    public void setHrm_zone_3_ceiling(int hrm_zone_3_ceiling) {
        this.hrm_zone_3_ceiling = hrm_zone_3_ceiling;
    }

    public int getHrm_zone_4_ceiling() {
        return hrm_zone_4_ceiling;
    }

    public void setHrm_zone_4_ceiling(int hrm_zone_4_ceiling) {
        this.hrm_zone_4_ceiling = hrm_zone_4_ceiling;
    }
}

