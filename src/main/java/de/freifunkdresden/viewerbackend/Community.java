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

import org.jetbrains.annotations.NotNull;

public enum Community {
    DRESDEN("Dresden"),
    LEIPZIG("Leipzig"),
    OBERLAUSITZ("Oberlausitz"),
    PIRNA("Pirna"),
    GERINGSWALDE("Geringswalde"),
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

    Community(@NotNull Community community) {
        this.name = community.name;
    }

    public String getName() {
        return name;
    }

    public static Community getCommunity(String community) {
        if (community == null) {
            return DEFAULT;
        }
        return switch (community) {
            case "Dresden" -> DRESDEN;
            case "Leipzig" -> LEIPZIG;
            case "Meißen", "Meissen", "Radebeul", "Dresden NW" -> DRESDEN_NW;
            case "Dresden NO" -> DRESDEN_NO;
            case "Dresden SO" -> DRESDEN_SO;
            case "Freiberg", "Freital", "Tharandt", "Dresden SW" -> DRESDEN_SW;
            case "OL", "O.L.", "Oberlausitz" -> OBERLAUSITZ;
            case "Pirna" -> PIRNA;
            case "Geringswalde" -> GERINGSWALDE;
            default -> DEFAULT;
        };
    }
}
