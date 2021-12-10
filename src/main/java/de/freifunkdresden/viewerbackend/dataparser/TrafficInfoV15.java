/*
 * The MIT License
 *
 * Copyright 2020 Niklas Merkelt.
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

public class TrafficInfoV15 extends TrafficInfo {

    public void readValues(JsonObject stats) {
        boolean fromTo = false;
        for (Interface out : Interface.values()) {
            for (Interface in : Interface.values()) {
                String name = String.format("traffic_%s_%s", out.name().toLowerCase(), in.name().toLowerCase());
                JsonElement j = stats.get(name);
                if (j != null && !j.getAsString().isEmpty()) {
                    fromTo = true;
                    trafficOut.put(out, getOutput(out) + j.getAsLong());
                    trafficIn.put(in, getInput(in) + j.getAsLong());
                }
            }
        }
        if (fromTo) {
            return;
        }
        for (Interface i : Interface.values()) {
            String name = String.format("traffic_%s", i.name().toLowerCase());
            JsonElement j = stats.get(name);
            if (j != null) {
                String[] t = j.getAsString().split(",");
                if (t.length == 2) {
                    trafficIn.put(i, Long.parseLong(t[0]));
                    trafficOut.put(i, Long.parseLong(t[1]));
                }
            }
        }
    }
}
