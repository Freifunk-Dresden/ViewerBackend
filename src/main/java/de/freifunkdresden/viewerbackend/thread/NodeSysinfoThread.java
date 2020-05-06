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

import de.freifunkdresden.viewerbackend.dataparser.DataParserSysinfo;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import de.freifunkdresden.viewerbackend.DataGen;
import de.freifunkdresden.viewerbackend.Node;
import de.freifunkdresden.viewerbackend.dataparser.DataParserSysinfoV10;
import de.freifunkdresden.viewerbackend.dataparser.DataParserSysinfoV11;
import de.freifunkdresden.viewerbackend.dataparser.DataParserSysinfoV13;
import de.freifunkdresden.viewerbackend.dataparser.DataParserSysinfoV14;
import de.freifunkdresden.viewerbackend.exception.EmptyJsonException;
import de.freifunkdresden.viewerbackend.exception.MalformedSysinfoException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class NodeSysinfoThread implements Runnable {

    private static final int RETRY_COUNT = 3;

    private final Node node;

    public NodeSysinfoThread(Node node) {
        this.node = node;
    }

    @Override
    public void run() {
        for (int i = 0; i < RETRY_COUNT; i++) {
            try {
                checkNode(node);
                return;
            } catch (NoRouteToHostException | ConnectException | SocketTimeoutException ex) {
                node.setOnline(false);
                if (i + 1 == RETRY_COUNT && !(ex instanceof NoRouteToHostException || ex.getMessage().startsWith("No route to host"))) {
                    DataGen.getLogger().log(Level.WARNING, "Node {0}: {1}", new Object[]{String.valueOf(node.getId()), ex.getMessage()});
                }
            } catch (IOException | NullPointerException ex) {
                node.setOnline(false);
                DataGen.getLogger().log(Level.SEVERE, "Node " + node.getId(), ex);
            } catch (JsonSyntaxException ex) {
                node.setOnline(false);
                DataGen.getLogger().log(Level.SEVERE, "Node {0} has malformed json", String.valueOf(node.getId()));
            } catch (EmptyJsonException ex) {
                node.setOnline(false);
                DataGen.getLogger().log(Level.SEVERE, "Node {0} has empty json", String.valueOf(node.getId()));
            } catch (MalformedSysinfoException ex) {
                node.setOnline(false);
                DataGen.getLogger().log(Level.SEVERE, "Node {0} has malformed sysinfo", String.valueOf(node.getId()));
            }
        }
    }

    private static void checkNode(Node n) throws IOException, EmptyJsonException, MalformedSysinfoException {
        HttpURLConnection con = (HttpURLConnection) new URL("http://" + n.getIpAddress() + "/sysinfo-json.cgi").openConnection();
        con.setConnectTimeout(10000);
        con.setReadTimeout(15000);
        String json;
        try (InputStreamReader reader = new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8)) {
            json = new BufferedReader(reader).lines().collect(Collectors.joining());
        }
        //Fix HTML injected in JSON
        int begin = json.indexOf("<!DOCTYPE html>");
        if (begin != -1) {
            json = json.replaceAll("(<!DOCTYPE html>[\\S\\s]*<\\/html>)", "{}");
        }
        n.fill(getDataParser(JsonParser.parseString(json).getAsJsonObject()));
    }

    private static DataParserSysinfo getDataParser(JsonObject sysinfo) throws EmptyJsonException, MalformedSysinfoException {
        if (sysinfo.size() == 0) {
            throw new EmptyJsonException();
        }
        if (!sysinfo.has("version") || !sysinfo.has("data")) {
            throw new MalformedSysinfoException();
        }
        int version = sysinfo.get("version").getAsInt();
        JsonObject data = sysinfo.get("data").getAsJsonObject();
        if (version >= 14) {
            return new DataParserSysinfoV14(data);
        } else if (version >= 13) {
            return new DataParserSysinfoV13(data);
        } else if (version >= 11) {
            return new DataParserSysinfoV11(data);
        } else if (version >= 10) {
            return new DataParserSysinfoV10(data);
        } else {
            return new DataParserSysinfo(data);
        }
    }
}
