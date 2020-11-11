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
import de.freifunkdresden.viewerbackend.LinkType;
import de.freifunkdresden.viewerbackend.Node;

import java.util.HashSet;
import java.util.Set;

public class DataParserSysinfoV14 extends DataParserSysinfoV13 {

    public DataParserSysinfoV14(JsonObject data) {
        super(data);
    }

    @Override
    public String getModel() {
        return data.get("system").getAsJsonObject().get("model2").getAsString();
    }

    @Override
    public Set<Link> getLinkSet() {
        HashSet<Link> linkmap = new HashSet<>();
        Node node = DataGen.getDataHolder().getNode(getNodeId());
        JsonObject bmxd = data.get("bmxd").getAsJsonObject();
        bmxd.get("links").getAsJsonArray().forEach(link -> {
            JsonObject l = link.getAsJsonObject();
            Node target = DataGen.getDataHolder().getNode(l.get("node").getAsInt());
            byte tq = Byte.parseByte(l.get("tq").getAsString());
            LinkType linkType = LinkType.getTypeByType(l.get("type").getAsString());
            linkmap.add(new Link(linkType, tq, target, node));
        });
        return linkmap;
    }

    @Override
    public boolean getAutoUpdate() {
        return data.get("system").getAsJsonObject().get("autoupdate").getAsInt() == 1;
    }
}
