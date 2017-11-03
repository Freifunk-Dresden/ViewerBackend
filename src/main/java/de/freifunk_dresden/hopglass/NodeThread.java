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
package de.freifunk_dresden.hopglass;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NodeThread implements Runnable {

    private final int nodeId;

    public NodeThread(int nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    public void run() {
        try {
            HttpURLConnection con = (HttpURLConnection) new URL("http://" + "10.200." + (nodeId / 255 % 256) + "." + ((nodeId % 255) + 1) + ".freifunk-dresden.de/sysinfo-json.cgi").openConnection();
            con.setConnectTimeout(10000);
            con.setReadTimeout(10000);
            if (con.getResponseCode() == 503) {
                DataGen.getDB().queryUpdate("UPDATE nodes SET online = 0 WHERE id = ?", nodeId);
                return;
            }
            InputStreamReader reader;
            try (InputStream stream = con.getInputStream()) {
                reader = new InputStreamReader(stream, "UTF-8");
                JsonObject sysinfo = new JsonParser().parse(reader).getAsJsonObject();
                reader.close();
                DataParser dp = new DataParser(sysinfo.get("data").getAsJsonObject(), sysinfo.get("version").getAsInt());
                DataGen.getDB().queryUpdate("UPDATE nodes SET community = ?, role = ?, model = ?, firmwareVersion = ?, firmwareBase = ?, lastseen = ?, gatewayIp = ?, uptime = ?, memory_usage = ?, loadavg = ?, clients = ?, online = 1, name = ?, email = ? WHERE id = ?",
                        dp.getCommunity(),
                        dp.getRole(),
                        dp.getModel(),
                        dp.getFirmwareVersion(),
                        dp.getFirmwareBase(),
                        System.currentTimeMillis() / 1000,
                        dp.getGatewayIp(),
                        dp.getUptime(),
                        dp.getMemoryUsage(),
                        dp.getLoadAvg(),
                        dp.getClients(),
                        dp.getName(),
                        dp.getEMail(),
                        nodeId);
                HashMap<Integer, Link> linkmap = dp.getLinkMap();
                if (!linkmap.isEmpty()) {
                    MySQL.PreparedUpdate prep = DataGen.getDB().queryPrepUpdate("INSERT INTO links SET `from` = ?, `to` = ?, `interface` = ?, `tq` = ?");
                    linkmap.entrySet().stream().forEach((e) -> {
                        prep.add(nodeId, e.getKey(), e.getValue().getIface(), e.getValue().getTq());
                    });
                    prep.done();
                }
            }
        } catch (IOException ex) {
            DataGen.getDB().queryUpdate("UPDATE nodes SET online = 0 WHERE id = ?", nodeId);
        } catch (NullPointerException ex) {
            DataGen.getDB().queryUpdate("UPDATE nodes SET online = 0 WHERE id = ?", nodeId);
            Logger.getLogger(NodeThread.class.getName()).log(Level.SEVERE, "Node " + nodeId, ex);
        }
    }
}
