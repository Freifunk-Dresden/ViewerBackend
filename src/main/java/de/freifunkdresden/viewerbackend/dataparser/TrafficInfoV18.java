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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class TrafficInfoV18 extends TrafficInfoV15 {

    private static final Logger LOGGER = LogManager.getLogger(TrafficInfoV18.class);

    @Override
    public void readValues(@NotNull JsonObject networks) {
        for (Interface i : Interface.values()) {
            if (i.getNetworkInfoType().equals(Interface.NetworkInfoType.NO_NETWORK)) {
                continue;
            }
            JsonObject network = networks;
            String networkName = i.getNetworkName();
            if (i.getNetworkInfoType().equals(Interface.NetworkInfoType.DETAILS)) {
                JsonElement details = networks.get("details");
                if (details != null) {
                    network = details.getAsJsonObject();
                }
            }
            if (networkName != null) {
                JsonElement jRx = network.get(String.format("%s_tx", networkName));
                JsonElement jTx = network.get(String.format("%s_rx", networkName));
                if (i.equals(Interface.OVPN)) {
                    // New vpn network values are the wrong way
                    jRx = network.get(String.format("%s_rx", networkName));
                    jTx = network.get(String.format("%s_tx", networkName));
                }
                try {
                    if (jRx != null) {
                        trafficIn.put(i, getInput(i) + jRx.getAsLong());
                    }
                    if (jTx != null) {
                        trafficOut.put(i, getOutput(i) + jTx.getAsLong());
                    }
                } catch (NumberFormatException e) {
                    LOGGER.log(Level.ERROR, String.format("Interface %s: values unreadable (%s,%s)", networkName, jRx, jTx));
                }
            }
        }
    }
}
