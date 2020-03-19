/*
 * The MIT License
 *
 * Copyright 2019 NMerkelt.
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

public enum VPN {
    VPN1("1", 255),
    VPN2("2", 111),
    VPN3("3", 2),
    VPN6("6", 51017),
    VPN7("7", 4),
    VPN10("10", 120),
    VPN11("11", 200),
    VPN12("12", 256),
    VPN13("13", 13),
    VPN14("14", 14),
    VPN15("15", 51033),
    VPN16("16", 201),
    VPNZH("ZH", 51064),
    VPNOFF("OFF", 51051),
    NS1("NS1", 3),
    NS2("NS2", 15);

    private final String vpnId;
    private final int nodeId;

    VPN(String vpnId, int nodeId) {
        this.vpnId = vpnId;
        this.nodeId = nodeId;
    }

    public String getVpnId() {
        return vpnId;
    }

    public int getNodeId() {
        return nodeId;
    }

    public static VPN getVPN(int nodeId) {
        for (VPN vpn : VPN.values()) {
            if (vpn.nodeId == nodeId) {
                return vpn;
            }
        }
        return null;
    }
}
