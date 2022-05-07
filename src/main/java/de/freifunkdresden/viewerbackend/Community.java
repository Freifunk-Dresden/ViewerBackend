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
    OBERLAUSITZ("Oberlausitz"),
    PIRNA("Pirna"),
    DRESDEN_NW("Dresden Nord-West"),
    DRESDEN_NO("Dresden Nord-Ost"),
    DRESDEN_SO("Dresden Süd-Ost"),
    DRESDEN_SW("Dresden Süd-West"),
    DEFAULT(DRESDEN),
    ;

    private final String name;

    Community(String name) {
        this.name = name;
    }

    Community(Community community) {
        this.name = community.name;
    }

    public String getName() {
        return name;
    }

    public static Community getCommunity(String community) {
        if (community == null) {
            return DEFAULT;
        }
        switch (community) {
            case "Dresden":
                return DRESDEN;
            case "Meißen":
            case "Meissen":
            case "Radebeul":
            case "Dresden NW":
                return DRESDEN_NW;
            case "Dresden NO":
                return DRESDEN_NO;
            case "Dresden SO":
                return DRESDEN_SO;
            case "Freiberg":
            case "Freital":
            case "Tharandt":
            case "Dresden SW":
                return DRESDEN_SW;
            case "OL":
            case "O.L.":
            case "Oberlausitz":
                return OBERLAUSITZ;
            case "Pirna":
                return PIRNA;
            default:
                return DEFAULT;
        }
    }
}
