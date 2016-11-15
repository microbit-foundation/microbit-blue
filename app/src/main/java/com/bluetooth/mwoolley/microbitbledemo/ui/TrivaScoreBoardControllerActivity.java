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
import android.content.SharedPreferences;
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
import android.text.Html;
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

public class TrivaScoreBoardControllerActivity extends AppCompatActivity implements ConnectionStatusListener {


    private static final String TRIVIA_BOARD_STATE_FILE = "com.bluetooth.mwoolley.microbitbledemo.trivia_board_state_file";
    private static final String BLUE_STATE = "BLUE_STATE";
    private static final String PINK_STATE = "PINK_STATE";
    private static final String YELLOW_STATE = "YELLOW_STATE";
    private static final String BROWN_STATE = "BROWN_STATE";
    private static final String GREEN_STATE = "GREEN_STATE";
    private static final String ORANGE_STATE = "ORANGE_STATE";
    private int blue_state=0;
    private int pink_state=0;
    private int yellow_state=0;
    private int brown_state=0;
    private int green_state=0;
    private int orange_state=0;

    private BleAdapterService bluetooth_le_adapter;
    private boolean exiting = false;
    private boolean notifications_on = false;

    // micro:bit event codes:
    //    #define MES_TRIVIA                 1300 = 0x1405 (Little Endian)
    //
    //            #define MES_TRIVIA_BLUE            1
    //            #define MES_TRIVIA_PINK            2
    //            #define MES_TRIVIA_YELLOW          3
    //
    //            #define MES_TRIVIA_BROWN           4
    //            #define MES_TRIVIA_GREEN           5
    //            #define MES_TRIVIA_ORANGE          6

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.d(Constants.TAG, "onServiceConnected");
            bluetooth_le_adapter = ((BleAdapterService.LocalBinder) service).getService();
            bluetooth_le_adapter.setActivityHandler(mMessageHandler);
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
        restore(this);
        setContentView(R.layout.activity_trivia_score_board);
        if (blue_state == 1) {
            findViewById(R.id.star_blue).setVisibility(View.VISIBLE);
        }
        if (pink_state == 1) {
            findViewById(R.id.star_pink).setVisibility(View.VISIBLE);
        }
        if (yellow_state == 1) {
            findViewById(R.id.star_yellow).setVisibility(View.VISIBLE);
        }
        if (brown_state == 1) {
            findViewById(R.id.star_brown).setVisibility(View.VISIBLE);
        }
        if (green_state == 1) {
            findViewById(R.id.star_green).setVisibility(View.VISIBLE);
        }
        if (orange_state == 1) {
            findViewById(R.id.star_orange).setVisibility(View.VISIBLE);
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
        if (MicroBit.getInstance().isMicrobit_connected() && notifications_on) {
            bluetooth_le_adapter.setNotificationsState(Utility.normaliseUUID(BleAdapterService.EVENTSERVICE_SERVICE_UUID), Utility.normaliseUUID(BleAdapterService.MICROBITEVENT_CHARACTERISTIC_UUID), false);
        }
        try {
            // may already have unbound. No API to check state so....
            unbindService(mServiceConnection);
        } catch (Exception e) {
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_trivia, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.menu_trivia_new_game) {
            blue_state = 0;
            findViewById(R.id.star_blue).setVisibility(View.INVISIBLE);
            pink_state = 0;
            findViewById(R.id.star_pink).setVisibility(View.INVISIBLE);
            yellow_state = 0;
            findViewById(R.id.star_yellow).setVisibility(View.INVISIBLE);
            brown_state = 0;
            findViewById(R.id.star_brown).setVisibility(View.INVISIBLE);
            green_state = 0;
            findViewById(R.id.star_green).setVisibility(View.INVISIBLE);
            orange_state = 0;
            findViewById(R.id.star_orange).setVisibility(View.INVISIBLE);
            save(this);
            return true;
        }
        if (id == R.id.menu_trivia_help) {
            Intent intent = new Intent(TrivaScoreBoardControllerActivity.this, HelpActivity.class);
            intent.putExtra(Constants.URI, Constants.TRIVIA_HELP);
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
                        showMsg(Utility.htmlColorGreen("Trivia score event notifications ON"));
                        notifications_on = true;
                        bluetooth_le_adapter.writeCharacteristic(Utility.normaliseUUID(BleAdapterService.EVENTSERVICE_SERVICE_UUID), Utility.normaliseUUID(BleAdapterService.CLIENTREQUIREMENTS_CHARACTERISTIC_UUID), Utility.leBytesFromTwoShorts(Constants.MICROBIT_EVENT_TYPE_TRIVIA, Constants.MICROBIT_EVENT_VALUE_ANY));
                    } else {
                        showMsg(Utility.htmlColorGreen("Trivia score event notifications OFF"));
                        notifications_on = false;
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
                        MicroBitEvent mb_event = new MicroBitEvent(b);
                        Log.d(Constants.TAG, "Trivia update received: event=" + mb_event.getEvent_type() + " value=" + mb_event.getEvent_value());
                        if (mb_event.getEvent_type() == Constants.MICROBIT_EVENT_TYPE_TRIVIA) {
                            setScoreBoard(mb_event.getEvent_value());
                        }
                    }
                    break;
                case BleAdapterService.MESSAGE:
                    bundle = msg.getData();
                    String text = bundle.getString(BleAdapterService.PARCEL_TEXT);
                    showMsg(text);
            }
        }
    };

    public void onStarTouched(View view) {
        Log.d(Constants.TAG, "onStarTouched");
        view.setVisibility(View.INVISIBLE);
        switch (view.getId()) {
            case R.id.star_blue:
                blue_state = 0;
                break;
            case R.id.star_pink:
                pink_state = 0;
                break;
            case R.id.star_yellow:
                yellow_state = 0;
                break;
            case R.id.star_brown:
                brown_state = 0;
                break;
            case R.id.star_green:
                green_state = 0;
                break;
            case R.id.star_orange:
                orange_state = 0;
                break;
        }
        save(this);
    }

    public void onLabelTouched(View view) {
        Log.d(Constants.TAG, "onLabelTouched");
        if (view.getId() == R.id.lbl_geography) {
            View star = findViewById(R.id.star_blue);
            star.setVisibility(View.VISIBLE);
            blue_state = 1;
            save(this);
            return;
        }
        if (view.getId() == R.id.lbl_entertainment) {
            View star = findViewById(R.id.star_pink);
            star.setVisibility(View.VISIBLE);
            pink_state = 1;
            save(this);
            return;
        }
        if (view.getId() == R.id.lbl_history) {
            View star = findViewById(R.id.star_yellow);
            star.setVisibility(View.VISIBLE);
            yellow_state = 1;
            save(this);
            return;
        }
        if (view.getId() == R.id.lbl_art) {
            View star = findViewById(R.id.star_brown);
            star.setVisibility(View.VISIBLE);
            brown_state = 1;
            save(this);
            return;
        }
        if (view.getId() == R.id.lbl_science) {
            View star = findViewById(R.id.star_green);
            star.setVisibility(View.VISIBLE);
            green_state = 1;
            save(this);
            return;
        }
        if (view.getId() == R.id.lbl_sport) {
            View star = findViewById(R.id.star_orange);
            star.setVisibility(View.VISIBLE);
            orange_state = 1;
            save(this);
            return;
        }
    }

    public void setScoreBoard(short event_value) {
        Log.d(Constants.TAG, "setScoreBoard: " + event_value);
        View star = null;
        switch (event_value) {
            case 1:
                star = findViewById(R.id.star_blue);
                blue_state = toggleState(blue_state);
                break;
            case 2:
                star = findViewById(R.id.star_pink);
                pink_state = toggleState(pink_state);
                break;
            case 3:
                star = findViewById(R.id.star_yellow);
                yellow_state = toggleState(yellow_state);
                break;
            case 4:
                star = findViewById(R.id.star_brown);
                brown_state = toggleState(brown_state);
                break;
            case 5:
                star = findViewById(R.id.star_green);
                green_state = toggleState(green_state);
                break;
            case 6:
                star = findViewById(R.id.star_orange);
                orange_state = toggleState(orange_state);
                break;
        }
        if (star != null) {
            star.setVisibility(Utility.toggleVisibility(star.getVisibility()));
        }
        save(this);
    }

    private int toggleState(int current_state) {
        if (current_state == 0) {
            return 1;
        } else {
            return 0;
        }
    }

    private void showMsg(final String msg) {
        Log.d(Constants.TAG, msg);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) TrivaScoreBoardControllerActivity.this.findViewById(R.id.message)).setText(Html.fromHtml(msg));
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

    public void save(Context context) {
        Log.d(Constants.TAG,"Saving preferences");
        SharedPreferences sharedPref = context.getSharedPreferences(TRIVIA_BOARD_STATE_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(BLUE_STATE, blue_state);
        editor.putInt(PINK_STATE, pink_state);
        editor.putInt(YELLOW_STATE, yellow_state);
        editor.putInt(BROWN_STATE, brown_state);
        editor.putInt(GREEN_STATE, green_state);
        editor.putInt(ORANGE_STATE, orange_state);
        editor.commit();
    }


    public void restore(Context context) {
        Log.d(Constants.TAG,"Restoring preferences");
        SharedPreferences sharedPref = context.getSharedPreferences(TRIVIA_BOARD_STATE_FILE, Context.MODE_PRIVATE);
        blue_state = sharedPref.getInt(BLUE_STATE,0);
        pink_state = sharedPref.getInt(PINK_STATE,0);
        yellow_state = sharedPref.getInt(YELLOW_STATE,0);
        brown_state = sharedPref.getInt(BROWN_STATE,0);
        green_state = sharedPref.getInt(GREEN_STATE,0);
        orange_state = sharedPref.getInt(ORANGE_STATE,0);
    }

}