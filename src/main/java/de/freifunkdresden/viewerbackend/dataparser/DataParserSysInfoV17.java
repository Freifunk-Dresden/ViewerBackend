/*
 * The MIT License
 *
 * Copyright 2021 Niklas Merkelt.
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

package de.freifunkdresden.viewerbackend.dataparser;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.freifunkdresden.viewerbackend.DataGen;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class DataParserSysInfoV17 extends DataParserSysInfoV16 {

    private static final Logger LOGGER = LogManager.getLogger(DataParserSysInfoV17.class);

    public DataParserSysInfoV17(JsonObject data) {
        super(data);
    }

    @Override
    protected void checkCommunity() {
        JsonObject common = data.get("common").getAsJsonObject();
        if (common.has("community")) {
            String c = common.get("community").getAsString();
            community = DataGen.getConfig().getCommunityDirectory().getCommunityMapping(c);
            if (!DataGen.getConfig().getCommunityDirectory().existsMapping(c)) {
                LOGGER.log(Level.WARN, "Node {} has invalid community `{}` (Name: {})", getNodeId(), c, getName());
            }
        } else {
            super.checkCommunity();
        }
    }

    @Override
    public TrafficInfo getTraffic() {
        JsonElement statistic = data.get("statistic");
        if (statistic != null) {
            JsonElement interfaces = statistic.getAsJsonObject().get("interfaces");
            TrafficInfo trafficInfo;
            if (interfaces != null) {
                trafficInfo = new TrafficInfoV17();
                trafficInfo.readValues(interfaces.getAsJsonObject());
            } else {
                trafficInfo = new TrafficInfoV15();
                trafficInfo.readValues(statistic.getAsJsonObject());
            }
            return trafficInfo;
        }
        return new TrafficInfoEmpty();
    }

    @Override
    public Optional<Integer> getWifiChannel2g() {
        JsonElement wifiChannel2g = data.get("system").getAsJsonObject().get("wifi_2g_channel");
        if (wifiChannel2g != null) {
            try {
                return Optional.of(wifiChannel2g.getAsInt());
            } catch (NumberFormatException e) {
                LOGGER.log(Level.WARN, "Node {}: invalid 2g channel number '{}'", getNodeId(), e.getMessage());
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Integer> getWifiChannel5g() {
        JsonElement wifiChannel5g = data.get("system").getAsJsonObject().get("wifi_5g_channel");
        if (wifiChannel5g != null) {
            try {
                return Optional.of(wifiChannel5g.getAsInt());
            } catch (NumberFormatException e) {
                LOGGER.log(Level.WARN, "Node {}: invalid 5g channel number '{}'", getNodeId(), e.getMessage());
            }
        }
        return Optional.empty();
    }
}
