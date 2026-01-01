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

package de.freifunkdresden.viewerbackend.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.freifunkdresden.viewerbackend.DataGen;
import de.freifunkdresden.viewerbackend.exception.ConfigurationException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class VpnServerDirectory {

    private static final Logger LOGGER = LogManager.getLogger(VpnServerDirectory.class);

    private final Map<Integer, VpnServer> vpnServers = new HashMap<>();

    public void loadConfig() {
        Path vpnConfig = Paths.get(DataGen.getConfig().getValue("config_path"), "vpn.json");
        if (!Files.exists(vpnConfig) || !Files.isReadable(vpnConfig)) {
            throw new ConfigurationException("VPN config file don't exist");
        }
        try {
            String json = Files.readString(vpnConfig, StandardCharsets.UTF_8);
            JsonArray vpnArray = JsonParser.parseString(json).getAsJsonArray();
            vpnArray.forEach(vpn -> {
                JsonObject v = vpn.getAsJsonObject();
                String vpnId = v.get("vpnId").getAsString();
                int nodeId = v.get("nodeId").getAsInt();
                vpnServers.put(nodeId, new VpnServer(vpnId, nodeId));
            });
        } catch (RuntimeException | IOException e) {
            throw new ConfigurationException(e);
        }
        if (DataGen.isDebug()) {
            LOGGER.log(Level.DEBUG, "== vpn configuration ==");
            LOGGER.log(Level.DEBUG, "vpn servers:");
            vpnServers.values().forEach(vpnServer -> {
                LOGGER.log(Level.DEBUG, "VPN: {} -> Node: {}", vpnServer.vpnId, vpnServer.nodeId);
            });
        }
    }

    public VpnServer getServerByNode(int nodeId) {
        return vpnServers.get(nodeId);
    }

    public record VpnServer(String vpnId, int nodeId){}
}
