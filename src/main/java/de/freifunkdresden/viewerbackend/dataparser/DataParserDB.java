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
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DataParserDB {

    private static final Logger LOGGER = LogManager.getLogger(DataParserDB.class);

    private boolean autoUpdate;
    private Community community = Community.DEFAULT;
    private String eMail;
    private String firmwareBase;
    private String firmwareVersion;
    private long firstSeen;
    private long lastSeen;
    private Location location;
    private String model;
    private String name;
    private NodeType role = NodeType.STANDARD;

    public void parse(@NotNull ResultSet rs) {
        try {
            boolean au = rs.getInt("autoupdate") == 1;
            autoUpdate = !rs.wasNull() && au;
        } catch (SQLException e) {
            LOGGER.log(Level.ERROR, "Database read failed", e);
        }
        try {
            community = Community.getCommunity(rs.getString("community"));
        } catch (SQLException e) {
            LOGGER.log(Level.ERROR, "Database read failed", e);
        }
        try {
            eMail = rs.getString("email");
        } catch (SQLException e) {
            LOGGER.log(Level.ERROR, "Database read failed", e);
        }
        try {
            firmwareBase = rs.getString("firmwareBase");
        } catch (SQLException e) {
            LOGGER.log(Level.ERROR, "Database read failed", e);
        }
        try {
            firmwareVersion = rs.getString("firmwareVersion");
        } catch (SQLException e) {
            LOGGER.log(Level.ERROR, "Database read failed", e);
        }
        try {
            firstSeen = rs.getLong("firstseen") * 1000;
        } catch (SQLException e) {
            LOGGER.log(Level.ERROR, "Database read failed", e);
        }
        try {
            lastSeen = rs.getLong("lastseen") * 1000;
        } catch (SQLException e) {
            LOGGER.log(Level.ERROR, "Database read failed", e);
        }
        try {
            double latitude = rs.getDouble("latitude");
            latitude = rs.wasNull() ? Double.NaN : latitude;
            double longitude = rs.getDouble("longitude");
            longitude = rs.wasNull() ? Double.NaN : longitude;
            location = new Location(latitude, longitude);
        } catch (SQLException e) {
            LOGGER.log(Level.ERROR, "Database read failed", e);
        }
        try {
            model = rs.getString("model");
        } catch (SQLException e) {
            LOGGER.log(Level.ERROR, "Database read failed", e);
        }
        try {
            name = rs.getString("name");
        } catch (SQLException e) {
            LOGGER.log(Level.ERROR, "Database read failed", e);
        }
        try {
            String r = rs.getString("role");
            role = r == null ? NodeType.STANDARD : NodeType.valueOf(r.toUpperCase());
        } catch (SQLException e) {
            LOGGER.log(Level.ERROR, "Database read failed", e);
        }
    }

    public boolean getAutoUpdate() {
        return autoUpdate;
    }

    public Community getCommunity() {
        return community;
    }

    public String getEMail() {
        return eMail;
    }

    public String getFirmwareBase() {
        return firmwareBase;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public long getFirstSeen() {
        return firstSeen;
    }

    public long getLastSeen() {
        return lastSeen;
    }

    public Location getLocation() {
        return location;
    }

    public String getModel() {
        return model;
    }

    public String getName() {
        return name;
    }

    public NodeType getRole() {
        return role;
    }
}
