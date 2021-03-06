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

public enum LinkType {
    WIRELESS("wireless", "wifi"),
    TUNNEL("tunnel", "vpn"),
    OTHER("other", "other");

    private final String hopglass;
    private final String meshviewer;

    LinkType(String hopglass, String meshviewer) {
        this.hopglass = hopglass;
        this.meshviewer = meshviewer;
    }

    public String getHopglass() {
        return hopglass;
    }

    public String getMeshviewer() {
        return meshviewer;
    }

    public static LinkType getTypeByInterface(String iface) {
        switch (iface.toLowerCase()) {
            case "wlan0":
            case "mesh-80211s":
            case "mesh-adhoc":
            case "mesh2g-80211s":
            case "mesh5g-80211s":
                return LinkType.WIRELESS;
            case "br-tbb":
            case "br-meshwire":
            case "br-mesh_lan":
            case "br-mesh_wan":
                return LinkType.OTHER;
            case "tbb-fastd":
            case "tbb_fastd":
            case "tbb_fastd2":
            default:
                return LinkType.TUNNEL;
        }
    }

    public static LinkType getTypeByType(String type) {
        switch (type.toLowerCase()) {
            case "lan":
                return LinkType.OTHER;
            case "wifi":
            case "wifi_mesh":
            case "wifi_adhoc":
                return LinkType.WIRELESS;
            case "backbone":
            default:
                return LinkType.TUNNEL;
        }
    }
}
