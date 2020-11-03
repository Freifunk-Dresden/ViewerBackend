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
package de.freifunkdresden.viewerbackend.json;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.freifunkdresden.viewerbackend.Link;
import de.freifunkdresden.viewerbackend.Node;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TimeZone;

public class JsonFileGen {

    private final DateFormat dateMesh = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    private final DateFormat dateHop = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    private final Gson gson = new Gson();
    private final JsonArray hopGlassNodes = new JsonArray();
    private final JsonArray graphNodes = new JsonArray();
    private final JsonArray graphLinks = new JsonArray();
    private final JsonArray meshViewerNodes = new JsonArray();
    private final JsonArray meshViewerLinks = new JsonArray();
    private final Map<Node, Integer> nodeIds = new HashMap<>();
    private final Path path;

    public JsonFileGen(Path path, Collection<Node> nodes, Collection<Map<Integer, Link>> links) {
        this.path = path;
        dateHop.setTimeZone(TimeZone.getTimeZone("UTC"));
        Iterator<Node> it = nodes.stream().filter(Node::isDisplayed).iterator();
        for (int i = 0; it.hasNext(); i++) {
            Node node = it.next();
            hopGlassNodes.add(JsonNodeGen.getJsonObject(node, dateHop));
            meshViewerNodes.add(JsonNodeGen.getMeshViewerObj(node, dateMesh));
            JsonObject jsonNode = new JsonObject();
            jsonNode.addProperty("node_id", String.valueOf(node.getId()));
            jsonNode.addProperty("id", String.valueOf(node.getId()));
            jsonNode.addProperty("seq", i);
            graphNodes.add(jsonNode);
            nodeIds.put(node, i);
        }
        links.forEach(map -> map.values().stream()
                .filter(link -> link.getSource().isDisplayed() && link.getTarget().isDisplayed())
                .filter(link -> link.getSource().isOnline() && link.getTarget().isOnline())
                .forEach(link -> {
                    JsonObject jsonLink;
                    //MeshViewer
                    jsonLink = new JsonObject();
                    jsonLink.addProperty("source", link.getSource().getFakeId());
                    jsonLink.addProperty("target", link.getTarget().getFakeId());
                    jsonLink.addProperty("source_tq", Link.convertToMeshV(link.getSourceTq()));
                    jsonLink.addProperty("target_tq", Link.convertToMeshV(link.getTargetTq()));
                    jsonLink.addProperty("type", link.getType().getMeshviewer());
                    jsonLink.addProperty("source_addr", link.getSource().getFakeMac());
                    jsonLink.addProperty("target_addr", link.getTarget().getFakeMac());
                    meshViewerLinks.add(jsonLink);
                    //HopGlass
                    Integer source = nodeIds.get(link.getSource());
                    Integer target = nodeIds.get(link.getTarget());
                    jsonLink = new JsonObject();
                    jsonLink.addProperty("source", source);
                    jsonLink.addProperty("target", target);
                    jsonLink.addProperty("tq", Link.convertToHop(link.getSourceTq()));
                    jsonLink.addProperty("type", link.getType().getHopglass());
                    graphLinks.add(jsonLink);
                    if (link.getTargetTq() != -1) {
                        jsonLink = new JsonObject();
                        jsonLink.addProperty("source", target);
                        jsonLink.addProperty("target", source);
                        jsonLink.addProperty("tq", Link.convertToHop(link.getTargetTq()));
                        jsonLink.addProperty("type", link.getType().getHopglass());
                        graphLinks.add(jsonLink);
                    }
                }));
    }

    public void genNodes() throws IOException {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("nodes", hopGlassNodes);
        jsonObject.addProperty("timestamp", dateHop.format(new Date()));
        jsonObject.addProperty("version", 2);
        Path fp = path.resolve("nodes.json");
        Files.writeString(fp, gson.toJson(jsonObject), StandardCharsets.UTF_8);
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
        Path fp = path.resolve("graph.json");
        Files.writeString(fp, gson.toJson(jsonObject), StandardCharsets.UTF_8);
    }

    public void genMeshViewer() throws IOException {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("timestamp", dateMesh.format(new Date()));
        jsonObject.add("nodes", meshViewerNodes);
        jsonObject.add("links", meshViewerLinks);
        Path fp = path.resolve("meshviewer.json");
        Files.writeString(fp, gson.toJson(jsonObject), StandardCharsets.UTF_8);
    }
}
