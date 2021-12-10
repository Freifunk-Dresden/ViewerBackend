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

public class DataParserSysInfoV15 extends DataParserSysInfoV14 {

    public DataParserSysInfoV15(JsonObject data) {
        super(data);
    }

    @Override
    public short getClients() {
        JsonElement clients = data.get("statistic").getAsJsonObject().get("clients");
        if (clients != null && clients.isJsonArray()) {
            return clients.getAsJsonArray().get(1).getAsShort();
        }
        return super.getClients();
    }

    @Override
    public TrafficInfo getTraffic() {
        TrafficInfo ti = new TrafficInfo();
        ti.readValues(data.get("statistic").getAsJsonObject());
        return ti;
    }

    @Override
    public String getFirmwareBase() {
        return data.get("firmware").getAsJsonObject().get("DISTRIB_DESCRIPTION").getAsString();
    }
}
