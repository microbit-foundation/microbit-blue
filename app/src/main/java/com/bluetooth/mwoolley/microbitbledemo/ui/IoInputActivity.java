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
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

import com.bluetooth.mwoolley.microbitbledemo.bluetooth.BleAdapterService;
import com.bluetooth.mwoolley.microbitbledemo.bluetooth.ConnectionStatusListener;
import com.bluetooth.mwoolley.microbitbledemo.Constants;
import com.bluetooth.mwoolley.microbitbledemo.MicroBit;
import com.bluetooth.mwoolley.microbitbledemo.R;
import com.bluetooth.mwoolley.microbitbledemo.Utility;

// NOT READY FOR USE

public class IoInputActivity extends AppCompatActivity implements ConnectionStatusListener {

    private BleAdapterService bluetooth_le_adapter;

    private int exit_step = 0;
    private boolean exiting = false;
    private boolean on = false;
    private boolean configured = false;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.d(Constants.TAG, "onServiceConnected");
            bluetooth_le_adapter = ((BleAdapterService.LocalBinder) service).getService();
            bluetooth_le_adapter.setActivityHandler(mMessageHandler);

            // PIN 0 configured for analogue
            byte[] ad_flags = {0x01};
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
        setContentView(R.layout.activity_io_input);

        exiting = false;

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
    public void onBackPressed() {
        Log.d(Constants.TAG, "onBackPressed");
        exiting = true;
        switch (exit_step) {
            case 0:
                exit_step++;
                bluetooth_le_adapter.setNotificationsState(Utility.normaliseUUID(BleAdapterService.IOPINSERVICE_SERVICE_UUID), Utility.normaliseUUID(BleAdapterService.PINDATA_CHARACTERISTIC_UUID), false);
                return;
        }
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
                case BleAdapterService.GATT_CHARACTERISTIC_WRITTEN:
                    Log.d(Constants.TAG, "Handler received characteristic written result");
                    bundle = msg.getData();
                    service_uuid = bundle.getString(BleAdapterService.PARCEL_SERVICE_UUID);
                    characteristic_uuid = bundle.getString(BleAdapterService.PARCEL_CHARACTERISTIC_UUID);
                    b = bundle.getByteArray(BleAdapterService.PARCEL_VALUE);
                    Log.d(Constants.TAG, "characteristic " + characteristic_uuid + " of service " + service_uuid.toString() + " written OK:0x" + Utility.byteArrayAsHexString(b));
                    if (characteristic_uuid.equalsIgnoreCase((Utility.normaliseUUID(BleAdapterService.PINADCONFIGURATION_CHARACTERISTIC_UUID)))) {
                        // P{IN 0 configured for input
                        byte[] io_flags = {0x01};
                        bluetooth_le_adapter.writeCharacteristic(Utility.normaliseUUID(BleAdapterService.IOPINSERVICE_SERVICE_UUID), Utility.normaliseUUID(BleAdapterService.PINIOCONFIGURATION_CHARACTERISTIC_UUID), io_flags);
                    } else {
                        if (characteristic_uuid.equalsIgnoreCase((Utility.normaliseUUID(BleAdapterService.PINIOCONFIGURATION_CHARACTERISTIC_UUID)))) {
                            configured = true;
                            showMsg("Reading Pin Data");
                            bluetooth_le_adapter.readCharacteristic(Utility.normaliseUUID(BleAdapterService.IOPINSERVICE_SERVICE_UUID), Utility.normaliseUUID(BleAdapterService.PINDATA_CHARACTERISTIC_UUID));
//                            if (bluetooth_le_adapter.setNotificationsState(Utility.normaliseUUID(BleAdapterService.IOPINSERVICE_SERVICE_UUID), Utility.normaliseUUID(BleAdapterService.PINDATA_CHARACTERISTIC_UUID), true)) {
//                                showMsg(Utility.htmlColorGreen("Pin Data notifications ON"));
//                            } else {
//                                showMsg(Utility.htmlColorRed("Failed to set Pin Data notifications ON"));
//                            }
                        }
                    }
                    break;
                case BleAdapterService.GATT_CHARACTERISTIC_READ:
                    Log.d(Constants.TAG, "Handler received characteristic read result");
                    bundle = msg.getData();
                    service_uuid = bundle.getString(BleAdapterService.PARCEL_SERVICE_UUID);
                    characteristic_uuid = bundle.getString(BleAdapterService.PARCEL_CHARACTERISTIC_UUID);
                    b = bundle.getByteArray(BleAdapterService.PARCEL_VALUE);
                    Log.d(Constants.TAG, "characteristic " + characteristic_uuid + " of service " + service_uuid.toString() + " read value: 0x" + Utility.byteArrayAsHexString(b));
                    if (characteristic_uuid.equalsIgnoreCase((Utility.normaliseUUID(BleAdapterService.PINDATA_CHARACTERISTIC_UUID)))) {
                        ((TextView) IoInputActivity.this.findViewById(R.id.input_value)).setText("Input: " + b[0]);
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
                        ((TextView) IoInputActivity.this.findViewById(R.id.input_value)).setText("Input: " + b[0]);
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
                ((TextView) IoInputActivity.this.findViewById(R.id.message)).setText(msg);
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