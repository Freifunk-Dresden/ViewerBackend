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
import org.jetbrains.annotations.NotNull;

public class DataParserAPI {

    private final JsonObject node;
    private final JsonObject status;

    public DataParserAPI(@NotNull JsonObject node) {
        this.node = node;
        this.status = node.get("status").getAsJsonObject();
    }

    public short getClients() {
        return status.get("clients").getAsShort();
    }

    public Location getLocation() {
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

    public long getFirstSeen() {
        return status.get("firstseen").getAsLong() * 1000;
    }

    public long getLastSeen() {
        return status.get("lastseen").getAsLong() * 1000;
    }

    public boolean isGateway() {
        return status.get("gateway").getAsBoolean();
    }

    public boolean hasBackbone() {
        return status.get("backbone").getAsInt() == 1;
    }

    public String getName() {
        return node.get("name").getAsString();
    }

    public String getModel() {
        return node.get("model").getAsString();
    }

    public String getFirmwareVersion() {
        return node.get("firmware").getAsString();
    }

    public boolean getAutoUpdate() {
        return status.get("autoupdate").getAsInt() == 1;
    }
}
