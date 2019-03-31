/*
 * The MIT License
 *
 * Copyright 2019 Niklas Merkelt.
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
package de.freifunkdresden.viewerbackend.stats;

import de.freifunkdresden.viewerbackend.DataGen;
import de.freifunkdresden.viewerbackend.Node;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.influxdb.dto.Point;

public class StatsSQL {

    private static final Map<GeneralStatType, Double> generalStats = new EnumMap<GeneralStatType, Double>(GeneralStatType.class);
    private static final Set<Node> nodes = Collections.synchronizedSet(new LinkedHashSet<>());

    public static void addToStats(Node n) {
        nodes.add(n);
    }

    public static void addGeneralStats(GeneralStatType type, double value) {
        generalStats.put(type, value);
    }

    public static void processStats() {
        nodes.forEach((e) -> {
            DataGen.getInflux().write(Point.measurement("node_clients")
                    .tag("node", String.valueOf(e.getId()))
                    .addField("value", e.getClients())
                    .build());
            DataGen.getInflux().write(Point.measurement("node_load")
                    .tag("node", String.valueOf(e.getId()))
                    .addField("value", e.getLoadAvg())
                    .build());
            DataGen.getInflux().write(Point.measurement("node_memory")
                    .tag("node", String.valueOf(e.getId()))
                    .addField("value", e.getMemoryUsage())
                    .build());
        });
        generalStats.entrySet().forEach((e) -> {
            DataGen.getInflux().write(Point.measurement(e.getKey().name().toLowerCase())
                    .addField("value", e.getValue())
                    .build());
        });
    }
}
