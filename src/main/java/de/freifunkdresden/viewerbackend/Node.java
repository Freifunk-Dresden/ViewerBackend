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
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.logging.Level;

public class Node {

    private final int id;
    private final HashSet<Link> linkset = new HashSet<>();
    private String hostname;
    private String name;
    private String community;
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
    private boolean gateway;
    private long lastseen = -1;
    private long firstseen = -1;
    private Location location;
    private String gatewayIp;
    private boolean valid = false;
    private boolean displayed;
    private boolean autoupdate;

    public Node(int id) {
        this.id = id;
        this.hostname = String.valueOf(id);
    }

    public String getIpAdress() {
        return getIpAdress(false);
    }

    public String getIpAdress(boolean subnet201) {
        return String.format("10.%s.%s.%s", (subnet201 ? "201" : "200"), (id / 255 % 256), ((id % 255) + 1));
    }

    public void fill(DataParser dp) {
        try {
            if (dp.getName() != null) {
                setName(dp.getName());
            }
            if (dp.getCommunity() != null) {
                community = dp.getCommunity().isEmpty() ? "Dresden" : dp.getCommunity();
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
                setEmail(dp.getEMail());
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
            if (dp.getGatewayIp() != null) {
                gatewayIp = dp.getGatewayIp();
            }
            if (dp.getLinkSet() != null) {
                linkset.addAll(dp.getLinkSet());
            }
            if (dp.getAutoUpdate() != null) {
                autoupdate = dp.getAutoUpdate();
            }
            if (dp.getLocation() != null) {
                location = dp.getLocation();
            }
            if (dp.isOnline() != null) {
                online = dp.isOnline();
            }
            if (dp.isGateway() != null) {
                gateway = dp.isGateway();
            }
            if (dp.getLastseen() != null) {
                setLastseen(dp.getLastseen());
            }
            if (dp.getFirstseen() != null) {
                firstseen = dp.getFirstseen();
            }
            valid = true;
        } catch (Exception e) {
            valid = false;
            DataGen.getLogger().log(Level.SEVERE, "Node " + getId(), e);
        }
    }

    public int getId() {
        return id;
    }

    private void setName(String name) throws UnsupportedEncodingException {
        if (name == null || name.isEmpty()) {
            this.name = name;
        } else {
            this.name = URLDecoder.decode(name, "UTF-8");
            this.hostname = id + "-" + name;
        }
    }

    private void setEmail(String email) throws UnsupportedEncodingException {
        if (email == null) {
            this.email = email;
        } else {
            this.email = URLDecoder.decode(email, "UTF-8");
        }
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    private void setLastseen(long lastseen) {
        this.lastseen = lastseen;
        //display only nodes lastseen within the last 30 days
        displayed = lastseen / 1000 > (System.currentTimeMillis() / 1000) - 60 * 60 * 24 * 30;
    }

    public boolean isValid() {
        return valid;
    }

    public boolean isDisplayed() {
        return displayed && isValid();
    }

    public boolean isOnline() {
        return online;
    }

    public Collection<Link> getLinks() {
        return linkset;
    }

    public short getClients() {
        return clients;
    }

    public boolean hasValidLocation() {
        return location != null && location.isValid();
    }

    public JsonObject getJsonObject() {
        if (!isValid()) {
            return null;
        }
        try {
            JsonObject node = new JsonObject();
            JsonObject nodeinfo = new JsonObject();
            JsonObject network = new JsonObject();
            JsonArray addresses = new JsonArray();
            addresses.add(getIpAdress());
            network.add("addresses", addresses);
            nodeinfo.add("network", network);
            nodeinfo.addProperty("hostname", hostname);
            JsonObject system = new JsonObject();
            system.addProperty("site_code", community);
            system.addProperty("role", role.name().toLowerCase());
            nodeinfo.add("system", system);
            JsonObject hardware = new JsonObject();
            if (model != null && !model.isEmpty()) {
                hardware.addProperty("model", model);
            }
            nodeinfo.add("hardware", hardware);
            nodeinfo.addProperty("node_id", id);
            JsonObject software = new JsonObject();
            JsonObject autoupdater = new JsonObject();
            autoupdater.addProperty("enabled", autoupdate);
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
            if (id > 1000 && hasValidLocation()) {
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
            if (!gateway && gatewayIp != null && !gatewayIp.isEmpty()) {
                statistics.addProperty("gateway", gatewayIp);
            }
            node.add("statistics", statistics);
            JsonObject flags = new JsonObject();
            flags.addProperty("gateway", gateway);
            flags.addProperty("uplink", gateway); //@TODO:correct with direct connection to internet
            flags.addProperty("online", online);
            node.add("flags", flags);
            node.addProperty("firstseen", DataGen.DATE_HOP.format(new Date(firstseen)));
            node.addProperty("lastseen", DataGen.DATE_HOP.format(new Date(lastseen)));
            return node;
        } catch (Exception e) {
            DataGen.getLogger().log(Level.SEVERE, "Fehler bei Node " + id, e);
        }
        return null;
    }

    public JsonObject getMeshViewerObj() {
        if (!isValid()) {
            return null;
        }
        try {
            JsonObject node = new JsonObject();
            node.addProperty("firstseen", DataGen.DATE_MESH.format(new Date(firstseen)));
            node.addProperty("lastseen", DataGen.DATE_MESH.format(new Date(lastseen)));
            node.addProperty("is_gateway", gateway);
            node.addProperty("is_online", online);
            node.addProperty("clients", clients);
            node.addProperty("clients_wifi24", clients);
            node.addProperty("clients_wifi5", 0);
            node.addProperty("clients_other", 0);
            if (online) {
                node.addProperty("loadavg", loadAvg);
                node.addProperty("memory_usage", memoryUsage);
                Date date = new Date(System.currentTimeMillis() - (long) (uptime * 1000));
                node.addProperty("uptime", DataGen.DATE_MESH.format(date));
                node.addProperty("nproc", 1); //TODO: Correct processor count
            }
            if (!gateway && gatewayIp != null && !gatewayIp.isEmpty()) {
                node.addProperty("gateway", String.valueOf(convertIpToId(gatewayIp)));
                node.addProperty("gateway_nexthop", String.valueOf(convertIpToId(gatewayIp))); //TODO: Correct value
            }
            node.addProperty("node_id", String.valueOf(id));
            JsonArray addresses = new JsonArray();
            addresses.add(getIpAdress());
            node.add("addresses", addresses);
            node.addProperty("site_code", community);
            node.addProperty("hostname", hostname);
            if (id > 1000 && hasValidLocation()) {
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
            autoupdater.addProperty("enabled", autoupdate);
            autoupdater.addProperty("branch", "stable");
            node.add("autoupdater", autoupdater);
            node.addProperty("vpn", gateway); //TODO: Correct value
            node.addProperty("mac", convertIdToMac(id));
            return node;
        } catch (Exception e) {
            DataGen.getLogger().log(Level.SEVERE, "Fehler bei Node " + id, e);
        }
        return null;
    }

    public void updateDatabase() {
        if (!hasValidLocation()) {
            DataGen.getDB().queryUpdate("INSERT INTO nodes SET id = ? ON DUPLICATE KEY UPDATE id = id", id);
        } else {
            DataGen.getDB().queryUpdate("INSERT INTO nodes SET id = ?, latitude = ?, longitude = ? ON DUPLICATE KEY UPDATE latitude = ?, longitude = ?", id, location.getLatitude(), location.getLongitude(), location.getLatitude(), location.getLongitude());
        }
        DataGen.getDB().queryUpdate("UPDATE nodes SET community = ?, role = ?, model = ?, firmwareVersion = ?, firmwareBase = ?, firstseen = ?, lastseen = ?, online = ?, autoupdate = ?, gateway = ?, name = ?, email = ? WHERE id = ?",
                community, role.name(), model, firmwareVersion, firmwareBase, firstseen / 1000, lastseen / 1000, online, autoupdate, gateway, name, email, id);
        //Statistics
        if (isOnline() && (id >= 1000 && id < 51000)) {
            StatsSQL.addClientStat(this, clients);
            StatsSQL.addLoadStat(this, loadAvg);
            StatsSQL.addMemoryStat(this, memoryUsage);
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

    public static String convertIdToMac(int id) {
        int third = id / 255 % 256;
        int fourth = (id % 255) + 1;
        return "ff:dd:00:00:" + String.format("%02x", third) + ":" + String.format("%02x", fourth);
    }
}
