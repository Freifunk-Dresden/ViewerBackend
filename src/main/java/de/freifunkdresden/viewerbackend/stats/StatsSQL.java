/*
 * The MIT License
 *
 * Copyright 2018 Niklas Merkelt.
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class StatsSQL {

    private static final Map<GeneralStatType, Double> generalStats = new EnumMap<GeneralStatType, Double>(GeneralStatType.class);
    private static final Map<Node, Short> clients = Collections.synchronizedMap(new LinkedHashMap<>());
    private static final Map<Node, Float> load = Collections.synchronizedMap(new LinkedHashMap<>());
    private static final Map<Node, Double> memory = Collections.synchronizedMap(new LinkedHashMap<>());

    public static void addClientStat(Node n, short c) {
        clients.put(n, c);
    }
    
    public static void addLoadStat(Node n, float l) {
        load.put(n, l);
    }
    
    public static void addMemoryStat(Node n, double m) {
        memory.put(n, m);
    }
    
    public static void addGeneralStats(GeneralStatType type, double value) {
        generalStats.put(type, value);
    }

    public static void processStats() {
        if (!clients.isEmpty()) {
            String query = "INSERT INTO statsClients (node, value) VALUES ";
            ArrayList<Object> data = new ArrayList<>();
            for (Entry<Node, Short> e : clients.entrySet()) {
                query += "(?,?),";
                data.add(e.getKey().getId());
                data.add(e.getValue());
            }
            query = query.substring(0, query.length() - 1);
            DataGen.getDB().queryUpdate(query, data.toArray());
        }
        if (!load.isEmpty()) {
            String query = "INSERT INTO statsLoad (node, value) VALUES ";
            ArrayList<Object> data = new ArrayList<>();
            for (Entry<Node, Float> e : load.entrySet()) {
                query += "(?,?),";
                data.add(e.getKey().getId());
                data.add(e.getValue());
            }
            query = query.substring(0, query.length() - 1);
            DataGen.getDB().queryUpdate(query, data.toArray());
        }
        if (!memory.isEmpty()) {
            String query = "INSERT INTO statsMemory (node, value) VALUES ";
            ArrayList<Object> data = new ArrayList<>();
            for (Entry<Node, Double> e : memory.entrySet()) {
                query += "(?,?),";
                data.add(e.getKey().getId());
                data.add(e.getValue());
            }
            query = query.substring(0, query.length() - 1);
            DataGen.getDB().queryUpdate(query, data.toArray());
        }
        if (!generalStats.isEmpty()) {
            String query = "INSERT INTO statsGeneral (type, value) VALUES ";
            ArrayList<Object> data = new ArrayList<>();
            for (Entry<GeneralStatType, Double> stat : generalStats.entrySet()) {
                query += "(?,?),";
                data.add(stat.getKey().name().toLowerCase());
                data.add(stat.getValue());
            }
            query = query.substring(0, query.length() - 1);
            DataGen.getDB().queryUpdate(query, data.toArray());
        }
    }
}
