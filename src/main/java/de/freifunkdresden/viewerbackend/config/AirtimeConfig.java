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
import java.util.ArrayList;
import java.util.List;

public class AirtimeConfig {

    private static final Logger LOGGER = LogManager.getLogger(AirtimeConfig.class);

    private final List<String> ignoreAirtime2g = new ArrayList<>();
    private final List<String> ignoreAirtime5g = new ArrayList<>();

    public void loadConfig() {
        Path airtimeConfig = Paths.get(DataGen.getConfig().getValue("config_path"), "airtime.json");
        if (!Files.exists(airtimeConfig) || !Files.isReadable(airtimeConfig)) {
            throw new ConfigurationException("Airtime config file don't exist");
        }
        try {
            String json = Files.readString(airtimeConfig, StandardCharsets.UTF_8);
            JsonObject airtimeObject = JsonParser.parseString(json).getAsJsonObject();

            JsonArray ignore2g = airtimeObject.getAsJsonArray("ignore_2g");
            ignore2g.forEach(target -> ignoreAirtime2g.add(target.getAsString()));

            JsonArray ignore5g = airtimeObject.getAsJsonArray("ignore_5g");
            ignore5g.forEach(target -> ignoreAirtime5g.add(target.getAsString()));
        } catch (RuntimeException | IOException e) {
            throw new ConfigurationException(e);
        }
        if (DataGen.isDebug()) {
            LOGGER.log(Level.DEBUG, "== airtime configuration ==");
            LOGGER.log(Level.DEBUG, "ignore airtime 2g targets:");
            ignoreAirtime2g.forEach(target -> LOGGER.log(Level.DEBUG, "{}", target));
            LOGGER.log(Level.DEBUG, "ignore airtime 5g targets:");
            ignoreAirtime5g.forEach(target -> LOGGER.log(Level.DEBUG, "{}", target));
        }
    }

    public boolean isTargetIgnored2g(String target) {
        return ignoreAirtime2g.contains(target);
    }

    public boolean isTargetIgnored5g(String target) {
        return ignoreAirtime5g.contains(target);
    }
}
