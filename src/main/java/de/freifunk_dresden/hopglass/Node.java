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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Node {

    private final int id;
    private String hostname;
    private String name;
    private String community;
    private NodeType role;
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
    private long lastseen;
    private long firstseen;
    private double latitude = Double.NaN;
    private double longitude = Double.NaN;
    private String gatewayIp;
    private boolean valid;
    private boolean displayed;
    private final HashMap<Integer, Link> linkmap = new HashMap<>();

    public Node(int id) {
        this.id = id;
        this.hostname = String.valueOf(id);
    }

    public String getIpAdress() {
        return getIpAdress(false);
    }

    public String getIpAdress(boolean subnet201) {
        return (subnet201 ? "10.201." : "10.200.") + (id / 255 % 256) + "." + ((id % 255) + 1);
    }

    public void parseData(DataParser dp) throws UnsupportedEncodingException {
        setName(dp.getName());
        community = dp.getCommunity().isEmpty() ? "Dresden" : dp.getCommunity();
        role = dp.getRole();
        model = dp.getModel();
        firmwareVersion = dp.getFirmwareVersion();
        firmwareBase = dp.getFirmwareBase();
        setEmail(dp.getEMail());
        uptime = dp.getUptime();
        memoryUsage = dp.getMemoryUsage();
        clients = dp.getClients();
        loadAvg = dp.getLoadAvg();
        gatewayIp = dp.getGatewayIp();
        linkmap.putAll(dp.getLinkMap());
        online = true;
        lastseen = System.currentTimeMillis();
        valid = true;
    }

    public void parseData(ResultSet rs) throws Exception {
        community = rs.getString("community");
        String r = rs.getString("role");
        role = r == null ? NodeType.STANDARD : NodeType.valueOf(r.toUpperCase());
        model = rs.getString("model");
        firmwareVersion = rs.getString("firmwareVersion");
        firmwareBase = rs.getString("firmwareBase");
        firstseen = rs.getLong("firstseen") * 1000;
        setLastseen(rs.getLong("lastseen") * 1000);
        gatewayIp = rs.getString("gatewayIp");
        latitude = rs.getDouble("latitude");
        if (rs.wasNull()) {
            latitude = Double.NaN;
        }
        longitude = rs.getDouble("longitude");
        if (rs.wasNull()) {
            longitude = Double.NaN;
        }
        clients = 0;
        gateway = rs.getBoolean("gateway");
        setName(rs.getString("name"));
        setEmail(rs.getString("email"));
        valid = true;
    }

    public int getId() {
        return id;
    }

    private void setName(String name) throws UnsupportedEncodingException {
        if (name == null || name.isEmpty()) {
            this.name = name;
        } else {
            this.name = URLDecoder.decode(name, "UTF-8");
            this.hostname = id + "-" + name.replace(" ", "+");
        }
    }

    private void setEmail(String email) throws UnsupportedEncodingException {
        if (email == null) {
            this.email = email;
        } else {
            this.email = URLDecoder.decode(email, "UTF-8");
        }
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public void setGateway(boolean gateway) {
        this.gateway = gateway;
    }

    public void setLastseen(long lastseen) {
        this.lastseen = lastseen;
        //display only nodes lastseen within the last 30 days
        displayed = lastseen / 1000 > (System.currentTimeMillis() / 1000) - 60 * 60 * 24 * 30;
    }

    public void setFirstseen(long firstseen) {
        this.firstseen = firstseen;
    }

    public boolean isValid() {
        return valid;
    }

    public boolean isDisplayed() {
        return displayed && isValid();
    }

    public Collection<Link> getLinks() {
        return linkmap.values();
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
            if (id > 1000 && !Double.isNaN(latitude) && !Double.isNaN(longitude)) {
                JsonObject location = new JsonObject();
                location.addProperty("latitude", latitude);
                location.addProperty("longitude", longitude);
                nodeinfo.add("location", location);
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
            Logger.getLogger(Node.class.getName()).log(Level.SEVERE, "Fehler bei Node " + id, e);
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
            }
            if (!gateway && gatewayIp != null && !gatewayIp.isEmpty()) {
                node.addProperty("gateway", gatewayIp);
            }
            node.addProperty("node_id", String.valueOf(id));
            JsonArray addresses = new JsonArray();
            addresses.add(getIpAdress());
            node.add("addresses", addresses);
            node.addProperty("site_code", community);
            node.addProperty("hostname", hostname);
            if (id > 1000 && !Double.isNaN(latitude) && !Double.isNaN(longitude)) {
                JsonObject location = new JsonObject();
                location.addProperty("latitude", latitude);
                location.addProperty("longitude", longitude);
                node.add("location", location);
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
            autoupdater.addProperty("enabled", false);
            autoupdater.addProperty("branch", "stable");
            node.add("autoupdater", autoupdater);
            return node;
        } catch (Exception e) {
            Logger.getLogger(Node.class.getName()).log(Level.SEVERE, "Fehler bei Node " + id, e);
        }
        return null;
    }

    public void updateDatabase() {
        if (Double.isNaN(latitude) || Double.isNaN(longitude)) {
            DataGen.getDB().queryUpdate("INSERT INTO nodes SET id = ? ON DUPLICATE KEY UPDATE id = id", id);
        } else {
            DataGen.getDB().queryUpdate("INSERT INTO nodes SET id = ?, latitude = ?, longitude = ? ON DUPLICATE KEY UPDATE latitude = ?, longitude = ?", id, latitude, longitude, latitude, longitude);
        }
        DataGen.getDB().queryUpdate("UPDATE nodes SET community = ?, role = ?, model = ?, firmwareVersion = ?, firmwareBase = ?, firstseen = ?, lastseen = ?, gatewayIp = ?, uptime = ?, memory_usage = ?, loadavg = ?, clients = ?, online = ?, gateway = ?, name = ?, email = ? WHERE id = ?",
                community, role.name(), model, firmwareVersion, firmwareBase, firstseen / 1000, lastseen / 1000, gatewayIp, uptime, memoryUsage, loadAvg, clients, online, gateway, name, email, id);
        if (!linkmap.isEmpty()) {
            MySQL.PreparedUpdate prep = DataGen.getDB().queryPrepUpdate("INSERT INTO links SET `from` = ?, `to` = ?, `interface` = ?, `tq` = ?");
            linkmap.entrySet().stream().forEach((e) -> {
                prep.add(id, e.getKey(), e.getValue().getType().name().toLowerCase(), e.getValue().getSourceTq());
            });
            prep.done();
        }
    }
    
    public enum NodeType {
        STANDARD
    }
}
