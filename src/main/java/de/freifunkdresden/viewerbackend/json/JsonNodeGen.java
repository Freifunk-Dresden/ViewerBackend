/*
 * The MIT License
 *
 * Copyright 2020 Niklas Merkelt.
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

package de.freifunkdresden.viewerbackend.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.freifunkdresden.viewerbackend.Airtime;
import de.freifunkdresden.viewerbackend.Node;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DateFormat;
import java.util.Date;

public class JsonNodeGen {

    private static final Logger LOGGER = LogManager.getLogger(JsonNodeGen.class);

    private JsonNodeGen() {
    }

    @Nullable
    public static JsonObject getJsonObject(@NotNull Node n, @NotNull DateFormat df) {
        Airtime a2g = Airtime.diff(n.getAirtime2g(), n.getAirtime2GOld());
        Airtime a5g = Airtime.diff(n.getAirtime5g(), n.getAirtime5GOld());
        try {
            JsonObject node = new JsonObject();
            JsonObject nodeinfo = new JsonObject();
            JsonObject network = new JsonObject();
            JsonArray addresses = new JsonArray();
            addresses.add(n.getIpAddressString());
            network.add("addresses", addresses);
            nodeinfo.add("network", network);
            nodeinfo.addProperty("hostname", n.getHostname());
            JsonObject system = new JsonObject();
            system.addProperty("site_code", n.getCommunity().getName());
            system.addProperty("role", n.getRole().name().toLowerCase());
            nodeinfo.add("system", system);
            JsonObject hardware = new JsonObject();
            if (n.getModel() != null && !n.getModel().isEmpty()) {
                hardware.addProperty("model", n.getModel());
            }
            nodeinfo.add("hardware", hardware);
            nodeinfo.addProperty("node_id", String.valueOf(n.getId()));
            JsonObject software = new JsonObject();
            JsonObject autoupdater = new JsonObject();
            autoupdater.addProperty("enabled", n.isAutoUpdateEnabled());
            autoupdater.addProperty("branch", "stable");
            software.add("autoupdater", autoupdater);
            if (n.getFirmwareVersion() != null && !n.getFirmwareVersion().isEmpty()) {
                JsonObject firmware = new JsonObject();
                firmware.addProperty("release", n.getFirmwareVersion());
                firmware.addProperty("base", n.getFirmwareBase());
                software.add("firmware", firmware);
            }
            nodeinfo.add("software", software);
            JsonObject owner = new JsonObject();
            if (n.getEMail() != null && !n.getEMail().isEmpty()) {
                owner.addProperty("contact", n.getEMail());
            }
            nodeinfo.add("owner", owner);
            if (n.isShown()) {
                nodeinfo.add("location", n.getLocation().toJson());
            }
            JsonArray pages = new JsonArray();
            pages.add(String.format("http://%s.freifunk-dresden.de", n.getId()));
            nodeinfo.add("pages", pages);
            JsonObject wireless = new JsonObject();
            nodeinfo.add("wireless", wireless);
            node.add("nodeinfo", nodeinfo);
            JsonObject statistics = new JsonObject();
            statistics.addProperty("clients", n.getClients());
            if (n.isOnline()) {
                statistics.addProperty("uptime", n.getUptime());
                statistics.addProperty("memory_usage", n.getMemoryUsage());
                statistics.addProperty("loadavg", n.getLoadAvg());
                JsonArray airtime = new JsonArray();
                JsonObject ja2 = getAirtime(a2g, 2472);
                if (ja2 != null) {
                    airtime.add(ja2);
                }
                JsonObject ja5 = getAirtime(a5g, 5220);
                if (ja5 != null) {
                    airtime.add(ja5);
                }
                statistics.add("airtime", airtime);
                JsonObject w = new JsonObject();
                Number wat2 = getWirelessAirtime(a2g);
                if (wat2 != null) {
                    wireless.addProperty("chan2", 13);
                    w.addProperty("airtime2", wat2);
                }
                Number wat5 = getWirelessAirtime(a5g);
                if (wat5 != null) {
                    wireless.addProperty("chan5", 44);
                    w.addProperty("airtime5", wat5);
                }
                statistics.add("wireless", w);
            }
            if (!n.isGateway() && n.getGateway() != null) {
                statistics.addProperty("gateway", n.getGateway().getIpAddressString());
            }
            node.add("statistics", statistics);
            JsonObject flags = new JsonObject();
            flags.addProperty("gateway", n.isGateway());
            flags.addProperty("backbone", n.hasBackbone());
            flags.addProperty("online", n.isOnline());
            node.add("flags", flags);
            node.addProperty("firstseen", df.format(new Date(n.getFirstSeen())));
            node.addProperty("lastseen", df.format(new Date(n.getLastSeen())));
            return node;
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, String.format("Fehler bei Node %s", n.getId()), e);
        }
        return null;
    }

    @Nullable
    public static JsonObject getMeshViewerObj(@NotNull Node n, @NotNull DateFormat df) {
        try {
            JsonObject node = new JsonObject();
            node.addProperty("firstseen", df.format(new Date(n.getFirstSeen())));
            node.addProperty("lastseen", df.format(new Date(n.getLastSeen())));
            node.addProperty("is_gateway", n.isGateway());
            node.addProperty("is_online", n.isOnline());
            node.addProperty("clients", n.getClients());
            node.addProperty("clients_wifi24", n.getClients());
            node.addProperty("clients_wifi5", 0);
            node.addProperty("clients_other", 0);
            if (n.isOnline()) {
                node.addProperty("loadavg", n.getLoadAvg());
                node.addProperty("memory_usage", n.getMemoryUsage());
                Date date = new Date(System.currentTimeMillis() - (long) (n.getUptime() * 1000));
                node.addProperty("uptime", df.format(date));
                node.addProperty("nproc", n.getNproc());
            }
            if (!n.isGateway() && n.getGateway() != null) {
                node.addProperty("gateway", n.getGateway().getFakeId());
                node.addProperty("gateway_nexthop", n.getGateway().getFakeId()); //TODO: Correct value
            }
            node.addProperty("node_id", n.getFakeId());
            JsonArray addresses = new JsonArray();
            addresses.add(n.getIpAddressString());
            node.add("addresses", addresses);
            node.addProperty("site_code", n.getCommunity().getName());
            node.addProperty("hostname", n.getHostname());
            if (n.isShown()) {
                node.add("location", n.getLocation().toJson());
            }
            if (n.getFirmwareVersion() != null && !n.getFirmwareVersion().isEmpty()) {
                JsonObject firmware = new JsonObject();
                firmware.addProperty("release", n.getFirmwareVersion());
                firmware.addProperty("base", n.getFirmwareBase());
                node.add("firmware", firmware);
            }
            if (n.getModel() != null && !n.getModel().isEmpty()) {
                node.addProperty("model", n.getModel());
            }
            node.addProperty("contact", n.getEMail());
            JsonObject autoupdater = new JsonObject();
            autoupdater.addProperty("enabled", n.isAutoUpdateEnabled());
            autoupdater.addProperty("branch", "stable");
            node.add("autoupdater", autoupdater);
            node.addProperty("vpn", n.hasBackbone());
            node.addProperty("mac", n.getFakeMac());
            return node;
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, String.format("Fehler bei Node %s", n.getId()), e);
        }
        return null;
    }

    @Nullable
    private static JsonObject getAirtime(@NotNull Airtime diff, int freq) {
        if (!diff.isEmpty()) {
            double a = diff.getActive();
            double b = diff.getBusy();
            double r = diff.getReceive();
            double t = diff.getTransmit();
            double busy = b / a;
            double rx = r / a;
            double tx = t / a;
            if (!Double.isFinite(busy) || !Double.isFinite(rx) || !Double.isFinite(tx)) {
                return null;
            }
            JsonObject ja = new JsonObject();
            ja.addProperty("frequency", freq);
            ja.addProperty("busy", busy);
            ja.addProperty("rx", rx);
            ja.addProperty("tx", tx);
            return ja;
        }
        return null;
    }

    @Nullable
    private static Number getWirelessAirtime(@NotNull Airtime diff) {
        if (!diff.isEmpty()) {
            double b = diff.getBusy();
            double a = diff.getActive();
            double airtime = b / a;
            return Double.isFinite(airtime) ? airtime : null;
        }
        return null;
    }
}
