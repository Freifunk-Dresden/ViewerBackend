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
import de.freifunkdresden.viewerbackend.Community;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DataParserSysInfoV17 extends DataParserSysInfoV16 {

    private static final Logger LOGGER = LogManager.getLogger(DataParserSysInfo.class);

    public DataParserSysInfoV17(JsonObject data) {
        super(data);
    }

    @Override
    protected void checkCommunity() {
        JsonObject common = data.get("common").getAsJsonObject();
        if (common.has("community")) {
            String c = common.get("community").getAsString();
            community = Community.getCommunity(c);
            if (community == Community.DEFAULT) {
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
}
