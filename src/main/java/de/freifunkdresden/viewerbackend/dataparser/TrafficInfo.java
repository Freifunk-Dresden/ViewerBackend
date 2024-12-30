/*
 * The MIT License
 *
 * Copyright 2021 Niklas Merkelt.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package de.freifunkdresden.viewerbackend.dataparser;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public abstract class TrafficInfo {

    protected final Map<Interface, Long> trafficIn = new EnumMap<>(Interface.class);
    protected final Map<Interface, Long> trafficOut = new EnumMap<>(Interface.class);

    public abstract void readValues(JsonObject json);

    public boolean isEmpty() {
        return trafficIn.isEmpty() && trafficOut.isEmpty();
    }

    public boolean hasInterface(Interface i) {
        return trafficIn.containsKey(i) || trafficOut.containsKey(i);
    }

    public long getInput(Interface i) {
        return trafficIn.getOrDefault(i, 0L);
    }

    public long getOutput(Interface i) {
        return trafficOut.getOrDefault(i, 0L);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Interface i : Interface.values()) {
            if (hasInterface(i)) {
                sb.append(String.format("%s: %d<>%d%n", i.name(), getInput(i), getOutput(i)));
            }
        }
        return sb.toString();
    }

    public enum Interface {
        WAN(
                new String[]{"wan"},
                NetworkInfoType.BASIC,
                "wan"
        ),
        ADHOC(
                new String[]{"adhoc"},
                NetworkInfoType.NO_NETWORK,
                null,
                "wifi_adhoc"
        ),
        AP(
                new String[]{"ap"},
                NetworkInfoType.BASIC,
                "ap",
                "wifi2"
        ),
        OVPN(
                new String[]{"ovpn"},
                NetworkInfoType.BASIC,
                "vpn"
        ),
        TBB_FASTD(
                new String[]{"tbb_fastd"},
                NetworkInfoType.DETAILS,
                "backbone_mesh_fastd",
                "tbb_fastd"
        ),
        TBB_WG(
                new String[]{"tbb_wg"},
                NetworkInfoType.DETAILS,
                "backbone_mesh_wg",
                "tbb_wg"
        ),
        MESH_CABLE(
                new String[]{"mesh_lan", "mesh_wan", "mesh_vlan"},
                NetworkInfoType.BASIC,
                "cable_mesh",
                "mesh_lan", "mesh_wan", "mesh_vlan"
        ),
        MESH2G(
                new String[]{"mesh2g"},
                NetworkInfoType.DETAILS,
                "wifi_mesh_2g",
                "wifi_mesh2g"
        ),
        MESH5G(
                new String[]{"mesh5g"},
                NetworkInfoType.DETAILS,
                "wifi_mesh_5g",
                "wifi_mesh5g"
        ),
        ;

        private final List<String> interfaceNamePre17;
        private final NetworkInfoType networkInfoType;
        private final String networkName;
        private final List<String> interfaceNames;

        Interface(@NotNull String[] interfaceNamePre17, @NotNull NetworkInfoType networkType, @Nullable String networkName, @NotNull String... interfaceNames) {
            this.interfaceNamePre17 = Arrays.asList(interfaceNamePre17);
            this.networkInfoType = networkType;
            this.networkName = networkName;
            this.interfaceNames = Arrays.asList(interfaceNames);
        }

        public @NotNull List<String> getInterfaceNamePre17() {
            return interfaceNamePre17;
        }

        public @NotNull String getStatName() {
            return this.name().toLowerCase();
        }

        public @NotNull NetworkInfoType getNetworkInfoType() {
            return networkInfoType;
        }

        public @Nullable String getNetworkName() {
            return networkName;
        }

        public @NotNull List<String> getInterfaceNames() {
            return interfaceNames;
        }

        public enum NetworkInfoType {
            NO_NETWORK,
            BASIC,
            DETAILS,
        }
    }
}
