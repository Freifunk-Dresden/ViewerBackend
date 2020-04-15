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
package de.freifunkdresden.viewerbackend.dataparser;

import de.freifunkdresden.viewerbackend.Community;
import de.freifunkdresden.viewerbackend.Location;
import de.freifunkdresden.viewerbackend.NodeType;
import java.sql.ResultSet;

public class DataParserDB extends DataParser {

    private final ResultSet rs;

    public DataParserDB(ResultSet rs) {
        this.rs = rs;
    }

    @Override
    public Boolean getAutoUpdate() throws Exception {
        boolean autoupdate = rs.getBoolean("autoupdate");
        return rs.wasNull() ? null : autoupdate;
    }

    @Override
    public Community getCommunity() throws Exception {
        return Community.getCommunity(rs.getString("community"));
    }

    @Override
    public String getEMail() throws Exception {
        return rs.getString("email");
    }

    @Override
    public String getFirmwareBase() throws Exception {
        return rs.getString("firmwareBase");
    }

    @Override
    public String getFirmwareVersion() throws Exception {
        return rs.getString("firmwareVersion");
    }

    @Override
    public Long getFirstSeen() throws Exception {
        return rs.getLong("firstseen") * 1000;
    }

    @Override
    public Long getLastSeen() throws Exception {
        return rs.getLong("lastseen") * 1000;
    }

    @Override
    public Location getLocation() throws Exception {
        try {
            double latitude = rs.getDouble("latitude");
            latitude = rs.wasNull() ? Double.NaN : latitude;
            double longitude = rs.getDouble("longitude");
            longitude = rs.wasNull() ? Double.NaN : longitude;
            return new Location(latitude, longitude);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    @Override
    public String getModel() throws Exception {
        return rs.getString("model");
    }

    @Override
    public String getName() throws Exception {
        return rs.getString("name");
    }

    @Override
    public NodeType getRole() throws Exception {
        String r = rs.getString("role");
        return r == null ? NodeType.STANDARD : NodeType.valueOf(r.toUpperCase());
    }

    @Override
    public Boolean isGateway() throws Exception {
        return rs.getBoolean("gateway");
    }
}
