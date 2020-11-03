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

package de.freifunkdresden.viewerbackend;

public class Link {

    private byte sourceTq = 0;
    private byte targetTq = -1;
    private final LinkType type;
    private final Node target;
    private final Node source;

    public Link(LinkType type, Node target, Node source) {
        this.type = type;
        this.target = target;
        this.source = source;
    }

    public Link(LinkType type, byte tq, Node target, Node source) {
        this.type = type;
        this.sourceTq = tq;
        this.target = target;
        this.source = source;
    }

    public byte getSourceTq() {
        return sourceTq;
    }

    public Node getTarget() {
        return target;
    }

    public Node getSource() {
        return source;
    }

    public void setSourceTq(byte tq) {
        this.sourceTq = tq;
    }

    public byte getTargetTq() {
        return targetTq;
    }

    public void setTargetTq(byte tq) {
        this.targetTq = tq;
    }

    public LinkType getType() {
        return type;
    }

    public static float convertToHop(float rawTq) {
        return rawTq < 1 ? 100000 : (Math.round(100f / rawTq * 1000f) / 1000f);
    }

    public static float convertToMeshV(float rawTq) {
        return rawTq == -1 ? 0 : rawTq / 100f;
    }
}
