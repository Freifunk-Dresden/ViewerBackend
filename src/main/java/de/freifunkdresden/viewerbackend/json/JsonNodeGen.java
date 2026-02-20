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
import java.util.Optional;

public class JsonNodeGen {

    private static final Logger LOGGER = LogManager.getLogger(JsonNodeGen.class);

    private JsonNodeGen() {
    }

    @Nullable
    public static JsonObject getJsonObject(@NotNull Node n, @NotNull DateFormat df) {
        Optional<Airtime> a2g = Airtime.diff(n.getAirtime2g(), n.getAirtime2GOld());
        Optional<Airtime> a5g = Airtime.diff(n.getAirtime5g(), n.getAirtime5GOld());
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
            system.addProperty("site_code", n.getCommunity().name());
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
            node.add("nodeinfo", nodeinfo);
            JsonObject statistics = new JsonObject();
            statistics.addProperty("clients", n.getClients());
            if (n.isOnline()) {
                statistics.addProperty("uptime", n.getUptime());
                statistics.addProperty("memory_usage", n.getMemoryUsage());
                statistics.addProperty("loadavg", n.getLoadAvg());
                statistics.add("airtime", getHopGlassAirtimeArray(n, a2g, a5g));
                JsonObject wireless = new JsonObject();
                nodeinfo.add("wireless", wireless);
                statistics.add("wireless", getHopGlassAirtimeObject(n, a2g, a5g, wireless));
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
                node.addProperty("nproc", n.getCpuCount());
            }
            if (!n.isGateway() && n.getGateway() != null) {
                node.addProperty("gateway", n.getGateway().getFakeId());
                node.addProperty("gateway_nexthop", n.getGateway().getFakeId()); //TODO: Correct value
            }
            node.addProperty("node_id", n.getFakeId());
            JsonArray addresses = new JsonArray();
            addresses.add(n.getIpAddressString());
            node.add("addresses", addresses);
            node.addProperty("site_code", n.getCommunity().name());
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

    @NotNull
    private static JsonArray getHopGlassAirtimeArray(@NotNull Node n, @NotNull Optional<Airtime> a2g, @NotNull Optional<Airtime> a5g) {
        JsonArray airtime = new JsonArray();
        Optional<Integer> wifiChannel2g = n.getWifiChannel2g();
        if (wifiChannel2g.isPresent() && a2g.isPresent()) {
            JsonObject ja2 = getJsonAirtime(a2g.get(), 2407 + wifiChannel2g.get() * 5);
            if (ja2 != null) {
                airtime.add(ja2);
            }
        }
        Optional<Integer> wifiChannel5g = n.getWifiChannel5g();
        if (wifiChannel5g.isPresent() && a5g.isPresent()) {
            JsonObject ja5 = getJsonAirtime(a5g.get(), 5000 + wifiChannel5g.get() * 5);
            if (ja5 != null) {
                airtime.add(ja5);
            }
        }
        return airtime;
    }

    @NotNull
    private static JsonObject getHopGlassAirtimeObject(@NotNull Node n, @NotNull Optional<Airtime> a2g,
                                                       @NotNull Optional<Airtime> a5g, @NotNull JsonObject wireless) {
        JsonObject w = new JsonObject();
        Optional<Integer> wifiChannel2g = n.getWifiChannel2g();
        if (a2g.isPresent() && wifiChannel2g.isPresent()) {
            wireless.addProperty("chan2", wifiChannel2g.get());
            w.addProperty("airtime2", getWirelessAirtime(a2g.get()));
        }
        Optional<Integer> wifiChannel5g = n.getWifiChannel5g();
        if (a5g.isPresent() && wifiChannel5g.isPresent()) {
            wireless.addProperty("chan5", wifiChannel5g.get());
            w.addProperty("airtime5", getWirelessAirtime(a5g.get()));
        }
        return w;
    }

    @Nullable
    private static JsonObject getJsonAirtime(@NotNull Airtime diff, int freq) {
        double a = diff.active();
        double b = diff.busy();
        double r = diff.receive();
        double t = diff.transmit();
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

    @Nullable
    private static Number getWirelessAirtime(@NotNull Airtime diff) {
        double b = diff.busy();
        double a = diff.active();
        double airtime = b / a;
        return Double.isFinite(airtime) ? airtime : null;
    }
}
