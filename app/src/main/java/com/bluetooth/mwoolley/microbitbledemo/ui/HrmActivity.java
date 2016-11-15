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

import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.GridLayout;
import android.widget.TextView;

import com.bluetooth.mwoolley.microbitbledemo.Constants;
import com.bluetooth.mwoolley.microbitbledemo.MicroBit;
import com.bluetooth.mwoolley.microbitbledemo.R;
import com.bluetooth.mwoolley.microbitbledemo.Settings;
import com.bluetooth.mwoolley.microbitbledemo.Utility;
import com.bluetooth.mwoolley.microbitbledemo.bluetooth.BleAdapterService;
import com.bluetooth.mwoolley.microbitbledemo.bluetooth.HrmAdapterService;
import com.bluetooth.mwoolley.microbitbledemo.bluetooth.ConnectionStatusListener;

import java.io.UnsupportedEncodingException;

public class HrmActivity extends AppCompatActivity implements ConnectionStatusListener, Runnable {


    private HrmAdapterService hrm_le_adapter;
    private BleAdapterService mb_le_adapter;

    private boolean exiting=false;

    private boolean notifications_on =false;

    private BluetoothDevice hrm_device;

    private boolean hrm_connected=true;

    private byte[] led_matrix_state;
    // contains 25 cells, one per LED

    // 0=off, 1=flashing, 2=solid on
    private byte[] local_led_state;

    private boolean partial_on = false;

    private boolean microbit_display_refresh_running=true;
    private Object mutex=new Object();

    private int hr_measurement=0;
    private long total_hrm_measurements;
    private long [] hrm_histogram;
    private int [] hrm_histogram_pc;

