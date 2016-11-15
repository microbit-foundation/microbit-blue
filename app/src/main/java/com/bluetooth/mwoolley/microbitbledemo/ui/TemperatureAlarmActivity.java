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
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bluetooth.mwoolley.microbitbledemo.bluetooth.BleAdapterService;
import com.bluetooth.mwoolley.microbitbledemo.bluetooth.ConnectionStatusListener;
import com.bluetooth.mwoolley.microbitbledemo.Constants;
import com.bluetooth.mwoolley.microbitbledemo.MicroBit;
import com.bluetooth.mwoolley.microbitbledemo.R;
import com.bluetooth.mwoolley.microbitbledemo.Settings;
import com.bluetooth.mwoolley.microbitbledemo.Utility;

/**
 * Microbit Events
 *
 * Measure Temperature in micro:bit - send event when it exceeds or falls below a hard coded threshold
 *
 * Requires a custom microbit application with the BLE profile in the build
 */

public class TemperatureAlarmActivity extends AppCompatActivity implements ConnectionStatusListener {

    private BleAdapterService bluetooth_le_adapter;

    private boolean exiting=false;
    private boolean notifications_on =false;
    private ImageView alarm_image;
    private TextView alarm_message;

    private byte upper_limit;
    private byte lower_limit;

    // micro:bit event codes:
    // 9000 = 0x2823 (LE) = temperature alarm. Value=0 means OK, 1 means cold, 2 means hot

    // client event codes:
    // 9001=0x2923 (LE) = set lower limit, value is the limit value in celsius
    // 9002=0x2A23 (LE) = set upper limit, value is the limit value in celsius

    private byte [] event_set_lower = { 0x29, 0x23, 0x00, 0x00}; // event 9001
    private byte [] event_set_upper = { 0x2A, 0x23, 0x00, 0x00}; // event 9002

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.d(Constants.TAG,"onServiceConnected");
            notifications_on=false;
            bluetooth_le_adapter = ((BleAdapterService.LocalBinder) service).getService();
            bluetooth_le_adapter.setActivityHandler(mMessageHandler);

            setLowerLimit();
            setUpperLimit();

            if (bluetooth_le_adapter.setNotificationsState(Utility.normaliseUUID(BleAdapterService.EVENTSERVICE_SERVICE_UUID), Utility.normaliseUUID(BleAdapterService.MICROBITEVENT_CHARACTERISTIC_UUID), true)) {
                showMsg(Utility.htmlColorGreen("micro:bit event notifications ON"));
            } else {
                showMsg(Utility.htmlColorRed("Failed to set micro:bit event notifications ON"));
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bluetooth_le_adapter = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_temperature_alarm);
        alarm_image = (ImageView) TemperatureAlarmActivity.this.findViewById(R.id.temperature_status);
        alarm_message = (TextView) TemperatureAlarmActivity.this.findViewById(R.id.temperature_status_label);

        // read intent data
        final Intent intent = getIntent();
        MicroBit.getInstance().setConnection_status_listener(this);

        // connect to the Bluetooth smart service
        Intent gattServiceIntent = new Intent(this, BleAdapterService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (MicroBit.getInstance().isMicrobit_connected() && notifications_on) {
            bluetooth_le_adapter.setNotificationsState(Utility.normaliseUUID(BleAdapterService.EVENTSERVICE_SERVICE_UUID), Utility.normaliseUUID(BleAdapterService.MICROBITEVENT_CHARACTERISTIC_UUID), false);
        }
        try {
            // may already have unbound. No API to check state so....
            unbindService(mServiceConnection);
        } catch (Exception e) {
        }
    }

