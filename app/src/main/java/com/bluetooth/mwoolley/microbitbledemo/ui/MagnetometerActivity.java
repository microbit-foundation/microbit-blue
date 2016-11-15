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
import android.widget.TextView;

import com.bluetooth.mwoolley.microbitbledemo.bluetooth.BleAdapterService;
import com.bluetooth.mwoolley.microbitbledemo.bluetooth.ConnectionStatusListener;
import com.bluetooth.mwoolley.microbitbledemo.Constants;
import com.bluetooth.mwoolley.microbitbledemo.MicroBit;
import com.bluetooth.mwoolley.microbitbledemo.R;
import com.bluetooth.mwoolley.microbitbledemo.Settings;
import com.bluetooth.mwoolley.microbitbledemo.Utility;

public class MagnetometerActivity extends AppCompatActivity implements ConnectionStatusListener {

    private static final int MAGNETOMETER_G_RANGE = 2;
    private static final int MAGNETOMETER_DIVISOR = 512;

    private float[] magnet_input = new float[3];
    private float[] magnet_output = new float[3];

    private float[] bearing_input = new float[1];
    private float[] bearing_output = new float[1];

    private int bearing_count=0;
    private boolean bearing_smoothed = false;

    private BleAdapterService bluetooth_le_adapter;

    private int magnetometer_period;
    private short current_bearing;
    private boolean exiting=false;
    private int exit_step=0;
    private boolean mag_data_notifications_on =false;
    private boolean mag_bearing_notifications_on =false;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.d(Constants.TAG,"onServiceConnected");
            mag_bearing_notifications_on=false;
            mag_data_notifications_on=false;
            bluetooth_le_adapter = ((BleAdapterService.LocalBinder) service).getService();
            bluetooth_le_adapter.setActivityHandler(mMessageHandler);

            if (bluetooth_le_adapter.setNotificationsState(Utility.normaliseUUID(BleAdapterService.MAGNETOMETERSERVICE_SERVICE_UUID), Utility.normaliseUUID(BleAdapterService.MAGNETOMETERDATA_CHARACTERISTIC_UUID), true)) {
                showMsg(Utility.htmlColorGreen("Magnetometer Data notifications ON"));
            } else {
                showMsg(Utility.htmlColorRed("Failed to set Magnetometer Data notifications ON"));
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_magnetometer);
        getSupportActionBar().setTitle(R.string.screen_title_magnetometer);

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
        getMenuInflater().inflate(R.menu.menu_magnetometer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.menu_magnetometer_settings) {
            magnetometer_period = Settings.getInstance().getMagnetometer_period();
            Intent intent = new Intent(MagnetometerActivity.this, MagnetometerSettingsActivity.class);
            startActivityForResult(intent, MagnetometerSettingsActivity.START_MAGNETOMETER_SETTINGS);
            return true;
        }
        if (id == R.id.menu_magnetometer_help) {
            Intent intent = new Intent(MagnetometerActivity.this, HelpActivity.class);
            intent.putExtra(Constants.URI, Constants.MAGNETOMETER_HELP);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(Constants.TAG, "onActivityResult");
        if (requestCode == MagnetometerSettingsActivity.START_MAGNETOMETER_SETTINGS) {
            if (resultCode == RESULT_OK) {
                Log.d(Constants.TAG, "onActivityResult RESULT_OK");
                if (Settings.getInstance().getMagnetometer_period() != magnetometer_period) {
                    Log.d(Constants.TAG, "magnetometer period has changed to "+Settings.getInstance().getMagnetometer_period());
                    showMsg(Utility.htmlColorBlue("Changing magnetometer period to:"));
                    Log.d(Constants.TAG, Utility.byteArrayAsHexString(Utility.leBytesFromShort(Settings.getInstance().getMagnetometer_period())));
                    bluetooth_le_adapter.writeCharacteristic(Utility.normaliseUUID(BleAdapterService.MAGNETOMETERSERVICE_SERVICE_UUID), Utility.normaliseUUID(BleAdapterService.MAGNETOMETERPERIOD_CHARACTERISTIC_UUID), Utility.leBytesFromShort(Settings.getInstance().getMagnetometer_period()));
                }
            } else {
                Log.d(Constants.TAG, "onActivityResult NOT RESULT_OK");
            }
        }
    }

