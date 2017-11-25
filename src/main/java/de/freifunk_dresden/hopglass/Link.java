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

public class Link {

    private int sourceTq = -1;
    private int targetTq = 0;
    private final String iface;
    private final Node target;
    private final Node source;

    public Link(String iface, Node target, Node source) {
        this.iface = iface;
        this.target = target;
        this.source = source;
    }

    public Link(String iface, int tq, Node target, Node source) {
        this.iface = iface;
        this.sourceTq = tq;
        this.target = target;
        this.source = source;
    }

    public int getSourceTq() {
        return sourceTq;
    }

    public String getIface() {
        return iface;
    }

    public Node getTarget() {
        return target;
    }

    public Node getSource() {
        return source;
    }

    public void setSourceTq(int tq) {
        this.sourceTq = tq;
    }

    public int getTargetTq() {
        return targetTq;
    }

    public void setTargetTq(int tq) {
        this.targetTq = tq;
    }

    public String getType() {
        switch (iface) {
            case "wlan0":
                return "wireless";
            case "br-tbb":
            case "br-meshwire":
                return "other";
            case "tbb-fastd":
            case "tbb_fastd":
            case "tbb_fastd2":
                return "tunnel";
            default:
                return null;
        }
    }
}
