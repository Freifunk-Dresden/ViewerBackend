package de.freifunkdresden.viewerbackend.dataparser;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class DataParserSysinfoV15 extends DataParserSysinfoV14 {

    public DataParserSysinfoV15(JsonObject data) {
        super(data);
    }

    @Override
    public short getClients() {
        JsonElement clients = data.get("statistic").getAsJsonObject().get("clients");
        if (clients.isJsonArray()) {
            return clients.getAsJsonArray().get(1).getAsShort();
        }
        return super.getClients();
    }

    @Override
    public TrafficInfo getTraffic() {
        TrafficInfo ti = new TrafficInfo();
        ti.readValues(data.get("statistic").getAsJsonObject());
        return ti;
    }
}
