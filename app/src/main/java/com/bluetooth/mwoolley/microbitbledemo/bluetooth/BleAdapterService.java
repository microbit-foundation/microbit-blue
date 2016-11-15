package com.bluetooth.mwoolley.microbitbledemo.bluetooth;

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

import android.bluetooth.BluetoothAdapter;

/*
Much of the code in this class was generated using Bluetooth Developer Studio. See http://www.bluetooth.com/~/media/developer-studio/index
*/

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.bluetooth.mwoolley.microbitbledemo.Constants;
import com.bluetooth.mwoolley.microbitbledemo.MicroBit;
import com.bluetooth.mwoolley.microbitbledemo.Utility;

public class BleAdapterService extends Service implements Runnable{

    private BluetoothAdapter bluetooth_adapter;
    private BluetoothGatt bluetooth_gatt;
    private BluetoothManager bluetooth_manager;
    private Handler activity_handler = null;
    private BluetoothDevice device;
    private BluetoothGattDescriptor descriptor;

    public BluetoothDevice getDevice() {
        return device;
    }

    // messages sent back to activity
    public static final int GATT_CONNECTED = 1;
    public static final int GATT_DISCONNECT = 2;
    public static final int GATT_SERVICES_DISCOVERED = 3;
    public static final int GATT_CHARACTERISTIC_READ = 4;
    public static final int GATT_REMOTE_RSSI = 5;
    public static final int MESSAGE = 6;
    public static final int NOTIFICATION_OR_INDICATION_RECEIVED = 7;
    public static final int SIMULATED_NOTIFICATION_RECEIVED = 8;
    public static final int GATT_CHARACTERISTIC_WRITTEN = 9;
    public static final int GATT_DESCRIPTOR_WRITTEN = 10;

    // message parms
    public static final String PARCEL_DESCRIPTOR_UUID = "DESCRIPTOR_UUID";
    public static final String PARCEL_CHARACTERISTIC_UUID = "CHARACTERISTIC_UUID";
    public static final String PARCEL_SERVICE_UUID = "SERVICE_UUID";
    public static final String PARCEL_VALUE = "VALUE";
    public static final String PARCEL_RSSI = "RSSI";
    public static final String PARCEL_TEXT = "TEXT";


    // UUIDs


    public static String GENERICACCESS_SERVICE_UUID = "0000180000001000800000805F9B34FB";

    public static String DEVICENAME_CHARACTERISTIC_UUID = "00002A0000001000800000805F9B34FB";
    public static String APPEARANCE_CHARACTERISTIC_UUID = "00002A0100001000800000805F9B34FB";

    public static String GENERICATTRIBUTE_SERVICE_UUID = "0000180100001000800000805F9B34FB";

    public static String SERVICECHANGED_CHARACTERISTIC_UUID = "2A05";

    public static String DEVICEINFORMATION_SERVICE_UUID = "0000180A00001000800000805F9B34FB";

    public static String MANUFACTURERNAMESTRING_CHARACTERISTIC_UUID = "00002A2900001000800000805F9B34FB";
    public static String MODELNUMBERSTRING_CHARACTERISTIC_UUID = "00002A2400001000800000805F9B34FB";
    public static String SERIALNUMBERSTRING_CHARACTERISTIC_UUID = "00002A2500001000800000805F9B34FB";
    public static String HARDWAREREVISIONSTRING_CHARACTERISTIC_UUID = "00002A2700001000800000805F9B34FB";
    public static String FIRMWAREREVISIONSTRING_CHARACTERISTIC_UUID = "00002A2600001000800000805F9B34FB";

    public static String ACCELEROMETERSERVICE_SERVICE_UUID = "E95D0753251D470AA062FA1922DFA9A8";

    public static String ACCELEROMETERDATA_CHARACTERISTIC_UUID = "E95DCA4B251D470AA062FA1922DFA9A8";
    public static String ACCELEROMETERPERIOD_CHARACTERISTIC_UUID = "E95DFB24251D470AA062FA1922DFA9A8";

