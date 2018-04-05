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
package de.freifunkdresden.viewerbackend;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.logging.Level;

public class NodeThread implements Runnable {

    private final Node node;

    public NodeThread(Node nodeId) {
        this.node = nodeId;
    }

    @Override
    public void run() {
        try {
            HttpURLConnection con = (HttpURLConnection) new URL("http://" + node.getIpAdress() + "/sysinfo-json.cgi").openConnection();
            con.setConnectTimeout(10000);
            con.setReadTimeout(10000);
            InputStreamReader reader;
            try (InputStream stream = con.getInputStream()) {
                reader = new InputStreamReader(stream, "UTF-8");
                JsonObject sysinfo = new JsonParser().parse(reader).getAsJsonObject();
                reader.close();
                DataParser dp = new DataParser(sysinfo.get("data").getAsJsonObject(), sysinfo.get("version").getAsInt());
                node.parseData(dp);
            }
        } catch (NoRouteToHostException | ConnectException | SocketTimeoutException ex) {
            node.setOnline(false);
            DataGen.getLogger().log(Level.WARNING, "Node {0}: {1}", new Object[]{node.getId(), ex.getMessage()});
        } catch (IOException | NullPointerException ex) {
            node.setOnline(false);
            DataGen.getLogger().log(Level.SEVERE, "Node " + node.getId(), ex);
        }
    }
}