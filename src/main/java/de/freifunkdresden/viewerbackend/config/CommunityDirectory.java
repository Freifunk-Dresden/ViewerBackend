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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class CommunityDirectory {

    private final Map<String, Community> communityMapping = new HashMap<>();
    private Community defaultCommunity;

    public void loadConfig() {
        Path communityConfig = Paths.get(DataGen.getConfig().getValue("config_path"), "community.json");
        if (!Files.exists(communityConfig) || !Files.isReadable(communityConfig)) {
            throw new ConfigurationException("Community config file don't exist");
        }
        try {
            String json = Files.readString(communityConfig, StandardCharsets.UTF_8);
            JsonArray communityArray = JsonParser.parseString(json).getAsJsonArray();
            communityArray.forEach(vpn -> {
                JsonObject communityObject = vpn.getAsJsonObject();
                String name = communityObject.get("name").getAsString();
                Community c = new Community(name);
                JsonArray mapping = communityObject.get("mapping").getAsJsonArray();
                mapping.forEach(m -> {
                    String map = m.getAsString();
                    communityMapping.put(map, c);
                });
                if (communityObject.has("default") && communityObject.get("default").getAsBoolean()) {
                    defaultCommunity = c;
                }
            });
        } catch (RuntimeException | IOException e) {
            throw new ConfigurationException(e);
        }
    }

    public Community getDefaultCommunity() {
        return defaultCommunity;
    }

    public Community getCommunityMapping(String community) {
        return communityMapping.getOrDefault(community, defaultCommunity);
    }

    public boolean existsMapping(String community) {
        return communityMapping.containsKey(community);
    }

    public record Community(String name){}
}
