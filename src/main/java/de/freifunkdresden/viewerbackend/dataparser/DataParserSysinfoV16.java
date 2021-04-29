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

import com.google.gson.JsonObject;

public class DataParserSysinfoV16 extends DataParserSysinfoV15 {

    public DataParserSysinfoV16(JsonObject data) {
        super(data);
    }

    @Override
    public short getClients() {
        short client = 0;
        JsonObject stats = data.get("statistic").getAsJsonObject();
        if (stats != null) {
            JsonObject c2g = stats.get("client2g").getAsJsonObject();
            if (c2g != null) {
                client += c2g.get("5min").getAsShort();
            }
            JsonObject c5g = stats.get("client5g").getAsJsonObject();
            if (c5g != null) {
                client += c5g.get("5min").getAsShort();
            }
        }
        return client;
    }

}
