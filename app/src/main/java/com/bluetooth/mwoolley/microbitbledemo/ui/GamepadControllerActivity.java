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
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bluetooth.mwoolley.microbitbledemo.AudioToneMaker;
import com.bluetooth.mwoolley.microbitbledemo.Constants;
import com.bluetooth.mwoolley.microbitbledemo.MicroBit;
import com.bluetooth.mwoolley.microbitbledemo.MicroBitEvent;
import com.bluetooth.mwoolley.microbitbledemo.R;
import com.bluetooth.mwoolley.microbitbledemo.Settings;
import com.bluetooth.mwoolley.microbitbledemo.Utility;
import com.bluetooth.mwoolley.microbitbledemo.bluetooth.BleAdapterService;
import com.bluetooth.mwoolley.microbitbledemo.bluetooth.ConnectionStatusListener;

public class GamepadControllerActivity extends AppCompatActivity implements ConnectionStatusListener, OnTouchListener {

    private BleAdapterService bluetooth_le_adapter;
    private Vibrator vibrator;
    private boolean has_vibrator;
    private ImageView gamepad;
    private ImageView gamepad_mask;

    private int pad_1_up_colour;
    private int pad_1_down_colour;
    private int pad_1_left_colour;
    private int pad_1_right_colour;
    private int pad_2_up_colour;
    private int pad_2_down_colour;
    private int pad_2_left_colour;
    private int pad_2_right_colour;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.d(Constants.TAG, "onServiceConnected");
            bluetooth_le_adapter = ((BleAdapterService.LocalBinder) service).getService();
            bluetooth_le_adapter.setActivityHandler(mMessageHandler);
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
        setContentView(R.layout.activity_gamepad);
//        getSupportActionBar().setTitle(R.string.screen_title_controller);

        gamepad_mask = (ImageView) GamepadControllerActivity.this.findViewById(R.id.gamepad_mask);
        gamepad = (ImageView) GamepadControllerActivity.this.findViewById(R.id.gamepad);
        gamepad.setOnTouchListener(this);

        pad_1_up_colour = getResources().getColor(R.color.pad_1_up_colour);
        pad_1_down_colour = getResources().getColor(R.color.pad_1_down_colour);
        pad_1_left_colour = getResources().getColor(R.color.pad_1_left_colour);
        pad_1_right_colour = getResources().getColor(R.color.pad_1_right_colour);

        pad_2_up_colour = getResources().getColor(R.color.pad_2_up_colour);
        pad_2_down_colour = getResources().getColor(R.color.pad_2_down_colour);
        pad_2_left_colour = getResources().getColor(R.color.pad_2_left_colour);
        pad_2_right_colour = getResources().getColor(R.color.pad_2_right_colour);

        // read intent data
        final Intent intent = getIntent();
        MicroBit.getInstance().setConnection_status_listener(this);

        // connect to the Bluetooth smart service
        Intent gattServiceIntent = new Intent(this, BleAdapterService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        has_vibrator = vibrator.hasVibrator();
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
        getMenuInflater().inflate(R.menu.menu_controller, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.menu_gamepad_settings) {
            Intent intent = new Intent(GamepadControllerActivity.this, GamepadControllerSettingsActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.menu_controller_help) {
            Intent intent = new Intent(GamepadControllerActivity.this, HelpActivity.class);
            intent.putExtra(Constants.URI, Constants.CONTROLLER_HELP);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(Constants.TAG, "onActivityResult");
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
                    break;
                case BleAdapterService.MESSAGE:
                    bundle = msg.getData();
                    String text = bundle.getString(BleAdapterService.PARCEL_TEXT);
                    showMsg(text);
            }
        }
    };

