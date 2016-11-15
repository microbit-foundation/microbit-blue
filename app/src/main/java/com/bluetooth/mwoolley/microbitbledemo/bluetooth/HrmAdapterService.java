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

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.bluetooth.mwoolley.microbitbledemo.Constants;
import com.bluetooth.mwoolley.microbitbledemo.Utility;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;

public class HrmAdapterService extends Service implements Runnable{

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
    public static final int NOTIFICATION_RECEIVED = 7;
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
    public static String HEARTRATE_SERVICE_UUID = "0000180D00001000800000805F9B34FB";
    public static String HEARTRATE_SERVICE_16_BIT_UUID = "180D";
    public static String HEARTRATEMEASUREMENT_CHARACTERISTIC_UUID = "00002a3700001000800000805F9B34FB";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    private boolean request_processor_running = false;

    private Object mutex = new Object();
    // queue will never contain more than one operation in this version and it will always represent
    // the current request being processed whereas an empty queue means the system can process a new
    // request now
    private ArrayList<Operation> operation_queue = new ArrayList<Operation>();

    private long timestamp;

    // Ble Gatt Callback ///////////////////////
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                            int newState) {
            Log.d(Constants.TAG, "onConnectionStateChange: status=" + status);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Message msg = Message.obtain(activity_handler, GATT_CONNECTED);
                msg.sendToTarget();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Message msg = Message.obtain(activity_handler, GATT_DISCONNECT);
                msg.sendToTarget();
                bluetooth_gatt.close();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Message msg = Message.obtain(activity_handler, GATT_SERVICES_DISCOVERED);
            msg.sendToTarget();
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            Bundle bundle = new Bundle();
            bundle.putString(PARCEL_CHARACTERISTIC_UUID, characteristic.getUuid().toString());
            bundle.putString(PARCEL_SERVICE_UUID, characteristic.getService().getUuid().toString());
            bundle.putByteArray(PARCEL_VALUE, characteristic.getValue());
            Message msg = Message.obtain(activity_handler, NOTIFICATION_RECEIVED);
            msg.setData(bundle);
            msg.sendToTarget();
        }

        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
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

    };

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
        Log.d(Constants.TAG, "HRM GATT Request processor thread starting");
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
                            case Operation.OPERATION_WRITE_DESCRIPTOR_REQUEST:
                                ok = executeSetNotificationsState(op.getService_uuid(),op.getCharacteristic_uuid(),op.isSubscribe());
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

    public class LocalBinder extends Binder {
        public HrmAdapterService getService() {
            return HrmAdapterService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        startRequestProcessor();
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {

        stopRequestProcessor();

        if (bluetooth_gatt != null) {
            bluetooth_gatt.close();
            bluetooth_gatt = null;
        }


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

    // disconnect from device
    public void disconnect() {
        sendConsoleMessage("disconnecting");
        if (bluetooth_adapter == null || bluetooth_gatt == null) {
            sendConsoleMessage("disconnect: bluetooth_adapter|bluetooth_gatt null");
            return;
        }
        bluetooth_gatt.disconnect();
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

    public boolean setNotificationsState(String serviceUuid, String characteristicUuid, boolean enabled) {
        if (bluetooth_adapter == null || bluetooth_gatt == null) {
            sendConsoleMessage("setNotificationsState: bluetooth_adapter|bluetooth_gatt null");
            return false;
        }

        BluetoothGattService gattService = bluetooth_gatt.getService(UUID.fromString(serviceUuid));
        if (gattService == null) {
            sendConsoleMessage("setNotificationsState: gattService null");
            return false;
        }
        BluetoothGattCharacteristic gattChar = gattService.getCharacteristic(UUID.fromString(characteristicUuid));
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

    private boolean executeSetNotificationsState(String serviceUuid, String characteristicUuid, boolean enabled) {
        if (bluetooth_adapter == null || bluetooth_gatt == null) {
            sendConsoleMessage("setNotificationsState: bluetooth_adapter|bluetooth_gatt null");
            return false;
        }

        BluetoothGattService gattService = bluetooth_gatt.getService(UUID.fromString(serviceUuid));
        if (gattService == null) {
            sendConsoleMessage("setNotificationsState: gattService null");
            return false;
        }
        BluetoothGattCharacteristic gattChar = gattService.getCharacteristic(UUID.fromString(characteristicUuid));
        if (gattChar == null) {
            sendConsoleMessage("setNotificationsState: gattChar null");
            return false;
        }
        bluetooth_gatt.setCharacteristicNotification(gattChar, enabled);
        // Enable remote notifications
        descriptor = gattChar.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG));
        if (enabled) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        } else {
            descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        }
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

}