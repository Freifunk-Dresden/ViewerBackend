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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class DataHolder {

    private final Map<Integer, Node> nodes = new LinkedHashMap<>();
    private final Map<LinkKey, Link> links = new HashMap<>();

    public Node getNode(int id) {
        return nodes.computeIfAbsent(id, Node::new);
    }

    @Nullable
    public Node getNodeByIp(@NotNull String ip) {
        int id = Node.convertIpToId(ip);
        if (id == -1) {
            return null;
        }
        return getNode(id);
    }

    public Link getLink(Node node1, Node node2, LinkType type) {
        return links.get(new LinkKey(node1, node2, type));
    }

    public void addLink(Link l) {
        links.put(new LinkKey(l.getSource(), l.getTarget(), l.getType()), l);
    }

    public Map<Integer, Node> getNodes() {
        return new LinkedHashMap<>(nodes);
    }

    public Collection<Link> getLinks() {
        return links.values();
    }

    private static class LinkKey {
        private final Node small;
        private final Node big;
        private final LinkType type;

        public LinkKey(@NotNull Node node1, @NotNull Node node2, LinkType type) {
            if (node1.getId() < node2.getId()) {
                this.small = node1;
                this.big = node2;
            } else {
                this.small = node2;
                this.big = node1;
            }
            this.type = type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            LinkKey linkKey = (LinkKey) o;

            if (!small.equals(linkKey.small)) return false;
            if (!big.equals(linkKey.big)) return false;
            return type == linkKey.type;
        }

        @Override
        public int hashCode() {
            int result = small.hashCode();
            result = 31 * result + big.hashCode();
            result = 31 * result + type.hashCode();
            return result;
        }
    }
}
