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
import de.freifunk_dresden.hopglass.Link.LinkType;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class JsonFileGen {

    private final Gson gson;
    private final JsonArray hopGlassNodes = new JsonArray();
    private final JsonArray graphNodes = new JsonArray();
    private final JsonArray graphLinks = new JsonArray();
    private final JsonArray meshViewerNodes = new JsonArray();
    private final JsonArray meshViewerLinks = new JsonArray();
    private final HashMap<Integer, Integer> nodeIds = new HashMap<>();

    public JsonFileGen(Collection<Node> nodes, Collection<HashMap<Integer, Link>> links) {
        this.gson = new GsonBuilder().create();
        int i = 0;
        Iterator<Node> iterator = nodes.stream().filter((node) -> node.isDisplayed()).iterator();
        for (Iterator<Node> it = iterator; it.hasNext();) {
            Node node = it.next();
            hopGlassNodes.add(node.getJsonObject());
            meshViewerNodes.add(node.getMeshViewerObj());
            JsonObject jsonNode = new JsonObject();
            jsonNode.addProperty("node_id", String.valueOf(node.getId()));
            jsonNode.addProperty("id", String.valueOf(node.getId()));
            jsonNode.addProperty("seq", i);
            graphNodes.add(jsonNode);
            nodeIds.put(node.getId(), i);
            i++;
        }
        links.forEach((map) -> {
            map.values().stream()
                    .filter((link) -> link.getSource().isDisplayed() && link.getTarget().isDisplayed())
                    .forEach((link) -> {
                        JsonObject jsonLink;
                        //Meshviewer
                        if (!link.getType().equals(LinkType.TUNNEL)) {
                            jsonLink = new JsonObject();
                            jsonLink.addProperty("source", link.getSource().getId());
                            jsonLink.addProperty("target", link.getTarget().getId());
                            jsonLink.addProperty("source_tq", (float) link.getSourceTq() / 100f);
                            jsonLink.addProperty("target_tq", (float) link.getTargetTq() / 100f);
                            jsonLink.addProperty("type", link.getType().name().toLowerCase());
                            meshViewerLinks.add(jsonLink);
                        }
                        //Hopglass
                        Integer source = nodeIds.get(link.getSource().getId());
                        Integer target = nodeIds.get(link.getTarget().getId());
                        jsonLink = new JsonObject();
                        jsonLink.addProperty("source", source);
                        jsonLink.addProperty("target", target);
                        jsonLink.addProperty("tq", link.getSourceTq() < 1 ? 100000 : Math.round(100d / (double) link.getSourceTq()));
                        jsonLink.addProperty("type", link.getType().name().toLowerCase());
                        graphLinks.add(jsonLink);
                        jsonLink = new JsonObject();
                        jsonLink.addProperty("source", target);
                        jsonLink.addProperty("target", source);
                        jsonLink.addProperty("tq", link.getTargetTq() < 1 ? 100000 : Math.round(100d / (double) link.getTargetTq()));
                        jsonLink.addProperty("type", link.getType().name().toLowerCase());
                        graphLinks.add(jsonLink);
                    });
        });
    }

    public void genNodes() throws IOException {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("nodes", hopGlassNodes);
        jsonObject.addProperty("timestamp", DataGen.DATE_HOP.format(new Date()));
        jsonObject.addProperty("version", 2);
        File file = new File("nodes.json");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(gson.toJson(jsonObject));
            writer.flush();
        }
    }

    public void genGraph() throws IOException {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("version", 1);
        JsonObject batadv = new JsonObject();
        batadv.addProperty("multigraph", false);
        batadv.addProperty("directed", true);
        batadv.add("graph", new JsonArray());
        batadv.add("nodes", graphNodes);
        batadv.add("links", graphLinks);
        jsonObject.add("batadv", batadv);
        File file = new File("graph.json");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(gson.toJson(jsonObject));
            writer.flush();
        }
    }

    public void genMeshViewer() throws IOException {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("timestamp", DataGen.DATE_MESH.format(new Date()));
        jsonObject.add("nodes", meshViewerNodes);
        jsonObject.add("links", meshViewerLinks);
        File file = new File("meshviewer.json");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(gson.toJson(jsonObject));
            writer.flush();
        }
    }
}
