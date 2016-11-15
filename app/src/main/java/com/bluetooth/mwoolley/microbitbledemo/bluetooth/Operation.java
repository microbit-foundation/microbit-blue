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
import com.bluetooth.mwoolley.microbitbledemo.Utility;

import java.util.Arrays;


public class Operation {

    public static final int OPERATION_READ_CHARACTERISTIC_REQUEST  = 1;
    public static final int OPERATION_WRITE_CHARACTERISTIC_REQUEST = 2;
    public static final int OPERATION_WRITE_DESCRIPTOR_REQUEST = 3;
    public static final int OPERATION_EXIT_QUEUE_PROCESSING_REQUEST  = -1;

    public static final int OPERATION_PENDING = 0;
    public static final int OPERATION_EXECUTING = 1;

    private int operation_type;
    private int operation_status;
    private String service_uuid;
    private String characteristic_uuid;
    private String descriptor_uuid;
    private byte [] value;
    private boolean subscribe=false;

    public Operation(int operation_type, String service_uuid, String characteristic_uuid, String descriptor_uuid, byte [] value) {
        this.operation_type = operation_type;
        this.service_uuid = service_uuid;
        this.characteristic_uuid = characteristic_uuid;
        this.descriptor_uuid = descriptor_uuid;
        this.value = value;
    }

    public Operation(int operation_type, String service_uuid, String characteristic_uuid) {
        this.operation_type = operation_type;
        this.service_uuid = service_uuid;
        this.characteristic_uuid = characteristic_uuid;
    }

    public Operation(int operation_type, String service_uuid, String characteristic_uuid, byte [] value) {
        this.operation_type = operation_type;
        this.service_uuid = service_uuid;
        this.characteristic_uuid = characteristic_uuid;
        this.value = value;
    }

    public Operation(int operation_type, String service_uuid, String characteristic_uuid, boolean subscribe, byte [] value) {
        this.operation_type = operation_type;
        this.service_uuid = service_uuid;
        this.characteristic_uuid = characteristic_uuid;
        this.subscribe = subscribe;
        this.value = value;
    }

    public Operation(int operation_type) {
        this.operation_type = operation_type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Operation operation = (Operation) o;

        if (operation_type != operation.operation_type) return false;
        if (!service_uuid.equals(operation.service_uuid)) return false;
        if (!characteristic_uuid.equals(operation.characteristic_uuid)) return false;
        return descriptor_uuid.equals(operation.descriptor_uuid);

    }

    @Override
    public int hashCode() {
        int result = operation_type;
        result = 31 * result + service_uuid.hashCode();
        result = 31 * result + characteristic_uuid.hashCode();
        result = 31 * result + descriptor_uuid.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Operation{" +
                "operation_type=" + operation_type +
                ", operation_status=" + operation_status +
                ", service_uuid='" + service_uuid + '\'' +
                ", characteristic_uuid='" + characteristic_uuid + '\'' +
                ", descriptor_uuid='" + descriptor_uuid + '\'' +
                ", value=" + Utility.byteArrayAsHexString(value) +
                '}';
    }

    public int getOperation_type() {
        return operation_type;
    }

    public void setOperation_type(int operation_type) {
        this.operation_type = operation_type;
    }

    public String getService_uuid() {
        return service_uuid;
    }

    public void setService_uuid(String service_uuid) {
        this.service_uuid = service_uuid;
    }

    public String getCharacteristic_uuid() {
        return characteristic_uuid;
    }

    public void setCharacteristic_uuid(String characteristic_uuid) {
        this.characteristic_uuid = characteristic_uuid;
    }

    public String getDescriptor_uuid() {
        return descriptor_uuid;
    }

    public void setDescriptor_uuid(String descriptor_uuid) {
        this.descriptor_uuid = descriptor_uuid;
    }

    public int getOperation_status() {
        return operation_status;
    }

    public void setOperation_status(int operation_status) {
        this.operation_status = operation_status;
    }

    public byte[] getValue() {
        return value;
    }

    public void setValue(byte[] value) {
        this.value = value;
    }

    public boolean isSubscribe() {
        return subscribe;
    }

    public void setSubscribe(boolean subscribe) {
        this.subscribe = subscribe;
    }
}
