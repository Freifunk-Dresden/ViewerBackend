/*
 * The MIT License
 *
 * Copyright 2020 Niklas Merkelt.
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

import java.util.Objects;

public class Airtime {

    public static final Airtime EMPTY = new Airtime(0, 0, 0, 0);

    private final int active;
    private final int busy;
    private final int receive;
    private final int transmit;

    public Airtime(int active, int busy, int receive, int transmit) {
        this.active = active;
        this.busy = busy;
        this.receive = receive;
        this.transmit = transmit;
    }

    public int getActive() {
        return active;
    }

    public int getBusy() {
        return busy;
    }

    public int getReceive() {
        return receive;
    }

    public int getTransmit() {
        return transmit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Airtime airtime = (Airtime) o;
        return active == airtime.active &&
                busy == airtime.busy &&
                receive == airtime.receive &&
                transmit == airtime.transmit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(active, busy, receive, transmit);
    }
}