    public void onBackPressed() {
        Log.d(Constants.TAG, "onBackPressed");
        shutdownSteps();
    }

    private void shutdownSteps() {
        exiting=true;
        exit_step++;
        if (MicroBit.getInstance().isMicrobit_connected()) {
            switch (exit_step) {
                case 1:
                    if (mag_data_notifications_on) {
                        bluetooth_le_adapter.setNotificationsState(Utility.normaliseUUID(BleAdapterService.MAGNETOMETERSERVICE_SERVICE_UUID), Utility.normaliseUUID(BleAdapterService.MAGNETOMETERDATA_CHARACTERISTIC_UUID), false);
                    }
                    break;
                case 2:
                    if (mag_bearing_notifications_on) {
                        bluetooth_le_adapter.setNotificationsState(Utility.normaliseUUID(BleAdapterService.MAGNETOMETERSERVICE_SERVICE_UUID), Utility.normaliseUUID(BleAdapterService.MAGNETOMETERBEARING_CHARACTERISTIC_UUID), false);
                    }
                    break;
                default:
                    finish();
                    try {
                        unbindService(mServiceConnection);
                    } catch (Exception e) {
                    }

            }
        } else {
            finish();
            try {
                unbindService(mServiceConnection);
            } catch (Exception e) {
            }

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
                    Log.d(Constants.TAG, "characteristic " + characteristic_uuid + " of service " + service_uuid + " written OK");
                    showMsg(Utility.htmlColorGreen("Ready"));
                    break;
                case BleAdapterService.GATT_DESCRIPTOR_WRITTEN:
                    Log.d(Constants.TAG, "Handler received descriptor written result");
                    bundle = msg.getData();
                    service_uuid = bundle.getString(BleAdapterService.PARCEL_SERVICE_UUID);
                    characteristic_uuid = bundle.getString(BleAdapterService.PARCEL_CHARACTERISTIC_UUID);
                    descriptor_uuid = bundle.getString(BleAdapterService.PARCEL_DESCRIPTOR_UUID);
                    Log.d(Constants.TAG, "descriptor " + descriptor_uuid + " of characteristic " + characteristic_uuid + " of service " + service_uuid.toString() + " written OK");
                    Log.d(Constants.TAG,Utility.normaliseUUID(BleAdapterService.MAGNETOMETERDATA_CHARACTERISTIC_UUID));
                    if (!exiting) {
                        if (characteristic_uuid.equalsIgnoreCase(Utility.normaliseUUID(BleAdapterService.MAGNETOMETERDATA_CHARACTERISTIC_UUID))) {
                            mag_data_notifications_on=true;
                            Log.d(Constants.TAG, "Enabling bearing notifications");
                            if (bluetooth_le_adapter.setNotificationsState(Utility.normaliseUUID(BleAdapterService.MAGNETOMETERSERVICE_SERVICE_UUID), Utility.normaliseUUID(BleAdapterService.MAGNETOMETERBEARING_CHARACTERISTIC_UUID), true)) {
                                showMsg(Utility.htmlColorGreen("Magnetometer Bearing notifications ON"));
                            } else {
                                showMsg(Utility.htmlColorRed("Failed to set Magnetometer Bearing notifications ON"));
                            }
                        } else {
                            mag_bearing_notifications_on = true;
                        }
                    } else {
                        shutdownSteps();
                    }
                    break;

                case BleAdapterService.NOTIFICATION_OR_INDICATION_RECEIVED:
                    bundle = msg.getData();
                    service_uuid = bundle.getString(BleAdapterService.PARCEL_SERVICE_UUID);
                    characteristic_uuid = bundle.getString(BleAdapterService.PARCEL_CHARACTERISTIC_UUID);
                    b = bundle.getByteArray(BleAdapterService.PARCEL_VALUE);
                    Log.d(Constants.TAG, "Value=" + Utility.byteArrayAsHexString(b));
                    if (characteristic_uuid.equalsIgnoreCase((Utility.normaliseUUID(BleAdapterService.MAGNETOMETERDATA_CHARACTERISTIC_UUID)))) {
                        byte[] x_bytes = new byte[2];
                        byte[] y_bytes = new byte[2];
                        byte[] z_bytes = new byte[2];
                        System.arraycopy(b, 0, x_bytes, 0, 2);
                        System.arraycopy(b, 2, y_bytes, 0, 2);
                        System.arraycopy(b, 4, z_bytes, 0, 2);
                        short raw_x = Utility.shortFromLittleEndianBytes(x_bytes);
                        short raw_y = Utility.shortFromLittleEndianBytes(y_bytes);
                        short raw_z = Utility.shortFromLittleEndianBytes(z_bytes);
                        Log.d(Constants.TAG, "Magnetometer Data received: x=" + raw_x + " y=" + raw_y + " z=" + raw_z);

                        float magnet_x = raw_x / 1000f;
                        float magnet_y = raw_y / 1000f;
                        float magnet_z = raw_z / 1000f;
                        Log.d(Constants.TAG, "magnetometer data converted: x=" + magnet_x + " y=" + magnet_y + " z=" + magnet_z);

                        magnet_input[0] = magnet_x;
                        magnet_input[1] = magnet_y;
                        magnet_input[2] = magnet_z;
                        magnet_output = Utility.lowPass(magnet_input, magnet_output);
                        Log.d(Constants.TAG, "Smoothed magnetometer data: x=" + magnet_output[0] + " y=" + magnet_output[1] + " z=" + magnet_output[2]);

                        ((TextView) MagnetometerActivity.this.findViewById(R.id.magnet_x)).setText("X: " + String.format("%.3f", magnet_output[0]));
                        ((TextView) MagnetometerActivity.this.findViewById(R.id.magnet_y)).setText("Y: " + String.format("%.3f", magnet_output[1]));
                        ((TextView) MagnetometerActivity.this.findViewById(R.id.magnet_z)).setText("Z: " + String.format("%.3f", magnet_output[2]));

                    } else {
                        if (characteristic_uuid.equalsIgnoreCase((Utility.normaliseUUID(BleAdapterService.MAGNETOMETERBEARING_CHARACTERISTIC_UUID)))) {
                            byte[] bearing_bytes = new byte[2];
                            System.arraycopy(b, 0, bearing_bytes, 0, 2);
                            short bearing = Utility.shortFromLittleEndianBytes(bearing_bytes);
                            Log.d(Constants.TAG, "Magnetometer Bearing received: " + bearing);

                            bearing_input[0] = bearing;
                            bearing_output = Utility.lowPass(bearing_input, bearing_output);

                            ((TextView) MagnetometerActivity.this.findViewById(R.id.bearing)).setText(bearing_output[0] + " degrees");
                            current_bearing = bearing;

                            if (!bearing_smoothed) {
                                bearing_count++;
                                if (bearing_count > 30) {
                                    bearing_smoothed = true;
                                }
                            }
                            String point_name = compassPoint(current_bearing);
                            Log.d(Constants.TAG, "Point Name: " + point_name);
                            ((TextView) MagnetometerActivity.this.findViewById(R.id.compass_point)).setText(point_name);
                        }

                    }
                    break;
                case BleAdapterService.GATT_REMOTE_RSSI:
                    bundle = msg.getData();
                    int rssi = bundle.getInt(BleAdapterService.PARCEL_RSSI);
//                    PeripheralControlActivity.this.updateRssi(rssi);
                    break;
                case BleAdapterService.MESSAGE:
                    bundle = msg.getData();
                    String text = bundle.getString(BleAdapterService.PARCEL_TEXT);
                    showMsg(Utility.htmlColorRed(text));
            }
        }
    };

    private String compassPoint(short bearing) {
        double d = bearing / Constants.COMPASS_POINT_DELTA;
        int name_inx = (int) d;
        if (d - name_inx > 0.5) {
            name_inx++;
        }
        if (name_inx > 15) {
            name_inx = 0;
        }
        return Constants.COMPASS_POINTS[name_inx];
    }

    private void showMsg(final String msg) {
        Log.d(Constants.TAG, msg);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) MagnetometerActivity.this.findViewById(R.id.message)).setText(Html.fromHtml(msg));
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