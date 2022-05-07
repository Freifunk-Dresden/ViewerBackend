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
import de.freifunkdresden.viewerbackend.exception.HTTPStatusCodeException;
import de.freifunkdresden.viewerbackend.exception.MalformedSysInfoException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class NodeSysInfoThread implements Runnable {

    private static final int RETRY_COUNT = 3;
    private static final Logger LOGGER = LogManager.getLogger(NodeSysInfoThread.class);

    private final Node node;

    public NodeSysInfoThread(Node node) {
        this.node = node;
    }

    @Override
    public void run() {
        for (int i = 0; i < RETRY_COUNT; i++) {
            try {
                if (!isReachable(node.getIpAddress())) {
                    continue;
                }
                String sysInfoString = getSysInfoString();
                JsonObject json = JsonParser.parseString(sysInfoString).getAsJsonObject();
                node.setDpSysInfo(getDataParser(json));
                return;
            } catch (NoRouteToHostException ignored) {
            } catch (JsonSyntaxException | EmptyJsonException | MalformedSysInfoException |
                    ConnectException | SocketTimeoutException | HTTPStatusCodeException ex) {
                if (i + 1 == RETRY_COUNT && !ex.getMessage().startsWith("No route to host")) {
                    LOGGER.log(Level.WARN, "Node {}: {}", node.getId(), ex.getMessage());
                }
            } catch (IOException | NullPointerException ex) {
                LOGGER.log(Level.ERROR, String.format("Node %s: ", node.getId()), ex);
            }
        }
    }

    private String getSysInfoString() throws IOException {
        String conString = String.format("http://%s/sysinfo-json.cgi", node.getIpAddressString());
        HttpURLConnection con = (HttpURLConnection) new URL(conString).openConnection();
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
                LOGGER.log(Level.WARN, "Node {}: {}", node.getId(), "Stripped html from json");
            }
            return json;
        } else {
            throw new HTTPStatusCodeException(con.getResponseCode());
        }
    }

    @NotNull
    @Contract("_ -> new")
    private static DataParserSysInfo getDataParser(@NotNull JsonObject sysInfo) throws EmptyJsonException, MalformedSysInfoException {
        if (sysInfo.size() == 0) {
            throw new EmptyJsonException();
        }
        if (!sysInfo.has("version") || !sysInfo.has("data")) {
            throw new MalformedSysInfoException();
        }
        int version = sysInfo.get("version").getAsInt();
        JsonObject data = sysInfo.get("data").getAsJsonObject();
        if (version >= 17) {
            return new DataParserSysInfoV17(data);
        } else if (version >= 16) {
            return new DataParserSysInfoV16(data);
        } else if (version >= 15) {
            return new DataParserSysInfoV15(data);
        } else if (version >= 14) {
            return new DataParserSysInfoV14(data);
        } else if (version >= 13) {
            return new DataParserSysInfoV13(data);
        } else if (version >= 11) {
            return new DataParserSysInfoV11(data);
        } else if (version >= 10) {
            return new DataParserSysInfoV10(data);
        } else {
            return new DataParserSysInfo(data);
        }
    }

    private static boolean isReachable(InetAddress inetAddress) {
        try {
            Process process = Runtime.getRuntime().exec("ip r get " + inetAddress.getHostAddress());
            process.waitFor(100, TimeUnit.MILLISECONDS);
            return process.exitValue() != 2;
        } catch (IOException e) {
            // empty
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return false;
    }
}
