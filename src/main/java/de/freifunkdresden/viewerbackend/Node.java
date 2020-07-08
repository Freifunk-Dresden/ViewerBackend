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
package de.freifunkdresden.viewerbackend;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.freifunkdresden.viewerbackend.dataparser.DataParser;
import de.freifunkdresden.viewerbackend.stats.StatsSQL;
import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Node {

    private static final Logger LOGGER = LogManager.getLogger(Node.class);

    private final int id;
    private final HashSet<Link> linkSet = new HashSet<>();
    private String name;
    private Community community = Community.DRESDEN;
    private NodeType role = NodeType.STANDARD;
    private String model;
    private String firmwareVersion;
    private String firmwareBase;
    private String email;
    private boolean online = false;
    private float uptime;
    private double memoryUsage;
    private short clients;
    private float loadAvg;
    private boolean isGateway;
    private boolean backbone;
    private long lastseen = -1;
    private long firstseen = -1;
    private Location location;
    private Node gateway;
    private boolean valid = false;
    private boolean autoUpdate;
    private int nproc = 1;

    public Node(int id) {
        this.id = id;
    }

    public String getIpAddress() {
        return getIpAddress(false);
    }

    public String getIpAddress(boolean subnet201) {
        return String.format("10.%s.%s.%s", (subnet201 ? "201" : "200"), (id / 255), ((id % 255) + 1));
    }

    public void fill(DataParser dp) {
        try {
            if (dp.getName() != null) {
                name = dp.getName();
            }
            if (dp.getCommunity() != null) {
                community = dp.getCommunity();
            }
            if (dp.getRole() != null) {
                role = dp.getRole();
            }
            if (dp.getModel() != null) {
                model = dp.getModel();
            }
            if (dp.getFirmwareVersion() != null) {
                firmwareVersion = dp.getFirmwareVersion();
            }
            if (dp.getFirmwareBase() != null) {
                firmwareBase = dp.getFirmwareBase();
            }
            if (dp.getEMail() != null) {
                email = dp.getEMail();
            }
            if (dp.getUptime() != null) {
                uptime = dp.getUptime();
            }
            if (dp.getMemoryUsage() != null) {
                memoryUsage = dp.getMemoryUsage();
            }
            if (dp.getClients() != null) {
                clients = dp.getClients();
            }
            if (dp.getLoadAvg() != null) {
                loadAvg = dp.getLoadAvg();
            }
            if (dp.getGateway() != null) {
                gateway = dp.getGateway();
            }
            if (dp.getLinkSet() != null) {
                linkSet.addAll(dp.getLinkSet());
            }
            if (dp.getAutoUpdate() != null) {
                autoUpdate = dp.getAutoUpdate();
            }
            if (dp.getLocation() != null) {
                location = dp.getLocation();
            }
            if (dp.isOnline() != null) {
                online = dp.isOnline();
            }
            if (dp.isGateway() != null) {
                isGateway = dp.isGateway();
            }
            if (dp.hasBackbone() != null) {
                backbone = dp.hasBackbone();
            }
            if (dp.getLastSeen() != null) {
                lastseen = dp.getLastSeen();
            }
            if (dp.getFirstSeen() != null) {
                firstseen = dp.getFirstSeen();
            }
            if (dp.getCPUCount() != null) {
                nproc = dp.getCPUCount();
            }
            valid = true;
        } catch (Exception e) {
            valid = false;
            LOGGER.log(Level.ERROR, "Node {}", e, getId());
        }
    }

    public int getId() {
        return id;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public boolean isValid() {
        return valid && !isTemporaryNode();
    }

    public boolean isDisplayed() {
        //display only nodes lastseen within the last 30 days
        return isValid() && (lastseen > System.currentTimeMillis() - (1000L * 60 * 60 * 24 * 30));
    }

    public boolean isShown() {
        switch (role) {
            case STANDARD:
                return isNormalNode() && hasValidLocation();
            case MOBILE:
                return isNormalNode() && hasValidLocation() && isOnline() && isFWVersionHigher(0, 9);
            case SERVER:
            default:
                return false;
        }
    }

    public boolean canHasClients() {
        switch (role) {
            case STANDARD:
            case MOBILE:
                return true;
            case SERVER:
            default:
                return false;
        }
    }

    public boolean isOnline() {
        return online;
    }

    public boolean isServerNode() {
        return id <= 256;
    }

    public boolean isTemporaryNode() {
        return id >= 900 && id < 1000;
    }

    public boolean isNormalNode() {
        return id > 1000 && id < 51000;
    }

    public boolean isAlternativeNode() {
        return id > 51000 && id < 60000;
    }

    public boolean isFWVersionHigher(int minor, int patch) {
        String[] fw = firmwareVersion.split("\\.");
        if (fw.length == 3) {
            if (Integer.parseInt(fw[1]) > minor) {
                return true;
            } else if (Integer.parseInt(fw[1]) == minor && Integer.parseInt(fw[2]) >= patch) {
                return true;
            }
        }
        return false;
    }

    public Collection<Link> getLinks() {
        return linkSet;
    }

    public short getClients() {
        return clients;
    }

    public double getMemoryUsage() {
        return memoryUsage;
    }

    public float getLoadAvg() {
        return loadAvg;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public Community getCommunity() {
        return community;
    }

    public Node getGateway() {
        return gateway;
    }

    public String getHostname() {
        return (name == null || name.isEmpty()) ? String.valueOf(id) : id + "-" + name;
    }

    public String getFakeMac() {
        int third = id / 255;
        int fourth = (id % 255) + 1;
        return "ff:dd:00:00:" + String.format("%02x", third) + ":" + String.format("%02x", fourth);
    }

    public String getFakeId() {
        int third = id / 255;
        int fourth = (id % 255) + 1;
        return "ffdd0000" + String.format("%02x", third) + String.format("%02x", fourth);
    }

    public boolean hasValidLocation() {
        return location != null && location.isValid();
    }

    public JsonObject getJsonObject(DateFormat df) {
        if (this.community == Community.DEFAULT) {
            LOGGER.log(Level.WARN, "Node {} has invalid community (Kontakt: {})", id, name);
        }
        try {
            JsonObject node = new JsonObject();
            JsonObject nodeinfo = new JsonObject();
            JsonObject network = new JsonObject();
            JsonArray addresses = new JsonArray();
            addresses.add(getIpAddress());
            network.add("addresses", addresses);
            nodeinfo.add("network", network);
            nodeinfo.addProperty("hostname", getHostname());
            JsonObject system = new JsonObject();
            system.addProperty("site_code", community.getName());
            system.addProperty("role", role.name().toLowerCase());
            nodeinfo.add("system", system);
            JsonObject hardware = new JsonObject();
            if (model != null && !model.isEmpty()) {
                hardware.addProperty("model", model);
            }
            nodeinfo.add("hardware", hardware);
            nodeinfo.addProperty("node_id", String.valueOf(id));
            JsonObject software = new JsonObject();
            JsonObject autoupdater = new JsonObject();
            autoupdater.addProperty("enabled", autoUpdate);
            autoupdater.addProperty("branch", "stable");
            software.add("autoupdater", autoupdater);
            if (firmwareVersion != null && !firmwareVersion.isEmpty()) {
                JsonObject firmware = new JsonObject();
                firmware.addProperty("release", firmwareVersion);
                firmware.addProperty("base", firmwareBase);
                software.add("firmware", firmware);
            }
            nodeinfo.add("software", software);
            JsonObject owner = new JsonObject();
            if (email != null && !email.isEmpty()) {
                owner.addProperty("contact", email);
            }
            nodeinfo.add("owner", owner);
            if (isShown()) {
                nodeinfo.add("location", location.toJson());
            }
            node.add("nodeinfo", nodeinfo);
            JsonObject statistics = new JsonObject();
            statistics.addProperty("clients", clients);
            if (online) {
                statistics.addProperty("uptime", uptime);
                statistics.addProperty("memory_usage", memoryUsage);
                statistics.addProperty("loadavg", loadAvg);
            }
            if (!isGateway && gateway != null) {
                statistics.addProperty("gateway", gateway.getIpAddress());
            }
            node.add("statistics", statistics);
            JsonObject flags = new JsonObject();
            flags.addProperty("gateway", isGateway);
            flags.addProperty("backbone", backbone);
            flags.addProperty("online", online);
            node.add("flags", flags);
            node.addProperty("firstseen", df.format(new Date(firstseen)));
            node.addProperty("lastseen", df.format(new Date(lastseen)));
            return node;
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, "Fehler bei Node {}", e, id);
        }
        return null;
    }

    public JsonObject getMeshViewerObj(DateFormat df) {
        try {
            JsonObject node = new JsonObject();
            node.addProperty("firstseen", df.format(new Date(firstseen)));
            node.addProperty("lastseen", df.format(new Date(lastseen)));
            node.addProperty("is_gateway", isGateway);
            node.addProperty("is_online", online);
            node.addProperty("clients", clients);
            node.addProperty("clients_wifi24", clients);
            node.addProperty("clients_wifi5", 0);
            node.addProperty("clients_other", 0);
            if (online) {
                node.addProperty("loadavg", loadAvg);
                node.addProperty("memory_usage", memoryUsage);
                Date date = new Date(System.currentTimeMillis() - (long) (uptime * 1000));
                node.addProperty("uptime", df.format(date));
                node.addProperty("nproc", nproc);
            }
            if (!isGateway && gateway != null) {
                node.addProperty("gateway", gateway.getFakeId());
                node.addProperty("gateway_nexthop", gateway.getFakeId()); //TODO: Correct value
            }
            node.addProperty("node_id", getFakeId());
            JsonArray addresses = new JsonArray();
            addresses.add(getIpAddress());
            node.add("addresses", addresses);
            node.addProperty("site_code", community.getName());
            node.addProperty("hostname", getHostname());
            if (isShown()) {
                node.add("location", location.toJson());
            }
            if (firmwareVersion != null && !firmwareVersion.isEmpty()) {
                JsonObject firmware = new JsonObject();
                firmware.addProperty("release", firmwareVersion);
                firmware.addProperty("base", firmwareBase);
                node.add("firmware", firmware);
            }
            if (model != null && !model.isEmpty()) {
                node.addProperty("model", model);
            }
            node.addProperty("contact", email);
            JsonObject autoupdater = new JsonObject();
            autoupdater.addProperty("enabled", autoUpdate);
            autoupdater.addProperty("branch", "stable");
            node.add("autoupdater", autoupdater);
            node.addProperty("vpn", backbone);
            node.addProperty("mac", getFakeMac());
            return node;
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, "Fehler bei Node {}", e, id);
        }
        return null;
    }

    public void updateDatabase() {
        Double lat = null;
        Double lon = null;
        if (hasValidLocation()) {
            lat = location.getLatitude();
            lon = location.getLongitude();
        }
        DataGen.getDB().queryUpdate("CALL updateNode(?,?,?,?,?,?,?,?,?,?,?,?,?,?)", id, lat, lon, community.getName(),
                role.name(), model, firmwareVersion, firmwareBase, firstseen / 1000, lastseen / 1000, autoUpdate,
                isGateway, name, email);
    }

    public void collectStats() {
        if (!isDisplayed()) return;
        StatsSQL.addVersion(getFirmwareVersion());
        StatsSQL.addCommunity(getCommunity());
        if (isNormalNode()) {
            StatsSQL.addGatewayUsage(getGateway());
            StatsSQL.addGatewayUsageClients(getGateway(), getClients());
        }
        if (isOnline()) {
            StatsSQL.addToStats(this);
        }
        VPN vpn = VPN.getVPN(id);
        if (vpn != null) {
            StatsSQL.addVpnUsage(vpn, linkSet.size());
        }
    }

    public static int convertIpToId(String ip) {
        String[] split = ip.split("\\.");
        if (split.length == 4) {
            int third = Integer.parseInt(split[2]);
            int fourth = Integer.parseInt(split[3]);
            return third * 255 + (fourth - 1);
        }
        return -1;
    }
}
