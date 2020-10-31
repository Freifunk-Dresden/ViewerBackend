package de.freifunkdresden.viewerbackend.dataparser;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.EnumMap;
import java.util.Map;

public class TrafficInfo {

    private final Map<Interface, Long> traffic_in = new EnumMap<>(Interface.class);
    private final Map<Interface, Long> traffic_out = new EnumMap<>(Interface.class);

    public TrafficInfo() {
    }

    public void readValues(JsonObject stats) {
        boolean from_to = false;
        for (Interface out : Interface.values()) {
            for (Interface in : Interface.values()) {
                String name = String.format("traffic_%s_%s", out.name().toLowerCase(), in.name().toLowerCase());
                JsonElement j = stats.get(name);
                if (j != null) {
                    from_to = true;
                    traffic_out.put(out, getOutput(out) + j.getAsLong());
                    traffic_in.put(in, getInput(in) + j.getAsLong());
                }
            }
        }
        if (!from_to) {
            for (Interface i : Interface.values()) {
                String name = String.format("traffic_%s", i.name().toLowerCase());
                JsonElement j = stats.get(name);
                if (j != null) {
                    String[] t = j.getAsString().split(",");
                    if (t.length == 2) {
                        traffic_in.put(i, Long.parseLong(t[0]));
                        traffic_out.put(i, Long.parseLong(t[1]));
                    }
                }
            }
        }
    }

    public boolean isEmpty() {
        return traffic_in.isEmpty() && traffic_out.isEmpty();
    }

    public boolean hasInterface(Interface i) {
        return traffic_in.containsKey(i) || traffic_out.containsKey(i);
    }

    public long getInput(Interface i) {
        return traffic_in.getOrDefault(i, 0L);
    }

    public long getOutput(Interface i) {
        return traffic_out.getOrDefault(i, 0L);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Interface i : Interface.values()) {
            if (hasInterface(i)) {
                sb.append(String.format("%s: %d<>%d%n", i.name().toLowerCase(), getInput(i), getOutput(i)));
            }
        }
        return sb.toString();
    }

    public enum Interface {
        LAN,
        WAN,
        ADHOC,
        AP,
        OVPN,
        GWT,
        PRIVNET,
        TBB_FASTD,
        TBB_WG,
        MESH_LAN,
        MESH_WAN,
    }
}
