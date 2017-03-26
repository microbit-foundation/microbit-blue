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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.view.WindowManager;

import android.util.Log;

import com.bluetooth.mwoolley.microbitbledemo.bluetooth.BleAdapterService;
import com.bluetooth.mwoolley.microbitbledemo.bluetooth.ConnectionStatusListener;
import com.bluetooth.mwoolley.microbitbledemo.Constants;
import com.bluetooth.mwoolley.microbitbledemo.MicroBit;
import com.bluetooth.mwoolley.microbitbledemo.R;
import com.bluetooth.mwoolley.microbitbledemo.Settings;
import com.bluetooth.mwoolley.microbitbledemo.Utility;

public class AccelerometerActivity extends AppCompatActivity implements ConnectionStatusListener {

    private static final int ACCELEROMETER_G_RANGE = 2;
    private static final int ACCELEROMETER_DIVISOR = 512;

    private float[] accel_input = new float[3];
    private float[] accel_output = new float[3];

    private BleAdapterService bluetooth_le_adapter;

    private boolean exiting=false;
    private int accelerometer_period;

    private boolean notifications_on =false;
    private long start_time;
    private int minute_number;
    private int notification_count;
    private boolean apply_smoothing=true;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.d(Constants.TAG,"onServiceConnected");
            notifications_on=false;
            start_time = System.currentTimeMillis();
            minute_number=1;
            notification_count=0;
            showBenchmark();
            bluetooth_le_adapter = ((BleAdapterService.LocalBinder) service).getService();
            bluetooth_le_adapter.setActivityHandler(mMessageHandler);
            bluetooth_le_adapter.readCharacteristic(Utility.normaliseUUID(BleAdapterService.ACCELEROMETERSERVICE_SERVICE_UUID),Utility.normaliseUUID(BleAdapterService.ACCELEROMETERPERIOD_CHARACTERISTIC_UUID));
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_accelerometer);
        getSupportActionBar().setTitle(R.string.screen_title_accelerometer);

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
        if (notifications_on) {
            bluetooth_le_adapter.setNotificationsState(Utility.normaliseUUID(BleAdapterService.ACCELEROMETERSERVICE_SERVICE_UUID), Utility.normaliseUUID(BleAdapterService.ACCELEROMETERDATA_CHARACTERISTIC_UUID), false);
        }
        try {
            // may already have unbound. No API to check state so....
            unbindService(mServiceConnection);
        } catch (Exception e) {
        }
    }

    public void onBackPressed() {
        Log.d(Constants.TAG, "onBackPressed");
        if (MicroBit.getInstance().isMicrobit_connected() && notifications_on) {
            bluetooth_le_adapter.setNotificationsState(Utility.normaliseUUID(BleAdapterService.ACCELEROMETERSERVICE_SERVICE_UUID), Utility.normaliseUUID(BleAdapterService.ACCELEROMETERDATA_CHARACTERISTIC_UUID), false);
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
        exiting=true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_accelerometer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.menu_accelerometer_settings) {
            accelerometer_period = Settings.getInstance().getAccelerometer_period();
            Intent intent = new Intent(AccelerometerActivity.this, AccelerometerSettingsActivity.class);
            startActivityForResult(intent, AccelerometerSettingsActivity.START_ACCELEROMETER_SETTINGS);
            return true;
        }
        if (id == R.id.menu_accelerometer_help) {
            Intent intent = new Intent(AccelerometerActivity.this, HelpActivity.class);
            intent.putExtra(Constants.URI, Constants.ACCELEROMETER_HELP);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(Constants.TAG, "onActivityResult");
        if (requestCode == AccelerometerSettingsActivity.START_ACCELEROMETER_SETTINGS) {
            if (resultCode == RESULT_OK) {
                Log.d(Constants.TAG, "onActivityResult RESULT_OK");
                if (Settings.getInstance().getAccelerometer_period() != accelerometer_period) {
                    Log.d(Constants.TAG, "accelerometer period has changed to "+Settings.getInstance().getAccelerometer_period());
                    accelerometer_period = Settings.getInstance().getAccelerometer_period();
                    showMsg(Utility.htmlColorBlue("Changing accelerometer period to:"));
                    Log.d(Constants.TAG, Utility.byteArrayAsHexString(Utility.leBytesFromShort(Settings.getInstance().getAccelerometer_period())));
                    bluetooth_le_adapter.writeCharacteristic(Utility.normaliseUUID(BleAdapterService.ACCELEROMETERSERVICE_SERVICE_UUID), Utility.normaliseUUID(BleAdapterService.ACCELEROMETERPERIOD_CHARACTERISTIC_UUID), Utility.leBytesFromShort(Settings.getInstance().getAccelerometer_period()));
                }
            } else {
                Log.d(Constants.TAG, "onActivityResult NOT RESULT_OK");
            }
        }
    }

    public void onApplySmoothingChanged(View v) {
        apply_smoothing = ((Switch) v).isChecked();
    }

    private static Handler mMessageHandler = new Handler() {
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
                    if (characteristic_uuid.equalsIgnoreCase(Utility.normaliseUUID(BleAdapterService.ACCELEROMETERPERIOD_CHARACTERISTIC_UUID))) {
                        boolean got_accelerometer_period = false;
                        byte [] period_bytes = new byte[2];
                        if (b.length == 2) {
                            period_bytes[0] = b[0];
                            period_bytes[1] = b[1];
                            got_accelerometer_period = true;
                        } else {
                            if (b.length == 1) {
                                period_bytes[0] = b[0];
                                period_bytes[1] = 0x00;
                                got_accelerometer_period = true;
                            } else {
                                Log.d(Constants.TAG,"Couldn't obtain value of accelerometer period");
                            }
                        }
                        if (got_accelerometer_period) {
                            accelerometer_period = (int) Utility.shortFromLittleEndianBytes(period_bytes);
                            Settings.getInstance().setAccelerometer_period((short) accelerometer_period);
                            showAccelerometerPeriod();
                        }
                    }
                    bluetooth_le_adapter.setNotificationsState(Utility.normaliseUUID(BleAdapterService.ACCELEROMETERSERVICE_SERVICE_UUID), Utility.normaliseUUID(BleAdapterService.ACCELEROMETERDATA_CHARACTERISTIC_UUID), true);
                    break;
                case BleAdapterService.GATT_CHARACTERISTIC_WRITTEN:
                    Log.d(Constants.TAG, "Handler received characteristic written result");
                    bundle = msg.getData();
                    service_uuid = bundle.getString(BleAdapterService.PARCEL_SERVICE_UUID);
                    characteristic_uuid = bundle.getString(BleAdapterService.PARCEL_CHARACTERISTIC_UUID);
                    Log.d(Constants.TAG, "characteristic " + characteristic_uuid + " of service " + service_uuid + " written OK");
                    showAccelerometerPeriod();
                    showMsg(Utility.htmlColorGreen("Ready"));
                    break;
                case BleAdapterService.GATT_DESCRIPTOR_WRITTEN:
                    Log.d(Constants.TAG, "Handler received descriptor written result");
                    bundle = msg.getData();
                    service_uuid = bundle.getString(BleAdapterService.PARCEL_SERVICE_UUID);
                    characteristic_uuid = bundle.getString(BleAdapterService.PARCEL_CHARACTERISTIC_UUID);
                    descriptor_uuid = bundle.getString(BleAdapterService.PARCEL_DESCRIPTOR_UUID);
                    Log.d(Constants.TAG, "descriptor " + descriptor_uuid + " of characteristic " + characteristic_uuid + " of service " + service_uuid + " written OK");
                    if (!exiting) {
                        showMsg(Utility.htmlColorGreen("Accelerometer Data notifications ON"));
                        notifications_on=true;
                        start_time = System.currentTimeMillis();
                    } else {
                        showMsg(Utility.htmlColorGreen("Accelerometer Data notifications OFF"));
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
                    if (characteristic_uuid.equalsIgnoreCase((Utility.normaliseUUID(BleAdapterService.ACCELEROMETERDATA_CHARACTERISTIC_UUID)))) {
                        notification_count++;
                        if (System.currentTimeMillis() - start_time >= 60000) {
                            showBenchmark();
                            notification_count = 0;
                            minute_number++;
                            start_time = System.currentTimeMillis();
                        }
                        byte[] x_bytes = new byte[2];
                        byte[] y_bytes = new byte[2];
                        byte[] z_bytes = new byte[2];
                        System.arraycopy(b, 0, x_bytes, 0, 2);
                        System.arraycopy(b, 2, y_bytes, 0, 2);
                        System.arraycopy(b, 4, z_bytes, 0, 2);
                        short raw_x = Utility.shortFromLittleEndianBytes(x_bytes);
                        short raw_y = Utility.shortFromLittleEndianBytes(y_bytes);
                        short raw_z = Utility.shortFromLittleEndianBytes(z_bytes);
                        Log.d(Constants.TAG, "Accelerometer Data received: x=" + raw_x + " y=" + raw_y + " z=" + raw_z);


                        // range is -1024 : +1024
                        // Starting with the LED display face up and level (perpendicular to gravity) and edge connector towards your body:
                        // A negative X value means tilting left, a positive X value means tilting right
                        // A negative Y value means tilting away from you, a positive Y value means tilting towards you
                        // A negative Z value means ?

                        accel_input[0] = raw_x / 1000f;
                        accel_input[1] = raw_y / 1000f;
                        accel_input[2] = raw_z / 1000f;
                        if (apply_smoothing) {
                            accel_output = Utility.lowPass(accel_input, accel_output);
                        } else {
                            accel_output[0] = accel_input[0];
                            accel_output[1] = accel_input[1];
                            accel_output[2] = accel_input[2];
                        }

                        double pitch = Math.atan(accel_output[0] / Math.sqrt(Math.pow(accel_output[1], 2) + Math.pow(accel_output[2], 2)));
                        double roll = Math.atan(accel_output[1] / Math.sqrt(Math.pow(accel_output[0], 2) + Math.pow(accel_output[2], 2)));
                        //convert radians into degrees
                        pitch = pitch * (180.0 / Math.PI);
                        roll = -1 * roll * (180.0 / Math.PI);

                        showAccelerometerData(accel_output,pitch,roll);

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

    private void showMsg(final String msg) {
        Log.d(Constants.TAG, msg);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) AccelerometerActivity.this.findViewById(R.id.message)).setText(Html.fromHtml(msg));
            }
        });
    }

    private void showAccelerometerPeriod() {
        Log.d(Constants.TAG, "Accelerometer Period: "+accelerometer_period+"ms");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) AccelerometerActivity.this.findViewById(R.id.accel_period)).setText("Polling: "+Integer.toString(accelerometer_period)+"ms");
            }
        });
    }

    private void showAccelerometerData(final float [] accel_data, final double pitch, final double roll) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) AccelerometerActivity.this.findViewById(R.id.accel_x)).setText("X: " + String.format("%.3f", accel_data[0]));
                ((TextView) AccelerometerActivity.this.findViewById(R.id.accel_y)).setText("Y: " + String.format("%.3f", accel_data[1]));
                ((TextView) AccelerometerActivity.this.findViewById(R.id.accel_z)).setText("Z: " + String.format("%.3f", accel_data[2]));
                ((TextView) AccelerometerActivity.this.findViewById(R.id.pitch)).setText("PITCH: " + String.format("%.1f", pitch));
                ((TextView) AccelerometerActivity.this.findViewById(R.id.roll)).setText("ROLL: " + String.format("%.1f", roll));
                (AccelerometerActivity.this.findViewById(R.id.microbit)).setRotationX((float) roll);
                (AccelerometerActivity.this.findViewById(R.id.microbit)).setRotationY((float) pitch);
            }
        });
    }

	private void showBenchmark() {
			final int notifications_per_minute = notification_count;
			final int notifications_per_second = notification_count / 60;
			Log.d(Constants.TAG,"Minute: " + Integer.toString(minute_number));
			Log.d(Constants.TAG,"Notification Count: " + Integer.toString(notification_count));
			Log.d(Constants.TAG,"Notifications per Second: " + Integer.toString(notifications_per_second));
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