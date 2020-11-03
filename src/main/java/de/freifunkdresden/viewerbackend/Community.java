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

public enum Community {
    DRESDEN("Dresden"),
    FREIBERG("Freiberg"),
    FREITAL("Freital"),
    MEISSEN("Meissen"),
    OBERLAUSITZ("Oberlausitz"),
    PIRNA("Pirna"),
    RADEBEUL("Radebeul"),
    THARANDT("Tharandt"),
    WALDHEIM("Waldheim"),
    DEFAULT(DRESDEN);

    private final String name;

    private Community(Community c) {
        this(c.getName());
    }

    private Community(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static Community getCommunity(String community) {
        if (community == null) {
            return DRESDEN;
        }
        switch (community) {
            case "Dresden":
                return DRESDEN;
            case "Freiberg":
                return FREIBERG;
            case "Freital":
                return FREITAL;
            case "Meißen":
            case "Meissen":
                return MEISSEN;
            case "OL":
            case "O.L.":
            case "Oberlausitz":
                return OBERLAUSITZ;
            case "Pirna":
                return PIRNA;
            case "Radebeul":
                return RADEBEUL;
            case "Tharandt":
                return THARANDT;
            case "Waldheim":
                return WALDHEIM;
            default:
                return DEFAULT;
        }
    }
}
