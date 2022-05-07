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

import com.google.gson.JsonObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public enum LinkType {
    WIRELESS("wireless", "wifi"),
    TUNNEL("tunnel", "vpn"),
    OTHER("other", "other");

    private final String hopGlass;
    private final String meshViewer;

    LinkType(String hopGlass, String meshViewer) {
        this.hopGlass = hopGlass;
        this.meshViewer = meshViewer;
    }

    public String getHopGlass() {
        return hopGlass;
    }

    public String getMeshViewer() {
        return meshViewer;
    }

    @Contract(pure = true)
    public static LinkType getTypeByInterface(@NotNull String interfaceName) {
        switch (interfaceName.toLowerCase()) {
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

    @Contract(pure = true)
    public static LinkType getTypeByType(@NotNull String type) {
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

    public static LinkType getTypeFromLink(@NotNull JsonObject l) {
        if (l.has("type")) {
            String type = l.get("type").getAsString();
            if (!type.isBlank()) {
                return getTypeByType(type);
            }
        }
        String linkInterface = l.get("interface").getAsString();
        return getTypeByInterface(linkInterface);
    }
}
