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
import de.freifunkdresden.viewerbackend.Community;
import de.freifunkdresden.viewerbackend.DataGen;
import de.freifunkdresden.viewerbackend.Link;
import de.freifunkdresden.viewerbackend.LinkType;
import de.freifunkdresden.viewerbackend.Location;
import de.freifunkdresden.viewerbackend.Node;
import de.freifunkdresden.viewerbackend.NodeType;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DataParserSysinfo {

    private static final Logger LOGGER = LogManager.getLogger(DataParserSysinfo.class);

    final JsonObject data;
    private final JsonObject stats;
    private final long lastSeen = System.currentTimeMillis();

    public DataParserSysinfo(JsonObject data) {
        this.data = data;
        if (data.has("statistic")) {
            stats = data.get("statistic").getAsJsonObject();
        } else {
            stats = data.get("statistics").getAsJsonObject();
        }
    }

    public long getLastSeen() {
        return lastSeen;
    }

    int getNodeId() {
        return data.get("common").getAsJsonObject().get("node").getAsInt();
    }

    public Community getCommunity() {
        return Community.getCommunity(data.get("common").getAsJsonObject().get("city").getAsString());
    }

    public NodeType getRole() {
        return NodeType.STANDARD;
    }

    public String getModel() {
        return data.get("system").getAsJsonObject().get("model").getAsString();
    }

    public String getFirmwareVersion() {
        return data.get("firmware").getAsJsonObject().get("version").getAsString();
    }

    public String getFirmwareBase() {
        JsonObject firmware = data.get("firmware").getAsJsonObject();
        String distribId = firmware.get("DISTRIB_ID").getAsString();
        String distribRelease = firmware.get("DISTRIB_RELEASE").getAsString();
        String distribRev = firmware.get("DISTRIB_REVISION").getAsString();
        return distribId + " " + distribRelease + " " + distribRev;
    }

    public Node getGateway() {
        String ip = data.get("bmxd").getAsJsonObject().get("gateways").getAsJsonObject().get("selected").getAsString();
        int id = Node.convertIpToId(ip);
        return DataGen.getDataHolder().getNode(id);
    }

    public float getUptime() {
        String jsonUptime = data.get("system").getAsJsonObject().get("uptime").getAsString();
        String[] uptime = jsonUptime.split("\\s+");
        if (jsonUptime.contains(":")) {
            String array3 = uptime[3].replace(",", "");
            if (array3.contains(":")) {
                return parseMinutes(array3) * 60f;
            } else {
                short days = Short.parseShort(array3);
                int min;
                String minutes = uptime[5].replace(",", "");
                String time = uptime[6].replace(",", "");
                if (minutes.isEmpty()) {
                    min = parseMinutes(time);
                } else {
                    min = parseMinutes(minutes);
                }
                return min * 60f + days * 86400f;
            }
            //Ab v10
        } else {
            return Float.parseFloat(uptime[0]);
        }
    }

    public double getMemoryUsage() {
        double memTotal = Integer.parseInt(stats.get("meminfo_MemTotal").getAsString().split(" ")[0]);
        double memFree = Integer.parseInt(stats.get("meminfo_MemFree").getAsString().split(" ")[0]);
        return (memTotal - memFree) / memTotal;
    }

    public float getLoadAvg() {
        return Float.parseFloat(stats.get("cpu_load").getAsString().split(" ")[1]);
    }

    public short getClients() {
        return stats.get("accepted_user_count").getAsShort();
    }

    public HashSet<Link> getLinkSet() {
        HashSet<Link> linkmap = new HashSet<>();
        Node node = DataGen.getDataHolder().getNode(getNodeId());
        JsonObject bmxd = data.get("bmxd").getAsJsonObject();
        JsonObject rt = bmxd.has("routing_tables") ? bmxd.get("routing_tables").getAsJsonObject() : bmxd.get("RoutingTables").getAsJsonObject();
        rt.get("route").getAsJsonObject().get("link").getAsJsonArray().forEach(link -> {
            JsonObject l = link.getAsJsonObject();
            int targetId = Node.convertIpToId(l.get("target").getAsString());
            LinkType linkType = LinkType.getTypeByInterface(l.get("interface").getAsString());
            Node target = DataGen.getDataHolder().getNode(targetId);
            linkmap.add(new Link(linkType, target, node));
        });
        return linkmap;
    }

    public String getName() {
        String name = data.get("contact").getAsJsonObject().get("name").getAsString();
        return URLDecoder.decode(name, StandardCharsets.UTF_8);
    }

    public String getEMail() {
        String email = data.get("contact").getAsJsonObject().get("email").getAsString();
        return URLDecoder.decode(email, StandardCharsets.UTF_8);
    }

    public boolean getAutoUpdate() {
        return false;
    }

    public Location getLocation() {
        try {
            JsonObject gps = data.get("gps").getAsJsonObject();
            double lat = gps.get("latitude").getAsDouble();
            lat = lat == 0 ? Double.NaN : lat;
            double lon = gps.get("longitude").getAsDouble();
            lon = lon == 0 ? Double.NaN : lon;
            return new Location(lat, lon);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public int getCPUCount() {
        JsonObject system = data.get("system").getAsJsonObject();
        return system.has("cpucount") ? system.get("cpucount").getAsInt() : 0;
    }

    private static int parseMinutes(String time) {
        if (time.contains(":")) {
            return Integer.parseInt(time.split(":")[0]) * 60 + Integer.parseInt(time.split(":")[1]);
        } else {
            return Integer.parseInt(time);
        }
    }
}
