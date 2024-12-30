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

package de.freifunkdresden.viewerbackend.thread;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import de.freifunkdresden.viewerbackend.Node;
import de.freifunkdresden.viewerbackend.dataparser.DataParserSysInfo;
import de.freifunkdresden.viewerbackend.dataparser.DataParserSysInfoV10;
import de.freifunkdresden.viewerbackend.dataparser.DataParserSysInfoV11;
import de.freifunkdresden.viewerbackend.dataparser.DataParserSysInfoV13;
import de.freifunkdresden.viewerbackend.dataparser.DataParserSysInfoV14;
import de.freifunkdresden.viewerbackend.dataparser.DataParserSysInfoV15;
import de.freifunkdresden.viewerbackend.dataparser.DataParserSysInfoV16;
import de.freifunkdresden.viewerbackend.dataparser.DataParserSysInfoV17;
import de.freifunkdresden.viewerbackend.exception.EmptyJsonException;
import de.freifunkdresden.viewerbackend.exception.HttpStatusCodeException;
import de.freifunkdresden.viewerbackend.exception.MalformedSysInfoException;
import de.freifunkdresden.viewerbackend.exception.NodeCollectionException;
import de.freifunkdresden.viewerbackend.exception.NodeCollectionInfoException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.NoRouteToHostException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.stream.Collectors;

public class NodeSysInfoThread implements Runnable {

    private static final short RETRY_COUNT = 3;
    private static final Logger LOGGER = LogManager.getLogger(NodeSysInfoThread.class);

    private final Node node;
    private short round = 1;

    public NodeSysInfoThread(Node node) {
        this.node = node;
    }

    @Override
    public void run() {
        if (Thread.interrupted()) {
            return;
        }
        try {
            Optional<JsonObject> sysInfo = getSysInfo();
            if (sysInfo.isPresent()) {
                node.setDpSysInfo(getDataParser(sysInfo.get()));
                return;
            }
        } catch (NodeCollectionInfoException e) {
            if (round == RETRY_COUNT && !e.getMessage().startsWith("No route to host")) {
                LOGGER.log(Level.WARN, "Node {}: {}", node.getId(), e.getMessage());
            }
        } catch (RuntimeException e) {
            LOGGER.log(Level.ERROR, String.format("Node %s: ", node.getId()), e);
        }
        if (round < RETRY_COUNT) {
            round++;
            run();
        }
    }

    private Optional<JsonObject> getSysInfo() {
        try {
            String conString = String.format("http://%s/sysinfo-json.cgi", node.getIpAddressString());
            HttpURLConnection con = (HttpURLConnection) new URI(conString).toURL().openConnection();
            con.setConnectTimeout(10000);
            con.setReadTimeout(15000);
            if (con.getResponseCode() == 200) {
                String json;
                try (InputStreamReader reader = new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8)) {
                    try (BufferedReader bufferedReader = new BufferedReader(reader)) {
                        json = bufferedReader.lines().collect(Collectors.joining());
                    }
                }
                //Fix HTML injected in JSON
                int begin = json.indexOf("<!DOCTYPE html>");
                if (begin != -1) {
                    json = json.replaceAll("(<!DOCTYPE html>[\\S\\s]*</html>)", "{}");
                    LOGGER.log(Level.WARN, "Node {}: Stripped html from json", node.getId());
                }
                return Optional.of(JsonParser.parseString(json).getAsJsonObject());
            } else {
                throw new HttpStatusCodeException(con.getResponseCode());
            }
        } catch (NoRouteToHostException ignored) {
            return Optional.empty();
        } catch (UncheckedIOException | SocketException | SocketTimeoutException | HttpStatusCodeException |
                 JsonSyntaxException e) {
            throw new NodeCollectionInfoException(e);
        } catch (RuntimeException | IOException | URISyntaxException e) {
            throw new NodeCollectionException(e);
        }
    }

    @NotNull
    @Contract("_ -> new")
    private static DataParserSysInfo getDataParser(@NotNull JsonObject sysInfo) {
        try {
            if (sysInfo.isEmpty()) {
                throw new EmptyJsonException();
            }
            if (!sysInfo.has("version") || !sysInfo.has("data")) {
                throw new MalformedSysInfoException();
            }
            int version = sysInfo.get("version").getAsInt();
            JsonObject data = sysInfo.get("data").getAsJsonObject();
            if (version >= 17) {
                return new DataParserSysInfoV17(data);
            } else if (version == 16) {
                return new DataParserSysInfoV16(data);
            } else if (version == 15) {
                return new DataParserSysInfoV15(data);
            } else if (version == 14) {
                return new DataParserSysInfoV14(data);
            } else if (version == 13) {
                return new DataParserSysInfoV13(data);
            } else if (version >= 11) {
                return new DataParserSysInfoV11(data);
            } else if (version == 10) {
                return new DataParserSysInfoV10(data);
            } else {
                return new DataParserSysInfo(data);
            }
        } catch (EmptyJsonException | MalformedSysInfoException e) {
            throw new NodeCollectionInfoException(e);
        }
    }
}
