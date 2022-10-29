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

import de.freifunkdresden.viewerbackend.Airtime;
import de.freifunkdresden.viewerbackend.Community;
import de.freifunkdresden.viewerbackend.DataGen;
import de.freifunkdresden.viewerbackend.Node;
import de.freifunkdresden.viewerbackend.VPN;
import de.freifunkdresden.viewerbackend.dataparser.TrafficInfo;
import org.influxdb.dto.Point;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class StatsSQL {

    private StatsSQL() {
    }

    private static final List<Point> general = new ArrayList<>();
    private static final List<Point> vpnUsage = new ArrayList<>();
    private static final List<Point> vpnUsageFastD = new ArrayList<>();
    private static final List<Point> vpnUsageWireGuard = new ArrayList<>();
    private static final Map<String, Integer> versions = new LinkedHashMap<>();
    private static final Map<String, Integer> communities = new LinkedHashMap<>();
    private static final Map<String, Integer> model = new LinkedHashMap<>();
    private static final Map<String, Integer> modelWithoutVersion = new LinkedHashMap<>();
    private static final Map<String, Integer> manufacturer = new LinkedHashMap<>();
    private static final Map<Node, Integer> gatewayUsage = new LinkedHashMap<>();
    private static final Map<Node, Integer> gatewayClients = new LinkedHashMap<>();

    public static void addGeneralStats(@NotNull GeneralStatType type, double value) {
        synchronized (general) {
            general.add(Point.measurement(type.name().toLowerCase())
                    .addField("value", value)
                    .build());
        }
    }

    public static void addVersion(@Nullable String version) {
        if (version != null && !version.isEmpty()) {
            synchronized (versions) {
                versions.put(version, versions.getOrDefault(version, 0) + 1);
            }
        }
    }

    public static void addCommunity(@NotNull Community c) {
        synchronized (communities) {
            String cName = c.getName();
            communities.put(cName, communities.getOrDefault(cName, 0) + 1);
        }
    }

    public static void addModel(@NotNull String mod, @NotNull String man) {
        if (mod.isBlank()) {
            return;
        }
        synchronized (model) {
            model.put(mod, model.getOrDefault(mod, 0) + 1);
        }
        synchronized (manufacturer) {
            manufacturer.put(man, manufacturer.getOrDefault(man, 0) + 1);
        }
        String modWithoutV = mod.replaceAll("(?:\\s\\(IL\\))*(?:N/ND)*N*(?:ND)*\\s*v\\d+(?:\\.\\d)*$", "").replace("TP-LINK", "TP-Link");
        synchronized (modelWithoutVersion) {
            modelWithoutVersion.put(modWithoutV, modelWithoutVersion.getOrDefault(modWithoutV, 0) + 1);
        }
    }

    public static void addVpnUsage(@NotNull VPN vpn, int usage) {
        synchronized (vpnUsage) {
            vpnUsage.add(Point.measurement("vpn_usage")
                    .tag("vpn", vpn.getVpnId())
                    .addField("usage", usage)
                    .build());
        }
    }

    public static void addVpnUsageFastD(@NotNull VPN vpn, int usage) {
        synchronized (vpnUsageFastD) {
            vpnUsageFastD.add(Point.measurement("vpn_usage_fastd")
                    .tag("vpn", vpn.getVpnId())
                    .addField("usage", usage)
                    .build());
        }
    }

    public static void addVpnUsageWireGuard(@NotNull VPN vpn, int usage) {
        synchronized (vpnUsageWireGuard) {
            vpnUsageWireGuard.add(Point.measurement("vpn_usage_wg")
                    .tag("vpn", vpn.getVpnId())
                    .addField("usage", usage)
                    .build());
        }
    }

    public static void addGatewayUsage(Node gw) {
        if (gw == null || gw.getId() < 0) {
            return;
        }
        synchronized (gatewayUsage) {
            gatewayUsage.put(gw, gatewayUsage.getOrDefault(gw, 0) + 1);
        }
    }

    public static void addGatewayUsageClients(Node gw, int cl) {
        if (gw == null || gw.getId() < 0) {
            return;
        }
        synchronized (gatewayClients) {
            gatewayClients.put(gw, gatewayClients.getOrDefault(gw, 0) + cl);
        }
    }

    public static void processStats() {
        DataGen.getInflux().write(general);
        DataGen.getInflux().write(vpnUsage);
        DataGen.getInflux().write(vpnUsageFastD);
        DataGen.getInflux().write(vpnUsageWireGuard);
        List<Point> nodeClients = new ArrayList<>();
        List<Point> nodeLoad = new ArrayList<>();
        List<Point> nodeMemory = new ArrayList<>();
        List<Point> nodeAirtime = new ArrayList<>();
        List<Point> nodeUptime = new ArrayList<>();
        List<Point> nodeTraffic = new ArrayList<>();
        DataGen.getDataHolder().getNodes().values().stream()
                .filter(Node::isOnline)
                .forEach(e -> {
                    long currentTimeMillis = System.currentTimeMillis();
                    if (e.canHasClients()) {
                        nodeClients.add(Point.measurement("node_clients")
                                .time(currentTimeMillis, TimeUnit.MILLISECONDS)
                                .tag("node", String.valueOf(e.getId()))
                                .addField("value", e.getClients())
                                .build());
                    }
                    nodeLoad.add(Point.measurement("node_load")
                            .time(currentTimeMillis, TimeUnit.MILLISECONDS)
                            .tag("node", String.valueOf(e.getId()))
                            .addField("value", e.getLoadAvg())
                            .build());
                    nodeMemory.add(Point.measurement("node_memory")
                            .time(currentTimeMillis, TimeUnit.MILLISECONDS)
                            .tag("node", String.valueOf(e.getId()))
                            .addField("value", e.getMemoryUsage())
                            .build());
                    nodeUptime.add(Point.measurement("node_uptime")
                            .time(currentTimeMillis, TimeUnit.MILLISECONDS)
                            .tag("node", String.valueOf(e.getId()))
                            .addField("value", e.getUptime())
                            .build());
                    Optional<Airtime> airtime2g = e.getAirtime2g();
                    if (airtime2g.isPresent()) {
                        Airtime airtime = airtime2g.get();
                        nodeAirtime.add(Point.measurement("node_airtime_2g")
                                .time(currentTimeMillis, TimeUnit.MILLISECONDS)
                                .tag("node", String.valueOf(e.getId()))
                                .addField("active", airtime.getActive())
                                .addField("busy", airtime.getBusy())
                                .addField("receive", airtime.getReceive())
                                .addField("transmit", airtime.getTransmit())
                                .build());
                    }
                    Optional<Airtime> airtime5g = e.getAirtime5g();
                    if (airtime5g.isPresent()) {
                        Airtime airtime = airtime5g.get();
                        nodeAirtime.add(Point.measurement("node_airtime_5g")
                                .time(currentTimeMillis, TimeUnit.MILLISECONDS)
                                .tag("node", String.valueOf(e.getId()))
                                .addField("active", airtime.getActive())
                                .addField("busy", airtime.getBusy())
                                .addField("receive", airtime.getReceive())
                                .addField("transmit", airtime.getTransmit())
                                .build());
                    }
                    TrafficInfo t = e.getTraffic();
                    if (!t.isEmpty()) {
                        for (TrafficInfo.Interface i : TrafficInfo.Interface.values()) {
                            if (t.hasInterface(i)) {
                                nodeTraffic.add(Point.measurement("node_traffic")
                                        .time(currentTimeMillis, TimeUnit.MILLISECONDS)
                                        .tag("node", String.valueOf(e.getId()))
                                        .tag("interface", i.name().toLowerCase())
                                        .addField("in", t.getInput(i))
                                        .addField("out", t.getOutput(i))
                                        .build());
                            }
                        }
                    }
                });
        DataGen.getInflux().write(nodeClients);
        DataGen.getInflux().write(nodeLoad);
        DataGen.getInflux().write(nodeMemory);
        DataGen.getInflux().write(nodeUptime);
        DataGen.getInflux().write(nodeAirtime);
        DataGen.getInflux().write(nodeTraffic);
        List<Point> nodesVersions = new ArrayList<>();
        versions.forEach((v, c) -> nodesVersions.add(Point.measurement("nodes_versions")
                .tag("version", v)
                .addField("value", c)
                .build()));
        DataGen.getInflux().write(nodesVersions);
        List<Point> nodesCommunities = new ArrayList<>();
        communities.forEach((c, v) -> nodesCommunities.add(Point.measurement("nodes_communities")
                .tag("community", c)
                .addField("value", v)
                .build()));
        DataGen.getInflux().write(nodesCommunities);
        // gateway usage
        List<Point> nodesGateway = new ArrayList<>();
        gatewayUsage.forEach((gw, v) -> nodesGateway.add(Point.measurement("nodes_gateway")
                .tag("gateway", String.valueOf(gw.getId()))
                .addField("value", v)
                .build()));
        DataGen.getInflux().write(nodesGateway);
        // gateway usage clients
        List<Point> nodesGatewayClients = new ArrayList<>();
        gatewayClients.forEach((gw, v) -> nodesGatewayClients.add(Point.measurement("nodes_gateway_clients")
                .tag("gateway", String.valueOf(gw.getId()))
                .addField("value", v)
                .build()));
        DataGen.getInflux().write(nodesGatewayClients);
        // model, modelWithoutVersion, manufacturer
        List<Point> nodesModel = new ArrayList<>();
        model.forEach((m, v) -> nodesModel.add(Point.measurement("nodes_model")
                .tag("model", m)
                .addField("value", v)
                .build()));
        DataGen.getInflux().write(nodesModel);
        List<Point> nodesModelWithoutVersion = new ArrayList<>();
        modelWithoutVersion.forEach((m, v) -> nodesModelWithoutVersion.add(Point.measurement("nodes_model_without_version")
                .tag("model", m)
                .addField("value", v)
                .build()));
        DataGen.getInflux().write(nodesModelWithoutVersion);
        List<Point> nodesManufacturer = new ArrayList<>();
        manufacturer.forEach((m, v) -> nodesManufacturer.add(Point.measurement("nodes_manufacturer")
                .tag("manufacturer", m)
                .addField("value", v)
                .build()));
        DataGen.getInflux().write(nodesManufacturer);
    }
}
