/*
 * The MIT License
 *
 * Copyright 2020 NMerkelt.
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
import com.google.gson.JsonSyntaxException;
import de.freifunkdresden.viewerbackend.DataGen;
import de.freifunkdresden.viewerbackend.dataparser.DataParserAPI;
import de.freifunkdresden.viewerbackend.exception.ApiProcessingException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FreifunkApi {

    private static final Logger LOGGER = LogManager.getLogger(FreifunkApi.class);

    public static void downloadApiFile() {
        Gson gson = new Gson();
        String apiUrl = DataGen.getConfig().getValue("api_url");
        String cachePath = DataGen.getConfig().getValue("cache_path");
        try {
            Path cache = Paths.get(cachePath);
            if (Files.notExists(cache)) {
                Files.createDirectory(cache);
            }
            Path c = cache.resolve("api.json");
            URLConnection con = new URL(apiUrl).openConnection();
            try (InputStreamReader reader = new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8)) {
                JsonArray api = JsonParser.parseReader(reader).getAsJsonArray();
                reader.close();
                Files.writeString(c, gson.toJson(api), StandardCharsets.UTF_8);
            }
        } catch (JsonSyntaxException | IOException e) {
            LOGGER.log(Level.WARN, "API download failed", e);
        }
    }

    public static void processApi() throws ApiProcessingException {
        try {
            String cachePath = DataGen.getConfig().getValue("cache_path");
            Path cache = Paths.get(cachePath).resolve("api.json");
            String json = Files.readString(cache, StandardCharsets.UTF_8);
            JsonArray api = JsonParser.parseString(json).getAsJsonArray();
            api.forEach(node -> {
                JsonObject n = node.getAsJsonObject();
                DataGen.getDataHolder().getNode(n.get("id").getAsInt()).fill(new DataParserAPI(n));
            });
        } catch (JsonSyntaxException | IOException e) {
            throw new ApiProcessingException(e);
        }
    }
}
