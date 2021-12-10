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

public class TrafficInfoV17 extends TrafficInfoV15 {

    @Override
    public void readValues(JsonObject interfaces) {
        for (Interface i : Interface.values()) {
            for (String interfaceName : i.getInterfaceNames()) {
                String iRxName = String.format("%s_%s", interfaceName, "tx");
                String iTxName = String.format("%s_%s", interfaceName, "rx");

                JsonElement jRx = interfaces.get(iRxName);
                if (jRx != null) {
                    trafficIn.put(i, getInput(i) + jRx.getAsLong());
                }

                JsonElement jTx = interfaces.get(iTxName);
                if (jTx != null) {
                    trafficOut.put(i, getOutput(i) + jTx.getAsLong());
                }
            }
        }
    }
}
