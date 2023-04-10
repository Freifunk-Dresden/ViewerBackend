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

package de.freifunkdresden.viewerbackend.datasource;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.freifunkdresden.viewerbackend.DataGen;
import de.freifunkdresden.viewerbackend.dataparser.DataParserAPI;
import de.freifunkdresden.viewerbackend.exception.ApiProcessingException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class FreifunkApi {

    private static final Logger LOGGER = LogManager.getLogger(FreifunkApi.class);
    private static final String API_CACHE_FILE_NAME = "api.json";

    private FreifunkApi() {
    }

    public static void downloadApiFile() {
        Path cacheFile = DataGen.getCache().resolveCacheFile(API_CACHE_FILE_NAME);
        String apiUrl = DataGen.getConfig().getValue("api_url");
        try {
            URLConnection con = new URI(apiUrl).toURL().openConnection();
            try (InputStreamReader reader = new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8)) {
                JsonArray api = JsonParser.parseReader(reader).getAsJsonArray();
                Files.writeString(cacheFile, new Gson().toJson(api), StandardCharsets.UTF_8);
            }
        } catch (RuntimeException | IOException | URISyntaxException e) {
            LOGGER.log(Level.WARN, "API download failed", e);
        }
    }

    public static void processApi() {
        try {
            Path cacheFile = DataGen.getCache().resolveCacheFile(API_CACHE_FILE_NAME);
            String json = Files.readString(cacheFile, StandardCharsets.UTF_8);
            JsonArray api = JsonParser.parseString(json).getAsJsonArray();
            api.forEach(node -> {
                JsonObject n = node.getAsJsonObject();
                DataGen.getDataHolder().getNode(n.get("id").getAsInt()).setDpApi(new DataParserAPI(n));
            });
        } catch (RuntimeException | IOException e) {
            throw new ApiProcessingException(e);
        }
    }
}