    public static String MAGNETOMETERSERVICE_SERVICE_UUID = "E95DF2D8251D470AA062FA1922DFA9A8";

    public static String MAGNETOMETERDATA_CHARACTERISTIC_UUID = "E95DFB11251D470AA062FA1922DFA9A8";
    public static String MAGNETOMETERPERIOD_CHARACTERISTIC_UUID = "E95D386C251D470AA062FA1922DFA9A8";
    public static String MAGNETOMETERBEARING_CHARACTERISTIC_UUID = "E95D9715251D470AA062FA1922DFA9A8";

    public static String BUTTONSERVICE_SERVICE_UUID = "E95D9882251D470AA062FA1922DFA9A8";

    public static String BUTTON1STATE_CHARACTERISTIC_UUID = "E95DDA90251D470AA062FA1922DFA9A8";
    public static String BUTTON2STATE_CHARACTERISTIC_UUID = "E95DDA91251D470AA062FA1922DFA9A8";

    public static String IOPINSERVICE_SERVICE_UUID = "E95D127B251D470AA062FA1922DFA9A8";

    public static String PINDATA_CHARACTERISTIC_UUID = "E95D8D00251D470AA062FA1922DFA9A8";
    public static String PINADCONFIGURATION_CHARACTERISTIC_UUID = "E95D5899251D470AA062FA1922DFA9A8";
    public static String PINIOCONFIGURATION_CHARACTERISTIC_UUID = "E95DB9FE251D470AA062FA1922DFA9A8";

    public static String LEDSERVICE_SERVICE_UUID = "E95DD91D251D470AA062FA1922DFA9A8";

    public static String LEDMATRIXSTATE_CHARACTERISTIC_UUID = "E95D7B77251D470AA062FA1922DFA9A8";
    public static String LEDTEXT_CHARACTERISTIC_UUID = "E95D93EE251D470AA062FA1922DFA9A8";
    public static String SCROLLINGDELAY_CHARACTERISTIC_UUID = "E95D0D2D251D470AA062FA1922DFA9A8";

    public static String EVENTSERVICE_SERVICE_UUID = "E95D93AF251D470AA062FA1922DFA9A8";

    public static String MICROBITREQUIREMENTS_CHARACTERISTIC_UUID = "E95DB84C251D470AA062FA1922DFA9A8";
    public static String MICROBITEVENT_CHARACTERISTIC_UUID = "E95D9775251D470AA062FA1922DFA9A8";
    public static String CLIENTREQUIREMENTS_CHARACTERISTIC_UUID = "E95D23C4251D470AA062FA1922DFA9A8";
    public static String CLIENTEVENT_CHARACTERISTIC_UUID = "E95D5404251D470AA062FA1922DFA9A8";

    public static String DFUCONTROLSERVICE_SERVICE_UUID = "E95D93B0251D470AA062FA1922DFA9A8";

    public static String DFUCONTROL_CHARACTERISTIC_UUID = "E95D93B1251D470AA062FA1922DFA9A8";

    public static String TEMPERATURESERVICE_SERVICE_UUID = "E95D6100251D470AA062FA1922DFA9A8";

    public static String TEMPERATURE_CHARACTERISTIC_UUID = "E95D9250251D470AA062FA1922DFA9A8";

    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    public static String HEARTRATE_SERVICE_UUID = "0000180D00001000800000805F9B34FB";
    public static String HEARTRATE_SERVICE_16_BIT_UUID = "180D";
    public static String HEARTRATEMEASUREMENT_CHARACTERISTIC_UUID = "00002a3700001000800000805F9B34FB";

    public static String UARTSERVICE_SERVICE_UUID = "6E400001B5A3F393E0A9E50E24DCCA9E";
    public static String UART_TX_CHARACTERISTIC_UUID = "6E400002B5A3F393E0A9E50E24DCCA9E";
    public static String UART_RX_CHARACTERISTIC_UUID = "6E400003B5A3F393E0A9E50E24DCCA9E";

