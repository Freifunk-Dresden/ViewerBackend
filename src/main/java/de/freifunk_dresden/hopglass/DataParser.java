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
import java.util.HashMap;

public class DataParser {

    private final JsonObject data;
    private final int version;

    public DataParser(JsonObject data, int version) {
        this.data = data;
        this.version = version;
    }

    public String getCommunity() {
        String com = data.get("common").getAsJsonObject().get("city").getAsString();
        if (com.equals("Meissen")) {
            return "Meißen";
        }
        return com;
    }

    public String getRole() {
        if (version >= 13) {
            String type = data.get("system").getAsJsonObject().get("node_type").getAsString();
            switch(type) {
                case "node":
                    return "standard";
                //@TODO: Include other node types
            }
        }
        return "standard";
    }

    public String getModel() {
        return data.get("system").getAsJsonObject().get("model").getAsString();
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

    public double getUptime() {
        if (version < 10 && data.get("system").getAsJsonObject().get("uptime").getAsString().contains(":")) {
            String[] uptime = data.get("system").getAsJsonObject().get("uptime").getAsString().split(" ");
            int days = Integer.parseInt(uptime[3]);
            double min;
            String minutes = uptime[5].replace(",", "");
            String time = uptime[6].replace(",", "");
            if (minutes.isEmpty()) {
                if (time.contains(":")) {
                    min = Integer.parseInt(time.split(":")[0]) * 60 + Integer.parseInt(time.split(":")[1]);
                } else {
                    min = Integer.parseInt(time);
                }
            } else {
                if (minutes.contains(":")) {
                    min = Integer.parseInt(minutes.split(":")[0]) * 60 + Integer.parseInt(minutes.split(":")[1]);
                } else {
                    min = Integer.parseInt(minutes);
                }
            }
            return min * 60 + days * 86400;
            //Ab v10
        } else {
            return Double.parseDouble(data.get("system").getAsJsonObject().get("uptime").getAsString().split(" ")[0]);
        }
    }

    public double getMemoryUsage() {
        double memTotal = Double.parseDouble(data.get("statistic").getAsJsonObject().get("meminfo_MemTotal").getAsString().split(" ")[0]);
        double memFree = Double.parseDouble(data.get("statistic").getAsJsonObject().get("meminfo_MemFree").getAsString().split(" ")[0]);
        return (memTotal - memFree) / memTotal;
    }

    public double getLoadAvg() {
        return Double.parseDouble(data.get("statistic").getAsJsonObject().get("cpu_load").getAsString().split(" ")[0]);
    }

    public int getClients() {
        return data.get("statistic").getAsJsonObject().get("accepted_user_count").getAsInt();
    }

    public HashMap<Integer, Link> getLinkMap() {
        HashMap<Integer, Link> linkmap = new HashMap<>();
        if (version <= 10) {
            data.get("bmxd").getAsJsonObject().get("routing_tables").getAsJsonObject().get("route").getAsJsonObject().get("link").getAsJsonArray().forEach((link) -> {
                JsonObject l = link.getAsJsonObject();
                String[] split = l.get("target").getAsString().split("\\.");
                int targetId = (Integer.parseInt(split[2]) * 255) + (Integer.parseInt(split[3]) - 1);
                String intf = l.get("interface").getAsString();
                Link lnk = null;
                switch (intf) {
                    case "wlan0":
                        lnk = new Link("wireless");
                        break;
                    case "tbb-fastd":
                    case "tbb_fastd":
                        lnk = new Link("tunnel");
                        break;
                }
                if (lnk != null) {
                    linkmap.put(targetId, lnk);
                }
            });
        }
        if (version == 10) {
            if (data.get("bmxd").getAsJsonObject().has("links")) {
                data.get("bmxd").getAsJsonObject().get("links").getAsJsonArray().forEach((link) -> {
                    JsonObject l = link.getAsJsonObject();
                    Link lnk = linkmap.get(Integer.parseInt(l.get("node").getAsString()));
                    if (lnk != null) {
                        lnk.setTq(Integer.parseInt(l.get("tq").getAsString()));
                    }
                });
            }
        } else if (version >= 11) {
            data.get("bmxd").getAsJsonObject().get("links").getAsJsonArray().forEach((link) -> {
                JsonObject l = link.getAsJsonObject();
                String intf = l.get("interface").getAsString();
                Link lnk = null;
                switch (intf) {
                    case "wlan0":
                        lnk = new Link("wireless", Integer.parseInt(l.get("tq").getAsString()));
                        break;
                    case "br-tbb":
                        lnk = new Link("other", Integer.parseInt(l.get("tq").getAsString()));
                        break;
                    case "tbb-fastd":
                    case "tbb_fastd":
                        lnk = new Link("tunnel", Integer.parseInt(l.get("tq").getAsString()));
                        break;
                }
                if (lnk != null) {
                    linkmap.put(Integer.parseInt(l.get("node").getAsString()), lnk);
                }
            });
        }
        return linkmap;
    }

    public String getName() {
        return data.get("contact").getAsJsonObject().get("name").getAsString();
    }

    public String getEMail() {
        return data.get("contact").getAsJsonObject().get("email").getAsString();
    }
}
