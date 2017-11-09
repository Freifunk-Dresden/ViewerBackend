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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

public class JsonFileGen {

    private final Collection<Node> nodes;
    private final Gson gson;

    public JsonFileGen(Collection<Node> nodes) {
        this.nodes = nodes;
        this.gson = new GsonBuilder().create();
    }

    public void genNodes() throws IOException {
        JsonArray jsonNodes = new JsonArray();
        nodes.stream().filter((node) -> node.isDisplayed() && node.isValid()).forEach((node) -> jsonNodes.add(node.getJsonObject()));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("nodes", jsonNodes);
        jsonObject.addProperty("timestamp", sdf.format(new Date()));
        jsonObject.addProperty("version", 2);
        File file = new File("nodes.json");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(gson.toJson(jsonObject));
            writer.flush();
        }
    }
    
    public void genGraph() throws IOException {
        JsonArray jsonNodes = new JsonArray();
        int i = 0;
        HashMap<Integer,Integer> nodeIds = new HashMap<>();
        for (Node node : nodes) {
            if (node.isDisplayed()) {
                JsonObject jsonNode = new JsonObject();
                jsonNode.addProperty("node_id", String.valueOf(node.getId()));
                jsonNode.addProperty("id", String.valueOf(node.getId()));
                jsonNode.addProperty("seq", i);
                jsonNodes.add(jsonNode);
                nodeIds.put(node.getId(), i);
                i++;
            }
        }
        JsonArray jsonLinks = new JsonArray();
        nodes.stream().filter((node) -> node.isDisplayed()).forEach((node) -> {
            node.getLinks().stream().filter((link) -> link.getType() != null && link.getTarget() != null && link.getTarget().isDisplayed()).forEach((link) -> {
                try {
                    Integer source = nodeIds.get(node.getId());
                    Integer target = nodeIds.get(link.getTarget().getId());
                    JsonObject jsonLink = new JsonObject();
                    jsonLink.addProperty("source", source);
                    jsonLink.addProperty("target", target);
                    jsonLink.addProperty("tq", link.getTq() < 1 ? 100000 : Math.round(100d / link.getTq()));
                    jsonLink.addProperty("type", link.getType());
                    jsonLinks.add(jsonLink);
                } catch (ArrayIndexOutOfBoundsException e) {
                }
            });
        });
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("version", 1);
        JsonObject batadv = new JsonObject();
        batadv.addProperty("multigraph", false);
        batadv.addProperty("directed", true);
        batadv.add("graph", new JsonArray());
        batadv.add("nodes", jsonNodes);
        batadv.add("links", jsonLinks);
        jsonObject.add("batadv", batadv);
        File file = new File("graph.json");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(gson.toJson(jsonObject));
            writer.flush();
        }
    }
}
