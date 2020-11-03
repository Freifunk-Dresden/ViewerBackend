/*
 * The MIT License
 *
 * Copyright 2018 Niklas Merkelt.
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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

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
        if (!links.containsKey(min)) {
            links.put(min, new HashMap<>());
        }
        return links.get(min).get(max);
    }

    public void addLink(Link l) {
        int min = Math.min(l.getSource().getId(), l.getTarget().getId());
        int max = Math.max(l.getSource().getId(), l.getTarget().getId());
        if (!links.containsKey(min)) {
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
}
