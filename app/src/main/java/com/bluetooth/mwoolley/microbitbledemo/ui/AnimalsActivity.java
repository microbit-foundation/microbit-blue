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
import android.media.MediaPlayer;
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
import com.bluetooth.mwoolley.microbitbledemo.Utility;

public class AnimalsActivity extends AppCompatActivity implements ConnectionStatusListener {

    private BleAdapterService bluetooth_le_adapter;

    private int exit_step=0;
    private boolean exiting=false;
    private boolean b1_notifications_on =false;
    private boolean b2_notifications_on =false;

    private MediaPlayer player1_1;
    private MediaPlayer player1_2;
    private MediaPlayer player2_1;
    private MediaPlayer player2_2;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.d(Constants.TAG, "onServiceConnected");
            bluetooth_le_adapter = ((BleAdapterService.LocalBinder) service).getService();
            bluetooth_le_adapter.setActivityHandler(mMessageHandler);
            b1_notifications_on=false;
            b2_notifications_on=false;

            if (bluetooth_le_adapter.setNotificationsState(Utility.normaliseUUID(BleAdapterService.BUTTONSERVICE_SERVICE_UUID), Utility.normaliseUUID(BleAdapterService.BUTTON1STATE_CHARACTERISTIC_UUID), true)) {
                showMsg(Utility.htmlColorGreen("Button 1 State notifications ON"));
            } else {
                showMsg(Utility.htmlColorRed("Failed to set Button 1 State notifications ON"));
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
        setContentView(R.layout.activity_animals);
        getSupportActionBar().setTitle(R.string.screen_title_animal_magic);

        exiting = false;

        player1_1 = MediaPlayer.create(this,R.raw.miaow);
        player1_2 = MediaPlayer.create(this,R.raw.growl);
        player2_1 = MediaPlayer.create(this,R.raw.woof);
        player2_2 = MediaPlayer.create(this,R.raw.howl);

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
        player1_1.reset();
        player1_1.release();
        player1_1 = null;
        player1_2.reset();
        player1_2.release();
        player1_2 = null;
        player2_1.reset();
        player2_1.release();
        player2_1 = null;
        player2_2.reset();
        player2_2.release();
        player2_2 = null;
    }

    @Override
    public void onBackPressed() {
        Log.d(Constants.TAG,"onBackPressed");
        shutdownSteps();
    }

