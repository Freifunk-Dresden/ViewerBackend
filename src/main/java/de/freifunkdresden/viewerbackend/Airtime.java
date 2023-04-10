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

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public record Airtime(long active, long busy, long receive, long transmit) {

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
    public String toString() {
        return String.format("a: %d \t b: %d \t r: %d \t t: %d", active, busy, receive, transmit);
    }

    @NotNull
    @Contract("_, _ -> new")
    public static Airtime diff(@NotNull Airtime a, @NotNull Airtime b) {
        return new Airtime(a.active() < b.active() ? a.active() : a.active() - b.active(),
                a.busy() < b.busy() ? a.busy() : a.busy() - b.busy(),
                a.receive() < b.receive() ? a.receive() : a.receive() - b.receive(),
                a.transmit() < b.transmit() ? a.transmit() : a.transmit() - b.transmit());
    }
}