    private boolean request_processor_running = false;

    private Object mutex = new Object();
    // queue will never contain more than one operation in this version and it will always represent
    // the current request being processed whereas an empty queue means the system can process a new
    // request now
    private ArrayList<Operation> operation_queue = new ArrayList<Operation>();

    private long timestamp;
    private KeepAlive keep_alive = new KeepAlive();

    // Ble Gatt Callback ///////////////////////
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                            int newState) {
            Log.d(Constants.TAG, "onConnectionStateChange: status=" + status);
            timestamp();
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(Constants.TAG, "onConnectionStateChange: CONNECTED");
                MicroBit.getInstance().setMicrobit_connected(true);
                Message msg = Message.obtain(activity_handler, GATT_CONNECTED);
                msg.sendToTarget();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(Constants.TAG, "onConnectionStateChange: DISCONNECTED");
                MicroBit.getInstance().setMicrobit_connected(false);
                Message msg = Message.obtain(activity_handler, GATT_DISCONNECT);
                msg.sendToTarget();
                if (bluetooth_gatt != null) {
                    Log.d(Constants.TAG,"Closing and destroying BluetoothGatt object");
                    bluetooth_gatt.close();
                    bluetooth_gatt = null;
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            timestamp();
            Message msg = Message.obtain(activity_handler, GATT_SERVICES_DISCOVERED);
            msg.sendToTarget();
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
            Log.d(Constants.TAG, "onCharacteristicRead");
            timestamp();
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Bundle bundle = new Bundle();
                bundle.putString(PARCEL_CHARACTERISTIC_UUID, characteristic.getUuid().toString());
                bundle.putString(PARCEL_SERVICE_UUID, characteristic.getService().getUuid().toString());
                bundle.putByteArray(PARCEL_VALUE, characteristic.getValue());
                Message msg = Message.obtain(activity_handler, GATT_CHARACTERISTIC_READ);
                msg.setData(bundle);
                msg.sendToTarget();
            } else {
                sendConsoleMessage("characteristic read err:" + status);
            }
            // whatever the outcome we need to clear the Operation object from the request queue now
            operationCompleted();
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {
            Log.d(Constants.TAG, "onCharacteristicWrite");
            timestamp();
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Bundle bundle = new Bundle();
                bundle.putString(PARCEL_CHARACTERISTIC_UUID, characteristic.getUuid().toString());
                bundle.putString(PARCEL_SERVICE_UUID, characteristic.getService().getUuid().toString());
                bundle.putByteArray(PARCEL_VALUE, characteristic.getValue());
                Message msg = Message.obtain(activity_handler, GATT_CHARACTERISTIC_WRITTEN);
                msg.setData(bundle);
                msg.sendToTarget();
            } else {
                sendConsoleMessage("characteristic write err:" + status);
            }
            // whatever the outcome we need to clear the Operation object from the request queue now
            operationCompleted();
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            timestamp();
            Bundle bundle = new Bundle();
            bundle.putString(PARCEL_CHARACTERISTIC_UUID, characteristic.getUuid().toString());
            bundle.putString(PARCEL_SERVICE_UUID, characteristic.getService().getUuid().toString());
            bundle.putByteArray(PARCEL_VALUE, characteristic.getValue());
            // notifications and indications are both communicated from here in this way
            Message msg = Message.obtain(activity_handler, NOTIFICATION_OR_INDICATION_RECEIVED);
            msg.setData(bundle);
            msg.sendToTarget();
        }

        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            timestamp();
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Bundle bundle = new Bundle();
                bundle.putString(PARCEL_DESCRIPTOR_UUID, descriptor.getUuid().toString());
                bundle.putString(PARCEL_CHARACTERISTIC_UUID, descriptor.getCharacteristic().getUuid().toString());
                bundle.putString(PARCEL_SERVICE_UUID, descriptor.getCharacteristic().getService().toString());
                bundle.putByteArray(PARCEL_VALUE, descriptor.getValue());
                Message msg = Message.obtain(activity_handler, GATT_DESCRIPTOR_WRITTEN);
                msg.setData(bundle);
                msg.sendToTarget();
            } else {
                sendConsoleMessage("characteristic write err:" + status);
            }
            // whatever the outcome we need to clear the Operation object from the request queue now
            operationCompleted();
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Bundle bundle = new Bundle();
                bundle.putInt(PARCEL_RSSI, rssi);
                Message msg = Message.obtain(activity_handler, GATT_REMOTE_RSSI);
                msg.setData(bundle);
                msg.sendToTarget();
            } else {
                sendConsoleMessage("RSSI read err:" + status);
            }
        }
    };

    private void timestamp() {
        timestamp = System.currentTimeMillis();
    }
    // service binder ////////////////
    private final IBinder mBinder = new LocalBinder();

    public void startRequestProcessor() {
        Log.d(Constants.TAG,"startRequestProcessor");
        Thread t = new Thread(this);
        request_processor_running = true;
        t.start();
    }

    public void stopRequestProcessor() {
        Log.d(Constants.TAG,"stopRequestProcessor");
        request_processor_running = false;
        Operation stop = new Operation(Operation.OPERATION_EXIT_QUEUE_PROCESSING_REQUEST);
        addOperation(stop);
    }

    private boolean isRequestInProgress() {
        Log.d(Constants.TAG,"isRequestInProgress called");
        boolean busy = false;
        synchronized (mutex) {
            busy = (operation_queue.size() > 0);
            Log.d(Constants.TAG, "isRequestInProgress: busy=" + busy);
        }
        return busy;
    }

    private void addOperation(Operation op) {
        Log.d(Constants.TAG, "addOperation called");
        synchronized (mutex) {
            while (operation_queue.size() > 0) {
                try {
                    Log.d(Constants.TAG,"Waiting for queue to be empty");
                    mutex.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (operation_queue.size() == 0) {
                Log.d(Constants.TAG,"Adding operation to queue");
                operation_queue.add(op);
                mutex.notifyAll();
            }
        }
    }

    private void operationCompleted() {
        // operations are always processed one at a time in strict order. An operation completing must be the one currently with "executing" status and this must be the one
        // at index position 0 in the operation queue
        Log.d(Constants.TAG, "operationCompleted called");
        synchronized (mutex) {
            if (operation_queue.size() > 0) {
                Log.d(Constants.TAG, "Removing completed operation from queue");
                operation_queue.remove(0);
                mutex.notifyAll();
            }
        }
    }

    private void emptyOperationQueue() {
        Log.d(Constants.TAG, "emptyOperationQueue called");
        synchronized (mutex) {
            if (operation_queue.size() > 0) {
                operation_queue.clear();
                mutex.notifyAll();
            }
        }
    }

    public boolean isRequest_processor_running() {
        return request_processor_running;
    }

    public void setRequest_processor_running(boolean request_processor_running) {
        this.request_processor_running = request_processor_running;
    }

    @Override
    public void run() {
        Log.d(Constants.TAG, "GATT Request processor thread starting");
        Operation current_op = null;
        try {
            while (request_processor_running) {
                synchronized (mutex) {
                    while (operation_queue.size() == 0 || (operation_queue.get(0).getOperation_status() == Operation.OPERATION_EXECUTING)) {
                        try {
                            if (operation_queue.size() > 0) {
                                Log.d(Constants.TAG, "waiting for executing operation to complete");
                            } else {
                                Log.d(Constants.TAG, "waiting for operation to process");
                            }
                            mutex.wait(Constants.GATT_OPERATION_TIME_OUT);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if (operation_queue.size() > 0) {
                        Operation op = operation_queue.get(0);
                        if (current_op == op) {
                            Log.d(Constants.TAG, "Looks like GATT operation was not processed - timing out to clear the queue");
                            sendConsoleMessage("Previous operation timed out");
                            operationCompleted();
                            continue;
                        }
                        op.setOperation_status(Operation.OPERATION_EXECUTING);
                        current_op = op;
                        Log.d(Constants.TAG, "processing operation: " + operation_queue.toString());
                        boolean ok=false;
                        switch (op.getOperation_type()) {
                            case Operation.OPERATION_READ_CHARACTERISTIC_REQUEST:
                                ok = executeReadCharacteristic(op.getService_uuid(),op.getCharacteristic_uuid());
                                break;
                            case Operation.OPERATION_WRITE_CHARACTERISTIC_REQUEST:
                                ok = executeWriteCharacteristic(op.getService_uuid(), op.getCharacteristic_uuid(), op.getValue());
                                break;
                            case Operation.OPERATION_WRITE_DESCRIPTOR_REQUEST:
                                ok = executeSetCccdState(op.getService_uuid(), op.getCharacteristic_uuid(), op.isSubscribe(), op.getValue());
                                break;
                            case Operation.OPERATION_EXIT_QUEUE_PROCESSING_REQUEST:
                                ok = true;
                                operation_queue.remove(0);
                        }
                        if (!ok) {
                            sendConsoleMessage("Error: GATT operation failed");
                            // remove failed operation from queue
                            operationCompleted();
                        }
                        mutex.notifyAll();
                    }
                }
            }
        } catch (Exception e) {
            Log.d(Constants.TAG, "ERROR in operation queue processing thread:" + e.getClass().getName() + ":" + e.getMessage());
            sendConsoleMessage("ERROR: serious problem in Bluetooth request processor");
        }

        emptyOperationQueue();

        Log.d(Constants.TAG, "GATT Request processor thread exiting");
    }

    class KeepAlive implements Runnable {

        private int frequency = 10; // seconds

        boolean running = false;

        public void start() {
            Thread t = new Thread(this);
            t.start();
        }

        public void stop() {
            running = false;
        }

        @Override
        public void run() {
            Log.d(Constants.TAG,"Keep alive thread starting");
            running = true;
            try {
                // random delay
                Thread.sleep((long) Math.random() * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            while (running) {
                try {
                    Thread.sleep(Constants.CONNECTION_KEEP_ALIVE_FREQUENCY);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (running && System.currentTimeMillis() - timestamp > Constants.CONNECTION_KEEP_ALIVE_FREQUENCY) {
                    // it's possible we've been disconnected due to external factors like the micro:bit going out of range or being unplugged
                    // we assume the current Activity has informed the user so they can find and connect again
                    if (MicroBit.getInstance().isMicrobit_connected()) {
                        // then there has been no GATT activity for some time so we make sure the connection doesn't close by reading the firmware revision string
                        Log.d(Constants.TAG, "KeepAlive thread is reading firmware revision string");
                        readCharacteristic(Utility.normaliseUUID(DEVICEINFORMATION_SERVICE_UUID), Utility.normaliseUUID(FIRMWAREREVISIONSTRING_CHARACTERISTIC_UUID));
                    }
                }
            }
            Log.d(Constants.TAG,"Keep alive thread exiting");
        }
    }

    public class LocalBinder extends Binder {
        public BleAdapterService getService() {
            return BleAdapterService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        startRequestProcessor();
        keep_alive.start();
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {

        if (bluetooth_manager == null) {
            bluetooth_manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetooth_manager == null) {
                return;
            }
        }

        bluetooth_adapter = bluetooth_manager.getAdapter();
        if (bluetooth_adapter == null) {
            return;
        }

    }

    // connect to the device
    public boolean connect(final String address) {

        if (bluetooth_adapter == null || address == null) {
            sendConsoleMessage("connect: bluetooth_adapter=null");
            return false;
        }

        device = bluetooth_adapter.getRemoteDevice(address);
        if (device == null) {
            sendConsoleMessage("connect: device=null");
            return false;
        }

        bluetooth_gatt = device.connectGatt(this, false, mGattCallback);
        return true;
    }

    public Set<BluetoothDevice> getBondedDevices() {
        return bluetooth_adapter.getBondedDevices();
    }

    // disconnect from device
    public void disconnect() {
        sendConsoleMessage("disconnecting");
        if (bluetooth_adapter == null || bluetooth_gatt == null) {
            sendConsoleMessage("disconnect: bluetooth_adapter|bluetooth_gatt null");
            return;
        }
        stopRequestProcessor();
        keep_alive.stop();

        if (bluetooth_gatt != null) {
            Log.d(Constants.TAG,"Disconnecting");
            bluetooth_gatt.disconnect();
        }
    }

    public void discoverServices() {
        bluetooth_gatt.discoverServices();
    }

    // set activity the will receive the messages
    public void setActivityHandler(Handler handler) {
        activity_handler = handler;
    }

    // return list of supported services
    public List<BluetoothGattService> getSupportedGattServices() {
        if (bluetooth_gatt == null)
            return null;
        return bluetooth_gatt.getServices();
    }

    public boolean readCharacteristic(String serviceUuid, String characteristicUuid) {
        if (!MicroBit.getInstance().isMicrobit_connected()) {
            Log.d(Constants.TAG, "IGNORING readCharacteristic serviceUuid=" + serviceUuid + " characteristicUuid=" + characteristicUuid+" - not currently connected");
            return true;
        }
        if (bluetooth_adapter == null || bluetooth_gatt == null) {
            sendConsoleMessage("readCharacteristic: bluetooth_adapter|bluetooth_gatt null");
            return false;
        }

        BluetoothGattService gattService = bluetooth_gatt.getService(java.util.UUID.fromString(serviceUuid));
        if (gattService == null) {
            sendConsoleMessage("readCharacteristic: gattService null");
            return false;
        }
        BluetoothGattCharacteristic gattChar = gattService.getCharacteristic(java.util.UUID.fromString(characteristicUuid));
        if (gattChar == null) {
            sendConsoleMessage("readCharacteristic: gattChar null");
            return false;
        }

        // add operation to the request processor from a background thread to ensure we do not block the calling thread
        final Operation op = new Operation(Operation.OPERATION_READ_CHARACTERISTIC_REQUEST,
                serviceUuid,
                characteristicUuid
        );

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                addOperation(op);
            }
        });

        return true;
    }

    // called from the request processor thread
    private boolean executeReadCharacteristic(String serviceUuid, String characteristicUuid) {
        if (!MicroBit.getInstance().isMicrobit_connected()) {
            Log.d(Constants.TAG, "IGNORING executeReadCharacteristic serviceUuid=" + serviceUuid + " characteristicUuid=" + characteristicUuid+" - not currently connected");
            return true;
        }
        Log.d(Constants.TAG, "executeReadCharacteristic");
        if (bluetooth_adapter == null || bluetooth_gatt == null) {
            sendConsoleMessage("readCharacteristic: bluetooth_adapter|bluetooth_gatt null");
            return false;
        }

        BluetoothGattService gattService = bluetooth_gatt.getService(java.util.UUID.fromString(serviceUuid));
        if (gattService == null) {
            sendConsoleMessage("readCharacteristic: gattService null");
            return false;
        }
        BluetoothGattCharacteristic gattChar = gattService.getCharacteristic(java.util.UUID.fromString(characteristicUuid));
        if (gattChar == null) {
            sendConsoleMessage("readCharacteristic: gattChar null");
            return false;
        }
        timestamp();
        return bluetooth_gatt.readCharacteristic(gattChar);
    }

    public boolean writeCharacteristic(String serviceUuid, String characteristicUuid, byte[] value) {
        if (!MicroBit.getInstance().isMicrobit_connected()) {
            Log.d(Constants.TAG, "IGNORING writeCharacteristic serviceUuid=" + serviceUuid + " characteristicUuid=" + characteristicUuid+" - not currently connected");
            return true;
        }
        Log.d(Constants.TAG, "writeCharacteristic serviceUuid=" + serviceUuid + " characteristicUuid=" + characteristicUuid+ " value="+ Utility.byteArrayAsHexString(value));
        if (bluetooth_adapter == null || bluetooth_gatt == null) {
            sendConsoleMessage("writeCharacteristic: bluetooth_adapter|bluetooth_gatt null");
            return false;
        }

        BluetoothGattService gattService = bluetooth_gatt.getService(java.util.UUID.fromString(serviceUuid));
        if (gattService == null) {
            sendConsoleMessage("writeCharacteristic: gattService null");
            return false;
        }
        BluetoothGattCharacteristic gattChar = gattService.getCharacteristic(java.util.UUID.fromString(characteristicUuid));
        if (gattChar == null) {
            sendConsoleMessage("writeCharacteristic: gattChar null");
            return false;
        }
        // add operation to the request processor from a background thread to ensure we do not block the calling thread
        final Operation op = new Operation(Operation.OPERATION_WRITE_CHARACTERISTIC_REQUEST,
                serviceUuid,
                characteristicUuid,
                value
        );

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                addOperation(op);
            }
        });

        return true;
    }

    private boolean executeWriteCharacteristic(String serviceUuid, String characteristicUuid, byte[] value) {
        if (!MicroBit.getInstance().isMicrobit_connected()) {
            Log.d(Constants.TAG, "IGNORING executeWriteCharacteristic serviceUuid=" + serviceUuid + " characteristicUuid=" + characteristicUuid+" - not currently connected");
            return true;
        }

        Log.d(Constants.TAG, "executeWriteCharacteristic: serviceUuid=" + serviceUuid + " characteristicUuid=" + characteristicUuid+ " value="+ Utility.byteArrayAsHexString(value));
        boolean result = false;
        if (bluetooth_adapter == null || bluetooth_gatt == null) {
            sendConsoleMessage("writeCharacteristic: bluetooth_adapter|bluetooth_gatt null");
            return false;
        }

        BluetoothGattService gattService = bluetooth_gatt.getService(java.util.UUID.fromString(serviceUuid));
        if (gattService == null) {
            sendConsoleMessage("writeCharacteristic: gattService null");
            return false;
        }
        BluetoothGattCharacteristic gattChar = gattService.getCharacteristic(java.util.UUID.fromString(characteristicUuid));
        if (gattChar == null) {
            sendConsoleMessage("writeCharacteristic: gattChar null");
            return false;
        }

        gattChar.setValue(value);

        timestamp();
        result = bluetooth_gatt.writeCharacteristic(gattChar);
        Log.d(Constants.TAG, "executeWriteCharacteristic: result=" + result);
        return result;
    }

    public boolean setNotificationsState(String serviceUuid, String characteristicUuid, boolean enabled) {

        if (!MicroBit.getInstance().isMicrobit_connected()) {
            Log.d(Constants.TAG, "IGNORING setNotificationsState serviceUuid=" + serviceUuid + " characteristicUuid=" + characteristicUuid+" - not currently connected");
            return true;
        }

        if (bluetooth_adapter == null || bluetooth_gatt == null) {
            sendConsoleMessage("setNotificationsState: bluetooth_adapter|bluetooth_gatt null");
            return false;
        }

        BluetoothGattService gattService = bluetooth_gatt.getService(java.util.UUID.fromString(serviceUuid));
        if (gattService == null) {
            sendConsoleMessage("setNotificationsState: gattService null");
            return false;
        }
        BluetoothGattCharacteristic gattChar = gattService.getCharacteristic(java.util.UUID.fromString(characteristicUuid));
        if (gattChar == null) {
            sendConsoleMessage("setNotificationsState: gattChar null");
            return false;
        }
        // add operation to the request processor from a background thread to ensure we do not block the calling thread
        byte [] cccd_value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
        if (!enabled) {
            cccd_value = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
        }
        final Operation op = new Operation(Operation.OPERATION_WRITE_DESCRIPTOR_REQUEST,
                serviceUuid,
                characteristicUuid,
                enabled,
                cccd_value
        );

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                addOperation(op);
            }
        });

        return true;
    }

    public boolean setIndicationsState(String serviceUuid, String characteristicUuid, boolean enabled) {

        if (!MicroBit.getInstance().isMicrobit_connected()) {
            Log.d(Constants.TAG, "IGNORING setIndicationsState serviceUuid=" + serviceUuid + " characteristicUuid=" + characteristicUuid+" - not currently connected");
            return true;
        }

        if (bluetooth_adapter == null || bluetooth_gatt == null) {
            sendConsoleMessage("setIndicationsState: bluetooth_adapter|bluetooth_gatt null");
            return false;
        }

        BluetoothGattService gattService = bluetooth_gatt.getService(java.util.UUID.fromString(serviceUuid));
        if (gattService == null) {
            sendConsoleMessage("setIndicationsState: gattService null");
            return false;
        }
        BluetoothGattCharacteristic gattChar = gattService.getCharacteristic(java.util.UUID.fromString(characteristicUuid));
        if (gattChar == null) {
            sendConsoleMessage("setIndicationsState: gattChar null");
            return false;
        }
        // add operation to the request processor from a background thread to ensure we do not block the calling thread
        byte [] cccd_value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE;
        if (!enabled) {
            cccd_value = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
        }
        final Operation op = new Operation(Operation.OPERATION_WRITE_DESCRIPTOR_REQUEST,
                serviceUuid,
                characteristicUuid,
                enabled,
                cccd_value
        );

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                addOperation(op);
            }
        });

        return true;
    }


    private boolean executeSetCccdState(String serviceUuid, String characteristicUuid, boolean enabled, byte [] cccd_enable) {

        if (!MicroBit.getInstance().isMicrobit_connected()) {
            Log.d(Constants.TAG, "IGNORING executeSetCccdState serviceUuid=" + serviceUuid + " characteristicUuid=" + characteristicUuid+" - not currently connected");
            return true;
        }

        if (bluetooth_adapter == null || bluetooth_gatt == null) {
            sendConsoleMessage("executeSetNotificationsState: bluetooth_adapter|bluetooth_gatt null");
            return false;
        }

        BluetoothGattService gattService = bluetooth_gatt.getService(java.util.UUID.fromString(serviceUuid));
        if (gattService == null) {
            sendConsoleMessage("executeSetNotificationsState: gattService null");
            return false;
        }
        BluetoothGattCharacteristic gattChar = gattService.getCharacteristic(java.util.UUID.fromString(characteristicUuid));
        if (gattChar == null) {
            sendConsoleMessage("executeSetNotificationsState: gattChar null");
            return false;
        }
        bluetooth_gatt.setCharacteristicNotification(gattChar, enabled);
        // Enable remote notifications
        descriptor = gattChar.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG));

        if (enabled) {
            Log.d(Constants.TAG,"XXXX setting descriptor "+descriptor+" to "+Utility.byteArrayAsHexString(cccd_enable));
            descriptor.setValue(cccd_enable);
        } else {
            descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        }
        timestamp();
        boolean ok = bluetooth_gatt.writeDescriptor(descriptor);
        return ok;
    }

    public void readRemoteRssi() {

        if (bluetooth_adapter == null || bluetooth_gatt == null) {
            return;
        }
        bluetooth_gatt.readRemoteRssi();
    }

    private void sendConsoleMessage(String text) {
        Message msg = Message.obtain(activity_handler, MESSAGE);
        Bundle data = new Bundle();
        data.putString(PARCEL_TEXT, text);
        msg.setData(data);
        msg.sendToTarget();
    }


    public boolean refreshDeviceCache(){
        try {
            Method refresh_method = bluetooth_gatt.getClass().getMethod("refresh", new Class[0]);
            if (refresh_method != null) {
                boolean bool = ((Boolean) refresh_method.invoke(bluetooth_gatt, new Object[0])).booleanValue();
                return bool;
            }
        }
        catch (Exception e) {
            Log.e(Constants.TAG, "Exception refreshing GATT services:"+e.getClass().getName()+":"+e.getMessage());
        }
        return false;
    }
}