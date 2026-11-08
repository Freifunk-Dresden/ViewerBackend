/*
 * The MIT License
 *
 * Copyright 2026 Niklas Merkelt.
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

package de.freifunkdresden.viewerbackend.output;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.freifunkdresden.viewerbackend.Link;
import de.freifunkdresden.viewerbackend.Node;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

public class MeshViewer extends OutputGenerator {

    private static final Logger LOGGER = LogManager.getLogger(MeshViewer.class);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_INSTANT;

    private final Gson gson = new Gson();
    private final JsonArray nodes = new JsonArray();
    private final JsonArray links = new JsonArray();

    @Override
    public void save(@NotNull Path path) throws IOException {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("timestamp", DATE_TIME_FORMATTER.format(Instant.now()));
        jsonObject.add("nodes", nodes);
        jsonObject.add("links", links);
        Path fp = path.resolve("meshviewer.json");
        Files.writeString(fp, gson.toJson(jsonObject), StandardCharsets.UTF_8);
    }

    @Override
    public void addNode(@NotNull Node node) {
        JsonObject meshNode = getNodeJsonObject(node);
        if (meshNode != null) {
            nodes.add(meshNode);
        }
    }

    @Override
    public void addLink(@NotNull Link link) {
        JsonObject jsonLink = new JsonObject();
        jsonLink.addProperty("source", link.getSource().getFakeId());
        jsonLink.addProperty("target", link.getTarget().getFakeId());
        jsonLink.addProperty("source_tq", Link.convertToMeshV(link.getSourceTq()));
        jsonLink.addProperty("target_tq", Link.convertToMeshV(link.getTargetTq()));
        jsonLink.addProperty("type", link.getType().getMeshViewer());
        jsonLink.addProperty("source_addr", link.getSource().getFakeMac());
        jsonLink.addProperty("target_addr", link.getTarget().getFakeMac());
        links.add(jsonLink);
    }

    @Nullable
    private JsonObject getNodeJsonObject(@NotNull Node n) {
        try {
            JsonObject node = new JsonObject();
            node.addProperty("firstseen", DATE_TIME_FORMATTER.format(Instant.ofEpochMilli(n.getFirstSeen())));
            node.addProperty("lastseen", DATE_TIME_FORMATTER.format(Instant.ofEpochMilli(n.getLastSeen())));
            node.addProperty("is_gateway", n.isGateway());
            node.addProperty("is_online", n.isOnline());
            node.addProperty("clients", n.getClients());
            node.addProperty("clients_wifi24", n.getClients2g());
            node.addProperty("clients_wifi5", n.getClients5g());
            node.addProperty("clients_other", 0);
            if (n.isOnline()) {
                node.addProperty("loadavg", n.getLoadAvg());
                node.addProperty("memory_usage", n.getMemoryUsage());
                Instant dateTime = Instant.now().minusMillis((long) (n.getUptime() * 1000));
                node.addProperty("uptime", DATE_TIME_FORMATTER.format(dateTime));
                node.addProperty("nproc", n.getCpuCount());
            }
            if (!n.isGateway() && n.getGateway() != null) {
                node.addProperty("gateway", n.getGateway().getFakeId());
                node.addProperty("gateway_nexthop", n.getGateway().getFakeId()); //TODO: Correct value
            }
            node.addProperty("node_id", n.getFakeId());
            node.addProperty("custom_node_id", n.getId());
            JsonArray addresses = new JsonArray();
            addresses.add(n.getIpAddressString());
            node.add("addresses", addresses);
            node.addProperty("site_code", n.getCommunity().name());
            node.addProperty("domain", n.getCommunity().name());
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
}
