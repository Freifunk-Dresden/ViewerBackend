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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.freifunkdresden.viewerbackend.Airtime;
import de.freifunkdresden.viewerbackend.Community;
import de.freifunkdresden.viewerbackend.DataGen;
import de.freifunkdresden.viewerbackend.Link;
import de.freifunkdresden.viewerbackend.LinkType;
import de.freifunkdresden.viewerbackend.Location;
import de.freifunkdresden.viewerbackend.Node;
import de.freifunkdresden.viewerbackend.NodeType;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class DataParserSysInfo {

    private static final Logger LOGGER = LogManager.getLogger(DataParserSysInfo.class);

    final JsonObject data;
    private final JsonObject stats;
    private final long lastSeen = System.currentTimeMillis();
    protected Community community;
    protected AtomicInteger linkCountFastD = new AtomicInteger(0);
    protected AtomicInteger linkCountWireGuard = new AtomicInteger(0);
    protected Collection<Link> linkCollection = Collections.emptyList();
    protected boolean linksProcessed = false;

    public DataParserSysInfo(@NotNull JsonObject data) {
        this.data = data;
        if (data.has("statistic")) {
            stats = data.get("statistic").getAsJsonObject();
        } else {
            stats = data.get("statistics").getAsJsonObject();
        }
        checkCommunity();
    }

    protected void checkCommunity() {
        String c = data.get("common").getAsJsonObject().get("city").getAsString();
        community = Community.getCommunity(c);
        if (community == Community.DEFAULT) {
            LOGGER.log(Level.WARN, "Node {} has invalid community `{}` (Name: {})", getNodeId(), c, getName());
        }
    }

    public long getLastSeen() {
        return lastSeen;
    }

    int getNodeId() {
        return data.get("common").getAsJsonObject().get("node").getAsInt();
    }

    public Community getCommunity() {
        return community;
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
        String distributionId = firmware.get("DISTRIB_ID").getAsString();
        String distributionRelease = firmware.get("DISTRIB_RELEASE").getAsString();
        if (distributionRelease.contains("SNAPSHOT")) {
            String distributionRevision = firmware.get("DISTRIB_REVISION").getAsString();
            return String.format("%s %s %s", distributionId, distributionRelease, distributionRevision);
        } else {
            return String.format("%s %s", distributionId, distributionRelease);
        }
    }

    public String getFirmwareTarget() {
        return data.get("firmware").getAsJsonObject().get("DISTRIB_TARGET").getAsString();
    }

    public String getFirmwareBranch() {
        JsonElement branch = data.get("firmware").getAsJsonObject().get("git-ddmesh-branch");
        return branch != null ? branch.getAsString() : null;
    }

    public String getFirmwareGitRev() {
        JsonElement rev = data.get("firmware").getAsJsonObject().get("git-ddmesh-rev");
        return rev != null ? rev.getAsString() : null;
    }

    public Node getGateway() {
        String ip = data.get("bmxd").getAsJsonObject().get("gateways").getAsJsonObject().get("selected").getAsString();
        return DataGen.getDataHolder().getNodeByIp(ip);
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
        if (stats.has("accepted_user_count")) {
            return stats.get("accepted_user_count").getAsShort();
        }
        return 0;
    }

    public Collection<Link> getLinkSet() {
        return linkCollection;
    }

    public void processLinks() {
        if (linksProcessed) {
            return;
        }
        Node node = DataGen.getDataHolder().getNode(getNodeId());
        JsonObject bmxd = data.get("bmxd").getAsJsonObject();
        JsonObject rt = bmxd.has("routing_tables") ? bmxd.get("routing_tables").getAsJsonObject() : bmxd.get("RoutingTables").getAsJsonObject();
        JsonArray linkArray = rt.get("route").getAsJsonObject().get("link").getAsJsonArray();
        linkCollection = new ArrayList<>(linkArray.size());
        linkArray.forEach(link -> {
            JsonObject l = link.getAsJsonObject();
            String linkInterface = l.get("interface").getAsString();
            if (linkInterface.startsWith("tbb_fastd")) {
                linkCountFastD.incrementAndGet();
            } else if (linkInterface.startsWith("tbb_wg")) {
                linkCountWireGuard.incrementAndGet();
            }
            LinkType linkType = LinkType.getTypeByInterface(linkInterface);
            Node target = DataGen.getDataHolder().getNodeByIp(l.get("target").getAsString());
            linkCollection.add(new Link(linkType, target, node));
        });
        linksProcessed = true;
    }

    public int getLinkCountFastD() {
        return linkCountFastD.get();
    }

    public int getLinkCountWireGuard() {
        return linkCountWireGuard.get();
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

    public Optional<Airtime> getAirtime2g() {
        return getAirtime("radio2g");
    }

    public Optional<Airtime> getAirtime5g() {
        return getAirtime("radio5g");
    }

    public TrafficInfo getTraffic() {
        return new TrafficInfoEmpty();
    }

    public Optional<Integer> getWifiChannel2g() {
        switch (getRole()) {
            case STANDARD:
            case MOBILE:
                if (getAirtime2g().isPresent()) {
                    return Optional.of(13);
                } else {
                    return Optional.empty();
                }
            default:
                return Optional.empty();
        }
    }

    public Optional<Integer> getWifiChannel5g() {
        switch (getRole()) {
            case STANDARD:
            case MOBILE:
                if (getAirtime5g().isPresent()) {
                    return Optional.of(44);
                } else {
                    return Optional.empty();
                }
            default:
                return Optional.empty();
        }
    }

    private static int parseMinutes(@NotNull String time) {
        if (time.contains(":")) {
            return Integer.parseInt(time.split(":")[0]) * 60 + Integer.parseInt(time.split(":")[1]);
        } else {
            return Integer.parseInt(time);
        }
    }

    private Optional<Airtime> getAirtime(String radio) {
        JsonElement airtime = data.get("airtime");
        if (airtime != null && airtime.getAsJsonObject().has(radio)) {
            String at = airtime.getAsJsonObject().get(radio).getAsString();
            if (!at.isEmpty()) {
                return parseAirtime(at);
            }
        }
        return Optional.empty();
    }

    private Optional<Airtime> parseAirtime(@NotNull String airtime) {
        String[] split = airtime.split(",");
        if (split.length != 4) {
            LOGGER.log(Level.WARN, "Malformed airtime string (Node: {})", getNodeId());
            return Optional.empty();
        }
        try {
            return Optional.of(new Airtime(Long.parseLong(split[0]), Long.parseLong(split[1]), Long.parseLong(split[2]),
                    Long.parseLong(split[3])));
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            LOGGER.log(Level.ERROR, String.format("Airtime format (Node: %d)", getNodeId()), e);
            return Optional.empty();
        }
    }
}
