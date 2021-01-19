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

    private final long active;
    private final long busy;
    private final long receive;
    private final long transmit;

    public Airtime(long active, long busy, long receive, long transmit) {
        this.active = active;
        this.busy = busy;
        this.receive = receive;
        this.transmit = transmit;
    }

    public long getActive() {
        return active;
    }

    public long getBusy() {
        return busy;
    }

    public long getReceive() {
        return receive;
    }

    public long getTransmit() {
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

    @Override
    public String toString() {
        return String.format("a: %d \t b: %d \t r: %d \t t: %d", active, busy, receive, transmit);
    }

    public boolean isEmpty() {
        return this.equals(EMPTY);
    }

    public static Airtime diff(Airtime a, Airtime b) {
        if (a == null && b == null) {
            return Airtime.EMPTY;
        } else if (a == null) {
            return b;
        } else if (b == null) {
            return a;
        }
        return new Airtime(a.getActive() < b.getActive() ? a.getActive() : a.getActive() - b.getActive(),
                a.getBusy() < b.getBusy() ? a.getBusy() : a.getBusy() - b.getBusy(),
                a.getReceive() < b.getReceive() ? a.getReceive() : a.getReceive() - b.getReceive(),
                a.getTransmit() < b.getTransmit() ? a.getTransmit() : a.getTransmit() - b.getTransmit());
    }
}
