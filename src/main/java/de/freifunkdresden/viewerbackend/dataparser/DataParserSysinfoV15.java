package de.freifunkdresden.viewerbackend.dataparser;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class DataParserSysinfoV15 extends DataParserSysinfoV14 {

    public DataParserSysinfoV15(JsonObject data) {
        super(data);
    }

    @Override
    public Short getClients() throws Exception {
        JsonElement clients = data.get("statistic").getAsJsonObject().get("clients");
        if (clients.isJsonArray()) {
            return clients.getAsJsonArray().get(1).getAsShort();
        }
        return super.getClients();
    }
}
