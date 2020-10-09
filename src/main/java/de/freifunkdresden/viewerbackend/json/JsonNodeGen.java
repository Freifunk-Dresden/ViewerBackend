package de.freifunkdresden.viewerbackend.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.freifunkdresden.viewerbackend.Community;
import de.freifunkdresden.viewerbackend.Node;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.DateFormat;
import java.util.Date;

public class JsonNodeGen {

    private static final Logger LOGGER = LogManager.getLogger(Node.class);

    public static JsonObject getJsonObject(Node n, DateFormat df) {
        if (n.getCommunity() == Community.DEFAULT) {
            LOGGER.log(Level.WARN, "Node {} has invalid community (Kontakt: {})", n.getId(), n.getName());
        }
        try {
            JsonObject node = new JsonObject();
            JsonObject nodeinfo = new JsonObject();
            JsonObject network = new JsonObject();
            JsonArray addresses = new JsonArray();
            addresses.add(n.getIpAddress());
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
            if (n.getEmail() != null && !n.getEmail().isEmpty()) {
                owner.addProperty("contact", n.getEmail());
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
            }
            if (!n.isGateway() && n.getGateway() != null) {
                statistics.addProperty("gateway", n.getGateway().getIpAddress());
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

    public static JsonObject getMeshViewerObj(Node n, DateFormat df) {
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
            addresses.add(n.getIpAddress());
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
            node.addProperty("contact", n.getEmail());
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
}
