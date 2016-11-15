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
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bluetooth.mwoolley.microbitbledemo.bluetooth.BleAdapterService;
import com.bluetooth.mwoolley.microbitbledemo.bluetooth.ConnectionStatusListener;
import com.bluetooth.mwoolley.microbitbledemo.Constants;
import com.bluetooth.mwoolley.microbitbledemo.MicroBit;
import com.bluetooth.mwoolley.microbitbledemo.R;
import com.bluetooth.mwoolley.microbitbledemo.Utility;

public class IoDigitalOutputActivity extends AppCompatActivity implements ConnectionStatusListener, View.OnTouchListener {

    private BleAdapterService bluetooth_le_adapter;

    private int exit_step = 0;
    private boolean exiting = false;
    private boolean on = false;
    private boolean configured=false;
    private boolean requested_output = false;
    private ImageView led_switch;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.d(Constants.TAG, "onServiceConnected");
            bluetooth_le_adapter = ((BleAdapterService.LocalBinder) service).getService();
            bluetooth_le_adapter.setActivityHandler(mMessageHandler);

            // PIN 0 configured for digital
            byte[] ad_flags = {0x00};
            bluetooth_le_adapter.writeCharacteristic(Utility.normaliseUUID(BleAdapterService.IOPINSERVICE_SERVICE_UUID), Utility.normaliseUUID(BleAdapterService.PINADCONFIGURATION_CHARACTERISTIC_UUID), ad_flags)
            ;
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
        setContentView(R.layout.activity_io_digital_output);
        getSupportActionBar().setTitle(R.string.screen_title_digital_output);

        exiting = false;

        // read intent data
        final Intent intent = getIntent();
        MicroBit.getInstance().setConnection_status_listener(this);