    private void showMsg(final String msg) {
        Log.d(Constants.TAG, msg);
        // was sometimes getting android.view.WindowManager$BadTokenException: Unable to add window. This is an attempt to avoid trying to show a dialog when not in a suitable state
        if (!GamepadControllerActivity.this.hasWindowFocus()) {
            Log.d(Constants.TAG, "Activity not ready yet");
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final AlertDialog.Builder builder = new AlertDialog.Builder(GamepadControllerActivity.this);
                builder.setTitle("");
                builder.setMessage(msg);
                builder.setPositiveButton(android.R.string.ok, null);
                builder.show();
            }
        });
    }

    @Override
    public void connectionStatusChanged(boolean connected) {
        if (!connected) {
            showMsg("Disconnected");
        }
    }

    @Override
    public void serviceDiscoveryStatusChanged(boolean new_state) {
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (!MicroBit.getInstance().isMicrobit_connected()) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                showMsg("Currently disconnected - go back and connect again");
            }
            return true;
        }
        Log.d(Constants.TAG, "onTouch - " + event.actionToString((event.getAction())));
        Log.d(Constants.TAG, "onTouch action - " + event.getAction());
        Log.d(Constants.TAG, "onTouch action masked - " + event.getActionMasked());
        int pointer_index = 0;
        if (event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN || event.getActionMasked() == MotionEvent.ACTION_POINTER_UP) {
            pointer_index = event.getActionIndex();
        }

        final int evX = (int) event.getX(pointer_index);
        final int evY = (int) event.getY(pointer_index);

        int up_or_down=0; // 1 = down, 2 = up

        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_UP
                || event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN || event.getActionMasked() == MotionEvent.ACTION_POINTER_UP) {
            // get the colour of the region touched from the gamepad mask and use this to figure out which pad was pressed
            Log.d(Constants.TAG, "onTouch - determining pad touched at "+evX+","+evY);
            int touchColor = getHotspotColor (R.id.gamepad_mask, evX, evY);
            int tolerance = 25;
            int pad_no=-1;
            if (closeMatch(pad_1_up_colour, touchColor, tolerance)) {
                pad_no = Constants.DPAD_1_BUTTON_UP_VIEW_INX;
            }
            if (closeMatch(pad_1_down_colour, touchColor, tolerance)) {
                pad_no = Constants.DPAD_1_BUTTON_DOWN_VIEW_INX;
            }
            if (closeMatch(pad_1_left_colour, touchColor, tolerance)) {
                pad_no = Constants.DPAD_1_BUTTON_LEFT_VIEW_INX;
            }
            if (closeMatch(pad_1_right_colour, touchColor, tolerance)) {
                pad_no = Constants.DPAD_1_BUTTON_RIGHT_VIEW_INX;
            }
            if (closeMatch(pad_2_up_colour, touchColor, tolerance)) {
                pad_no = Constants.DPAD_2_BUTTON_UP_VIEW_INX;
            }
            if (closeMatch(pad_2_down_colour, touchColor, tolerance)) {
                pad_no = Constants.DPAD_2_BUTTON_DOWN_VIEW_INX;
            }
            if (closeMatch(pad_2_left_colour, touchColor, tolerance)) {
                pad_no = Constants.DPAD_2_BUTTON_LEFT_VIEW_INX;
            }
            if (closeMatch(pad_2_right_colour, touchColor, tolerance)) {
                pad_no = Constants.DPAD_2_BUTTON_RIGHT_VIEW_INX;
            }
            Log.d(Constants.TAG, "Touched pad " + pad_no);
            if (pad_no > -1) {
                if (event.getAction() == MotionEvent.ACTION_DOWN || event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN ) {
                    up_or_down = Constants.PAD_DOWN;
                } else {
                    up_or_down = Constants.PAD_UP;
                }
                byte[] event_data = makeEvent(up_or_down, pad_no);
                if (event_data == null) {
                    return true;
                }
                Log.d(Constants.TAG,"Writing event bytes:"+Utility.byteArrayAsHexString(event_data));
                bluetooth_le_adapter.writeCharacteristic(Utility.normaliseUUID(BleAdapterService.EVENTSERVICE_SERVICE_UUID), Utility.normaliseUUID(BleAdapterService.CLIENTEVENT_CHARACTERISTIC_UUID), event_data);
                if (event.getAction() == MotionEvent.ACTION_DOWN || event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN) {
                    if (has_vibrator) {
                        vibrator.vibrate(250);
                    } else {
                        AudioToneMaker.getInstance().playTone(getDtmfTone(pad_no));
                    }
                }
                return true;
            }
        }
        return false;
    }

    private int getDtmfTone(int pad_no) {
        switch (pad_no) {
            case Constants.DPAD_1_BUTTON_UP_VIEW_INX: return ToneGenerator.TONE_DTMF_1;
            case Constants.DPAD_1_BUTTON_LEFT_VIEW_INX: return ToneGenerator.TONE_DTMF_2;
            case Constants.DPAD_1_BUTTON_RIGHT_VIEW_INX: return ToneGenerator.TONE_DTMF_3;
            case Constants.DPAD_1_BUTTON_DOWN_VIEW_INX: return ToneGenerator.TONE_DTMF_A;
            case Constants.DPAD_2_BUTTON_UP_VIEW_INX: return ToneGenerator.TONE_DTMF_7;
            case Constants.DPAD_2_BUTTON_LEFT_VIEW_INX: return ToneGenerator.TONE_DTMF_8;
            case Constants.DPAD_2_BUTTON_RIGHT_VIEW_INX: return ToneGenerator.TONE_DTMF_9;
            case Constants.DPAD_2_BUTTON_DOWN_VIEW_INX: return ToneGenerator.TONE_DTMF_A;
            default:
                Log.d(Constants.TAG,"Error: unrecognised pad no");
                return ToneGenerator.TONE_DTMF_1;
        }
    }


    // see https://blahti.wordpress.com/2012/06/26/images-with-clickable-areas/
    public int getHotspotColor (int hotspotId, int x, int y) {
        ImageView img = (ImageView) findViewById(hotspotId);
        img.setDrawingCacheEnabled(true);
        Bitmap hotspots = Bitmap.createBitmap(img.getDrawingCache());
        img.setDrawingCacheEnabled(false);
        if (x >= 0 && y >= 0 && x <= hotspots.getWidth() && y <= hotspots.getHeight()) {
            return hotspots.getPixel(x, y);
        } else {
            return 0;
        }
    }

    // see https://blahti.wordpress.com/2012/06/26/images-with-clickable-areas/
    public boolean closeMatch (int color1, int color2, int tolerance) {
        int red_diff = (int) Math.abs (Color.red(color1) - Color.red (color2));
        int green_diff = (int) Math.abs (Color.green(color1) - Color.green(color2));
        int blue_diff = (int) Math.abs (Color.blue(color1) - Color.blue(color2));
        if (red_diff > tolerance ) return false;
        if (green_diff > tolerance ) return false;
        if (blue_diff > tolerance ) return false;
        return true;
    }

    private byte[] makeEvent(int action, int pad_number) {
//        struct event {
//            uint16 event_type;
//            uint16 event_value;
//        };

        MicroBitEvent mb_event;
        short event_value;
        Settings settings = Settings.getInstance();

        byte [] event_bytes = new byte[4];
        if (action == Constants.PAD_DOWN) {
            switch (pad_number) {
                case Constants.DPAD_1_BUTTON_UP_VIEW_INX: event_value = settings.getMes_dpad_1_button_up_on();
                    Log.d(Constants.TAG, "PAD_DOWN - DPAD_1_BUTTON_UP_VIEW_INX");
                    break;
                case Constants.DPAD_1_BUTTON_LEFT_VIEW_INX: event_value = settings.getMes_dpad_1_button_left_on();
                    Log.d(Constants.TAG, "PAD_DOWN - DPAD_1_BUTTON_LEFT_VIEW_INX");
                    break;
                case Constants.DPAD_1_BUTTON_RIGHT_VIEW_INX: event_value = settings.getMes_dpad_1_button_right_on();
                    Log.d(Constants.TAG, "PAD_DOWN - DPAD_1_BUTTON_RIGHT_VIEW_INX");
                    break;
                case Constants.DPAD_1_BUTTON_DOWN_VIEW_INX: event_value = settings.getMes_dpad_1_button_down_on();
                    Log.d(Constants.TAG, "PAD_DOWN - DPAD_1_BUTTON_DOWN_VIEW_INX");
                    break;
                case Constants.DPAD_2_BUTTON_UP_VIEW_INX: event_value = settings.getMes_dpad_2_button_up_on();
                    Log.d(Constants.TAG, "PAD_DOWN - DPAD_2_BUTTON_UP_VIEW_INX");
                    break;
                case Constants.DPAD_2_BUTTON_LEFT_VIEW_INX: event_value =settings.getMes_dpad_2_button_left_on();
                    Log.d(Constants.TAG, "PAD_DOWN - DPAD_2_BUTTON_LEFT_VIEW_INX");
                    break;
                case Constants.DPAD_2_BUTTON_RIGHT_VIEW_INX: event_value = settings.getMes_dpad_2_button_right_on();
                    Log.d(Constants.TAG, "PAD_DOWN - DPAD_2_BUTTON_RIGHT_VIEW_INX");
                    break;
                case Constants.DPAD_2_BUTTON_DOWN_VIEW_INX: event_value = settings.getMes_dpad_2_button_down_on();
                    Log.d(Constants.TAG, "PAD_DOWN - DPAD_2_BUTTON_DOWN_VIEW_INX");
                    break;
                default:
                    Log.d(Constants.TAG,"Error: unrecognised touch event / view");
                    return null;
            }
        } else {
            switch (pad_number) {
                case Constants.DPAD_1_BUTTON_UP_VIEW_INX: event_value = settings.getMes_dpad_1_button_up_off();
                    Log.d(Constants.TAG, "PAD_UP - DPAD_1_BUTTON_UP_VIEW_INX");
                    break;
                case Constants.DPAD_1_BUTTON_LEFT_VIEW_INX: event_value = settings.getMes_dpad_1_button_left_off();
                    Log.d(Constants.TAG, "PAD_UP - DPAD_1_BUTTON_LEFT_VIEW_INX");
                    break;
                case Constants.DPAD_1_BUTTON_RIGHT_VIEW_INX: event_value = settings.getMes_dpad_1_button_right_off();
                    Log.d(Constants.TAG, "PAD_UP - DPAD_1_BUTTON_RIGHT_VIEW_INX");
                    break;
                case Constants.DPAD_1_BUTTON_DOWN_VIEW_INX: event_value = settings.getMes_dpad_1_button_down_off();
                    Log.d(Constants.TAG, "PAD_UP - DPAD_1_BUTTON_DOWN_VIEW_INX");
                    break;
                case Constants.DPAD_2_BUTTON_UP_VIEW_INX: event_value = settings.getMes_dpad_2_button_up_off();
                    Log.d(Constants.TAG, "PAD_UP - DPAD_2_BUTTON_UP_VIEW_INX");
                    break;
                case Constants.DPAD_2_BUTTON_LEFT_VIEW_INX: event_value = settings.getMes_dpad_2_button_left_off();
                    Log.d(Constants.TAG, "PAD_UP - DPAD_2_BUTTON_LEFT_VIEW_INX");
                    break;
                case Constants.DPAD_2_BUTTON_RIGHT_VIEW_INX: event_value = settings.getMes_dpad_2_button_right_off();
                    Log.d(Constants.TAG, "PAD_UP - DPAD_2_BUTTON_RIGHT_VIEW_INX");
                    break;
                case Constants.DPAD_2_BUTTON_DOWN_VIEW_INX: event_value = settings.getMes_dpad_2_button_down_off();
                    Log.d(Constants.TAG, "PAD_UP - DPAD_2_BUTTON_DOWN_VIEW_INX");
                    break;
                default:
                    Log.d(Constants.TAG,"Error: unrecognised touch event / view");
                    return null;
            }
        }
        mb_event = new MicroBitEvent(settings.getMes_dpad_controller(),event_value);
        event_bytes = mb_event.getEventBytesForBle();
        return event_bytes;
    }

}