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
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;

import com.bluetooth.mwoolley.microbitbledemo.bluetooth.BleAdapterService;
import com.bluetooth.mwoolley.microbitbledemo.bluetooth.ConnectionStatusListener;
import com.bluetooth.mwoolley.microbitbledemo.Constants;
import com.bluetooth.mwoolley.microbitbledemo.MicroBit;
import com.bluetooth.mwoolley.microbitbledemo.R;
import com.bluetooth.mwoolley.microbitbledemo.Settings;
import com.bluetooth.mwoolley.microbitbledemo.Utility;

import java.io.UnsupportedEncodingException;

public class LEDsActivity extends AppCompatActivity implements ConnectionStatusListener, OnTouchListener {

    private BleAdapterService bluetooth_le_adapter;

    private short scrolling_delay;
    //    Octet 0, LED Row 1: bit4 bit3 bit2 bit1 bit0
    //    Octet 1, LED Row 2: bit4 bit3 bit2 bit1 bit0
    //    Octet 2, LED Row 3: bit4 bit3 bit2 bit1 bit0
    //    Octet 3, LED Row 4: bit4 bit3 bit2 bit1 bit0
    //    Octet 4, LED Row 5: bit4 bit3 bit2 bit1 bit0
    private byte[] led_matrix_state;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.d(Constants.TAG, "onServiceConnected");
            bluetooth_le_adapter = ((BleAdapterService.LocalBinder) service).getService();
            bluetooth_le_adapter.setActivityHandler(mMessageHandler);

            if (bluetooth_le_adapter.readCharacteristic(Utility.normaliseUUID(BleAdapterService.LEDSERVICE_SERVICE_UUID), Utility.normaliseUUID(BleAdapterService.LEDMATRIXSTATE_CHARACTERISTIC_UUID))) {
                showMsg(Utility.htmlColorGreen("Reading LED matrix state"));
            } else {
                showMsg(Utility.htmlColorRed("Failed to readLED matrix state"));
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
        setContentView(R.layout.activity_leds);
        getSupportActionBar().setTitle(R.string.screen_title_LEDs);

        GridLayout grid = (GridLayout) LEDsActivity.this.findViewById(R.id.grid);
        int count = grid.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = grid.getChildAt(i);
            child.setOnTouchListener(this);
        }

        // read intent data
        final Intent intent = getIntent();
        MicroBit.getInstance().setConnection_status_listener(this);

        // connect to the Bluetooth smart service
        Intent gattServiceIntent = new Intent(this, BleAdapterService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        Log.d(Constants.TAG, "onDestroy");
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
        getMenuInflater().inflate(R.menu.menu_leds, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.menu_leds_settings) {
            scrolling_delay = Settings.getInstance().getScrolling_delay();
            Intent intent = new Intent(LEDsActivity.this, LEDsSettingsActivity.class);
            startActivityForResult(intent, LEDsSettingsActivity.START_LEDS_SETTINGS);
            return true;
        }
        if (id == R.id.menu_leds_help) {
            Intent intent = new Intent(LEDsActivity.this, HelpActivity.class);
            intent.putExtra(Constants.URI, Constants.LEDS_HELP);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(Constants.TAG, "onActivityResult");
        if (requestCode == LEDsSettingsActivity.START_LEDS_SETTINGS) {
            if (resultCode == RESULT_OK) {
                Log.d(Constants.TAG, "onActivityResult RESULT_OK");
                if (Settings.getInstance().getScrolling_delay() != scrolling_delay) {
                    Log.d(Constants.TAG, "scrolling delay has changed to " + Settings.getInstance().getScrolling_delay());

                    byte[] bytes = new byte[1];
//                    bytes[0] = (byte)(Settings.getInstance().getScrolling_delay() & 0xff);
                    bytes = Utility.leBytesFromShort(Settings.getInstance().getScrolling_delay());
                    Log.d(Constants.TAG, Utility.byteArrayAsHexString(bytes));
                    bluetooth_le_adapter.writeCharacteristic(Utility.normaliseUUID(BleAdapterService.LEDSERVICE_SERVICE_UUID), Utility.normaliseUUID(BleAdapterService.SCROLLINGDELAY_CHARACTERISTIC_UUID), bytes);
                }
            } else {
                Log.d(Constants.TAG, "onActivityResult NOT RESULT_OK");
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
                case BleAdapterService.GATT_CHARACTERISTIC_READ:
                    Log.d(Constants.TAG, "Handler received characteristic read result");
                    bundle = msg.getData();
                    service_uuid = bundle.getString(BleAdapterService.PARCEL_SERVICE_UUID);
                    characteristic_uuid = bundle.getString(BleAdapterService.PARCEL_CHARACTERISTIC_UUID);
                    b = bundle.getByteArray(BleAdapterService.PARCEL_VALUE);
                    Log.d(Constants.TAG, "characteristic " + characteristic_uuid + " of service " + service_uuid + " read OK");
                    Log.d(Constants.TAG, "Value=" + Utility.byteArrayAsHexString(b));
                    if (characteristic_uuid.equalsIgnoreCase(Utility.normaliseUUID(BleAdapterService.LEDMATRIXSTATE_CHARACTERISTIC_UUID))) {
                        if (b.length > 4) {
                            led_matrix_state = b;
                            Log.d(Constants.TAG, "LED matrix state=" + Utility.byteArrayAsHexString(b));
                            setUiFromMatrixState(led_matrix_state);
                        }
                        // now read the Scrolling Delay and store in the Settings singleton
                        bluetooth_le_adapter.readCharacteristic(Utility.normaliseUUID(BleAdapterService.LEDSERVICE_SERVICE_UUID), Utility.normaliseUUID(BleAdapterService.SCROLLINGDELAY_CHARACTERISTIC_UUID));
                    } else {
                        if (characteristic_uuid.equalsIgnoreCase(Utility.normaliseUUID(BleAdapterService.SCROLLINGDELAY_CHARACTERISTIC_UUID))) {
                            if (b.length > 1) {
                                scrolling_delay = Utility.shortFromLittleEndianBytes(b);
                                Log.d(Constants.TAG,"Read Scrolling Delay from micro:bit="+scrolling_delay);
                                Settings.getInstance().setScrolling_delay(scrolling_delay);
                            }
                        }
                    }
                    showMsg(Utility.htmlColorGreen("Ready"));
                    break;
                case BleAdapterService.GATT_CHARACTERISTIC_WRITTEN:
                    Log.d(Constants.TAG, "Handler received characteristic written result");
                    bundle = msg.getData();
                    service_uuid = bundle.getString(BleAdapterService.PARCEL_SERVICE_UUID);
                    characteristic_uuid = bundle.getString(BleAdapterService.PARCEL_CHARACTERISTIC_UUID);
                    Log.d(Constants.TAG, "characteristic " + characteristic_uuid + " of service " + service_uuid + " written OK");
                    showMsg(Utility.htmlColorGreen("Ready"));
                    break;
                case BleAdapterService.MESSAGE:
                    bundle = msg.getData();
                    String text = bundle.getString(BleAdapterService.PARCEL_TEXT);
                    showMsg(Utility.htmlColorRed(text));
            }
        }
    };

    private void setUiFromMatrixState(byte[] matrix_state) {
        GridLayout grid = (GridLayout) LEDsActivity.this.findViewById(R.id.grid);
        int count = grid.getChildCount();
        int display_row = 0;
        int led_in_row = 4;
        for (int i = 0; i < count; i++) {
            Log.d(Constants.TAG, "display_row=" + display_row + ",led_in_row=" + led_in_row);
            View child = grid.getChildAt(i);
            if ((matrix_state[display_row] & (1 << led_in_row)) != 0) {
                child.setBackgroundColor(Color.RED);
            } else {
                child.setBackgroundColor(Color.parseColor("#C0C0C0"));
            }
            led_in_row = led_in_row - 1;
            if (led_in_row < 0) {
                led_in_row = 4;
                display_row++;
            }
        }
    }

    private void showMsg(final String msg) {
        Log.d(Constants.TAG, msg);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) LEDsActivity.this.findViewById(R.id.message)).setText(Html.fromHtml(msg));
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

