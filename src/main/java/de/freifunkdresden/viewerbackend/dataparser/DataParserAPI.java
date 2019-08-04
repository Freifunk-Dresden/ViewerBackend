/*
 * The MIT License
 *
 * Copyright 2018 Niklas Merkelt.
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
import de.freifunkdresden.viewerbackend.Location;

public class DataParserAPI extends DataParser {

    private final JsonObject node;

    public DataParserAPI(JsonObject node) {
        this.node = node;
    }

    @Override
    public Short getClients() throws Exception {
        return node.get("status").getAsJsonObject().get("clients").getAsShort();
    }

    @Override
    public Location getLocation() throws Exception {
        try {
            double lat = node.get("position").getAsJsonObject().get("lat").getAsDouble();
            lat = lat == 0 ? Double.NaN : lat;
            double lon = node.get("position").getAsJsonObject().get("lon").getAsDouble();
            lon = lon == 0 ? Double.NaN : lon;
            return new Location(lat, lon);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    @Override
    public Boolean isOnline() throws Exception {
        return node.get("status").getAsJsonObject().get("online").getAsBoolean();
    }

    @Override
    public Long getFirstseen() throws Exception {
        return node.get("status").getAsJsonObject().get("firstseen").getAsLong() * 1000;
    }

    @Override
    public Long getLastseen() throws Exception {
        return node.get("status").getAsJsonObject().get("lastseen").getAsLong() * 1000;
    }

    @Override
    public Boolean isGateway() throws Exception {
        return node.get("status").getAsJsonObject().get("gateway").getAsBoolean();
    }

    @Override
    public Boolean hasUplink() throws Exception {
        return node.get("status").getAsJsonObject().get("backbone").getAsBoolean();
    }
}
