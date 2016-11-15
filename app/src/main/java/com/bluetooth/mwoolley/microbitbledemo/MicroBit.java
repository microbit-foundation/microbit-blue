package com.bluetooth.mwoolley.microbitbledemo;
/*
 * Author: Martin Woolley
 * Twitter: @bluetooth_mdw
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
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import com.bluetooth.mwoolley.microbitbledemo.bluetooth.ConnectionStatusListener;
import com.bluetooth.mwoolley.microbitbledemo.bluetooth.Handle;

import java.util.Hashtable;
import java.util.List;

public class MicroBit {

    private static MicroBit instance;

    private BluetoothDevice bluetooth_device;
    private String microbit_name;
    private String microbit_address;
    private boolean microbit_connected;
    private boolean microbit_services_discovered;
    private ConnectionStatusListener connection_status_listener;

    private Hashtable<Handle,BluetoothGattService> services;
    // Service Handle, list of characteristics
    private Hashtable<Handle,List<BluetoothGattCharacteristic>> service_characteristics;

    private MicroBit() {
        resetAttributeTables();
    }

    public static synchronized MicroBit getInstance() {
        if (instance == null) {
            instance = new MicroBit();
        }
        return instance;
    }

    public void resetAttributeTables() {
        services = new Hashtable<Handle,BluetoothGattService>();
        service_characteristics = new Hashtable<Handle,List<BluetoothGattCharacteristic>>();
    }

    public BluetoothDevice getBluetooth_device() {
        return bluetooth_device;
    }

    public void setBluetooth_device(BluetoothDevice bluetooth_device) {
        this.bluetooth_device = bluetooth_device;
    }

    public String getMicrobit_name() {
        return microbit_name;
    }

    public void setMicrobit_name(String microbit_name) {
        this.microbit_name = microbit_name;
    }

    public String getMicrobit_address() {
        return microbit_address;
    }

    public void setMicrobit_address(String microbit_address) {
        this.microbit_address = microbit_address;
    }

    public boolean isMicrobit_connected() {
        return microbit_connected;
    }

    public void setMicrobit_connected(boolean microbit_connected) {
        this.microbit_connected = microbit_connected;
        if (connection_status_listener != null) {
            connection_status_listener.connectionStatusChanged(microbit_connected);
        }
    }

    public boolean isMicrobit_services_discovered() {
        return microbit_services_discovered;
    }

    public void setMicrobit_services_discovered(boolean microbit_services_discovered) {
        this.microbit_services_discovered = microbit_services_discovered;
        if (connection_status_listener != null) {
            connection_status_listener.serviceDiscoveryStatusChanged(microbit_services_discovered);
        }
    }

    public ConnectionStatusListener getConnection_status_listener() {
        return connection_status_listener;
    }

    public void setConnection_status_listener(ConnectionStatusListener connection_status_listener) {
        this.connection_status_listener = connection_status_listener;
    }

    public void addService(BluetoothGattService svc) {
        services.put(new Handle(svc.getUuid(), svc.getInstanceId()), svc);
        List<BluetoothGattCharacteristic> characteristics = svc.getCharacteristics();
        service_characteristics.put(new Handle(svc.getUuid(), svc.getInstanceId()), characteristics);
    }

    public boolean hasService(String service_uuid) {
        String svc_uuid = Utility.normaliseUUID(service_uuid).toLowerCase();
        for (BluetoothGattService service : services.values()) {
            Log.d(Constants.TAG,"hasService: "+svc_uuid+"="+service.getUuid().toString()+"?");
            if (service.getUuid().toString().equalsIgnoreCase(svc_uuid)) {
                return true;
            }
        }
        return false;
    }
}
