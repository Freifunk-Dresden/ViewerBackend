/*
 * The MIT License
 *
 * Copyright 2017 Niklas Merkelt.
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
import de.freifunkdresden.viewerbackend.DataGen;
import de.freifunkdresden.viewerbackend.Link;
import de.freifunkdresden.viewerbackend.Node;
import java.util.HashSet;

public class DataParserSysinfoV10 extends DataParserSysinfo {

    public DataParserSysinfoV10(JsonObject data) {
        super(data);
    }

    @Override
    public Float getUptime() throws Exception {
        String jsonUptime = data.get("system").getAsJsonObject().get("uptime").getAsString();
        String[] uptime = jsonUptime.split("\\s+");
        return Float.parseFloat(uptime[0]);
    }

    @Override
    public HashSet<Link> getLinkSet() throws Exception {
        HashSet<Link> linkmap = super.getLinkSet();
        JsonObject bmxd = data.get("bmxd").getAsJsonObject();
        bmxd.get("links").getAsJsonArray().forEach(link -> {
            JsonObject l = link.getAsJsonObject();
            Node target = DataGen.getDataHolder().getNode(l.get("node").getAsInt());
            byte tq = Byte.parseByte(l.get("tq").getAsString());
            for (Link lnk : linkmap) {
                if (lnk.getTarget().equals(target)) {
                    lnk.setSourceTq(tq);
                    return;
                }
            }
        });
        return linkmap;
    }
}