    public void onSetDisplay(View view) {
        Log.d(Constants.TAG, "onSetDisplay");
        bluetooth_le_adapter.writeCharacteristic(Utility.normaliseUUID(BleAdapterService.LEDSERVICE_SERVICE_UUID), Utility.normaliseUUID(BleAdapterService.LEDMATRIXSTATE_CHARACTERISTIC_UUID), led_matrix_state);
    }

    public void onSendText(View view) {
        Log.d(Constants.TAG, "onSendText");
        EditText text = (EditText) LEDsActivity.this.findViewById(R.id.display_text);
        Log.d(Constants.TAG, "onSendText: " + text.getText().toString());
        try {
            byte[] utf8_bytes = text.getText().toString().getBytes("UTF-8");
            Log.d(Constants.TAG, "UTF8 bytes: 0x" + Utility.byteArrayAsHexString(utf8_bytes));
            bluetooth_le_adapter.writeCharacteristic(Utility.normaliseUUID(BleAdapterService.LEDSERVICE_SERVICE_UUID), Utility.normaliseUUID(BleAdapterService.LEDTEXT_CHARACTERISTIC_UUID), utf8_bytes);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            showMsg("Unable to convert text to UTF8 bytes");
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.d(Constants.TAG, "onTouch - " + event.actionToString((event.getAction())));
        if (led_matrix_state == null) {
            Log.d(Constants.TAG, "onTouch - LED state array has not yet been initialised so ignoring touch");
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            GridLayout grid = (GridLayout) LEDsActivity.this.findViewById(R.id.grid);
            int count = grid.getChildCount();
            int display_row = 0;
            int led_in_row = 4;
            for (int i = 0; i < count; i++) {
                View child = grid.getChildAt(i);
                if (child == v) {
                    Log.d(Constants.TAG,"Touched row "+display_row+", LED "+led_in_row);
                    if ((led_matrix_state[display_row] & (1 << led_in_row)) != 0) {
                        child.setBackgroundColor(Color.parseColor("#C0C0C0"));
                        led_matrix_state[display_row] = (byte) (led_matrix_state[display_row] & ~(1 << led_in_row));
                    } else {
                        child.setBackgroundColor(Color.RED);
                        led_matrix_state[display_row] = (byte) (led_matrix_state[display_row] | (1 << led_in_row));
                    }
                    return true;
                }
                led_in_row = led_in_row - 1;
                if (led_in_row < 0) {
                    led_in_row = 4;
                    display_row++;
                }
            }
            return true;
        }
        return false;
    }

}