    public void onBackPressed() {
        Log.d(Constants.TAG, "onBackPressed connected="+MicroBit.getInstance().isMicrobit_connected()+" notifications_on="+notifications_on+" exiting="+exiting);
        if (MicroBit.getInstance().isMicrobit_connected() && notifications_on) {
            bluetooth_le_adapter.setNotificationsState(Utility.normaliseUUID(BleAdapterService.EVENTSERVICE_SERVICE_UUID), Utility.normaliseUUID(BleAdapterService.MICROBITEVENT_CHARACTERISTIC_UUID), false);
        }
        exiting=true;
        if (!MicroBit.getInstance().isMicrobit_connected()) {
            try {
                // may already have unbound. No API to check state so....
                unbindService(mServiceConnection);
            } catch (Exception e) {
            }
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_temperature_alarm, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.menu_temperature_alarm_settings) {
            lower_limit = Settings.getInstance().getLower_temperature_limit();
            upper_limit = Settings.getInstance().getUpper_temperature_limit();
            Intent intent = new Intent(TemperatureAlarmActivity.this, TemperatureAlarmSettingsActivity.class);
            startActivityForResult(intent, TemperatureAlarmSettingsActivity.START_TEMPERATURE_ALARM_SETTINGS);
            return true;
        }
        if (id == R.id.menu_temperature_alarm_help) {
            Intent intent = new Intent(TemperatureAlarmActivity.this, HelpActivity.class);
            intent.putExtra(Constants.URI, Constants.TEMPERATURE_ALARM_HELP);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(Constants.TAG, "onActivityResult");
        if (requestCode == TemperatureAlarmSettingsActivity.START_TEMPERATURE_ALARM_SETTINGS) {
            if (resultCode == RESULT_OK) {
                Log.d(Constants.TAG, "onActivityResult RESULT_OK");
                if (Settings.getInstance().getLower_temperature_limit() != lower_limit) {
                    Log.d(Constants.TAG, "Lower limit has changed to " + Settings.getInstance().getLower_temperature_limit());
                    setLowerLimit();
                }
                if (Settings.getInstance().getUpper_temperature_limit() != upper_limit) {
                    Log.d(Constants.TAG, "Upper limit has changed to " + Settings.getInstance().getUpper_temperature_limit());
                    setUpperLimit();
                }
            } else {
                Log.d(Constants.TAG, "onActivityResult NOT RESULT_OK");
            }
        }
    }

    private void setUpperLimit() {
        event_set_upper[2] = Settings.getInstance().getUpper_temperature_limit();
        Log.d(Constants.TAG, Utility.byteArrayAsHexString(event_set_upper));
        bluetooth_le_adapter.writeCharacteristic(Utility.normaliseUUID(BleAdapterService.EVENTSERVICE_SERVICE_UUID), Utility.normaliseUUID(BleAdapterService.CLIENTEVENT_CHARACTERISTIC_UUID), event_set_upper);
    }

    private void setLowerLimit() {
        event_set_lower[2] = Settings.getInstance().getLower_temperature_limit();
        Log.d(Constants.TAG, Utility.byteArrayAsHexString(event_set_lower));
        bluetooth_le_adapter.writeCharacteristic(Utility.normaliseUUID(BleAdapterService.EVENTSERVICE_SERVICE_UUID), Utility.normaliseUUID(BleAdapterService.CLIENTEVENT_CHARACTERISTIC_UUID), event_set_lower);
    }

    private Handler mMessageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            Bundle bundle;
            String service_uuid = "";
            String characteristic_uuid = "";
            String descriptor_uuid = "";
            byte[] b = null;
            TextView value_text = null;

            switch (msg.what) {
                case BleAdapterService.GATT_CHARACTERISTIC_WRITTEN:
                    Log.d(Constants.TAG, "Handler received characteristic written result");
                    bundle = msg.getData();
                    service_uuid = bundle.getString(BleAdapterService.PARCEL_SERVICE_UUID);
                    characteristic_uuid = bundle.getString(BleAdapterService.PARCEL_CHARACTERISTIC_UUID);
                    Log.d(Constants.TAG, "characteristic " + characteristic_uuid + " of service " + service_uuid + " written OK");
                    showMsg(Utility.htmlColorGreen("Subscribed to micro:bit event"));
                    break;
                case BleAdapterService.GATT_DESCRIPTOR_WRITTEN:
                    Log.d(Constants.TAG, "Handler received descriptor written result");
                    bundle = msg.getData();
                    service_uuid = bundle.getString(BleAdapterService.PARCEL_SERVICE_UUID);
                    characteristic_uuid = bundle.getString(BleAdapterService.PARCEL_CHARACTERISTIC_UUID);
                    descriptor_uuid = bundle.getString(BleAdapterService.PARCEL_DESCRIPTOR_UUID);
                    Log.d(Constants.TAG, "descriptor " + descriptor_uuid + " of characteristic " + characteristic_uuid + " of service " + service_uuid + " written OK");
                    if (!exiting) {
                        showMsg(Utility.htmlColorGreen("Temperature Alarm notifications ON"));
                        notifications_on=true;
                        bluetooth_le_adapter.writeCharacteristic(Utility.normaliseUUID(BleAdapterService.EVENTSERVICE_SERVICE_UUID), Utility.normaliseUUID(BleAdapterService.CLIENTREQUIREMENTS_CHARACTERISTIC_UUID), Utility.leBytesFromTwoShorts(Constants.MICROBIT_EVENT_TYPE_TEMPERATURE_ALARM,Constants.MICROBIT_EVENT_VALUE_ANY));
                    } else {
                        showMsg(Utility.htmlColorGreen("Temperature Alarm notifications OFF"));
                        notifications_on=false;
                        finish();
                    }
                    break;

                case BleAdapterService.NOTIFICATION_OR_INDICATION_RECEIVED:
                    bundle = msg.getData();
                    service_uuid = bundle.getString(BleAdapterService.PARCEL_SERVICE_UUID);
                    characteristic_uuid = bundle.getString(BleAdapterService.PARCEL_CHARACTERISTIC_UUID);
                    b = bundle.getByteArray(BleAdapterService.PARCEL_VALUE);
                    Log.d(Constants.TAG, "Value=" + Utility.byteArrayAsHexString(b));
                    if (characteristic_uuid.equalsIgnoreCase((Utility.normaliseUUID(BleAdapterService.MICROBITEVENT_CHARACTERISTIC_UUID)))) {
                        byte[] event_bytes = new byte[2];
                        byte[] value_bytes = new byte[2];
                        System.arraycopy(b, 0, event_bytes, 0, 2);
                        System.arraycopy(b, 2, value_bytes, 0, 2);
                        short event = Utility.shortFromLittleEndianBytes(event_bytes);
                        short value = Utility.shortFromLittleEndianBytes(value_bytes);
                        Log.d(Constants.TAG, "Temperature Alarm received: event=" + event + " value=" + value);
                        if (event == Constants.MICROBIT_EVENT_TYPE_TEMPERATURE_ALARM) {
                            setAlarmImage(alarm_image, value);
                            setAlarmMessage(value);
                        }
                    }
                    break;
                case BleAdapterService.MESSAGE:
                    bundle = msg.getData();
                    String text = bundle.getString(BleAdapterService.PARCEL_TEXT);
                    showMsg(Utility.htmlColorRed(text));
            }
        }
    };

    private void setAlarmImage(ImageView alarm_image, short state) {
        switch (state) {
            case 0:
                alarm_image.setImageResource(R.drawable.thumb_up);
                break;
            case 1:
                alarm_image.setImageResource(R.drawable.cold);
                break;
            case 2:
                alarm_image.setImageResource(R.drawable.hot);
                break;
        }
    }

    private void setAlarmMessage(short state) {
        switch (state) {
            case 0:
                alarm_message.setText("Just Right");
                break;
            case 1:
                alarm_message.setText("Too Cold!");
                break;
            case 2:
                alarm_message.setText("Too Hot!");
                break;
        }
    }

    private void showMsg(final String msg) {
        Log.d(Constants.TAG, msg);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) TemperatureAlarmActivity.this.findViewById(R.id.message)).setText(Html.fromHtml(msg));
            }
        });
    }

    @Override
    public void connectionStatusChanged(boolean connected) {
        if (connected) {
            showMsg(Utility.htmlColorGreen("Connected"));
        } else {
            showMsg(Utility.htmlColorRed("Disconnected"));
        }
    }

    @Override
    public void serviceDiscoveryStatusChanged(boolean new_state) {

    }
}