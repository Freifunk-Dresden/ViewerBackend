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
import java.sql.SQLException;

public class DataParserDB {

    private final ResultSet rs;

    public DataParserDB(ResultSet rs) {
        this.rs = rs;
    }

    public boolean getAutoUpdate() throws SQLException {
        boolean autoUpdate = rs.getBoolean("autoupdate");
        return !rs.wasNull() && autoUpdate;
    }

    public Community getCommunity() throws SQLException {
        return Community.getCommunity(rs.getString("community"));
    }

    public String getEMail() throws SQLException {
        return rs.getString("email");
    }

    public String getFirmwareBase() throws SQLException {
        return rs.getString("firmwareBase");
    }

    public String getFirmwareVersion() throws SQLException {
        return rs.getString("firmwareVersion");
    }

    public long getFirstSeen() throws SQLException {
        return rs.getLong("firstseen") * 1000;
    }

    public long getLastSeen() throws SQLException {
        return rs.getLong("lastseen") * 1000;
    }

    public Location getLocation() throws SQLException {
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

    public String getModel() throws SQLException {
        return rs.getString("model");
    }

    public String getName() throws SQLException {
        return rs.getString("name");
    }

    public NodeType getRole() throws SQLException {
        String r = rs.getString("role");
        return r == null ? NodeType.STANDARD : NodeType.valueOf(r.toUpperCase());
    }
}
