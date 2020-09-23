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
package de.freifunkdresden.viewerbackend.dataparser;

import de.freifunkdresden.viewerbackend.Community;
import de.freifunkdresden.viewerbackend.Link;
import de.freifunkdresden.viewerbackend.Location;
import de.freifunkdresden.viewerbackend.Node;
import de.freifunkdresden.viewerbackend.NodeType;
import java.util.HashSet;

public interface DataParser {

    default Boolean getAutoUpdate() throws Exception {
        return null;
    }

    default Short getClients() throws Exception {
        return null;
    }

    default Community getCommunity() throws Exception {
        return null;
    }

    default String getEMail() throws Exception {
        return null;
    }

    default String getFirmwareBase() throws Exception {
        return null;
    }

    default String getFirmwareVersion() throws Exception {
        return null;
    }

    default Long getFirstSeen() throws Exception {
        return null;
    }

    default Node getGateway() throws Exception {
        return null;
    }

    default Long getLastSeen() throws Exception {
        return null;
    }

    default HashSet<Link> getLinkSet() throws Exception {
        return null;
    }

    default Float getLoadAvg() throws Exception {
        return null;
    }

    default Location getLocation() throws Exception {
        return null;
    }

    default Double getMemoryUsage() throws Exception {
        return null;
    }

    default String getModel() throws Exception {
        return null;
    }

    default String getName() throws Exception {
        return null;
    }

    default NodeType getRole() throws Exception {
        return null;
    }

    default Float getUptime() throws Exception {
        return null;
    }

    default Boolean isOnline() throws Exception {
        return null;
    }

    default Boolean isGateway() throws Exception {
        return null;
    }

    default Boolean hasBackbone() throws Exception {
        return null;
    }

    default Integer getCPUCount() throws Exception {
        return null;
    }
}