        // connect to the Bluetooth smart service
        Intent gattServiceIntent = new Intent(this, BleAdapterService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

//        ((TextView) ButtonActivity.this.findViewById(R.id.message)).setText(Html.fromHtml(msg));

        led_switch = (ImageView) this.findViewById(R.id.big_switch);
        led_switch.setOnTouchListener(this);
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            // may already have unbound. No API to check state so....
            unbindService(mServiceConnection);
        } catch (Exception e) {
        }
        bluetooth_le_adapter = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_io_digital_output, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.menu_io_output_help) {
            Intent intent = new Intent(IoDigitalOutputActivity.this, HelpActivity.class);
            intent.putExtra(Constants.URI, Constants.IO_DIGITAL_OUTPUT_HELP);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    // Service message handler�//////////////////
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
                case BleAdapterService.GATT_CHARACTERISTIC_READ:
                    Log.d(Constants.TAG, "Handler received characteristic read result");
                    bundle = msg.getData();
                    service_uuid = bundle.getString(BleAdapterService.PARCEL_SERVICE_UUID);
                    characteristic_uuid = bundle.getString(BleAdapterService.PARCEL_CHARACTERISTIC_UUID);
                    b = bundle.getByteArray(BleAdapterService.PARCEL_VALUE);
                    Log.d(Constants.TAG, "characteristic " + characteristic_uuid + " of service " + service_uuid + " read OK");
                    Log.d(Constants.TAG, "Value=" + Utility.byteArrayAsHexString(b));
                    if (characteristic_uuid.equalsIgnoreCase(Utility.normaliseUUID(BleAdapterService.PINDATA_CHARACTERISTIC_UUID))) {
                        boolean done=false;
                        int i=0;
                        while (!done) {
                            if (i >= b.length) {
                                Log.d(Constants.TAG,"ERROR: could not find pin 0 data - will assume OFF");
                                on = false;
                                setSwitchImage(on);
                                done = true;
                            } else {
                                // we're looking for the value of PIN 0 so we can determine the initial on/off state
                                if (b[i] == 0) {
                                    i++;
                                    done = true;
                                    if (b.length >= i) {
                                        // then this is the value associated with pin 0 and....
                                        if (b[i] == 0) {
                                            on = false;
                                            Log.d(Constants.TAG, "PIN 0 currently seems to be off");
                                        } else {
                                            Log.d(Constants.TAG, "PIN 0 currently seems to be on");
                                            on = true;
                                        }
                                        setSwitchImage(on);
                                    }
                                } else {
                                    i++;
                                }
                            }
                        }
                    }
                    // PIN 0 configured for output
                    byte[] io_flags_out = {0x00};
                    requested_output = true;
                    bluetooth_le_adapter.writeCharacteristic(Utility.normaliseUUID(BleAdapterService.IOPINSERVICE_SERVICE_UUID), Utility.normaliseUUID(BleAdapterService.PINIOCONFIGURATION_CHARACTERISTIC_UUID), io_flags_out);
                    break;

                case BleAdapterService.GATT_CHARACTERISTIC_WRITTEN:
                    Log.d(Constants.TAG, "Handler received characteristic written result");
                    bundle = msg.getData();
                    service_uuid = bundle.getString(BleAdapterService.PARCEL_SERVICE_UUID);
                    characteristic_uuid = bundle.getString(BleAdapterService.PARCEL_CHARACTERISTIC_UUID);
                    b = bundle.getByteArray(BleAdapterService.PARCEL_VALUE);
                    Log.d(Constants.TAG, "characteristic " + characteristic_uuid + " of service " + service_uuid.toString() + " written OK:0x" + Utility.byteArrayAsHexString(b));
                    if (characteristic_uuid.equalsIgnoreCase((Utility.normaliseUUID(BleAdapterService.PINDATA_CHARACTERISTIC_UUID)))) {
                        on = !on;
                        setSwitchImage(on);
                        return;
                    }
                    if (characteristic_uuid.equalsIgnoreCase((Utility.normaliseUUID(BleAdapterService.PINADCONFIGURATION_CHARACTERISTIC_UUID)))) {
                        Log.d(Constants.TAG, "Temporarily setting pin 0 to input mode to allow reading its current state");
                        byte[] io_flags_in = {0x01};
                        bluetooth_le_adapter.writeCharacteristic(Utility.normaliseUUID(BleAdapterService.IOPINSERVICE_SERVICE_UUID), Utility.normaliseUUID(BleAdapterService.PINIOCONFIGURATION_CHARACTERISTIC_UUID), io_flags_in);
                        return;
                    }
                    if (characteristic_uuid.equalsIgnoreCase((Utility.normaliseUUID(BleAdapterService.PINIOCONFIGURATION_CHARACTERISTIC_UUID))) && !configured && !requested_output) {
                        bluetooth_le_adapter.readCharacteristic(Utility.normaliseUUID(BleAdapterService.IOPINSERVICE_SERVICE_UUID), Utility.normaliseUUID(BleAdapterService.PINDATA_CHARACTERISTIC_UUID));
                        return;
                    }
                    if (characteristic_uuid.equalsIgnoreCase((Utility.normaliseUUID(BleAdapterService.PINIOCONFIGURATION_CHARACTERISTIC_UUID))) && !configured && requested_output) {
                        configured = true;
                        return;
                    }
                    break;
                case BleAdapterService.GATT_DESCRIPTOR_WRITTEN:
                    Log.d(Constants.TAG, "Handler received descriptor written result");
                    bundle = msg.getData();
                    service_uuid = bundle.getString(BleAdapterService.PARCEL_SERVICE_UUID);
                    characteristic_uuid = bundle.getString(BleAdapterService.PARCEL_CHARACTERISTIC_UUID);
                    descriptor_uuid = bundle.getString(BleAdapterService.PARCEL_DESCRIPTOR_UUID);
                    b = bundle.getByteArray(BleAdapterService.PARCEL_VALUE);
                    Log.d(Constants.TAG, "descriptor " + descriptor_uuid + " of characteristic " + characteristic_uuid + " of service " + service_uuid.toString() + " written OK:0x" + Utility.byteArrayAsHexString(b));
                    Log.d(Constants.TAG, "exiting=" + exiting);
                    if (exiting) {
                        finish();
                    }
                    break;

                case BleAdapterService.NOTIFICATION_OR_INDICATION_RECEIVED:
                    bundle = msg.getData();
                    service_uuid = bundle.getString(BleAdapterService.PARCEL_SERVICE_UUID);
                    characteristic_uuid = bundle.getString(BleAdapterService.PARCEL_CHARACTERISTIC_UUID);
                    b = bundle.getByteArray(BleAdapterService.PARCEL_VALUE);
                    Log.d(Constants.TAG, "Value=" + Utility.byteArrayAsHexString(b));
                    if (characteristic_uuid.equalsIgnoreCase((Utility.normaliseUUID(BleAdapterService.PINDATA_CHARACTERISTIC_UUID)))) {
                        Log.d(Constants.TAG, "IO Pin Data received: " + Utility.byteArrayAsHexString(b));
                    }
                    break;
                case BleAdapterService.MESSAGE:
                    bundle = msg.getData();
                    String text = bundle.getString(BleAdapterService.PARCEL_TEXT);
                    showMsg(Utility.htmlColorRed(text));
            }
        }
    };

    private void showMsg(final String msg) {
        Log.d(Constants.TAG, msg);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) IoDigitalOutputActivity.this.findViewById(R.id.message)).setText(Html.fromHtml(msg));
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

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.d(Constants.TAG,"onTouch");
        if (v == led_switch) {
            if (on) {
                byte[] switch_off_pin_0 = {0x00, 0x00};
                bluetooth_le_adapter.writeCharacteristic(Utility.normaliseUUID(BleAdapterService.IOPINSERVICE_SERVICE_UUID), Utility.normaliseUUID(BleAdapterService.PINDATA_CHARACTERISTIC_UUID), switch_off_pin_0);
            } else {
                byte[] switch_on_pin_0 = {0x00, 0x01};
                bluetooth_le_adapter.writeCharacteristic(Utility.normaliseUUID(BleAdapterService.IOPINSERVICE_SERVICE_UUID), Utility.normaliseUUID(BleAdapterService.PINDATA_CHARACTERISTIC_UUID), switch_on_pin_0);
            }
        }
        return false;
    }

    private void setSwitchImage(boolean on) {
        ImageView b1_image = (ImageView) IoDigitalOutputActivity.this.findViewById(R.id.big_switch);
        if (on) {
            b1_image.setImageResource(R.drawable.switch_on);
        } else {
            b1_image.setImageResource(R.drawable.switch_off);
        }
    }
}