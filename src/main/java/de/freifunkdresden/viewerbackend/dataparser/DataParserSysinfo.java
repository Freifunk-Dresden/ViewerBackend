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
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashSet;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DataParserSysinfo extends DataParser {

    private static final Logger LOGGER = LogManager.getLogger(DataParserSysinfo.class);

    final JsonObject data;
    private final JsonObject stats;

    public DataParserSysinfo(JsonObject data) {
        this.data = data;
        if (data.has("statistic")) {
            stats = data.get("statistic").getAsJsonObject();
        } else {
            stats = data.get("statistics").getAsJsonObject();
        }
    }

    @Override
    public Long getLastSeen() throws Exception {
        return System.currentTimeMillis();
    }

    int getNodeId() {
        return data.get("common").getAsJsonObject().get("node").getAsInt();
    }

    @Override
    public Community getCommunity() throws Exception {
        return Community.getCommunity(data.get("common").getAsJsonObject().get("city").getAsString());
    }

    @Override
    public NodeType getRole() throws Exception {
        return NodeType.STANDARD;
    }

    @Override
    public String getModel() throws Exception {
        return data.get("system").getAsJsonObject().get("model").getAsString();
    }

    @Override
    public String getFirmwareVersion() throws Exception {
        return data.get("firmware").getAsJsonObject().get("version").getAsString();
    }

    @Override
    public String getFirmwareBase() throws Exception {
        JsonObject firmware = data.get("firmware").getAsJsonObject();
        String distribId = firmware.get("DISTRIB_ID").getAsString();
        String distribRelease = firmware.get("DISTRIB_RELEASE").getAsString();
        String distribRev = firmware.get("DISTRIB_REVISION").getAsString();
        return distribId + " " + distribRelease + " " + distribRev;
    }

    @Override
    public Node getGateway() throws Exception {
        String ip = data.get("bmxd").getAsJsonObject().get("gateways").getAsJsonObject().get("selected").getAsString();
        int id = Node.convertIpToId(ip);
        return DataGen.getDataHolder().getNode(id);
    }

    @Override
    public Float getUptime() throws Exception {
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

    @Override
    public Double getMemoryUsage() throws Exception {
        if (stats.has("meminfo_MemTotal") && stats.has("meminfo_MemFree")) {
            double memTotal = Integer.parseInt(stats.get("meminfo_MemTotal").getAsString().split(" ")[0]);
            double memFree = Integer.parseInt(stats.get("meminfo_MemFree").getAsString().split(" ")[0]);
            return (memTotal - memFree) / memTotal;
        } else {
            return null;
        }
    }

    @Override
    public Float getLoadAvg() throws Exception {
        return Float.parseFloat(stats.get("cpu_load").getAsString().split(" ")[1]);
    }

    @Override
    public Short getClients() throws Exception {
        return stats.get("accepted_user_count").getAsShort();
    }

    @Override
    public HashSet<Link> getLinkSet() throws Exception {
        HashSet<Link> linkmap = new HashSet<>();
        Node node = DataGen.getDataHolder().getNode(getNodeId());
        JsonObject bmxd = data.get("bmxd").getAsJsonObject();
        JsonObject rt = bmxd.has("routing_tables") ? bmxd.get("routing_tables").getAsJsonObject() : bmxd.get("RoutingTables").getAsJsonObject();
        rt.get("route").getAsJsonObject().get("link").getAsJsonArray().forEach((link) -> {
            JsonObject l = link.getAsJsonObject();
            int targetId = Node.convertIpToId(l.get("target").getAsString());
            LinkType linkType = LinkType.getTypeByInterface(l.get("interface").getAsString());
            Node target = DataGen.getDataHolder().getNode(targetId);
            linkmap.add(new Link(linkType, target, node));
        });
        return linkmap;
    }

    @Override
    public String getName() throws Exception {
        try {
            return URLDecoder.decode(data.get("contact").getAsJsonObject().get("name").getAsString(), "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            LOGGER.log(Level.ERROR, "", ex);
            return data.get("contact").getAsJsonObject().get("name").getAsString();
        }
    }

    @Override
    public String getEMail() throws Exception {
        try {
            return URLDecoder.decode(data.get("contact").getAsJsonObject().get("email").getAsString(), "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            LOGGER.log(Level.ERROR, "", ex);
            return data.get("contact").getAsJsonObject().get("email").getAsString();
        }
    }

    @Override
    public Boolean getAutoUpdate() throws Exception {
        return false;
    }

    @Override
    public Location getLocation() throws Exception {
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

    private static int parseMinutes(String time) {
        if (time.contains(":")) {
            return Integer.parseInt(time.split(":")[0]) * 60 + Integer.parseInt(time.split(":")[1]);
        } else {
            return Integer.parseInt(time);
        }
    }

    @Override
    public Boolean isOnline() throws Exception {
        return true;
    }

    @Override
    public Integer getCPUCount() throws Exception {
        JsonObject system = data.get("system").getAsJsonObject();
        return system.has("cpucount") ? system.get("cpucount").getAsInt() : null;
    }
}
