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

import com.bluetooth.mwoolley.microbitbledemo.Constants;
import com.bluetooth.mwoolley.microbitbledemo.MicroBit;
import com.bluetooth.mwoolley.microbitbledemo.R;
import com.bluetooth.mwoolley.microbitbledemo.Settings;
import com.bluetooth.mwoolley.microbitbledemo.Utility;
import com.bluetooth.mwoolley.microbitbledemo.bluetooth.BleAdapterService;
import com.bluetooth.mwoolley.microbitbledemo.bluetooth.ConnectionStatusListener;

import java.io.UnsupportedEncodingException;

public class DeviceInformationActivity extends AppCompatActivity implements ConnectionStatusListener {

    private BleAdapterService bluetooth_le_adapter;

    private String model_number;
    private String serial_number;
    private String firmware_revision;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.d(Constants.TAG,"onServiceConnected");
            bluetooth_le_adapter = ((BleAdapterService.LocalBinder) service).getService();
            bluetooth_le_adapter.setActivityHandler(mMessageHandler);
            bluetooth_le_adapter.readCharacteristic(Utility.normaliseUUID(BleAdapterService.DEVICEINFORMATION_SERVICE_UUID),Utility.normaliseUUID(BleAdapterService.MODELNUMBERSTRING_CHARACTERISTIC_UUID));
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_device_information);
        getSupportActionBar().setTitle(R.string.screen_title_device_information);

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
        try {
            // may already have unbound. No API to check state so....
            unbindService(mServiceConnection);
        } catch (Exception e) {
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_device_information, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.menu_device_information_help) {
            Intent intent = new Intent(DeviceInformationActivity.this, HelpActivity.class);
            intent.putExtra(Constants.URI, Constants.DEVICE_INFORMATION_HELP);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
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
                case BleAdapterService.GATT_CHARACTERISTIC_READ:
                    Log.d(Constants.TAG, "Handler received characteristic read result");
                    bundle = msg.getData();
                    service_uuid = bundle.getString(BleAdapterService.PARCEL_SERVICE_UUID);
                    characteristic_uuid = bundle.getString(BleAdapterService.PARCEL_CHARACTERISTIC_UUID);
                    b = bundle.getByteArray(BleAdapterService.PARCEL_VALUE);
                    Log.d(Constants.TAG, "characteristic " + characteristic_uuid + " of service " + service_uuid + " read OK");
                    Log.d(Constants.TAG, "Value=" + Utility.byteArrayAsHexString(b));
                    if (characteristic_uuid.equalsIgnoreCase(Utility.normaliseUUID(BleAdapterService.MODELNUMBERSTRING_CHARACTERISTIC_UUID))) {
                        try {
                            model_number = new String(b,"UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                            model_number = "ERROR: Bad encoding";
                        }
                        showModelNumber();
                        // next read the serial number
                        bluetooth_le_adapter.readCharacteristic(Utility.normaliseUUID(BleAdapterService.DEVICEINFORMATION_SERVICE_UUID),Utility.normaliseUUID(BleAdapterService.SERIALNUMBERSTRING_CHARACTERISTIC_UUID));
                        break;
                    }
                    if (characteristic_uuid.equalsIgnoreCase(Utility.normaliseUUID(BleAdapterService.SERIALNUMBERSTRING_CHARACTERISTIC_UUID))) {
                        try {
                            serial_number = new String(b,"UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                            serial_number = "ERROR: Bad encoding";
                        }
                        showSerialNumber();
                        // finally, read the firmware revision
                        bluetooth_le_adapter.readCharacteristic(Utility.normaliseUUID(BleAdapterService.DEVICEINFORMATION_SERVICE_UUID),Utility.normaliseUUID(BleAdapterService.FIRMWAREREVISIONSTRING_CHARACTERISTIC_UUID));
                        break;
                    }
                    if (characteristic_uuid.equalsIgnoreCase(Utility.normaliseUUID(BleAdapterService.FIRMWAREREVISIONSTRING_CHARACTERISTIC_UUID))) {
                        try {
                            firmware_revision = new String(b,"UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                            firmware_revision = "ERROR: Bad encoding";
                        }
                        showFirmwareRevision();
                        break;
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
                ((TextView) DeviceInformationActivity.this.findViewById(R.id.message)).setText(Html.fromHtml(msg));
            }
        });
    }

    private void showModelNumber() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) DeviceInformationActivity.this.findViewById(R.id.model_number)).setText(model_number);
            }
        });
    }

    private void showSerialNumber() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) DeviceInformationActivity.this.findViewById(R.id.serial_number)).setText(serial_number);
            }
        });
    }

    private void showFirmwareRevision() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) DeviceInformationActivity.this.findViewById(R.id.firnware_revision)).setText(firmware_revision);
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