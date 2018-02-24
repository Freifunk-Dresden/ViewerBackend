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
package de.freifunk_dresden.hopglass;

import com.google.gson.JsonObject;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.logging.Level;

public class DataParser {

    private final JsonObject data;
    private final JsonObject stats;
    private final int version;

    public DataParser(JsonObject data, int version) {
        this.data = data;
        this.version = version;
        if (data.has("statistic")) {
            stats = data.get("statistic").getAsJsonObject();
        } else {
            stats = data.get("statistics").getAsJsonObject();
        }
    }

    public int getNodeId() {
        return data.get("common").getAsJsonObject().get("node").getAsInt();
    }

    public String getCommunity() {
        String com = data.get("common").getAsJsonObject().get("city").getAsString();
        if (com.equals("Meissen")) {
            return "Meißen";
        }
        return com;
    }

    public NodeType getRole() {
        if (version >= 13) {
            switch (data.get("system").getAsJsonObject().get("node_type").getAsString()) {
                case "node":
                    return NodeType.STANDARD;
                //@TODO: Include other node types
            }
        }
        return NodeType.STANDARD;
    }

    public String getModel() {
        return data.get("system").getAsJsonObject().get(version >= 14 ? "model2" : "model").getAsString();
    }

    public String getFirmwareVersion() {
        return data.get("firmware").getAsJsonObject().get("version").getAsString();
    }

    public String getFirmwareBase() {
        JsonObject firmware = data.get("firmware").getAsJsonObject();
        String DISTRIB_ID = firmware.get("DISTRIB_ID").getAsString();
        String DISTRIB_RELEASE = firmware.get("DISTRIB_RELEASE").getAsString();
        String DISTRIB_REVISION = firmware.get("DISTRIB_REVISION").getAsString();
        return DISTRIB_ID + " " + DISTRIB_RELEASE + " " + DISTRIB_REVISION;
    }

    public String getGatewayIp() {
        return data.get("bmxd").getAsJsonObject().get("gateways").getAsJsonObject().get("selected").getAsString();
    }

    public float getUptime() {
        String jsonUptime = data.get("system").getAsJsonObject().get("uptime").getAsString();
        String[] uptime = jsonUptime.replace("  ", " ").split(" ");
        if (version < 10 && jsonUptime.contains(":")) {
            if (uptime[3].replace(",", "").contains(":")) {
                return parseMinutes(uptime[3].replace(",", "")) * 60;
            } else {
                short days = Short.parseShort(uptime[3].replace(",", ""));
                int min;
                String minutes = uptime[5].replace(",", "");
                String time = uptime[6].replace(",", "");
                if (minutes.isEmpty()) {
                    min = parseMinutes(time);
                } else {
                    min = parseMinutes(minutes);
                }
                return min * 60 + days * 86400;
            }
            //Ab v10
        } else {
            return Float.parseFloat(uptime[0]);
        }
    }

    public double getMemoryUsage() {
        if (stats.has("meminfo_MemTotal") && stats.has("meminfo_MemFree")) {
            double memTotal = Integer.parseInt(stats.get("meminfo_MemTotal").getAsString().split(" ")[0]);
            double memFree = Integer.parseInt(stats.get("meminfo_MemFree").getAsString().split(" ")[0]);
            return (memTotal - memFree) / memTotal;
        } else {
            return 0;
        }
    }

    public float getLoadAvg() {
        return Float.parseFloat(stats.get("cpu_load").getAsString().split(" ")[0]);
    }

    public short getClients() {
        return stats.get("accepted_user_count").getAsShort();
    }

    public HashSet<Link> getLinkSet() {
        HashSet<Link> linkmap = new HashSet<>();
        Node node = DataGen.getNode(getNodeId());
        JsonObject bmxd = data.get("bmxd").getAsJsonObject();
        if (version <= 10) {
            JsonObject rt = bmxd.has("routing_tables") ? bmxd.get("routing_tables").getAsJsonObject() : bmxd.get("RoutingTables").getAsJsonObject();
            rt.get("route").getAsJsonObject().get("link").getAsJsonArray().forEach((link) -> {
                JsonObject l = link.getAsJsonObject();
                String[] split = l.get("target").getAsString().split("\\.");
                int targetId = (Integer.parseInt(split[2]) * 255) + (Integer.parseInt(split[3]) - 1);
                LinkType linkType = LinkType.getType(l.get("interface").getAsString());
                Node target = DataGen.getNode(targetId);
                linkmap.add(new Link(linkType, target, node));
            });
        }
        if (bmxd.has("links")) {
            bmxd.get("links").getAsJsonArray().forEach((link) -> {
                JsonObject l = link.getAsJsonObject();
                Node target = DataGen.getNode(l.get("node").getAsInt());
                byte tq = Byte.parseByte(l.get("tq").getAsString());
                if (version == 10) {
                    for (Link lnk : linkmap) {
                        if (lnk.getTarget().equals(target)) {
                            lnk.setSourceTq(tq);
                            return;
                        }
                    }
                } else if (version >= 11) {
                    LinkType linkType = LinkType.getType(l.get("interface").getAsString());
                    linkmap.add(new Link(linkType, tq, target, node));
                }
            });
        }
        return linkmap;
    }

    public String getName() {
        try {
            return URLDecoder.decode(data.get("contact").getAsJsonObject().get("name").getAsString(), "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            DataGen.getLogger().log(Level.SEVERE, null, ex);
            return data.get("contact").getAsJsonObject().get("name").getAsString();
        }
    }

    public String getEMail() {
        return data.get("contact").getAsJsonObject().get("email").getAsString();
    }

    public boolean getAutoUpdate() {
        return version >= 14 ? data.get("system").getAsJsonObject().get("autoupdate").getAsInt() == 1 : false;
    }

    public static int parseMinutes(String time) {
        if (time.contains(":")) {
            return Integer.parseInt(time.split(":")[0]) * 60 + Integer.parseInt(time.split(":")[1]);
        } else {
            return Integer.parseInt(time);
        }
    }
}
