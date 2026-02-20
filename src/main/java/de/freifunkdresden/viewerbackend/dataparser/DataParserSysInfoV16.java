/*
 * The MIT License
 *
 * Copyright 2021 NMerkelt.
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

import java.util.Optional;

public class DataParserSysInfoV16 extends DataParserSysInfoV15 {

    public DataParserSysInfoV16(JsonObject data) {
        super(data);
    }

    @Override
    public short getClients() {
        short client = 0;
        Optional<Short> c2g = getClients2g();
        if (c2g.isPresent()) {
            client += c2g.get();
        }
        Optional<Short> c5g = getClients5g();
        if (c5g.isPresent()) {
            client += c5g.get();
        }
        return client;
    }

    @Override
    public Optional<Short> getClients2g() {
        JsonElement stats = data.get("statistic");
        if (stats != null) {
            JsonElement c2g = stats.getAsJsonObject().get("client2g");
            if (c2g != null) {
                return Optional.of(c2g.getAsJsonObject().get("5min").getAsShort());
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Short> getClients5g() {
        JsonElement stats = data.get("statistic");
        if (stats != null) {
            JsonElement c2g = stats.getAsJsonObject().get("client5g");
            if (c2g != null) {
                return Optional.of(c2g.getAsJsonObject().get("5min").getAsShort());
            }
        }
        return Optional.empty();
    }
}
