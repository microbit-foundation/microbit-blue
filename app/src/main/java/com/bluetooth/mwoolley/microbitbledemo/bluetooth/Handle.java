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
import java.util.UUID;

public class Handle {
    // Android API does not give you access to handles to use as unique ID of GATT attribute so composite of UUID and instance ID being used instead

    private UUID uuid;
    private int instance_id;

    public Handle(UUID uuid, int instance_id) {
        this.uuid = uuid;
        this.instance_id = instance_id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Handle handle = (Handle) o;

        if (instance_id != handle.instance_id) return false;
        return !(uuid != null ? !uuid.equals(handle.uuid) : handle.uuid != null);
    }

    @Override
    public int hashCode() {
        int result = uuid != null ? uuid.hashCode() : 0;
        result = 31 * result + instance_id;
        return result;
    }

    @Override
    public String toString() {
        return "Handle{" +
                "uuid=" + uuid +
                ", instance_id=" + instance_id +
                '}';
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public int getInstance_id() {
        return instance_id;
    }

    public void setInstance_id(int instance_id) {
        this.instance_id = instance_id;
    }
}