    private void shutdownSteps() {
        exiting=true;
        exit_step++;
        if (MicroBit.getInstance().isMicrobit_connected()) {
            switch (exit_step) {
                case 1:
                    Log.d(Constants.TAG, "Disabling Button 1 State notifications");
                    if (b1_notifications_on) {
                        bluetooth_le_adapter.setNotificationsState(Utility.normaliseUUID(BleAdapterService.BUTTONSERVICE_SERVICE_UUID), Utility.normaliseUUID(BleAdapterService.BUTTON1STATE_CHARACTERISTIC_UUID), false);
                    }
                    break;
                case 2:
                    Log.d(Constants.TAG, "Disabling Button 2 State notifications");
                    if (b2_notifications_on) {
                        bluetooth_le_adapter.setNotificationsState(Utility.normaliseUUID(BleAdapterService.BUTTONSERVICE_SERVICE_UUID), Utility.normaliseUUID(BleAdapterService.BUTTON2STATE_CHARACTERISTIC_UUID), false);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_animals, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.menu_animals_help) {
            Intent intent = new Intent(AnimalsActivity.this, HelpActivity.class);
            intent.putExtra(Constants.URI, Constants.ANIMALS_HELP);
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
                    b = bundle.getByteArray(BleAdapterService.PARCEL_VALUE);
                    Log.d(Constants.TAG, "descriptor " + descriptor_uuid + " of characteristic " + characteristic_uuid + " of service " + service_uuid.toString() + " written OK:0x"+Utility.byteArrayAsHexString(b));
                    if (!exiting) {
                        if (characteristic_uuid.equalsIgnoreCase(Utility.normaliseUUID(BleAdapterService.BUTTON1STATE_CHARACTERISTIC_UUID))) {
                            b1_notifications_on = true;
                            Log.d(Constants.TAG, "Enabling Button 2 State notifications");
                            if (bluetooth_le_adapter.setNotificationsState(Utility.normaliseUUID(BleAdapterService.BUTTONSERVICE_SERVICE_UUID), Utility.normaliseUUID(BleAdapterService.BUTTON2STATE_CHARACTERISTIC_UUID), true)) {
                                showMsg(Utility.htmlColorGreen("Button 2 State notifications ON"));
                            } else {
                                showMsg(Utility.htmlColorRed("Failed to set Button 2 State notifications ON"));
                            }
                            return;
                        }
                        if (characteristic_uuid.equalsIgnoreCase(Utility.normaliseUUID(BleAdapterService.BUTTON2STATE_CHARACTERISTIC_UUID))) {
                            b2_notifications_on = true;
                            return;
                        }
                    } else {
                        if (characteristic_uuid.equalsIgnoreCase(Utility.normaliseUUID(BleAdapterService.BUTTON1STATE_CHARACTERISTIC_UUID))) {
                            b1_notifications_on = false;
                            shutdownSteps();
                        } else {
                            b2_notifications_on = false;
                            shutdownSteps();
                        }
                    }
                    break;

                case BleAdapterService.NOTIFICATION_OR_INDICATION_RECEIVED:
                    bundle = msg.getData();
                    service_uuid = bundle.getString(BleAdapterService.PARCEL_SERVICE_UUID);
                    characteristic_uuid = bundle.getString(BleAdapterService.PARCEL_CHARACTERISTIC_UUID);
                    b = bundle.getByteArray(BleAdapterService.PARCEL_VALUE);
                    byte btn_state = b[0];
                    Log.d(Constants.TAG, "Value=" + Utility.byteArrayAsHexString(b));
                    if (characteristic_uuid.equalsIgnoreCase((Utility.normaliseUUID(BleAdapterService.BUTTON1STATE_CHARACTERISTIC_UUID)))) {
                        Log.d(Constants.TAG, "Button 1 State received: " + btn_state);
                        ImageView b1_image = (ImageView) AnimalsActivity.this.findViewById(R.id.button1);
                        setButtonMedia(1,b1_image, btn_state);
                        TextView b1_label = (TextView) AnimalsActivity.this.findViewById(R.id.button1_state);
                        setButtonLabel(1,b1_label, btn_state);
                    } else {
                        if (characteristic_uuid.equalsIgnoreCase((Utility.normaliseUUID(BleAdapterService.BUTTON2STATE_CHARACTERISTIC_UUID)))) {
                            Log.d(Constants.TAG, "Button 2 State received: " + btn_state);
                            ImageView b2_image = (ImageView) AnimalsActivity.this.findViewById(R.id.button2);
                            setButtonMedia(2,b2_image, btn_state);
                            TextView b2_label = (TextView) AnimalsActivity.this.findViewById(R.id.button2_state);
                            setButtonLabel(2,b2_label, btn_state);
                        }
                    }
                    break;
                case BleAdapterService.GATT_REMOTE_RSSI:
                    bundle = msg.getData();
                    int rssi = bundle.getInt(BleAdapterService.PARCEL_RSSI);
                    break;
                case BleAdapterService.MESSAGE:
                    bundle = msg.getData();
                    String text = bundle.getString(BleAdapterService.PARCEL_TEXT);
                    showMsg(Utility.htmlColorRed(text));
            }
        }
    };

    private void setButtonMedia(int button, ImageView btn_image, byte btn_state) {
        Log.d(Constants.TAG,"setButtonMedia: button="+button+" state="+btn_state);
        if (button == 1) {
            switch (btn_state) {
                case 0:
                    btn_image.setImageResource(R.drawable.blank);
                    break;
                case 1:
                    btn_image.setImageResource(R.drawable.cat);
                    if (player1_1 != null && player1_1.isPlaying()) {
                        player1_1.stop();
                    }
                    player1_1.start();
                    break;
                case 2:
                    btn_image.setImageResource(R.drawable.tiger);
                    if (player1_2 != null && player1_2.isPlaying()) {
                        player1_2.stop();
                    }
                    player1_2.start();
                    break;
            }
        } else {
            switch (btn_state) {
                case 0:
                    btn_image.setImageResource(R.drawable.blank);
                    break;
                case 1:
                    btn_image.setImageResource(R.drawable.dog);
                    if (player2_1 != null && player2_1.isPlaying()) {
                        player2_1.stop();
                    }
                    player2_1.start();
                    break;
                case 2:
                    btn_image.setImageResource(R.drawable.wolf);
                    if (player2_2 != null && player2_2.isPlaying()) {
                        player2_2.stop();
                    }
                    player2_2.start();
                    break;
            }
        }
    }

    private void setButtonLabel(int button, TextView btn_label, byte btn_state) {
        Log.d(Constants.TAG,"setButtonLabel: button="+button+" state="+btn_state);
        if (button == 1) {
            switch (btn_state) {
                case 0:
                    btn_label.setText("");
                    break;
                case 1:
                    btn_label.setText("Kitten");
                    break;
                case 2:
                    btn_label.setText("Tiger!");
                    break;
            }
        } else {
            switch (btn_state) {
                case 0:
                    btn_label.setText("");
                    break;
                case 1:
                    btn_label.setText("Puppy");
                    break;
                case 2:
                    btn_label.setText("Wolf!");
                    break;
            }
        }
    }

    private void showMsg(final String msg) {
        Log.d(Constants.TAG, msg);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) AnimalsActivity.this.findViewById(R.id.message)).setText(Html.fromHtml(msg));
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