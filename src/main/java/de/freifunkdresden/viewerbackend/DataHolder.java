/*
 * The MIT License
 *
 * Copyright 2018 NMerkelt.
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
import de.freifunkdresden.viewerbackend.dataparser.DataParserAPI;
import de.freifunkdresden.viewerbackend.dataparser.DataParserRegister;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class DataHolder {
    
    private final Map<Integer, Node> nodes = new LinkedHashMap<>();
    private final Map<Integer, Map<Integer, Link>> links = new HashMap<>();
    
    public Node getNode(int id) {
        Node n = nodes.getOrDefault(id, new Node(id));
        if (!nodes.containsKey(id)) {
            nodes.put(id, n);
        }
        return n;
    }
    
    public Link getLink(int node1, int node2) {
        int min = Math.min(node1, node2);
        int max = Math.max(node1, node2);
        Map<Integer, Link> get = links.get(min);
        if (get == null) {
            links.put(min, new HashMap<>());
        }
        return links.get(min).get(max);
    }
    
    public void addLink(Link l) {
        int min = Math.min(l.getSource().getId(), l.getTarget().getId());
        int max = Math.max(l.getSource().getId(), l.getTarget().getId());
        if (links.get(min) == null) {
            links.put(min, new HashMap<>());
        }
        links.get(min).put(max, l);
    }

    public Map<Integer, Node> getNodes() {
        return new LinkedHashMap<>(nodes);
    }

    public Map<Integer, Map<Integer, Link>> getLinks() {
        return new HashMap<>(links);
    }
    
    public void processAPI() throws IOException {
        URLConnection con = new URL("http://api.freifunk-dresden.de/freifunk-nodes.json").openConnection();
        InputStreamReader reader;
        try (InputStream stream = con.getInputStream()) {
            reader = new InputStreamReader(stream, "UTF-8");
            JsonObject api = new JsonParser().parse(reader).getAsJsonObject();
            reader.close();
            api.get("nodes").getAsJsonArray().forEach((node) -> {
                JsonObject n = node.getAsJsonObject();
                getNode(n.get("id").getAsInt()).fill(new DataParserAPI(n));
            });
        }
    }
    
    public void processRegister() throws IOException {
        URLConnection register = new URL("http://register.freifunk-dresden.de/").openConnection();
        InputStreamReader reader;
        try (InputStream stream = register.getInputStream()) {
            reader = new InputStreamReader(stream, "UTF-8");
            String html = new BufferedReader(reader).lines().collect(Collectors.joining(" "));
            reader.close();
            Document doc = Jsoup.parse(html);
            Element tbody = doc.select("tbody").first();
            tbody.children().select(".node_db_color0").forEach(this::parseRegister);
            tbody.children().select(".node_db_color2").forEach(this::parseRegister);
        }
    }

    private void parseRegister(Element tr) {
        getNode(Integer.parseInt(tr.child(1).child(0).text())).fill(new DataParserRegister(tr));
    }
}