    private final ServiceConnection hrmServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.d(Constants.TAG,"HRM onServiceConnected");
            notifications_on=false;
            hrm_le_adapter = ((HrmAdapterService.LocalBinder) service).getService();
            hrm_le_adapter.setActivityHandler(mMessageHandler);
            hrm_device = hrm_le_adapter.getDevice();
            hrm_le_adapter.setNotificationsState(Utility.normaliseUUID(HrmAdapterService.HEARTRATE_SERVICE_UUID), Utility.normaliseUUID(HrmAdapterService.HEARTRATEMEASUREMENT_CHARACTERISTIC_UUID), true);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }

    };

    private final ServiceConnection mbServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.d(Constants.TAG,"micro:bit onServiceConnected");
            mb_le_adapter = ((BleAdapterService.LocalBinder) service).getService();
            mb_le_adapter.setActivityHandler(mMessageHandler);
            clearMicrobitDisplay();
            startMicrobitDisplayRefresh();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }

    };

    private void startMicrobitDisplayRefresh() {
        Thread refresh_thread = new Thread(this);
        refresh_thread.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_hrm);
        getSupportActionBar().setTitle(R.string.screen_title_hrm);

        led_matrix_state = new byte[5];
        local_led_state = new byte[25];
        total_hrm_measurements = 0;
        hrm_histogram = new long[5];
        hrm_histogram_pc = new int[5];

        // read intent data
        final Intent intent = getIntent();
        MicroBit.getInstance().setConnection_status_listener(this);

        Intent hrmServiceIntent = new Intent(this, HrmAdapterService.class);
        bindService(hrmServiceIntent, hrmServiceConnection, BIND_AUTO_CREATE);
        Intent mbServiceIntent = new Intent(this, BleAdapterService.class);
        bindService(mbServiceIntent, mbServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (notifications_on) {
            hrm_le_adapter.setNotificationsState(Utility.normaliseUUID(HrmAdapterService.HEARTRATE_SERVICE_UUID), Utility.normaliseUUID(HrmAdapterService.HEARTRATEMEASUREMENT_CHARACTERISTIC_UUID), false);
        }
        try {
            // may already have unbound. No API to check state so....
            unbindService(hrmServiceConnection);
        } catch (Exception e) {
        }
        microbit_display_refresh_running=false;
    }

    public void onBackPressed() {
        Log.d(Constants.TAG, "onBackPressed");
        if (MicroBit.getInstance().isMicrobit_connected() && hrm_connected && notifications_on) {
            hrm_le_adapter.setNotificationsState(Utility.normaliseUUID(HrmAdapterService.HEARTRATE_SERVICE_UUID), Utility.normaliseUUID(HrmAdapterService.HEARTRATEMEASUREMENT_CHARACTERISTIC_UUID), false);
        }
        exiting=true;
        if (!MicroBit.getInstance().isMicrobit_connected()) {
            try {
                // may already have unbound. No API to check state so....
                unbindService(hrmServiceConnection);
            } catch (Exception e) {
            }
            finish();
        }
        microbit_display_refresh_running=false;
        exiting=true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_hrm, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.menu_hrm_settings) {
            Intent intent = new Intent(HrmActivity.this, HrmSettingsActivity.class);
            startActivityForResult(intent, HrmSettingsActivity.START_HRM_SETTINGS);
            return true;
        }
        if (id == R.id.menu_hrm_help) {
            Intent intent = new Intent(HrmActivity.this, HelpActivity.class);
            intent.putExtra(Constants.URI, Constants.HRM_HELP);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(Constants.TAG, "onActivityResult");
        if (requestCode == HrmSettingsActivity.START_HRM_SETTINGS) {
            if (resultCode == RESULT_OK) {
                Log.d(Constants.TAG, "onActivityResult RESULT_OK");
                Settings settings = Settings.getInstance();
                settings.save(this);
            } else {
                Log.d(Constants.TAG, "onActivityResult NOT RESULT_OK");
            }
        }
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
                case HrmAdapterService.GATT_DESCRIPTOR_WRITTEN:
                    Log.d(Constants.TAG, "Handler received descriptor written result");
                    bundle = msg.getData();
                    service_uuid = bundle.getString(HrmAdapterService.PARCEL_SERVICE_UUID);
                    characteristic_uuid = bundle.getString(HrmAdapterService.PARCEL_CHARACTERISTIC_UUID);
                    descriptor_uuid = bundle.getString(HrmAdapterService.PARCEL_DESCRIPTOR_UUID);
                    Log.d(Constants.TAG, "descriptor " + descriptor_uuid + " of characteristic " + characteristic_uuid + " of service " + service_uuid + " written OK");
                    if (!exiting) {
                        notifications_on=true;
                    } else {
                        showMsg(Utility.htmlColorGreen("Heart rate measurement notifications OFF"));
                        notifications_on=false;
                        finish();
                    }
                    break;

                case HrmAdapterService.NOTIFICATION_RECEIVED:
                    bundle = msg.getData();
                    service_uuid = bundle.getString(HrmAdapterService.PARCEL_SERVICE_UUID);
                    characteristic_uuid = bundle.getString(HrmAdapterService.PARCEL_CHARACTERISTIC_UUID);
                    b = bundle.getByteArray(HrmAdapterService.PARCEL_VALUE);
                    Log.d(Constants.TAG, "Value=" + Utility.byteArrayAsHexString(b));
                    if (characteristic_uuid.equalsIgnoreCase((Utility.normaliseUUID(HrmAdapterService.HEARTRATEMEASUREMENT_CHARACTERISTIC_UUID)))) {
                        hr_measurement=0;
                        if(is16BitHrMeasurement(b[0])) {
                           hr_measurement = (int) Utility.shortFromLittleEndianBytes(b[1],b[2]);
                        } else {
                            hr_measurement = (int) (b[1] & 0xff);
                        }
                        Log.d(Constants.TAG, "Heart rate measurement received: " + hr_measurement);
                        updateBpm(hr_measurement);
                        update_HrmStats(hr_measurement);
                        updateLocalLedMatrix();
                    }
                    break;
                case HrmAdapterService.MESSAGE:
                    bundle = msg.getData();
                    String text = bundle.getString(HrmAdapterService.PARCEL_TEXT);
                    showMsg(Utility.htmlColorRed(text));
            }
        }
    };

    private void showHistogramOnMicrobit() {
        mb_le_adapter.writeCharacteristic(Utility.normaliseUUID(BleAdapterService.LEDSERVICE_SERVICE_UUID), Utility.normaliseUUID(BleAdapterService.LEDMATRIXSTATE_CHARACTERISTIC_UUID), led_matrix_state);
    }

    private void showHeartRateOnMicrobit() {
        final String bpm_msg = Integer.toString(hr_measurement);
        byte[] utf8_bytes = new byte[0];
        try {
            utf8_bytes = bpm_msg.getBytes("UTF-8");
            mb_le_adapter.writeCharacteristic(Utility.normaliseUUID(BleAdapterService.LEDSERVICE_SERVICE_UUID), Utility.normaliseUUID(BleAdapterService.LEDTEXT_CHARACTERISTIC_UUID), utf8_bytes);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void clearMicrobitDisplay() {
        for (int i=0;i<5;i++) {
            led_matrix_state[i] = 0x00;
        }
        showHistogramOnMicrobit();
    }

    private void updateLocalLedMatrix() {
        Log.d(Constants.TAG,"total_hrm_measurements="+total_hrm_measurements);
        float one_pc = total_hrm_measurements / 100f;
        Log.d(Constants.TAG,"one_pc="+one_pc);
        if (one_pc == 0) {
            return;
        }

        Log.d(Constants.TAG, "hrm_histogram[0] " + hrm_histogram[0]);
        Log.d(Constants.TAG, "hrm_histogram[1] " + hrm_histogram[1]);
        Log.d(Constants.TAG, "hrm_histogram[2] " + hrm_histogram[2]);
        Log.d(Constants.TAG, "hrm_histogram[3] " + hrm_histogram[3]);
        Log.d(Constants.TAG, "hrm_histogram[4] " + hrm_histogram[4]);

        int zone1 = (int) (hrm_histogram[0] / one_pc);
        int zone2 = (int) (hrm_histogram[1] / one_pc);
        int zone3 = (int) (hrm_histogram[2] / one_pc);
        int zone4 = (int) (hrm_histogram[3] / one_pc);
        int zone5 = (int) (hrm_histogram[4] / one_pc);

        hrm_histogram_pc[0] = zone1;
        hrm_histogram_pc[1] = zone2;
        hrm_histogram_pc[2] = zone3;
        hrm_histogram_pc[3] = zone4;
        hrm_histogram_pc[4] = zone5;

        Log.d(Constants.TAG, "Zone 1 % " + zone1);
        Log.d(Constants.TAG, "Zone 2 % " + zone2);
        Log.d(Constants.TAG, "Zone 3 % " + zone3);
        Log.d(Constants.TAG, "Zone 4 % " + zone4);
        Log.d(Constants.TAG, "Zone 5 % " + zone5);

        // reset local state matrix to all zeroes
        for (int i=0;i<25;i++) {
            synchronized (mutex) {
                local_led_state[i] = 0;
            }
        }

        setLedColumn(0,zone1 , 4);
        setLedColumn(1,zone2 , 3);
        setLedColumn(2,zone3 , 2);
        setLedColumn(3,zone4 , 1);
        setLedColumn(4, zone5, 0);

        Log.d(Constants.TAG, "local_led_state:");
        for (int r=0;r<5;r++) {
            synchronized (mutex) {
                Log.d(Constants.TAG, "R" + r + ": " + local_led_state[(r * 5) + 0] + "   " + local_led_state[(r * 5) + 1] + "   " + local_led_state[(r * 5) + 2] + "   " + local_led_state[(r * 5) + 3] + "   " + local_led_state[(r * 5) + 4]);
            }
        }
    }

    private void setLedColumn(int column, int percentage, int bit) {
        int leds_lit = (int) (percentage / 20);
        boolean partial = false;
        int remainder = percentage % 20;
        Log.d(Constants.TAG,"column: "+column+" percentage: "+percentage+" remainder: "+remainder);
        if (remainder > 10) {
            partial = true;
        }
        Log.d(Constants.TAG,"column: "+column+" leds_lit: "+leds_lit+" bit: "+bit+" partial: "+partial);
        int j=0;
        // starting at the bottom row and in the specified column set the local LED state matrix data
        int led=20+column;
        while(j<leds_lit) {
            synchronized (mutex) {
                local_led_state[led] = 2;
            }
            j++;
            led = led - 5;
        }
        if (partial && led >= 0) {
            synchronized (mutex) {
                local_led_state[led] = 1;
            }
        }
    }

    private void update_HrmStats(int hr_measurement) {
        Settings settings = Settings.getInstance();
        total_hrm_measurements++;
        if (hr_measurement <= settings.getHrm_zone_1_ceiling()) {
            hrm_histogram[0] = hrm_histogram[0] + 1;
            return;
        }
        if (hr_measurement <= settings.getHrm_zone_2_ceiling()) {
            hrm_histogram[1] = hrm_histogram[1] + 1;
            return;
        }
        if (hr_measurement <= settings.getHrm_zone_3_ceiling()) {
            hrm_histogram[2] = hrm_histogram[2] + 1;
            return;
        }
        if (hr_measurement <= settings.getHrm_zone_4_ceiling()) {
            hrm_histogram[3] = hrm_histogram[3] + 1;
            return;
        }
        hrm_histogram[4] = hrm_histogram[4] + 1;
        return;
    }

    private boolean is16BitHrMeasurement(byte value) {
        return ((value & 0x01) != 0);
    }

    private void showMsg(final String msg) {
        Log.d(Constants.TAG, msg);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) HrmActivity.this.findViewById(R.id.message)).setText(Html.fromHtml(msg));
            }
        });
    }

    private void updateBpm(final int bpm) {
        final String bpm_msg = Integer.toString(bpm)+" BPM";
        Log.d(Constants.TAG, bpm_msg);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) HrmActivity.this.findViewById(R.id.bpm)).setText(bpm_msg);
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
    public void run() {
        microbit_display_refresh_running = true;
        int counter = 0;
        while (microbit_display_refresh_running) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            counter++;
            if (counter < 10) {
                synchronized (mutex) {
                    //update display from local_led_state
                    updateUiGrid();
                    updateMicrobitLedState();
                    // update microbit display from local_led_state
                    Log.d(Constants.TAG, "microbit_led_state:");
                    Log.d(Constants.TAG, led_matrix_state[0] + "   " + led_matrix_state[1] + "   " + led_matrix_state[2] + "   " + led_matrix_state[3] + "   " + led_matrix_state[4]);
                    showHistogramOnMicrobit();
                }
            } else {
                counter = 0;
                showHeartRateOnMicrobit();
            }
        }
    }


    //    Octet 0, LED Row 1: bit4 bit3 bit2 bit1 bit0
    //    Octet 1, LED Row 2: bit4 bit3 bit2 bit1 bit0
    //    Octet 2, LED Row 3: bit4 bit3 bit2 bit1 bit0
    //    Octet 3, LED Row 4: bit4 bit3 bit2 bit1 bit0
    //    Octet 4, LED Row 5: bit4 bit3 bit2 bit1 bit0

    //  0   1   2   3   4
    //  5   6   7   8   9
    // 10  11  12  13  14
    // 15  16  17  18  19
    // 20  21  22  23  24

    private void updateMicrobitLedState() {

        int microbit_row=0;
        int row_bit=4;
        byte bit_value=0;
        for (int i=0;i<25;i++) {
            bit_value = (byte) (1 << row_bit);
            switch (local_led_state[i]) {
                case 2:
                    led_matrix_state[microbit_row] = (byte) (led_matrix_state[microbit_row] | (1 << row_bit));
                    break;
                case 1:
                    if (partial_on) {
                        led_matrix_state[microbit_row] = (byte) (led_matrix_state[microbit_row] | (1 << row_bit));
                    } else {
                        led_matrix_state[microbit_row] = (byte) (led_matrix_state[microbit_row] & ~(1 << row_bit));
                    }
                    break;
                case 0:
                    led_matrix_state[microbit_row] = (byte) (led_matrix_state[microbit_row] & ~(1 << row_bit));
                    break;
                default:
                    Log.d(Constants.TAG,"Invalid value in local_led_state matrix: cell "+i+" value "+local_led_state[i]);
                    break;
            }
            row_bit--;
            if (row_bit < 0) {
                row_bit = 4;
                microbit_row++;
            }
        }
    }

    private void updateUiGrid() {
        GridLayout grid = (GridLayout) HrmActivity.this.findViewById(R.id.grid);
        int count = grid.getChildCount();
        int local_led_index=0;
        for (int i = 0; i < count; i++) {
            if (i % 6 == 0) {
                // then this cell contains a text label
                continue;
            }
            final View child = grid.getChildAt(i);
            int bg_colour = 0;
            switch (local_led_state[local_led_index]) {
                case 2:
                    bg_colour = Color.RED;
                    break;
                case 1:
                    if (partial_on) {
                        bg_colour = Color.RED;
                    } else {
                        bg_colour = Color.parseColor("#C0C0C0");
                    }
                    break;
                case 0:
                    bg_colour = Color.parseColor("#C0C0C0");
                    break;
                default:
                    Log.d(Constants.TAG,"Invalid value in local_led_state matrix: cell "+local_led_index+" value "+local_led_state[local_led_index]);
                    break;
            }
            final int cell_colour = bg_colour;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    child.setBackgroundColor(cell_colour);
                }
            });
            local_led_index++;
        }
        final GridLayout pc_grid = (GridLayout) HrmActivity.this.findViewById(R.id.percentages);
        for (int i=1;i<6;i++) {
            final TextView pc = (TextView) pc_grid.getChildAt(i);
            final int pc_value = hrm_histogram_pc[i-1];
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pc.setText(pc_value+"%");
                }
            });
        }
        partial_on = !partial_on;
    }
}